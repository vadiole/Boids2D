package vadiole.boids2d

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import vadiole.boids2d.base.BaseDialog

class Settings : BaseDialog() {
    private val TAG = Settings::class.simpleName
    private var listener: OnDialogInteractionListener? = null
    private var isNeedApply = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.settings, null)
        isNeedApply = false
        val textview: TextView = view.findViewById(R.id.text_boids_count)
        val seekbar: SeekBar = view.findViewById(R.id.slider_boids_count)
        seekbar.apply {
            progress = Preferences.boidsCount / 20
            textview.text = (progress * 20).toString()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    textview.text = (progress * 20).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    isNeedApply = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    Preferences.boidsCount = seekBar.progress * 20
                }
            })
        }


        val textBoidsSize: TextView = view.findViewById(R.id.text_boids_size)
        val seekbarBoidsSize: SeekBar = view.findViewById(R.id.slider_boids_size)
        seekbarBoidsSize.apply {
            progress = Preferences.boidsSize - 1
            textBoidsSize.text = (progress + 1).toString()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
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

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings)
            .setView(view)
            .create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)   // must implement interface
        listener = context as OnDialogInteractionListener
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (isNeedApply) listener?.onSettingsAction()
        super.onDismiss(dialog)
    }

    interface OnDialogInteractionListener {
        fun onSettingsAction()
    }

}
