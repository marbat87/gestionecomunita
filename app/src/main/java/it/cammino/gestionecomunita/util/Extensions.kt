@file:Suppress("unused")

package it.cammino.gestionecomunita.util

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.textfield.TextInputLayout
import it.cammino.gestionecomunita.R
import java.sql.Date
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
        if (!isDarkMode) setLightNavigationBar()
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

fun Context.validateMandatoryField(textInput: TextInputLayout, showError: Boolean = true): Boolean {
    textInput.editText?.let {
        if (it.text.isNullOrBlank()) {
            textInput.error = if (showError) getString(R.string.mandatory_field) else null
            return false
        } else {
            textInput.error = null
            return true
        }
    }
    return true
}

fun String.capitalize(res: Resources): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            res.systemLocale
        ) else it.toString()
    }
}

fun CharSequence.capitalize(res: Resources): String {
    return this.toString().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            res.systemLocale
        ) else it.toString()
    }
}

val Context.isDarkMode: Boolean
    get() {
        return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

@SuppressLint("ClickableViewAccessibility")
fun EditText?.setupDatePicker(activity: FragmentActivity, tag: String, titleId: Int) {
    this?.inputType = InputType.TYPE_NULL
    this?.setOnKeyListener(null)
    this?.setOnTouchListener { _, motionEvent ->
        if (motionEvent.action == MotionEvent.ACTION_UP) {
            val picker =
                MaterialDatePicker.Builder.datePicker()
                    .setSelection(
                        if (this.text.isNullOrBlank()) MaterialDatePicker.todayInUtcMilliseconds() else
                            Utility.getDateFromString(
                                context,
                                this.text?.toString() ?: ""
                            )?.time
                    )
                    .setTitleText(titleId)
                    .build()
            picker.show(activity.supportFragmentManager, "${tag}Picker")
            picker.addOnPositiveButtonClickListener {
                this.setText(
                    Utility.getStringFromDate(
                        context,
                        Date(it)
                    )
                )
            }
        }
        false
    }
}

fun TextInputLayout?.validateDate(): Boolean {
    this?.editText?.let {
        if (!it.text.isNullOrEmpty() &&
            Utility.getDateFromString(
                context,
                it.text.toString()
            ) == null
        ) {
            this.error = context.getString(R.string.invalid_date)
            return false
        } else {
            this.error = null
            return true
        }
    }
    return true
}