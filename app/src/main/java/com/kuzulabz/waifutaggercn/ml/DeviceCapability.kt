package com.kuzulabz.waifutaggercn.ml

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File

/**
 * 设备 AI 加速能力检测：CPU / GPU / NNAPI / QNN。
 *
 * 用于在模型管理页面展示"当前设备支持哪些加速方案"，
 * 并给每个模型生成预估运行速度与推荐评级。
 */
object DeviceCapability {

    data class CapabilityReport(
        val cpuSupported: Boolean,         // 总是 true
        val gpuSupported: Boolean,          // 是否有 GPU（Vulkan/OpenCL）
        val nnapiSupported: Boolean,        // Android NNAPI 是否可用
        val qnnSupported: Boolean,          // Qualcomm QNN（仅骁龙判断）
        val totalMemMB: Long,               // 可用内存
        val cpuCores: Int,
        val bigCoreCount: Int,             // 大核+超大核数量
        val socName: String,               // 推测的 SoC 名称
        val androidVersion: Int
    ) {
        /** 整体性能等级 1-5（★） */
        val performanceTier: Int
            get() = when {
                totalMemMB >= 8000 && bigCoreCount >= 3 -> 5
                totalMemMB >= 6000 && bigCoreCount >= 2 -> 4
                totalMemMB >= 4000 && bigCoreCount >= 1 -> 3
                totalMemMB >= 3000 -> 2
                else -> 1
            }
    }

    /** 检测当前设备能力（可能涉及系统文件读取，应在 IO 线程调用） */
    fun detect(context: Context): CapabilityReport {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val totalMemMB = memInfo.totalMem / (1024 * 1024)

        val cpuCores = Runtime.getRuntime().availableProcessors()
        val bigCoreCount = detectBigCores()

        val socName = detectSocName()
        val isQualcomm = socName.contains("snapdragon", ignoreCase = true) ||
            socName.contains("qualcomm", ignoreCase = true) ||
            socName.contains("sm8", ignoreCase = true)

        val nnapiSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        val qnnSupported = isQualcomm
        val gpuSupported = hasGpu()

        return CapabilityReport(
            cpuSupported = true,
            gpuSupported = gpuSupported,
            nnapiSupported = nnapiSupported,
            qnnSupported = qnnSupported,
            totalMemMB = totalMemMB,
            cpuCores = cpuCores,
            bigCoreCount = bigCoreCount,
            socName = socName,
            androidVersion = Build.VERSION.SDK_INT
        )
    }

    /** 从 /sys/devices/system/cpu/cpuN/cpufreq/cpuinfo_max_freq 判断大小核 */
    private fun detectBigCores(): Int {
        val freqs = mutableListOf<Long>()
        var core = 0
        while (true) {
            val freqFile = File("/sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq")
            if (!freqFile.exists()) break
            val freq = freqFile.readText().trim().toLongOrNull() ?: 0L
            if (freq > 0) freqs.add(freq)
            core++
        }
        if (freqs.isEmpty()) return 1
        // 频率最高的核心群算大核（阈值取最大频率的 85%）
        val maxFreq = freqs.maxOrNull() ?: 0L
        val threshold = (maxFreq * 0.85).toLong()
        val bigCount = freqs.count { it >= threshold }
        return bigCount.coerceAtLeast(1)
    }

    /** 推测 SoC 名称 */
    private fun detectSocName(): String {
        // 读取 /proc/cpuinfo 中的 Hardware 字段
        return try {
            val cpuInfo = File("/proc/cpuinfo").readText()
            val hwLine = cpuInfo.lineSequence()
                .firstOrNull { it.startsWith("Hardware", ignoreCase = true) }
            val hardware = hwLine?.substringAfter(":")?.trim()
            when {
                hardware != null && hardware.isNotBlank() -> hardware
                cpuInfo.contains("SM8650", ignoreCase = true) -> "Snapdragon 8 Gen3 (SM8650)"
                cpuInfo.contains("SM8550", ignoreCase = true) -> "Snapdragon 8 Gen2 (SM8550)"
                cpuInfo.contains("SM8450", ignoreCase = true) -> "Snapdragon 8 Gen1 (SM8450)"
                cpuInfo.contains("MT6983", ignoreCase = true) ||
                    cpuInfo.contains("MT6985", ignoreCase = true) -> "MediaTek Dimensity 9000+"
                cpuInfo.contains("exynos", ignoreCase = true) -> "Samsung Exynos"
                else -> "Unknown SoC"
            }
        } catch (e: Exception) {
            Log.w("DeviceCapability", "SoC detection failed", e)
            "Unknown SoC"
        }
    }

    /** 粗略判断是否有 GPU：检查 /sys/class/kgsl 或 vulkan 可用性 */
    private fun hasGpu(): Boolean {
        // 高通 GPU 驱动
        val kgsl = File("/sys/class/kgsl/kgsl-3d0")
        if (kgsl.exists()) return true
        // Mali / Adreno 通用检查
        val gpuDirs = File("/sys/class/").listFiles()?.filter {
            it.name.contains("mali", ignoreCase = true) || it.name.contains("gpu", ignoreCase = true)
        }
        return !gpuDirs.isNullOrEmpty()
    }

    /**
     * 给一个模型生成兼容性评级（星级 0-5）和预估速度。
     * @param entry 模型条目
     * @param report 设备能力报告
     * @return (starRating, estimatedSpeedMs, recommendation)
     */
    fun evaluateModel(
        entry: ModelRegistry.ModelEntry,
        report: CapabilityReport
    ): Triple<Int, Long, String> {
        val tier = report.performanceTier
        // 模型越大越吃性能，根据大小和设备等级评估
        val sizeFactor = when {
            entry.sizeBytes > 500_000_000 -> 3   // >500MB 很重
            entry.sizeBytes > 100_000_000 -> 2   // >100MB 中等
            entry.sizeBytes > 10_000_000 -> 1     // >10MB 轻量
            else -> 0                              // <10MB 极轻
        }

        val baseSpeed = when (entry.category) {
            ModelRegistry.ModelCategory.TAGGER -> 800L * (1 + sizeFactor)
            ModelRegistry.ModelCategory.DETECTION -> 15L * (1 + sizeFactor)
            ModelRegistry.ModelCategory.SEGMENTATION -> 25L * (1 + sizeFactor)
        }
        val speedMultiplier = when (tier) {
            5 -> 0.3
            4 -> 0.5
            3 -> 0.8
            2 -> 1.2
            else -> 2.0
        }
        val nnapiBoost = if (report.nnapiSupported && entry.category != ModelRegistry.ModelCategory.TAGGER) 0.6 else 1.0
        val estimatedMs = (baseSpeed * speedMultiplier * nnapiBoost).toLong().coerceAtLeast(1L)

        val starRating = when {
            tier >= 4 && sizeFactor <= 1 -> 5
            tier >= 4 -> 4
            tier == 3 && sizeFactor <= 1 -> 4
            tier == 3 && sizeFactor == 2 -> 3
            tier == 3 -> 2
            tier == 2 && sizeFactor <= 1 -> 3
            tier == 2 && sizeFactor == 2 -> 2
            tier == 2 -> 1
            tier == 1 && sizeFactor <= 1 -> 2
            else -> 1
        }

        val recommendation = when {
            starRating == 5 -> "完美运行"
            starRating == 4 -> "推荐"
            starRating == 3 -> "可用"
            starRating == 2 -> "较慢"
            starRating == 1 -> "不推荐"
            else -> "可能无法运行"
        }

        return Triple(starRating, estimatedMs, recommendation)
    }
}
