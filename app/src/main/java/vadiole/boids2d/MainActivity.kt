package vadiole.boids2d

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat


@SuppressLint("ClickableViewAccessibility")
class MainActivity : AppCompatActivity(), Settings.OnDialogInteractionListener {
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var mDetector: GestureDetectorCompat

    private var glSurfaceView: BoidsGLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility =
            SYSTEM_UI_FLAG_HIDE_NAVIGATION or SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        supportActionBar?.hide()

        glSurfaceView = BoidsGLSurfaceView(this).apply {
            setEGLConfigChooser(8, 8, 8, 8, 16, 0)
            setRenderer(BoidsRenderer(this@MainActivity))
            setOnTouchListener { v, event ->
                mDetector.onTouchEvent(event)
            }
        }
        val tutorialText = TextView(this).apply {
            setText(R.string.tutorial_settings)
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
        }
        val tutorialLP = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, Gravity.CENTER)
        val frame = FrameLayout(this).apply {
            addView(glSurfaceView)
            if (!Preferences.tutorialSettingsShown) {
                addView(tutorialText, tutorialLP)
                Preferences.tutorialSettingsShown = true
            }
            setContentView(this)
        }
        mDetector = GestureDetectorCompat(this, MyGestureListener { motionEvent ->
            Log.i(TAG, "show settings")
            Settings().show(supportFragmentManager, "settings")
        })
    }

    override fun onPause() {
        glSurfaceView?.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView?.onResume()
    }

    private class MyGestureListener(val callback: (MotionEvent) -> Unit) :
        GestureDetector.SimpleOnGestureListener() {
        val DEBUG_TAG = "DEBUG"
        override fun onDoubleTap(e: MotionEvent): Boolean {
            Log.i(DEBUG_TAG, "double tap")
            callback(e)
            return true
        }
    }

    override fun onSettingsAction() {
        Log.i(TAG, "onSettingsAction")
        glSurfaceView = BoidsGLSurfaceView(this).apply {
            setEGLConfigChooser(8, 8, 8, 8, 16, 0)
            setRenderer(BoidsRenderer(this@MainActivity))
            setOnTouchListener { v, event ->
                mDetector.onTouchEvent(event)
            }
        }
        val frame = FrameLayout(this).apply {
            addView(glSurfaceView)
            setContentView(this)
        }
    }
}
