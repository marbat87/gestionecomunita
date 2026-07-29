package it.cammino.gestionecomunita.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityOptionsCompat
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.platform.MaterialSharedAxis
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.util.StringUtils.SHARED_AXIS

fun Activity.createTaskDescription(tag: String): ActivityManager.TaskDescription {
    return when (true) {
        OSUtils.hasT() -> createTaskDescriptionTiramisu(tag)
        OSUtils.hasP() -> createTaskDescriptionP(tag)
        else -> createTaskDescriptionLegacy(tag)
    }
}

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.P)
private fun Activity.createTaskDescriptionP(tag: String): ActivityManager.TaskDescription {
    return ActivityManager.TaskDescription(
        null,
        R.mipmap.ic_launcher,
        MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary, tag)
    )
}

@Suppress("DEPRECATION")
private fun Activity.createTaskDescriptionLegacy(tag: String): ActivityManager.TaskDescription {
    return ActivityManager.TaskDescription(
        null,
        null,
        MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary, tag)
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun Activity.createTaskDescriptionTiramisu(tag: String): ActivityManager.TaskDescription {
    return ActivityManager.TaskDescription.Builder().apply {
        setIcon(R.mipmap.ic_launcher)
        setPrimaryColor(
            MaterialColors.getColor(
                this@createTaskDescriptionTiramisu,
                androidx.appcompat.R.attr.colorPrimary,
                tag
            )
        )
    }.build()
}

fun Activity.slideInRight() {
    overrideOpenTransition(
        R.anim.animate_slide_in_right, R.anim.animate_slide_out_left
    )
}

fun Activity.overrideOpenTransition(@AnimRes enterAnim: Int, @AnimRes exitAnim: Int) {
    if (OSUtils.hasU()) overrideOpenTransitionU(enterAnim, exitAnim)
    else overrideOpenTransitionLegacy(enterAnim, exitAnim)
}

@RequiresApi(34)
fun Activity.overrideOpenTransitionU(@AnimRes enterAnim: Int, @AnimRes exitAnim: Int) {
    overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, enterAnim, exitAnim)
}

@Suppress("DEPRECATION")
fun Activity.overrideOpenTransitionLegacy(@AnimRes enterAnim: Int, @AnimRes exitAnim: Int) {
    overridePendingTransition(
        enterAnim, exitAnim
    )
}

fun Activity.startActivityWithTransition(intent: Intent, axis: Int) {
    if (OSUtils.isObySamsung()) {
        startActivity(intent)
        slideInRight()
    } else {
        val exit = MaterialSharedAxis(axis, true).apply {
            addTarget(R.id.nav_host_fragment_activity_main)
            duration = 700L
        }

        val enter = MaterialSharedAxis(axis, false).apply {
            addTarget(R.id.nav_host_fragment_activity_main)
            duration = 700L
        }
        window.exitTransition = exit
        window.reenterTransition = enter
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
        startActivity(
            intent.putExtras(
                Bundle().apply {
                    putInt(SHARED_AXIS, axis)
                }
            ), options.toBundle()
        )
    }

}

fun Activity.setEnterTransition() {
    if (!OSUtils.isObySamsung()) {
        val axis = intent.getIntExtra(SHARED_AXIS, MaterialSharedAxis.X)
        val enter = MaterialSharedAxis(axis, true).apply {
            duration = 700L
        }
        val returnT = MaterialSharedAxis(axis, false).apply {
            duration = 700L
        }
        window.enterTransition = enter
        window.returnTransition = returnT

        // Allow Activity A’s exit transition to play at the same time as this Activity’s
        // enter transition instead of playing them sequentially.
        window.allowEnterTransitionOverlap = true
    }
}