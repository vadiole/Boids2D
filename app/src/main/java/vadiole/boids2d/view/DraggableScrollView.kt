package vadiole.boids2d.view

import android.content.Context
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.ScrollView
import vadiole.boids2d.view.DraggableScrollView.ViewStateEnum.*


class DraggableScrollView : ScrollView {

    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    val TAG = DraggableScrollView::class.simpleName
    var state: ViewStateEnum = NONE
    private var mListener: OnDragScrollViewListener? = null
    private var mVelocityTracker: VelocityTracker? = null

    private var startX: Int = 0
    private var startY: Int = 0

    var dX: Float = 0f
    var path = Path()

    private val viewConfiguration by lazy {
        ViewConfiguration.get(context)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                state = NONE
                //  сохраняем начальные координаты
                startX = ev.rawX.toInt()
                startY = ev.rawY.toInt()

                dX = ev.x

                mVelocityTracker?.clear()
                mVelocityTracker = mVelocityTracker ?: VelocityTracker.obtain()
                mVelocityTracker?.addMovement(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                val x = ev.rawX.toInt()
                val y = ev.rawY.toInt()

                mVelocityTracker?.apply {
                    val pointerId: Int = ev.getPointerId(ev.actionIndex)
                    addMovement(ev)
                    computeCurrentVelocity(100)

//                    Log.d(TAG, "X velocity: ${getXVelocity(pointerId)}")
//                    Log.d(TAG, "Y velocity: ${getYVelocity(pointerId)}")
                }

                when (state) {
                    SCROLL -> return super.onInterceptTouchEvent(ev)
                    DRAG -> return true
                    NONE -> {
                        //  scroll condition
                        if (super.onInterceptTouchEvent(ev) && super.canScrollVertically(1)) {
                            state = SCROLL
                            return super.onInterceptTouchEvent(ev)
                        }

                        //  drag condition
                        if (startY - y > viewConfiguration.scaledTouchSlop) {
                            startY = y
                            return if (mListener?.onDragStarted(ev) == true) {
                                state = DRAG
                                true
                            } else false
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (state == DRAG) return true
                if (state == SCROLL) state = NONE
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (state != DRAG) return super.onTouchEvent(ev)

        when (ev.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val x = ev.rawX.toInt()
                val y = ev.rawY.toInt()

                mVelocityTracker?.apply {
                    val pointerId: Int = ev.getPointerId(ev.actionIndex)
                    addMovement(ev)
                    computeCurrentVelocity(100)

//                    Log.d(TAG, "X velocity: ${getXVelocity(pointerId)}")
//                    Log.d(TAG, "Y velocity: ${getYVelocity(pointerId)}")
                }

                mListener?.onDragChanged(ev)
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val pointerId: Int = ev.getPointerId(ev.actionIndex)
                mListener?.onDragEnd(ev, mVelocityTracker!!.getXVelocity(pointerId))
                state = NONE
                mVelocityTracker?.recycle()
                mVelocityTracker = null
                return true
            }
        }

        return super.onTouchEvent(ev)
    }

    fun setOnDragScrollViewListener(mScrollViewListener: OnDragScrollViewListener?) {
        this.mListener = mScrollViewListener
    }


    interface OnDragScrollViewListener {
        fun onDragStarted(event: MotionEvent): Boolean
        fun onDragChanged(event: MotionEvent)
        fun onDragEnd(event: MotionEvent, speed: Float)
    }

    enum class ViewStateEnum {
        NONE,   //  undefined state
        DRAG,   //  dragging
        SCROLL  //  scrolling
    }
}
