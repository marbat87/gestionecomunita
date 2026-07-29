package it.cammino.gestionecomunita.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.textfield.TextInputLayout
import it.cammino.gestionecomunita.R
import java.io.Serializable
import java.sql.Date
import java.util.Locale

val Resources.systemLocale: Locale
    get() {
        return configuration.locales.get(0)
    }

fun Activity.setupNavBarColor() {
    if (this is ComponentActivity) {
        val navBarColor = SurfaceColors.SURFACE_2.getColor(this)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(navBarColor, navBarColor)
        )
    }
}

//fun Activity.setLightNavigationBar(light: Boolean) {
//    WindowInsetsControllerCompat(
//        window,
//        window.decorView
//    ).isAppearanceLightNavigationBars = light
//}

fun Activity.setLightStatusBar(light: Boolean) {
    WindowInsetsControllerCompat(
        window, window.decorView
    ).isAppearanceLightStatusBars = light
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
            val picker = MaterialDatePicker.Builder.datePicker().setSelection(
                if (this.text.isNullOrBlank()) MaterialDatePicker.todayInUtcMilliseconds() else Utility.getDateFromString(
                    context, this.text?.toString() ?: ""
                )?.time
            ).setTitleText(titleId).build()
            picker.show(activity.supportFragmentManager, "${tag}Picker")
            picker.addOnPositiveButtonClickListener {
                this.setText(
                    Utility.getStringFromDate(
                        context, Date(it)
                    )
                )
            }
        }
        false
    }
}

fun TextInputLayout?.validateDate(): Boolean {
    this?.editText?.let {
        if (!it.text.isNullOrEmpty() && Utility.getDateFromString(
                context, it.text.toString()
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

fun <T : Serializable> Bundle.getSerializableWrapper(key: String, clazz: Class<T>): T? {
    return if (OSUtils.hasT()) {
        this.getSerializableT(key, clazz)
    } else {
        this.getSerializableLegacy(key)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun <T : Serializable> Bundle.getSerializableT(key: String, clazz: Class<T>): T? {
    return this.getSerializable(
        key, clazz
    )
}

@Suppress("UNCHECKED_CAST", "DEPRECATION")
private fun <T : Serializable> Bundle.getSerializableLegacy(key: String): T? {
    return this.getSerializable(
        key
    ) as? T
}