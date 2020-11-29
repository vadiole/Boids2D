package com.namaztime.qibla.tools.extensions

import kotlin.math.roundToInt

fun Float.roundToIntDegr(): Int {
    val int = this.roundToInt()
    return (int + 360) % 360
}
