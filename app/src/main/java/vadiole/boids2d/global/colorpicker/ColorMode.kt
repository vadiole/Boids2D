package vadiole.boids2d.global.colorpicker


import android.graphics.Color
import vadiole.boids2d.R

enum class ColorMode {

    ARGB {
        override val channels: List<Channel> = listOf(
            Channel(R.string.picker_channel_alpha, 0, 255, Color::alpha),
            Channel(R.string.picker_channel_red, 0, 255, Color::red),
            Channel(R.string.picker_channel_green, 0, 255, Color::green),
            Channel(R.string.picker_channel_blue, 0, 255, Color::blue)
        )

        override fun evaluateColor(channels: List<Channel>): Int = Color.argb(
            channels[0].progress, channels[1].progress, channels[2].progress, channels[3].progress)
    },

    RGB {
        override val channels: List<Channel> = ARGB.channels.drop(1)

        override fun evaluateColor(channels: List<Channel>): Int = Color.rgb(
            channels[0].progress, channels[1].progress, channels[2].progress)
    },

    HSV {
        override val channels: List<Channel> = listOf(
            Channel(R.string.picker_channel_hue, 0, 360, ::hue),
            Channel(R.string.picker_channel_saturation, 0, 100, ::saturation),
            Channel(R.string.picker_channel_value, 0, 100, ::value)
        )

        override fun evaluateColor(channels: List<Channel>): Int = Color.HSVToColor(
            floatArrayOf(
                (channels[0].progress).toFloat(),
                (channels[1].progress / 100.0).toFloat(),
                (channels[2].progress / 100.0).toFloat()
            ))
    };

    abstract internal val channels: List<Channel>

    abstract internal fun evaluateColor(channels: List<Channel>): Int

    internal data class Channel(val nameResourceId: Int,
                                val min: Int, val max: Int,
                                val extractor: (color: Int) -> Int,
                                var progress: Int = 0)

    companion object {
        @JvmStatic fun fromName(name: String?) = values().find { it.name == name } ?: ColorMode.RGB
    }
}
