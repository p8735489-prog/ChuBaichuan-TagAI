package com.kuzulabz.waifutaggercn.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import java.io.File
import java.nio.FloatBuffer
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

/**
 * YOLO11n / YOLOv8n 目标检测 ONNX 引擎（无 mask 输出，仅 bbox）。
 *
 * 用于"精准模式"：检测画面中的目标对象，按 bbox 裁剪出主体区域，
 * 交给 WD Tagger 做反推。
 *
 * 模型输出格式（ultralytics 导出，imgsz=640, opset>=12, simplify=true）：
 *   output0  : [1, 4+nc, 8400]  ->  [1, 84, 8400] (nc=80)
 *              84 = 4(bbox cx,cy,w,h) + 80(class scores, 已 sigmoid)
 *
 * 相比 SegEngine，本引擎不处理 mask 系数和 prototype masks，
 * 推理更快、模型更小，裁剪方式为直接按 bbox 外接矩形裁剪。
 */
class DetEngine(private val context: Context) {

    data class Detection(
        val bbox: RectF,            // 原图坐标 x1,y1,x2,y2
        val score: Float,
        val classId: Int,
        val className: String      // COCO 类别名
    )

    enum class ExecProvider { CPU, NNAPI }

    private val lock = Any()
    private var session: OrtSession? = null
    private var environment: OrtEnvironment? = null
    private var execProvider: ExecProvider = ExecProvider.CPU
    private var inputSize: Int = 640
    private var preferredModelName: String? = null  // 用户选择的检测模型文件名
    var currentProvider: ExecProvider = ExecProvider.CPU
        private set

    val isReady: Boolean
        get() = session != null

    /** COCO 80 类标签（用于检测结果显示） */
    private val cocoLabels = listOf(
        "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
        "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat",
        "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack",
        "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball",
        "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket",
        "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
        "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake",
        "chair", "couch", "potted plant", "bed", "dining table", "toilet", "tv", "laptop",
        "mouse", "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink",
        "refrigerator", "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"
    )

    /** 设置用户首选的检测模型文件名（如 "yolo11n.onnx"），下次 load 时生效 */
    fun setPreferredModel(modelFileName: String?) {
        preferredModelName = modelFileName
    }

    /**
     * 加载 YOLO 检测模型。
     * @param provider CPU 或 NNAPI
     * @return null 成功，否则返回错误信息
     */
    fun load(provider: ExecProvider = ExecProvider.CPU): String? = synchronized(lock) {
        runCatching { session?.close() }
        session = null

        val modelFile = resolveModelFile() ?: run {
            return "未找到检测模型。请在模型页面下载 YOLO 检测模型。"
        }

        val env = try {
            OrtEnvironment.getEnvironment().also { environment = it }
        } catch (e: Throwable) {
            return "ONNX 环境初始化失败: ${e.message}"
        }

        val opts = OrtSession.SessionOptions().apply {
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            setIntraOpNumThreads(4)
            setMemoryPatternOptimization(false)
            addConfigEntry("session.use_env_allocators", "1")
        }

        var effectiveProvider = provider
        session = try {
            when (provider) {
                ExecProvider.NNAPI -> {
                    try {
                        opts.addNnapi()
                    } catch (_: Throwable) {
                        effectiveProvider = ExecProvider.CPU
                    }
                }
                ExecProvider.CPU -> { /* 默认 CPU EP */ }
            }
            env.createSession(modelFile.absolutePath, opts)
        } catch (e: Throwable) {
            if (effectiveProvider == ExecProvider.NNAPI) {
                effectiveProvider = ExecProvider.CPU
                val cpuOpts = OrtSession.SessionOptions().apply {
                    setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
                    setIntraOpNumThreads(4)
                }
                try {
                    env.createSession(modelFile.absolutePath, cpuOpts)
                } catch (e2: Throwable) {
                    return "检测模型加载失败: ${e2.message}"
                }
            } else {
                return "检测模型加载失败: ${e.message}"
            }
        }

        execProvider = effectiveProvider
        currentProvider = effectiveProvider

        // 读取输入尺寸
        session?.inputNames?.firstOrNull()?.let { name ->
            session?.inputInfo?.get(name)?.info?.let { info ->
                val shape = (info as? ai.onnxruntime.TensorInfo)?.shape
                if (shape != null && shape.size == 4) {
                    val h = shape[2].takeIf { it > 0 }?.toInt()
                    val w = shape[3].takeIf { it > 0 }?.toInt()
                    if (h != null && w != null && h == w) inputSize = h
                }
            }
        }
        null
    }

    private fun resolveModelFile(): File? {
        val dir = TaggerEngine.modelDirectory(context)
        val candidates = mutableListOf<String>()
        preferredModelName?.let { candidates.add(it) }
        // 默认查找顺序
        listOf("yolo11n.onnx", "yolov8n.onnx", "yolo11s.onnx", "yolov8s.onnx").forEach { name ->
            if (name !in candidates) candidates.add(name)
        }
        candidates.forEach { name ->
            val f = File(dir, name)
            if (f.exists() && f.length() > 0L) return f
        }
        return null
    }

