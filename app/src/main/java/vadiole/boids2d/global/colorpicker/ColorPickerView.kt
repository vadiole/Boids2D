package vadiole.boids2d.global.colorpicker

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import vadiole.boids2d.R

internal class ColorPickerView : RelativeLayout {

    companion object {
        const val defaultColor = Color.GRAY
        val defaultModel = ColorMode.RGB
    }

    @ColorInt
    var currentColor: Int
        private set

    val colorMode: ColorMode

    constructor(context: Context) : this(defaultColor, defaultModel, context)

    constructor(
        @ColorInt initialColor: Int = Color.DKGRAY,
        colorMode: ColorMode,
        context: Context
    ) : super(context) {
        this.currentColor = initialColor
        this.colorMode = colorMode
        init()
    }

    private fun init(): Unit {
        inflate(context, R.layout.color_picker_view, this)
        clipToPadding = false

        val colorView: View = findViewById(R.id.color_view)
        colorView.setBackgroundColor(currentColor)

        val channelViews = colorMode.channels.map { ChannelView(it, currentColor, context) }

        val seekbarChangeListener: () -> Unit = {
            currentColor = colorMode.evaluateColor(channelViews.map { it.channel })
            colorView.background = ColorDrawable(currentColor)
        }

        val channelContainer = findViewById<ViewGroup>(R.id.channel_container)
        channelViews.forEach {
            channelContainer.addView(it)
            it.registerListener(seekbarChangeListener)
        }
    }

    internal interface ButtonBarListener {
        fun onPositiveButtonClick(color: Int)
        fun onNegativeButtonClick()
    }

    internal fun enableButtonBar(listener: ButtonBarListener?) {
        with(findViewById<LinearLayout>(R.id.button_bar)) {
            val positiveButton = findViewById<Button>(R.id.positive_button)
            val negativeButton = findViewById<Button>(R.id.negative_button)

            if (listener != null) {
                visibility = VISIBLE
                positiveButton.setOnClickListener { listener.onPositiveButtonClick(currentColor) }
                negativeButton.setOnClickListener { listener.onNegativeButtonClick() }
            } else {
                visibility = GONE
                positiveButton.setOnClickListener(null)
                negativeButton.setOnClickListener(null)
            }
        }
    }
}
