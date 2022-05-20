package it.cammino.gestionecomunita.util

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.textfield.TextInputLayout
import it.cammino.gestionecomunita.R
import java.util.*

@Suppress("DEPRECATION")
private fun Configuration.getSystemLocaleLegacy(): Locale {
    return locale
}

@TargetApi(Build.VERSION_CODES.N)
private fun Configuration.getSystemLocaleN(): Locale {
    return locales.get(0)
}

val Resources.systemLocale: Locale
    get() {
        return if (OSUtils.hasN())
            configuration.getSystemLocaleN()
        else
            configuration.getSystemLocaleLegacy()
    }

fun Activity.setupNavBarColor() {
    if (OSUtils.hasO()) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            if (!ThemeUtils.isDarkMode(context)) setLightNavigationBar(context)
        setLightNavigationBar()
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun Activity.setLightNavigationBar() {
    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).isAppearanceLightNavigationBars = true
}

fun Activity.setLigthStatusBar(light: Boolean) {
    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).isAppearanceLightStatusBars = light
    setLighStatusBarFlag(light)
}

private fun Activity.setLighStatusBarFlag(light: Boolean) {
    if (OSUtils.hasM())
        setLighStatusBarFlagM(light)
}

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.M)
private fun Activity.setLighStatusBarFlagM(light: Boolean) {
    if (light)
        window
            .decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
}

fun Context.validateMandatoryField(textInput: TextInputLayout): Boolean {
    textInput.editText?.let {
        if (it.text.isNullOrBlank()) {
            textInput.error = getString(R.string.mandatory_field)
            return false
        } else {
            textInput.error = null
            return true
        }
    }
    return true
}
