package vadiole.boids2d

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.WallpaperManager
import android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
import android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.HapticFeedbackConstants.*
import android.widget.Toast
import androidx.appcompat.graphics.drawable.AnimatedStateListDrawableCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import kotlinx.coroutines.delay
import vadiole.boids2d.base.BaseDialog
import vadiole.boids2d.databinding.AdvancedSettingsBinding
import vadiole.boids2d.databinding.DialogSettingsBinding
import vadiole.boids2d.global.AnalyticsEvent
import vadiole.boids2d.global.extensions.*
import vadiole.boids2d.global.onclick.onClick
import vadiole.boids2d.wallpaper.BoidsWallpaperService
import vadiole.colorpicker.ColorPickerDialog
import kotlin.math.abs


class SettingsDialog : BaseDialog() {
    private var listener: OnDialogInteractionListener? = null
    private lateinit var mDetector: GestureDetectorCompat
    private var isNeedApply = false
    private var isExitAnimateRun = false

    private var animPointX = 0f
    private var animPointY = 0f

    private var binding: DialogSettingsBinding? = null
    private var bindingAdvanced: AdvancedSettingsBinding? = null


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
                    300L,
                    animPointX,
                    animPointY
                )
                window?.decorView?.apply {
                    alpha = 0f
                    animate().alpha(1.0f).setDuration(250L)
                        .withEndAction {
                            window?.decorView?.performHapticFeedback(
                                KEYBOARD_TAP,
                                FLAG_IGNORE_GLOBAL_SETTING
                            )
                        }
                        .start()
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
        return DialogSettingsBinding.inflate(inflater).also {
            binding = it
            bindingAdvanced = AdvancedSettingsBinding.bind(it.root)
        }.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = binding?.run {
        view.setEdgeToEdgeSystemUiFlags()

        mDetector = GestureDetectorCompat(requireContext(), MyGestureListener {
            Log.i(TAG, "dismiss settings")
            logEvent(AnalyticsEvent.CLOSE_SETTINGS_SWIPE)
            onBackPressed()
        })

        isNeedApply = false
        viewBoidsColor.setImageDrawable(ColorDrawable(Config.boidsColor))
        viewBackgroundColor.setImageDrawable(ColorDrawable(Config.backgroundColor))

        settingsSetWallpaper.onClick {
            try {
                val component =
                    ComponentName(requireContext(), BoidsWallpaperService::class.java)
                val intent = Intent(ACTION_CHANGE_LIVE_WALLPAPER)
                intent.putExtra(EXTRA_LIVE_WALLPAPER_COMPONENT, component)
                startActivity(intent)
                logEvent(AnalyticsEvent.OPEN_SET_WALLPAPER)
            } catch (e3: ActivityNotFoundException) {
                e3.log("open live wallpaper preview failed")
                try {
                    val intent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                    startActivity(intent)
                    logEvent(AnalyticsEvent.OPEN_SET_WALLPAPER)
                } catch (e2: ActivityNotFoundException) {
                    e2.log("open live wallpaper chooser failed")
                    try {
                        val intent = Intent()
                        intent.action = "com.bn.nook.CHANGE_WALLPAPER"
                        startActivity(intent)
                        logEvent(AnalyticsEvent.OPEN_SET_WALLPAPER)
                    } catch (e: ActivityNotFoundException) {
                        e.log("showing error dialog:(")
                        alertDialog(
                            R.string.app_name,
                            R.string.error_live_wallpaper,
                            R.string.action_ok
                        ).show()
                    }
                }
            }
        }

        settingsBoidsCount.apply {
            max = Config.devicePerformance.getMaxBoidsSeekbar()
            progress = Config.boidsCount / 20

            setValueAdapter {
                (it * 20).toString()
            }

            setListener(
                onStart = {
                    isNeedApply = true
                },
                onStop = {
                    Config.boidsCount = it.progress * 20
                },
            )
        }

        settingsBoidsSize.apply {
            progress = Config.boidsSize - 1

            setValueAdapter {
                (it + 1).toString()
            }

            setListener(
                onStart = {
                    isNeedApply = true
                },
                onStop = {
                    Config.boidsSize = it.progress + 1
                },
            )
        }

        buttonBack.onClick {
            logEvent(AnalyticsEvent.CLOSE_SETTINGS_BUTTON)
            onBackPressed()
        }

        settingsBoidsColor.onClick {
            val picker = ColorPickerDialog.Builder()
                .setColorModel(Config.colorPickerModel)
                .setInitialColor(Config.boidsColor)
                .setButtonOkText(R.string.action_ok)
                .setButtonCancelText(R.string.action_cancel)
                .setColorModelSwitchEnabled(true)
                .onColorModelSwitched { colorModel ->
                    Config.colorPickerModel = colorModel
                }
                .onColorSelected { color ->
                    Config.boidsColor = color
                    viewBoidsColor.setImageDrawable(ColorDrawable(color))
                    isNeedApply = true
                }
                .create()
            picker.show(childFragmentManager, "boids_color")
        }

        settingsBackgroundColor.onClick {
            val picker = ColorPickerDialog.Builder()
                .setColorModel(Config.colorPickerModel)
                .setInitialColor(Config.backgroundColor)
                .setColorModelSwitchEnabled(true)
                .onColorModelSwitched { colorModel ->
                    Config.colorPickerModel = colorModel
                }
                .onColorSelected { color ->
                    Config.backgroundColor = color
                    viewBackgroundColor.setImageDrawable(ColorDrawable(color))
                    isNeedApply = true
                }
                .create()
            picker.show(childFragmentManager, "background_color")
        }

        settingsAdvanced.apply {

            val drawable = AnimatedStateListDrawableCompat().apply {
                addState(
                    intArrayOf(android.R.attr.state_selected),
                    VectorDrawableCompat.create(
                        resources,
                        R.drawable.expandcollapse_expanded,
                        null
                    )!!,
                    R.id.expanded
                )
                addState(
                    intArrayOf(),
                    VectorDrawableCompat.create(
                        resources,
                        R.drawable.expandcollapse_collapsed,
                        null
                    )!!,
                    R.id.collapsed
                )
                addTransition(
                    R.id.expanded,
                    R.id.collapsed,
                    AnimatedVectorDrawableCompat.create(
                        requireContext(),
                        R.drawable.expandcollapse_to_collapsed
                    )!!,
                    false
                )
                addTransition(
                    R.id.collapsed,
                    R.id.expanded,
                    AnimatedVectorDrawableCompat.create(
                        requireContext(),
                        R.drawable.expandcollapse_to_expanded,
                    )!!,
                    false
                )
            }

            isSelected = Config.advancedExtended


            setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)

            onClick {
                val visible = !Config.advancedExtended

                settingsAdvancedContainer.isVisible = visible
                isSelected = visible

                Config.advancedExtended = visible
            }

            setOnLongClickListener {
                with(Config) {
                    if (userAlignment != 10 || userSeparation != 10 || userCohesion != 10 || userTarget != 10) {

                        it.performHapticFeedback(KEYBOARD_TAP, FLAG_IGNORE_GLOBAL_SETTING)

                        Config.run {
                            userSeparation = 10
                            userAlignment = 10
                            userCohesion = 10
                            userTarget = 10
                        }

                        bindingAdvanced?.run {
                            settingsAdvancedSeparation.progress = 10
                            settingsAdvancedAlignment.progress = 10
                            settingsAdvancedCohesion.progress = 10
                            settingsAdvancedTarget.progress = 10
                        }

                        isNeedApply = true
                    }
                }
                true
            }
        }

        settingsAdvancedContainer.isVisible = Config.advancedExtended

        bindingAdvanced?.run {
            settingsAdvancedSeparation.progress = Config.userSeparation
            settingsAdvancedSeparation.setListener(
                onStart = {
                    isNeedApply = true
                },
                onStop = {
                    Config.userSeparation = it.progress
                },
            )

            settingsAdvancedAlignment.progress = Config.userAlignment
            settingsAdvancedAlignment.setListener(
                onStart = {
                    isNeedApply = true
                },
                onStop = {
                    Config.userAlignment = it.progress
                },
            )

            settingsAdvancedCohesion.progress = Config.userCohesion
            settingsAdvancedCohesion.setListener(
                onStart = {
                    isNeedApply = true
                },
                onStop = {
                    Config.userCohesion = it.progress
                },
            )

            settingsAdvancedTarget.progress = Config.userTarget
            settingsAdvancedTarget.setListener(
                onStart = {
                    isNeedApply = true
                },
                onStop = {
                    Config.userTarget = it.progress
                },
            )
        }

        madeBy.onClick {
            logEvent(AnalyticsEvent.OPEN_DEVELOPER_PAGE)
            openUrl(
                "https://play.google.com/store/apps/dev?id=4763171503902347202",
                R.string.error_google_play
            )
        }

        madeBy.setOnLongClickListener {
            if (fireworks.isStarted) return@setOnLongClickListener true
            it.performHapticFeedback(KEYBOARD_TAP, FLAG_IGNORE_GLOBAL_SETTING)
            fireworks.start()
            logEvent(AnalyticsEvent.FIREWORKS)
            true
        }

        layout.setOnTouchListener { _, event ->
            mDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }


        if (!Config.tutorialExitSettingsShown) {
            Toast.makeText(context, R.string.tutorial_settings_close, Toast.LENGTH_LONG).show()
            Config.tutorialExitSettingsShown = true
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(1000)
            logEvent(AnalyticsEvent.OPEN_SETTINGS)
            startPostponedEnterTransition()
        }

        root.doOnPreDraw {
        }
        Unit
    } ?: Unit

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
                isExitAnimateRun = true
                if (isNeedApply) listener?.onSettingsAction()
                it.withCircularAnimation(View.INVISIBLE, 400L, animPointX, animPointY) {

                    if (isAdded) dismissAllowingStateLoss()
                    isExitAnimateRun = false
                }
                it.alpha = 1.0f
                it.animate().alpha(0.0f).setDuration(300L).withEndAction {
                    if (it.isAttachedToWindow) it.performHapticFeedback(
                        KEYBOARD_TAP,
                        FLAG_IGNORE_GLOBAL_SETTING
                    )
                }.start()

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

    private fun logEvent(event: AnalyticsEvent) = analytics?.run {
        when (event) {
            AnalyticsEvent.OPEN_SETTINGS -> logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, "settings")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "SettingsDialog")
            }
            AnalyticsEvent.CLOSE_SETTINGS_SWIPE -> logEvent(BACK_EVENT) {
                param(BACK_EVENT_TYPE, EVENT_SWIPE_UP)
                param(FirebaseAnalytics.Param.SCREEN_NAME, "settings")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "SettingsDialog")
            }
            AnalyticsEvent.CLOSE_SETTINGS_BUTTON -> logEvent(BACK_EVENT) {
                param(BACK_EVENT_TYPE, EVENT_BACK_BUTTON)
                param(FirebaseAnalytics.Param.SCREEN_NAME, "settings")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "SettingsDialog")
            }

            AnalyticsEvent.OPEN_DEVELOPER_PAGE -> logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, "developer_page")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "GooglePlay")
            }

            AnalyticsEvent.FIREWORKS -> logEvent("easter_egg") {
                param("easter_egg_type", "fireworks")
                param(FirebaseAnalytics.Param.SCREEN_NAME, "settings")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "SettingsDialog")
            }
            AnalyticsEvent.OPEN_SET_WALLPAPER -> logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, "open_set_wallpaper")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "SetWallpaper")
            }
        }
    }

    companion object {
        const val ANIM_POINT_X = "animPointX"
        const val ANIM_POINT_Y = "animPointY"

        private const val TAG = "settings_dialog"
        const val BACK_EVENT = "settings_back_event"
        const val BACK_EVENT_TYPE = "settings_back_event_type"
        private const val EVENT_SWIPE_UP = "swipe_up"
        private const val EVENT_BACK_BUTTON = "back_button"
        const val EVENT_BACK_NAVIGATION = "navigation"

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
