package vadiole.boids2d.global.colorpicker

import androidx.annotation.ColorInt


interface OnPickColorListener {
    fun onColorSelected(@ColorInt color: Int)
}

