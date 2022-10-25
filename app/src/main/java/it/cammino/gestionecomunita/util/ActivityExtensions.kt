package it.cammino.gestionecomunita.util

import android.app.Activity
import android.app.ActivityManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.material.color.MaterialColors
import it.cammino.gestionecomunita.R
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