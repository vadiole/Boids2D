package vadiole.boids2d.base

import android.app.Dialog
import android.content.Context.WINDOW_SERVICE
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment

abstract class BaseDialog : DialogFragment(), View.OnClickListener, OnFragmentBackPressed {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() = requireActivity().onBackPressed()
            override fun setOnShowListener(listener: DialogInterface.OnShowListener?) {
                super.setOnShowListener {
                    with(dialog!!.window!!) {
                        clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

                        //Update the WindowManager with the new attributes (no nicer way I know of to do this)..
                        val wm = requireActivity().getSystemService(WINDOW_SERVICE) as WindowManager
                        wm.updateViewLayout(decorView, attributes)
                    }
                    listener?.onShow(it)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        with(dialog!!.window!!) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
            }
            setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            )
            decorView.systemUiVisibility = requireActivity().window.decorView.systemUiVisibility
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onClick(v: View?) {}

    override fun onBackPressed(): Boolean = false
}
