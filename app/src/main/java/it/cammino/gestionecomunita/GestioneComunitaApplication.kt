package it.cammino.gestionecomunita

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import it.cammino.gestionecomunita.util.OSUtils


class GestioneComunitaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(if (OSUtils.hasP()) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        DynamicColors.applyToActivitiesIfAvailable(this)

    }

}