    /**
     * 对一张图做目标检测，返回所有检测到的对象。
     * @param conf 置信度阈值，默认 0.25
     * @param iou NMS IoU 阈值，默认 0.45
     * @param maxDetections 最多返回数量
     */
    fun detectObjects(
        bitmap: Bitmap,
        conf: Float = 0.25f,
        iou: Float = 0.45f,
        maxDetections: Int = 20
    ): List<Detection> = synchronized(lock) {
        val currentSession = session ?: return emptyList<Detection>()
        val env = environment ?: return emptyList<Detection>()

        // 1. letterbox 预处理
        val (inputBuffer, scale, padW, padH) = preprocess(bitmap)
        val shape = longArrayOf(1, 3, inputSize.toLong(), inputSize.toLong())

        OnnxTensor.createTensor(env, inputBuffer, shape).use { tensor ->
            val inputName = currentSession.inputNames.first()
            currentSession.run(mapOf(inputName to tensor)).use { results ->
                val outputs = results.toList()
                if (outputs.isEmpty()) return@detectObjects emptyList<Detection>()
                // output0: [1, 84, 8400]
                val out0Raw = outputs[0].value.value
                val out0 = flattenAndReshape3D(out0Raw)
                if (out0.isEmpty()) return@detectObjects emptyList<Detection>()
                val detections = postprocess(out0, bitmap, scale, padW, padH, conf, iou)
                detections.sortedByDescending { it.score }.take(maxDetections)
            }
        }
    }

