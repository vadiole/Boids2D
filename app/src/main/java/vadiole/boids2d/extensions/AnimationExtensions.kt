package com.namaztime.qibla.tools.extensions

import android.view.animation.Animation


fun Animation.setAnimationListener(
    repeat: (Animation?) -> Unit = {},
    start: (Animation?) -> Unit = {},
    end: (Animation?) -> Unit = {}
): Animation {
    setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) = repeat.invoke(animation)

        override fun onAnimationStart(animation: Animation?) = start.invoke(animation)

        override fun onAnimationEnd(animation: Animation?) = end.invoke(animation)
    })
    return this
}


