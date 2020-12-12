package vadiole.boids2d.global.onclick

import android.view.View

fun View.onClick(intervalMillis: Long = 200, onClick: ((View) -> Unit)) {
    setOnClickListener(DebouncingOnClickListener(intervalMillis, onClick))
}