    /** letterbox：等比缩放 + 灰边填充到 inputSize×inputSize */
    private fun preprocess(bitmap: Bitmap): Quad<FloatBuffer, Float, Int, Int> {
        val srcW = bitmap.width
        val srcH = bitmap.height
        val scale = inputSize.toFloat() / max(srcW, srcH)
        val newW = (srcW * scale).toInt()
        val newH = (srcH * scale).toInt()
        val padW = (inputSize - newW) / 2
        val padH = (inputSize - newH) / 2

        val scaled = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        val padded = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(padded)
        canvas.drawColor(Color.rgb(114, 114, 114))
        canvas.drawBitmap(scaled, padW.toFloat(), padH.toFloat(), null)

        val buffer = FloatBuffer.allocate(3 * inputSize * inputSize)
        val pixels = IntArray(inputSize * inputSize)
        padded.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
        val rArr = FloatArray(inputSize * inputSize)
        val gArr = FloatArray(inputSize * inputSize)
        val bArr = FloatArray(inputSize * inputSize)
        for (i in pixels.indices) {
            val p = pixels[i]
            rArr[i] = ((p shr 16) and 0xFF) / 255f
            gArr[i] = ((p shr 8) and 0xFF) / 255f
            bArr[i] = (p and 0xFF) / 255f
        }
        buffer.put(rArr); buffer.put(gArr); buffer.put(bArr)
        buffer.rewind()

        if (scaled != bitmap) scaled.recycle()
        return Quad(buffer, scale, padW, padH)
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    private data class RawDet(
        val cx: Float, val cy: Float, val w: Float, val h: Float,
        val score: Float, val cls: Int
    )

    private fun postprocess(
        out0: Array<Array<FloatArray>>,  // [1][84][8400]
        bitmap: Bitmap,
        scale: Float,
        padW: Int,
        padH: Int,
        conf: Float,
        iou: Float
    ): List<Detection> {
        val numClasses = 80
        val anchors = out0[0][0].size        // 8400

        // 1. 解析候选框
        val candidates = ArrayList<RawDet>()
        for (a in 0 until anchors) {
            val cx = out0[0][0][a]
            val cy = out0[0][1][a]
            val w = out0[0][2][a]
            val h = out0[0][3][a]
            // 类别分数在 channel [4..4+nc)，取最大
            var maxScore = 0f
            var maxCls = 0
            for (c in 0 until numClasses) {
                val s = out0[0][4 + c][a]
                if (s > maxScore) { maxScore = s; maxCls = c }
            }
            if (maxScore < conf) continue
            candidates.add(RawDet(cx, cy, w, h, maxScore, maxCls))
        }
        if (candidates.isEmpty()) return emptyList()

        // 2. NMS（按分数降序贪心）
        candidates.sortByDescending { it.score }
        val kept = ArrayList<RawDet>()
        val suppressed = BooleanArray(candidates.size)
        for (i in candidates.indices) {
            if (suppressed[i]) continue
            kept.add(candidates[i])
            for (j in i + 1 until candidates.size) {
                if (suppressed[j]) continue
                if (iouRaw(candidates[i], candidates[j]) > iou) suppressed[j] = true
            }
        }

        // 3. 转换到原图坐标
        val results = ArrayList<Detection>()
        for (det in kept) {
            val x1 = ((det.cx - det.w / 2f) - padW) / scale
            val y1 = ((det.cy - det.h / 2f) - padH) / scale
            val x2 = ((det.cx + det.w / 2f) - padW) / scale
            val y2 = ((det.cy + det.h / 2f) - padH) / scale
            val bbox = RectF(
                x1.coerceIn(0f, bitmap.width - 1f),
                y1.coerceIn(0f, bitmap.height - 1f),
                x2.coerceIn(0f, bitmap.width - 1f),
                y2.coerceIn(0f, bitmap.height - 1f)
            )
            val className = if (det.cls < cocoLabels.size) cocoLabels[det.cls] else "class_${det.cls}"
            results.add(Detection(bbox, det.score, det.cls, className))
        }
        return results
    }

    private fun iouRaw(a: RawDet, b: RawDet): Float {
        val ax1 = a.cx - a.w / 2f; val ay1 = a.cy - a.h / 2f
        val ax2 = a.cx + a.w / 2f; val ay2 = a.cy + a.h / 2f
        val bx1 = b.cx - b.w / 2f; val by1 = b.cy - b.h / 2f
        val bx2 = b.cx + b.w / 2f; val by2 = b.cy + b.h / 2f
        val ix = max(ax1, bx1); val iy = max(ay1, by1)
        val ox = min(ax2, bx2); val oy = min(ay2, by2)
        val iw = max(0f, ox - ix); val ih = max(0f, oy - iy)
        val inter = iw * ih
        val union = (a.w * a.h) + (b.w * b.h) - inter
        return if (union > 0f) inter / union else 0f
    }

    /**
     * 按检测结果裁剪主体区域。
     * 取所有检测框的联合外接矩形（带 padding），裁剪出主体区域。
     * 若无检测结果则返回 null（调用方回退到普通模式）。
     */
    fun cropDetectedRegion(bitmap: Bitmap, detections: List<Detection>): Bitmap? {
        if (detections.isEmpty()) return null

        // 取所有检测框的联合外接矩形
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE; var maxY = Float.MIN_VALUE
        for (det in detections) {
            if (det.bbox.left < minX) minX = det.bbox.left
            if (det.bbox.top < minY) minY = det.bbox.top
            if (det.bbox.right > maxX) maxX = det.bbox.right
            if (det.bbox.bottom > maxY) maxY = det.bbox.bottom
        }
        if (minX == Float.MAX_VALUE) return null

        // padding 10%，避免裁太紧丢掉细节
        val regionW = maxX - minX
        val regionH = maxY - minY
        val pw = (regionW * 0.10f).coerceAtLeast(8f)
        val ph = (regionH * 0.10f).coerceAtLeast(8f)
        val left = (minX - pw).toInt().coerceAtLeast(0)
        val top = (minY - ph).toInt().coerceAtLeast(0)
        val right = (maxX + pw).toInt().coerceAtMost(bitmap.width)
        val bottom = (maxY + ph).toInt().coerceAtMost(bitmap.height)
        val cropW = right - left
        val cropH = bottom - top
        if (cropW <= 1 || cropH <= 1) return null
        return Bitmap.createBitmap(bitmap, left, top, cropW, cropH)
    }

    fun close() = synchronized(lock) {
        runCatching { session?.close() }
        session = null
    }

    /** 兜底：将任意嵌套结构扁平化为 float 数组，再 reshape 为 float[][][] */
    private fun flattenAndReshape3D(raw: Any?): Array<Array<FloatArray>> {
        val flat = mutableListOf<Float>()
        flattenToArray(raw, flat)
        if (flat.isEmpty()) return arrayOf(arrayOf(FloatArray(0)))
        // 期望形状 [1, channels, anchors]，channels 从数据推断
        // YOLO det: 84 channels, 8400 anchors
        val total = flat.size
        val channels = if (total % 8400 == 0) total / 8400 else 84
        val anchors = if (total % channels == 0) total / channels else 8400
        if (channels * anchors != total) return arrayOf(arrayOf(FloatArray(0)))
        val result = Array(1) { Array(channels) { FloatArray(anchors) } }
        for (c in 0 until channels) {
            for (a in 0 until anchors) {
                result[0][c][a] = flat[c * anchors + a]
            }
        }
        return result
    }

    @Suppress("UNCHECKED_CAST")
    private fun flattenToArray(value: Any?, out: MutableList<Float>) {
        when (value) {
            is FloatArray -> value.forEach { out.add(it) }
            is DoubleArray -> value.forEach { out.add(it.toFloat()) }
            is IntArray -> value.forEach { out.add(it.toFloat()) }
            is LongArray -> value.forEach { out.add(it.toFloat()) }
            is Number -> out.add(value.toFloat())
            is Array<*> -> value.forEach { flattenToArray(it, out) }
            is List<*> -> value.forEach { flattenToArray(it, out) }
        }
    }
}
