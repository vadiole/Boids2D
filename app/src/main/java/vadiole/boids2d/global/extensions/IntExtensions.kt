package vadiole.boids2d.global.extensions

import android.content.res.Resources
import kotlin.math.abs

val Int.toDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.toPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Int.toSignedString() = when {
    this > 0 -> "+${abs(this)}"
    this < 0 -> "−${abs(this)}"
    else -> "$this"
}
