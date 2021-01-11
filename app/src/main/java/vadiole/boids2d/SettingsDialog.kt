package vadiole.boids2d

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
import android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.HapticFeedbackConstants.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import vadiole.boids2d.base.BaseDialog
import vadiole.boids2d.databinding.DialogSettingsBinding
import vadiole.boids2d.global.colorpicker.ColorMode
import vadiole.boids2d.global.colorpicker.ColorPickerDialog
import vadiole.boids2d.global.extensions.openUrl
import vadiole.boids2d.global.extensions.toPx
import vadiole.boids2d.global.extensions.withCircularAnimation
import vadiole.boids2d.global.onclick.onClick
import vadiole.boids2d.global.viewbinding.viewBinding
import vadiole.boids2d.wallpaper.BoidsWallpaperService
import kotlin.math.abs


class SettingsDialog : BaseDialog() {
    private val TAG = "SettingsDialog"
    private var listener: OnDialogInteractionListener? = null
    private lateinit var mDetector: GestureDetectorCompat
    private var isNeedApply = false
    private var isExitAnimateRun = false

    private var animPointX = 0f
    private var animPointY = 0f

    private val binding by viewBinding(DialogSettingsBinding::bind)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            animPointX = it.getFloat(ANIM_POINT_X)
            animPointY = it.getFloat(ANIM_POINT_Y)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.decorView?.visibility = View.INVISIBLE
            setOnShowListener {
                window?.decorView?.withCircularAnimation(
                    View.VISIBLE,
                    400L,
                    animPointX,
                    animPointY
                )
                window?.decorView?.apply {
                    alpha = 0f
                    animate().alpha(1.0f).setDuration(250L)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                window?.decorView?.performHapticFeedback(
                                    KEYBOARD_TAP,
                                    FLAG_IGNORE_GLOBAL_SETTING
                                )
                            }
                        }).start()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_settings, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setEdgeToEdgeSystemUiFlags()

        mDetector = GestureDetectorCompat(requireContext(), MyGestureListener {
            Log.i(TAG, "dismiss settings")
            onBackPressed()
        })


        isNeedApply = false
        with(binding) {
            viewBoidsColor.setImageDrawable(ColorDrawable(Config.boidsColor))
            viewBackgroundColor.setImageDrawable(ColorDrawable(Config.backgroundColor))

            settingsSetWallpaper.onClick {
                val component = ComponentName(requireContext(), BoidsWallpaperService::class.java)
                val intent = Intent(ACTION_CHANGE_LIVE_WALLPAPER)
                intent.putExtra(EXTRA_LIVE_WALLPAPER_COMPONENT, component)
                startActivity(intent)
            }

            sliderBoidsCount.apply {
                max = Config.devicePerformance.getMaxBoidsSeekbar()
                progress = Config.boidsCount / 20
                textBoidsCount.text = (progress * 20).toString()
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        textBoidsCount.text = (progress * 20).toString()
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {
                        isNeedApply = true
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        Config.boidsCount = seekBar.progress * 20
                    }
                })
            }


            sliderBoidsSize.apply {
                progress = Config.boidsSize - 1
                textBoidsSize.text = (progress + 1).toString()
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        textBoidsSize.text = (progress + 1).toString()
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {
                        isNeedApply = true
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        Config.boidsSize = seekBar.progress + 1
                    }
                })
            }

            buttonBack.onClick {
                onBackPressed()
            }

            settingsBoidsColor.onClick {
                val picker = ColorPickerDialog.Builder()
                    .colorMode(ColorMode.RGB)
                    .initialColor(Config.boidsColor)
                    .onColorSelected { color ->
                        Config.boidsColor = color
                        viewBoidsColor.setImageDrawable(ColorDrawable(color))
                        isNeedApply = true
                    }
                    .build()
                picker.show(childFragmentManager, "boids_color")
//                Toast.makeText(context, "Pick boids color", Toast.LENGTH_SHORT).show()
            }
            settingsBackgroundColor.onClick {
                val picker = ColorPickerDialog.Builder()
                    .colorMode(ColorMode.RGB)
                    .initialColor(Config.backgroundColor)
                    .onColorSelected { color ->
                        Config.backgroundColor = color
                        viewBackgroundColor.setImageDrawable(ColorDrawable(color))
                        isNeedApply = true
                    }
                    .build()
                picker.show(childFragmentManager, "background_color")
//                Toast.makeText(context, "Pick background color", Toast.LENGTH_SHORT).show()
            }

            madeBy.onClick {
                openUrl(
                    "https://play.google.com/store/apps/dev?id=4763171503902347202",
                    R.string.error_google_play
                )
            }

            madeBy.setOnLongClickListener {
                if (fireworks.isStarted) return@setOnLongClickListener true
                it.performHapticFeedback(KEYBOARD_TAP, FLAG_IGNORE_GLOBAL_SETTING)
                fireworks.start()
                true
            }

            root.setOnTouchListener { _, event ->
                mDetector.onTouchEvent(event)
                return@setOnTouchListener true
            }
        }


        if (!Config.tutorialExitSettingsShown) {
            Toast.makeText(context, R.string.tutorial_settings_close, Toast.LENGTH_LONG).show()
            Config.tutorialExitSettingsShown = true
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as OnDialogInteractionListener
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    override fun getTheme(): Int = R.style.dialog_full_screen

    override fun onBackPressed(): Boolean {
        if (isResumed && !isExitAnimateRun) {
            dialog?.window?.decorView?.let {
                if (isNeedApply) listener?.onSettingsAction()
                isExitAnimateRun = true
                it.withCircularAnimation(View.INVISIBLE, 400L, animPointX, animPointY) {
                    if (it.isAttachedToWindow) it.performHapticFeedback(KEYBOARD_TAP)
                    if (isAdded) dismiss()
                    isExitAnimateRun = false
                }
                it.alpha = 1.0f
                it.animate().alpha(0.0f).setStartDelay(50L).setDuration(300L).start()

                return true
            }
        }
        return super.onBackPressed()
    }

    interface OnDialogInteractionListener {
        fun onSettingsAction()
    }

    private class MyGestureListener(val onFlingUp: () -> Unit) :
        GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val x1 = e1.x
            val y1 = e1.y
            val x2 = e2.x
            val y2 = e2.y
            if (abs(x1 - x2) < 80.toPx && y1 - y2 > 100.toPx) {
                onFlingUp.invoke()
                return true
            }
            return false
        }
    }

    companion object {
        const val ANIM_POINT_X = "animPointX"
        const val ANIM_POINT_Y = "animPointY"

        @JvmStatic
        fun newInstance(pointX: Float, pointY: Float) =
            SettingsDialog().apply {
                val bundle = Bundle()
                bundle.putFloat(ANIM_POINT_X, pointX)
                bundle.putFloat(ANIM_POINT_Y, pointY)
                arguments = bundle
            }
    }
}
