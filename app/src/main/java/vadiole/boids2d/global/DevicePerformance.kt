package vadiole.boids2d.global

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import vadiole.boids2d.App
import vadiole.boids2d.Config
import java.io.RandomAccessFile


enum class DevicePerformance(val code: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    fun getMaxBoidsSeekbar() = when (this) {
        LOW -> 50
        MEDIUM -> 60
        HIGH -> 80
    }

    fun getFireworksParticlesCount() = when (this) {
        LOW -> 50
        MEDIUM -> 60
        HIGH -> 100
    }

    fun getFireworksFallParticlesCount() = when (this) {
        LOW -> 20
        MEDIUM -> 30
        HIGH -> 40
    }

    companion object {
        private var valuesByCode: MutableMap<String, DevicePerformance> = HashMap(values().size)
        fun lookupByCode(code: String) = valuesByCode[code]

        init {
            for (value in values()) valuesByCode[value.code] = value
        }

        fun getDevicePerformance(): DevicePerformance {
            var maxCpuFreq = -1
            try {
                val reader =
                    RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r")
                val line: String = reader.readLine()
                maxCpuFreq = line.toInt() / 1000
                reader.close()
            } catch (ignore: Throwable) {
            }

            val androidVersion = Build.VERSION.SDK_INT
            val cpuCount: Int = Runtime.getRuntime().availableProcessors()
            val memoryClass =
                (App.context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).memoryClass
            val result =
                if (androidVersion < 21 || cpuCount <= 2 || memoryClass <= 100 || cpuCount <= 4 && maxCpuFreq != -1 && maxCpuFreq <= 1250 || cpuCount <= 4 && maxCpuFreq <= 1600 && memoryClass <= 128 && androidVersion <= 21 || cpuCount <= 4 && maxCpuFreq <= 1300 && memoryClass <= 128 && androidVersion <= 24) {
                    LOW
                } else if (cpuCount < 8 || memoryClass <= 160 || maxCpuFreq != -1 && maxCpuFreq <= 1650 || maxCpuFreq == -1 && cpuCount == 8 && androidVersion <= 23) {
                    MEDIUM
                } else {
                    HIGH
                }
            Config.devicePerformance = result
            return result
        }
    }
}
