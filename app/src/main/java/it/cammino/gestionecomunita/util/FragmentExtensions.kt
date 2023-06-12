package it.cammino.gestionecomunita.util

import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis

fun Fragment.setEnterTransition() {
    if (!OSUtils.isObySamsung()) {
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, /* forward = */ true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, /* forward = */ false)
    }
}