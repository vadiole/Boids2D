package vadiole.boids2d.global.colorpicker

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import vadiole.boids2d.R
import kotlin.properties.Delegates

class ColorPickerDialog : DialogFragment() {

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

        fun create(): ColorPickerDialog {
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

        return AlertDialog.Builder(
            ContextThemeWrapper(
                context,
                R.style.Theme_AppCompat_Dialog_Alert
            )
        )
            .setView(pickerView)
            .create()
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
