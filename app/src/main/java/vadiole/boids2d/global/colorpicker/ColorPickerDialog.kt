package vadiole.boids2d.global.colorpicker

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import kotlinx.coroutines.delay
import vadiole.boids2d.base.BaseDialog
import kotlin.properties.Delegates

class ColorPickerDialog : BaseDialog() {

    companion object {
        private const val ArgInitialColor = "arg_initial_color"
        private const val ArgColorModeName = "arg_color_mode_name"

        @JvmStatic
        private fun newInstance(
            @ColorInt initialColor: Int,
            colorMode: ColorMode
        ): ColorPickerDialog {
            val fragment = ColorPickerDialog()
            fragment.arguments = makeArgs(initialColor, colorMode)
            return fragment
        }

        @JvmStatic
        private fun makeArgs(@ColorInt initialColor: Int, colorMode: ColorMode): Bundle {
            val args = Bundle()
            args.putInt(ArgInitialColor, initialColor)
            args.putString(ArgColorModeName, colorMode.name)
            return args
        }
    }

    class Builder {
        @ColorInt
        private var initialColor: Int = ColorPickerView.defaultColor
        private var colorMode: ColorMode = ColorPickerView.defaultModel
        private var listener: OnPickColorListener? = null

        fun initialColor(@ColorInt initialColor: Int): Builder {
            this.initialColor = initialColor
            return this
        }

        fun colorMode(colorMode: ColorMode): Builder {
            this.colorMode = colorMode
            return this
        }

        fun onColorSelected(listener: OnPickColorListener): Builder {
            this.listener = listener
            return this
        }

        fun onColorSelected(callback: (color: Int) -> Unit): Builder {
            this.listener = object : OnPickColorListener {
                override fun onColorSelected(color: Int) {
                    callback(color)
                }

            }
            return this
        }

        fun build(): ColorPickerDialog {
            val fragment = newInstance(initialColor, colorMode)
            fragment.listener = listener
            return fragment
        }
    }

    private var listener: OnPickColorListener? = null
    private var pickerView: ColorPickerView by Delegates.notNull()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        pickerView = if (savedInstanceState == null) {
            ColorPickerView(
                arguments?.getInt(ArgInitialColor) ?: Color.DKGRAY,
                ColorMode.fromName(arguments?.getString(ArgColorModeName)),
                requireContext()
            )
        } else {
            ColorPickerView(
                savedInstanceState.getInt(ArgInitialColor, ColorPickerView.defaultColor),
                ColorMode.fromName(savedInstanceState.getString(ArgColorModeName)),
                requireContext()
            )
        }


        pickerView.enableButtonBar(object : ColorPickerView.ButtonBarListener {
            override fun onNegativeButtonClick() = dismiss()
            override fun onPositiveButtonClick(color: Int) {
                listener?.onColorSelected(color)
                dismiss()
            }
        })

        lifecycleScope.launchWhenResumed {
            delay(1000)
            analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, "color_picker")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "ColorPickerDialog")
            }
        }

        return AlertDialog.Builder(requireActivity())
            .setView(pickerView)
            .create().apply {
                setOnShowListener {
                    with(dialog!!.window!!) {
                        clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

                        //Update the WindowManager with the new attributes (no nicer way I know of to do this)..
                        val wm =
                            requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
                        wm.updateViewLayout(decorView, attributes)
                    }
                }
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putAll(makeArgs(pickerView.currentColor, pickerView.colorMode))
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        listener = null
        super.onDestroyView()
    }
}
