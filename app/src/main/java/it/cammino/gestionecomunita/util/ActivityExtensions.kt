package it.cammino.gestionecomunita.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.platform.MaterialSharedAxis
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.util.StringUtils.SHARED_AXIS
import java.io.*
import java.util.*

fun Activity.createTaskDescription(tag: String): ActivityManager.TaskDescription {
    return when (true) {
        OSUtils.hasT() -> createTaskDescriptionTiramisu(tag)
        OSUtils.hasP() -> createTaskDescriptionP(tag)
        else -> createTaskDescriptionLegacy(tag)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun Activity.createTaskDescriptionTiramisu(tag: String): ActivityManager.TaskDescription {
    val builder = ActivityManager.TaskDescription.Builder()
    builder.setIcon(R.mipmap.ic_launcher)
    builder.setPrimaryColor(
        MaterialColors.getColor(
            this,
            androidx.appcompat.R.attr.colorPrimary,
            tag
        )
    )
    return builder.build()
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

fun Activity.slideInRight() {
    overridePendingTransition(
        R.anim.animate_slide_in_right, R.anim.animate_slide_out_left
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
                bundleOf(SHARED_AXIS to axis)
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