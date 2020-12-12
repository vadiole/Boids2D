package vadiole.boids2d

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.view.MotionEvent.*

@SuppressLint("ClickableViewAccessibility")
class BoidsGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private var renderer: BoidsRenderer? = null

    override fun setRenderer(renderer: Renderer) {
        this.renderer = renderer as BoidsRenderer
        super.setRenderer(renderer)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            if (event.action == ACTION_MOVE || event.action == ACTION_DOWN) {
                queueEvent { renderer?.actionDown(event.x, event.y, event.pressure) }
            } else if (event.action == ACTION_UP) {
                queueEvent { renderer?.actionUp() }
            }
        }
        return true
    }
}
