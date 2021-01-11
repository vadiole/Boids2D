package vadiole.boids2d.global.extensions

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

fun FragmentManager.findDialogByTag(tag: String) = this.findFragmentByTag(tag) as? DialogFragment
