package vadiole.boids2d.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.setPadding
import vadiole.boids2d.R
import vadiole.boids2d.global.extensions.toPx


@SuppressLint("RtlHardcoded")
class ValueSeekbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var mListener: SeekBar.OnSeekBarChangeListener? = null
    private var mValueAdapter: (Int) -> String = { it.toString() }

    private var mSeekbar: SeekBar? = null
    private var mValueTextView: AppCompatTextView? = null


    private var textColor = Int.MAX_VALUE
    private var thumbTint = Int.MAX_VALUE
    private var progressTint = Int.MAX_VALUE

    var max: Int = 100
        set(value) {
            field = value
            mSeekbar?.max = value
        }


    var progress: Int = Int.MAX_VALUE
        set(value) {
            field = value
            mSeekbar?.progress = value
        }
        get() = mSeekbar?.progress ?: Int.MAX_VALUE


    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ValueSeekbar, 0, 0)
        try {
            progress = ta.getInteger(R.styleable.ValueSeekbar_progress, Int.MAX_VALUE)
            thumbTint = ta.getColor(R.styleable.ValueSeekbar_thumbTint, Int.MAX_VALUE)
            progressTint = ta.getColor(R.styleable.ValueSeekbar_progressTint, Int.MAX_VALUE)
            textColor = ta.getColor(R.styleable.ValueSeekbar_textColor, Int.MAX_VALUE)
            max = ta.getInteger(R.styleable.ValueSeekbar_max, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ta.recycle()
        }

        mSeekbar = SeekBar(context).apply {
            if (this@ValueSeekbar.progress != Int.MAX_VALUE) {
                progress = this@ValueSeekbar.progress
            }

            if (progressTint != Int.MAX_VALUE) {
                progressTintList = ColorStateList.valueOf(progressTint)
            }

            if (thumbTint != Int.MAX_VALUE) {
                thumbTintList = ColorStateList.valueOf(thumbTint)
            }


            max = this@ValueSeekbar.max

            setPadding(0)
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO

            val layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                Gravity.LEFT or Gravity.CENTER_VERTICAL
            ).apply {
//                setMargins(margin, 0, margin, 0)
            }

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    mValueTextView?.text = mValueAdapter(progress)
                    mListener?.onProgressChanged(seekBar, progress, fromUser)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    mListener?.onStartTrackingTouch(seekBar)
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    mListener?.onStopTrackingTouch(seekBar)
                }
            })

            addView(this, layoutParams)
        }

        mValueTextView = AppCompatTextView(context).apply {
            setTextAppearance(context, R.style.TextAppearance_AppCompat_Caption)

            if (this@ValueSeekbar.textColor != Int.MAX_VALUE) {
                setTextColor(textColor)
            }

            if (progress != Int.MAX_VALUE) {
                text = mValueAdapter(progress)
            }

            setLines(1)
            gravity = Gravity.CENTER
            maxLines = 1
            minWidth = 36.toPx
            typeface = Typeface.MONOSPACE
            isSingleLine = true
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
            val layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT,
                Gravity.END or Gravity.CENTER_VERTICAL
            ).apply {
//                setMargins(margin, 0, margin, 0)
            }
            addView(this, layoutParams)
        }
        setWillNotDraw(true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), heightMeasureSpec)

        val availableWidth: Int =
            measuredWidth - paddingRight - paddingLeft - 8.toPx
        var width = availableWidth / 2

        mValueTextView!!.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
        )

        width = availableWidth - mValueTextView!!.measuredWidth - 0.toPx

        mSeekbar?.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
        )
    }

    fun setListener(onStart: (seekbar: SeekBar) -> Unit, onStop: (seekbar: SeekBar) -> Unit) {
        mListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) =
                Unit

            override fun onStartTrackingTouch(seekBar: SeekBar) = onStart(seekBar)

            override fun onStopTrackingTouch(seekBar: SeekBar) = onStop(seekBar)
        }
    }

    fun setProgress(progress: Int, animate: Boolean = false) {
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            mSeekbar?.setProgress(progress, animate)
        } else {
            mSeekbar?.setProgress(progress)
        }
    }

    fun setValueAdapter(transform: (progress: Int) -> String) {
        mValueAdapter = transform
        mValueTextView?.text = mValueAdapter(progress)
    }
}

