package vadiole.boids2d

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import vadiole.boids2d.base.BaseDialog
import vadiole.boids2d.databinding.DialogSettingsBinding
import vadiole.boids2d.global.colorpicker.ColorMode
import vadiole.boids2d.global.colorpicker.ColorPickerDialog
import vadiole.boids2d.global.extensions.withCircularAnimation
import vadiole.boids2d.global.onclick.onClick
import vadiole.boids2d.global.viewbinding.viewBinding

class SettingsDialog : BaseDialog() {
    private var listener: OnDialogInteractionListener? = null
    private var isNeedApply = false

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
                window?.decorView?.withCircularAnimation(View.VISIBLE, 500L, animPointX, animPointY)
                window?.decorView?.apply {
                    alpha = 0f
                    animate().alpha(1.0f).setDuration(400L).start()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, b: Bundle?): View {
        return inflater.inflate(R.layout.dialog_settings, group, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setEdgeToEdgeSystemUiFlags()
        with(requireDialog()) {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            window?.decorView?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }

        isNeedApply = false
        with(binding) {
            viewBoidsColor.setImageDrawable(ColorDrawable(Preferences.boidsColor))
            viewBackgroundColor.setImageDrawable(ColorDrawable(Preferences.backgroundColor))

            sliderBoidsCount.apply {
                progress = Preferences.boidsCount / 20
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
                        Preferences.boidsCount = seekBar.progress * 20
                    }
                })
            }


            sliderBoidsSize.apply {
                progress = Preferences.boidsSize - 1
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
                        Preferences.boidsSize = seekBar.progress + 1
                    }
                })
            }

            buttonBack.onClick {
                onBackPressed()
            }

            settingsBoidsColor.onClick {
                val picker = ColorPickerDialog.Builder()
                    .colorMode(ColorMode.RGB)
                    .initialColor(Preferences.boidsColor)
                    .onColorSelected { color ->
                        Preferences.boidsColor = color
                        viewBoidsColor.setImageDrawable(ColorDrawable(color))
                        isNeedApply = true
                    }
                    .create()
                picker.show(childFragmentManager, "boids_color")
//                Toast.makeText(context, "Pick boids color", Toast.LENGTH_SHORT).show()
            }
            settingsBackgroundColor.onClick {
                val picker = ColorPickerDialog.Builder()
                    .colorMode(ColorMode.RGB)
                    .initialColor(Preferences.backgroundColor)
                    .onColorSelected { color ->
                        Preferences.backgroundColor = color
                        viewBackgroundColor.setImageDrawable(ColorDrawable(color))
                        isNeedApply = true
                    }
                    .create()
                picker.show(childFragmentManager, "background_color")
//                Toast.makeText(context, "Pick background color", Toast.LENGTH_SHORT).show()
            }
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
        if (isResumed) {
            dialog?.window?.decorView?.let {
                if (isNeedApply) listener?.onSettingsAction()
                it.withCircularAnimation(View.INVISIBLE, 400L, animPointX, animPointY) {
                    if (it.isAttachedToWindow) it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    if (isAdded) dismiss()
                }
                it.alpha = 1.0f
                it.animate().alpha(0.0f).setStartDelay(100L).setDuration(400L).start()

                return true
            }
        }
        return super.onBackPressed()
    }

    interface OnDialogInteractionListener {
        fun onSettingsAction()
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
