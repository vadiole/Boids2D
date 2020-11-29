package vadiole.boids2d.base

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment

abstract class BaseDialog : DialogFragment(), View.OnClickListener, OnFragmentBackPressed {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() = requireActivity().onBackPressed()
        }
    }

    override fun onClick(v: View?) {}

    override fun onBackPressed(): Boolean = false
}
