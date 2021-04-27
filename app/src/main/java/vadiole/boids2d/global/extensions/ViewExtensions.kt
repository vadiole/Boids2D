package vadiole.boids2d.global.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.postDelayed
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.hypot



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
    postDelayed(0) {
        if (!this.isAttachedToWindow) return@postDelayed

        val isHide = _visibility == View.INVISIBLE

        val viewRadius = getMaxRadius(x, y)
        val startRadius = if (isHide) viewRadius else 0f
        val endRadius = if (isHide) 0f else viewRadius

        val anim = ViewAnimationUtils.createCircularReveal(
            this,
            x.toInt(),
            y.toInt(),
            startRadius,
            endRadius
        ).apply {
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
    return listOf(radius1, radius2, radius3, radius4).maxOrNull()!!
}


fun View.absX() = IntArray(2).apply(::getLocationOnScreen)[0]

fun View.absY() = IntArray(2).apply(::getLocationOnScreen)[1]


fun View?.removeSelf() {
    this ?: return
    val parent = parent as? ViewGroup ?: return
    parent.removeView(this)
}

fun View.getRect(): Rect {
    val location = IntArray(2)
    getLocationOnScreen(location)
    return Rect(location[0], location[1], location[0] + height, location[1] + width)
}

fun View.getColor(id: Int): Int {
    return ContextCompat.getColor(context, id)
}

fun View.getDimen(id: Int): Float {
    return context.resources.getDimension(id)
}

fun View.getDimenInt(id: Int): Int {
    return context.resources.getDimension(id).toInt()
}


@ColorInt
@SuppressLint("Recycle")
fun View.themeColor(
    @AttrRes themeAttrId: Int,
): Int {
    return context.obtainStyledAttributes(
        intArrayOf(themeAttrId)
    ).use {
        it.getColor(0, Color.DKGRAY)
    }
}
