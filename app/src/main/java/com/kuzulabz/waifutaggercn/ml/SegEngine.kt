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
 * YOLO11n-seg（与 YOLOv8-seg 输出格式一致）实例分割 ONNX 引擎。
 *
 * 用于"精准模式"第二阶段：在 DetEngine 检测到目标后，对整图做实例分割，
 * 生成精确 mask，按 mask 外接矩形裁剪出多个主体区域，分别交给 WD Tagger。
 *
 * 模型输出格式（ultralytics 导出，imgsz=640, opset>=12, simplify=true）：
 *   output0  : [1, 4+nc+32, 8400]  ->  [1, 116, 8400] (nc=80)
 *              116 = 4(bbox cx,cy,w,h) + 80(class scores, 已 sigmoid) + 32(mask coef)
 *   output1  : [1, 32, 160, 160]   prototype masks
 *
 * mask = sigmoid(mask_coef[32] @ proto[32,160*160]) -> [160,160]
 *        再按 bbox 裁剪、>0.5 二值化、上采样回原图。
 */
class SegEngine(private val context: Context) {

    data class Instance(
        val bbox: RectF,            // 原图坐标 x1,y1,x2,y2
        val score: Float,
        val classId: Int,
        val mask: Bitmap            // 原图尺寸的二值 mask（人物像素为白，其余透明）
    )

    data class Det(
        val cx: Float, val cy: Float, val w: Float, val h: Float,
        val score: Float, val cls: Int, val coef: FloatArray
    )

    enum class ExecProvider { CPU, NNAPI }

    private val lock = Any()
    private var session: OrtSession? = null
    private var environment: OrtEnvironment? = null
    private var execProvider: ExecProvider = ExecProvider.CPU
    private var inputSize: Int = 640
    private var preferredModelName: String? = null  // 用户选择的分割模型文件名（不含路径）
    var currentProvider: ExecProvider = ExecProvider.CPU
        private set

    val isReady: Boolean
        get() = session != null

    /** COCO 80 类标签 */
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

    /** 设置用户首选的分割模型文件名（如 "yolo11s-seg.onnx"），下次 load 时生效 */
    fun setPreferredModel(modelFileName: String?) {
        preferredModelName = modelFileName
    }

    /**
     * 加载 YOLO11n-seg 模型。
     * @param provider CPU 或 NNAPI。NNAPI 在 Android 14/15 上可显著加速，
     *                 失败时自动回退到 CPU。
     * @return null 成功，否则返回人类可读错误。
     */
    fun load(provider: ExecProvider = ExecProvider.CPU): String? = synchronized(lock) {
        runCatching { session?.close() }
        session = null

        val modelFile = resolveModelFile() ?: run {
            return context.getStringSafe("seg_model_missing")
        }

        val env = try {
            OrtEnvironment.getEnvironment().also { environment = it }
        } catch (e: Throwable) {
            return "ONNX 环境初始化失败: ${e.message}"
        }

        val opts = OrtSession.SessionOptions().apply {
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            setIntraOpNumThreads(4)
            // 内存优化：释放中间张量
            setMemoryPatternOptimization(false)
            addConfigEntry("session.use_env_allocators", "1")
        }

        // 尝试指定 provider，失败则回退
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
            // NNAPI 失败 → 回退 CPU 重试
            if (effectiveProvider == ExecProvider.NNAPI) {
                effectiveProvider = ExecProvider.CPU
                val cpuOpts = OrtSession.SessionOptions().apply {
                    setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
                    setIntraOpNumThreads(4)
                }
                try {
                    env.createSession(modelFile.absolutePath, cpuOpts)
                } catch (e2: Throwable) {
                    return "分割模型加载失败: ${e2.message}"
                }
            } else {
                return "分割模型加载失败: ${e.message}"
            }
        }

        execProvider = effectiveProvider
        currentProvider = effectiveProvider

