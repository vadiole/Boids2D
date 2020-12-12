package vadiole.boids2d.global.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewAnimationUtils
import androidx.annotation.IntDef
import androidx.core.view.postDelayed
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.hypot


fun View.onClick(func: () -> Unit) = setOnClickListener { func() }


fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}



/** @hide */
@IntDef(View.VISIBLE, View.INVISIBLE)
annotation class Visibility

fun View.withCircularAnimation(
    @Visibility _visibility: Int,
    _duration: Long = 500L,
    x: Float = pivotX,
    y: Float = pivotY,
    onAnimationEnd: () -> Unit = {}
) {

    postDelayed(50) {
        val isHide = _visibility == View.INVISIBLE

        val viewRadius = getMaxRadius(x, y)
        val startRadius = if (isHide) viewRadius else 0f
        val endRadius = if (isHide) 0f else viewRadius

        val anim = ViewAnimationUtils.createCircularReveal(this, x.toInt(), y.toInt(), startRadius, endRadius).apply {
            duration = _duration
            interpolator = FastOutSlowInInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (isHide) visibility = View.INVISIBLE
                    onAnimationEnd.invoke()
                }
            })
        }


        if (!isHide) visibility = View.VISIBLE
        anim.start()
    }
}

fun View.getMaxRadius(x: Float = pivotX, y: Float = pivotY): Float {
/*   _______  x→
    |1     2|
    |   .   |
    |3     4|
     ‾‾‾‾‾‾‾‾
    y
    ↓
                 */
    val radius1 = hypot(x, y)
    val radius2 = hypot(width - x, y)
    val radius3 = hypot(x, height - y)
    val radius4 = hypot(width - x, height - y)
    return listOf(radius1, radius2, radius3, radius4).max()!!
}


fun View.absX() = IntArray(2).apply(::getLocationOnScreen)[0]

fun View.absY() = IntArray(2).apply(::getLocationOnScreen)[1]

