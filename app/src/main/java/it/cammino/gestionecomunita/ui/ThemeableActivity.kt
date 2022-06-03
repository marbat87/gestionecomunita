package it.cammino.gestionecomunita.ui

import android.annotation.TargetApi
import android.app.ActivityManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.MaterialColors
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.util.OSUtils
import it.cammino.gestionecomunita.util.isDarkMode
import it.cammino.gestionecomunita.util.setLigthStatusBar
import it.cammino.gestionecomunita.util.setupNavBarColor

abstract class ThemeableActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {

        setupNavBarColor()
        updateStatusBarLightMode(true)

        setTaskDescription()

        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        updateStatusBarLightMode(true)
    }

    private fun updateStatusBarLightMode(auto: Boolean) {
        setLigthStatusBar(if (auto) !isDarkMode else false)
    }

//    fun setTransparentStatusBar(trasparent: Boolean) {
//        window.statusBarColor = if (trasparent) ContextCompat.getColor(
//            this,
//            android.R.color.transparent
//        ) else SurfaceColors.SURFACE_2.getColor(this)
//    }

    private fun setTaskDescription() {
        if (OSUtils.hasP())
            setTaskDescriptionP()
        else
            setTaskDescriptionL()
    }

    @Suppress("DEPRECATION")
    private fun setTaskDescriptionL() {
        val taskDesc = ActivityManager.TaskDescription(
            null,
            null,
            MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary, TAG)
        )
        setTaskDescription(taskDesc)
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun setTaskDescriptionP() {
        val taskDesc = ActivityManager.TaskDescription(
            null,
            R.mipmap.ic_launcher,
            MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary, TAG)
        )
        setTaskDescription(taskDesc)
    }

    companion object {
        internal val TAG = ThemeableActivity::class.java.canonicalName
    }

}