        // 读取输入尺寸（通常 640）
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
        // 优先使用用户选择的分割模型
        val dir = TaggerEngine.modelDirectory(context)
        val candidates = mutableListOf<String>()
        preferredModelName?.let { candidates.add(it) }
        // 默认查找顺序
        listOf("yolo11n-seg.onnx", "yolov8n-seg.onnx", "yolo11s-seg.onnx").forEach { name ->
            if (name !in candidates) candidates.add(name)
        }
        candidates.forEach { name ->
            val f = File(dir, name)
            if (f.exists() && f.length() > 0L) return f
        }
        // 其次 assets 内置
        return try {
            context.assets.open("yolo11n-seg.onnx").use { input ->
                val out = File(dir, "yolo11n-seg.onnx")
                out.outputStream().use { output -> input.copyTo(output) }
                if (out.length() > 0L) out else null
            }
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * 对一张图做实例分割，仅返回 person(classId==0) 实例。
     * @param conf 置信度阈值，默认 0.25
     * @param iou NMS IoU 阈值，默认 0.45
     * @param maxInstances 最多返回数量
     */
    fun segmentPersons(
        bitmap: Bitmap,
        conf: Float = 0.25f,
        iou: Float = 0.45f,
        maxInstances: Int = 8
    ): List<Instance> = segmentAll(bitmap, conf, iou, maxInstances, setOf(0))

    /**
     * 对一张图做实例分割，返回所有类别的实例。
     * 与 [segmentPersons] 不同，不限定类别，适用于两阶段流水线中
     * DetEngine 已检测到目标后做精细分割。
     *
     * @param classFilter 若非 null，仅返回这些类别的实例；null 表示全部类别
     */
    fun segmentAll(
        bitmap: Bitmap,
        conf: Float = 0.25f,
        iou: Float = 0.45f,
        maxInstances: Int = 12,
        classFilter: Set<Int>? = null
    ): List<Instance> = synchronized(lock) {
        val currentSession = session ?: return emptyList<Instance>()
        val env = environment ?: return emptyList<Instance>()

        // 1. letterbox 预处理
        val (inputBuffer, scale, padW, padH) = preprocess(bitmap)
        val shape = longArrayOf(1, 3, inputSize.toLong(), inputSize.toLong())

        OnnxTensor.createTensor(env, inputBuffer, shape).use { tensor ->
            val inputName = currentSession.inputNames.first()
            currentSession.run(mapOf(inputName to tensor)).use { results ->
                val outputs = results.toList()
                if (outputs.size < 2) return@segmentAll emptyList<Instance>()
                val out0Raw = outputs[0].value.value
                val protosRaw = outputs[1].value.value
                val out0 = flattenAndReshape3D(out0Raw)
                val protos = flattenAndReshape4D(protosRaw)
                if (out0.isEmpty() || protos.isEmpty()) return@segmentAll emptyList<Instance>()
                val instances = postprocess(out0, protos, bitmap, scale, padW, padH, conf, iou, classFilter)
                instances.sortedByDescending { it.score }.take(maxInstances)
            }
        }
    }

    /** letterbox：等比缩放 + 灰边填充到 inputSize×inputSize。返回 (buffer, scale, padW, padH) */
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
        // NCHW, RGB, /255
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

    private fun postprocess(
        out0: Array<Array<FloatArray>>,          // [1][116][8400]
        protos: Array<Array<Array<FloatArray>>>, // [1][32][160][160]
        bitmap: Bitmap,
        scale: Float,
        padW: Int,
        padH: Int,
        conf: Float,
        iou: Float,
        classFilter: Set<Int>? = null
    ): List<Instance> {
        val numClasses = 80
        val maskCoefDim = 32
        val anchors = out0[0][0].size        // 8400
        val protoMat = protos[0]             // [32][160][160]
        val protoH = protoMat[0].size        // 160
        val protoW = protoMat[0][0].size     // 160

        // 1. 解析候选框（按 anchor 遍历，channel 维度是 116）
        val candidates = ArrayList<Det>()
        for (a in 0 until anchors) {
            // bbox: cx,cy,w,h 在 channel [0..3]，相对 640 输入
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
            if (classFilter != null && maxCls !in classFilter) continue
            val coef = FloatArray(maskCoefDim) { out0[0][4 + numClasses + it][a] }
            candidates.add(Det(cx, cy, w, h, maxScore, maxCls, coef))
        }
        if (candidates.isEmpty()) return emptyList()

        // 2. NMS（按分数降序贪心）
        candidates.sortByDescending { it.score }
        val kept = ArrayList<Det>()
        val suppressed = BooleanArray(candidates.size)
        for (i in candidates.indices) {
            if (suppressed[i]) continue
            kept.add(candidates[i])
            for (j in i + 1 until candidates.size) {
                if (suppressed[j]) continue
                if (iouDet(candidates[i], candidates[j]) > iou) suppressed[j] = true
            }
        }

        // 3. 合成 mask：sigmoid(coef[32] @ proto[32, 160*160]) -> [160,160]
        //    det.cx,cy,w,h 在 640 输入(letterbox)空间
        val protoScale = protoW.toFloat() / inputSize  // 160/640 = 0.25
        val results = ArrayList<Instance>()
        for (det in kept) {
            // proto 空间的 bbox（用于 crop）
            val bx1 = ((det.cx - det.w / 2f) * protoScale).toInt().coerceIn(0, protoW - 1)
            val by1 = ((det.cy - det.h / 2f) * protoScale).toInt().coerceIn(0, protoH - 1)
            val bx2 = (((det.cx + det.w / 2f) * protoScale).toInt() + 1).coerceIn(1, protoW)
            val by2 = (((det.cy + det.h / 2f) * protoScale).toInt() + 1).coerceIn(1, protoH)

            // 原图坐标：去掉 pad、除以 scale（用于 Instance 与裁剪）
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

            // mask 矩阵乘：sigmoid(coef[32] · proto[32,160,160]) -> [160,160]
            val mask = FloatArray(protoH * protoW)
            for (ph in 0 until protoH) {
                for (pw in 0 until protoW) {
                    var sum = 0f
                    for (k in 0 until maskCoefDim) {
                        sum += det.coef[k] * protoMat[k][ph][pw]
                    }
                    mask[ph * protoW + pw] = sigmoid(sum)
                }
            }

            // 上采样到原图尺寸：原图像素 -> letterbox 输入坐标 -> proto 坐标，
            // 仅在 bbox 内采样，否则置透明（crop_mask）
            val fullMask = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val maskPixels = IntArray(bitmap.width * bitmap.height)
            for (my in 0 until bitmap.height) {
                // 原图 y -> 640 输入 y -> proto y
                val iy = my * scale + padH
                val py = (iy * protoScale).toInt()
                val inBboxY = py in by1 until by2
                for (mx in 0 until bitmap.width) {
                    val ix = mx * scale + padW
                    val px = (ix * protoScale).toInt()
                    val v = if (inBboxY && px in bx1 until bx2) mask[py * protoW + px] else 0f
                    maskPixels[my * bitmap.width + mx] = if (v > 0.5f) Color.WHITE else Color.TRANSPARENT
                }
            }
            fullMask.setPixels(maskPixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            results.add(Instance(bbox, det.score, det.cls, fullMask))
        }
        return results
    }

    private fun iouDet(a: Det, b: Det): Float {
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

    private fun sigmoid(v: Float): Float = (1f / (1f + exp(-v.toDouble()))).toFloat()

    /**
     * 按 mask 外接矩形裁剪人物区域（带一定 padding，保证标签完整）。
     * 若没有人物则返回 null（调用方回退到普通模式）。
     */
    fun cropPersonRegion(bitmap: Bitmap, instances: List<Instance>): Bitmap? {
        if (instances.isEmpty()) return null
        // 取所有人物 mask 的联合外接矩形
        var minX = Int.MAX_VALUE; var minY = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE; var maxY = Int.MIN_VALUE
        for (ins in instances) {
            // 扫描 mask 找实际有效像素范围（bbox 已含 padding 内的精确边界）
            val mask = ins.mask
            val w = mask.width; val h = mask.height
            val px = IntArray(w * h)
            mask.getPixels(px, 0, w, 0, 0, w, h)
            for (y in 0 until h) {
                for (x in 0 until w) {
                    if (px[y * w + x] == Color.WHITE) {
                        if (x < minX) minX = x
                        if (x > maxX) maxX = x
                        if (y < minY) minY = y
                        if (y > maxY) maxY = y
                    }
                }
            }
        }
        if (minX == Int.MAX_VALUE) return null

        // padding 8%，避免裁太紧丢掉发丝/服饰细节
        val pw = ((maxX - minX) * 0.08f).toInt().coerceAtLeast(8)
        val ph = ((maxY - minY) * 0.08f).toInt().coerceAtLeast(8)
        val left = (minX - pw).coerceAtLeast(0)
        val top = (minY - ph).coerceAtLeast(0)
        val right = (maxX + pw).coerceAtMost(bitmap.width)
        val bottom = (maxY + ph).coerceAtMost(bitmap.height)
        val cropW = right - left
        val cropH = bottom - top
        if (cropW <= 1 || cropH <= 1) return null
        return Bitmap.createBitmap(bitmap, left, top, cropW, cropH)
    }

    /**
     * 按每个实例的 mask 外接矩形，分别裁剪出多个区域。
     * 与 [cropPersonRegion] 不同，本方法返回每个实例独立的裁剪图，
     * 适用于两阶段流水线中分别对每个主体做 WD Tagger 推理。
     *
     * @return 每个实例对应的 (classId, className, croppedBitmap)
     */
    fun cropInstances(
        bitmap: Bitmap,
        instances: List<Instance>,
        paddingRatio: Float = 0.10f
    ): List<CropResult> {
        if (instances.isEmpty()) return emptyList()
        val results = ArrayList<CropResult>()
        for (ins in instances) {
            val mask = ins.mask
            val w = mask.width; val h = mask.height
            val px = IntArray(w * h)
            mask.getPixels(px, 0, w, 0, 0, w, h)
            // 扫描 mask 找有效像素范围
            var minX = Int.MAX_VALUE; var minY = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE; var maxY = Int.MIN_VALUE
            for (y in 0 until h) {
                for (x in 0 until w) {
                    if (px[y * w + x] == Color.WHITE) {
                        if (x < minX) minX = x
                        if (x > maxX) maxX = x
                        if (y < minY) minY = y
                        if (y > maxY) maxY = y
                    }
                }
            }
            if (minX == Int.MAX_VALUE) continue
            val pw = ((maxX - minX) * paddingRatio).toInt().coerceAtLeast(8)
            val ph = ((maxY - minY) * paddingRatio).toInt().coerceAtLeast(8)
            val left = (minX - pw).coerceAtLeast(0)
            val top = (minY - ph).coerceAtLeast(0)
            val right = (maxX + pw).coerceAtMost(bitmap.width)
            val bottom = (maxY + ph).coerceAtMost(bitmap.height)
            val cropW = right - left
            val cropH = bottom - top
            if (cropW <= 1 || cropH <= 1) continue
            val cropped = Bitmap.createBitmap(bitmap, left, top, cropW, cropH)
            val className = if (ins.classId < cocoLabels.size) cocoLabels[ins.classId] else "class_${ins.classId}"
            results.add(CropResult(ins.classId, className, ins.score, cropped))
        }
        return results
    }

    /** 单个裁剪结果 */
    data class CropResult(
        val classId: Int,
        val className: String,
        val score: Float,
        val bitmap: Bitmap
    )

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
        // YOLO seg: 116 channels, 8400 anchors
        val total = flat.size
        val channels = if (total % 8400 == 0) total / 8400 else 116
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

    /** 兜底：将任意嵌套结构扁平化为 float 数组，再 reshape 为 float[][][][] */
    private fun flattenAndReshape4D(raw: Any?): Array<Array<Array<FloatArray>>> {
        val flat = mutableListOf<Float>()
        flattenToArray(raw, flat)
        if (flat.isEmpty()) return arrayOf(arrayOf(arrayOf(FloatArray(0))))
        // 期望形状 [1, 32, 160, 160]
        val total = flat.size
        val maskDim = 32
        val maskSize = 160 * 160  // 25600
        val expected = maskDim * maskSize
        if (total != expected && total % maskDim == 0) {
            // 尝试推断空间维度
            val spatial = total / maskDim
            val sq = Math.sqrt(spatial.toDouble()).toInt()
            if (sq * sq == spatial) {
                val result = Array(1) { Array(maskDim) { Array(sq) { FloatArray(sq) } } }
                for (m in 0 until maskDim) {
                    for (h in 0 until sq) {
                        for (w in 0 until sq) {
                            result[0][m][h][w] = flat[m * spatial + h * sq + w]
                        }
                    }
                }
                return result
            }
        }
        // 默认 160x160
        if (total != expected) return arrayOf(arrayOf(arrayOf(FloatArray(0))))
        val result = Array(1) { Array(maskDim) { Array(160) { FloatArray(160) } } }
        for (m in 0 until maskDim) {
            for (h in 0 until 160) {
                for (w in 0 until 160) {
                    result[0][m][h][w] = flat[m * 25600 + h * 160 + w]
                }
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

private fun Context.getStringSafe(key: String): String = when (key) {
    "seg_model_missing" -> "未找到分割模型。请将 yolo11n-seg.onnx 放入应用模型目录。"
    else -> ""
}
