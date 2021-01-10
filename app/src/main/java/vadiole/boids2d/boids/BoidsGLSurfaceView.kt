package vadiole.boids2d.boids

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import vadiole.boids2d.global.extensions.log
import vadiole.boids2d.model.Vector

@SuppressLint("ClickableViewAccessibility")
class BoidsGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val TAG: String = BoidsGLSurfaceView::class.java.simpleName
    private var renderer: BoidsRenderer? = null
    private val targets: SparseArray<Vector> = SparseArray()


    override fun setRenderer(renderer: Renderer) {
        this.renderer = renderer as BoidsRenderer
        super.setRenderer(renderer)

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        try {

            val pointerIndex = event.actionIndex
            val pointerId = event.getPointerId(pointerIndex)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    Log.i(TAG, "onTouch: down")
                    // We have a new pointer. Lets add it to the list of pointers
                    val vector = Vector(event.getX(pointerIndex), event.getY(pointerIndex), 0f)
                    this.targets.put(pointerId, vector)
                }
                MotionEvent.ACTION_MOVE -> {
                    // a pointer was moved
                    val size = event.pointerCount

                    var i = 0
                    while (i < size) {
                        val vector: Vector? = this.targets.get(event.getPointerId(i))
                        if (vector != null) {
                            vector.x = event.getX(i)
                            vector.y = event.getY(i)
                        }
                        i++
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                    Log.i(TAG, "onTouch: up")
                    this.targets.remove(pointerId)
                }
            }
        } catch (e: Exception) {
            e.log(null)
        }

        queueEvent {
            renderer?.onTouch(targets)
        }
        return true
    }
}
