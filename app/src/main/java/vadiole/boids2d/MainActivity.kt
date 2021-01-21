package vadiole.boids2d

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import vadiole.boids2d.SettingsDialog.Companion.BACK_EVENT_TYPE
import vadiole.boids2d.SettingsDialog.Companion.EVENT_BACK_NAVIGATION
import vadiole.boids2d.base.BaseDialog
import vadiole.boids2d.boids.BoidsGLSurfaceView
import vadiole.boids2d.boids.BoidsRenderer
import vadiole.boids2d.global.extensions.findDialogByTag
import vadiole.boids2d.global.extensions.hide
import vadiole.boids2d.global.extensions.hideSystemUI


@SuppressLint("ClickableViewAccessibility")
class MainActivity : AppCompatActivity(), SettingsDialog.OnDialogInteractionListener {
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var mDetector: GestureDetectorCompat

    private var glSurfaceView: BoidsGLSurfaceView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        glSurfaceView = BoidsGLSurfaceView(this).apply {
            setEGLConfigChooser(8, 8, 8, 8, 16, 0)
            setRenderer(BoidsRenderer(this@MainActivity))
            setOnTouchListener { v, event ->
                mDetector.onTouchEvent(event)
            }
        }
        val tutorialText = TextView(this).apply {
            setText(R.string.tutorial_settings_open)
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
        }
        val tutorialLP = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, Gravity.CENTER)
        FrameLayout(this).apply {
            addView(glSurfaceView)
            if (!Config.tutorialSettingsShown) {
                addView(tutorialText, tutorialLP)
                Config.tutorialSettingsShown = true
            }
            setContentView(this)
        }
        mDetector = GestureDetectorCompat(this, MyGestureListener { event ->
            tutorialText.hide()
            with(supportFragmentManager) {
                val dialog = findDialogByTag("settings") ?: SettingsDialog.newInstance(
                    event.rawX,
                    event.rawY
                )

                if (!dialog.isAdded) dialog.show(this, "settings")
            }
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

    override fun onBackPressed() {
        if ((supportFragmentManager.findDialogByTag("settings") as BaseDialog?)?.onBackPressed() == true) {
            FirebaseAnalytics.getInstance(this).logEvent(SettingsDialog.BACK_EVENT) {
                param(BACK_EVENT_TYPE, EVENT_BACK_NAVIGATION)
            }
            return
        }
        super.onBackPressed()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
        hideSystemUI()
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
            setOnTouchListener { _, event ->
                mDetector.onTouchEvent(event)
            }
        }
        FrameLayout(this).apply {
            addView(glSurfaceView)
            setContentView(this)
        }
    }
}
