package it.cammino.gestionecomunita.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import it.cammino.gestionecomunita.util.createTaskDescription
import it.cammino.gestionecomunita.util.isDarkMode
import it.cammino.gestionecomunita.util.setLigthStatusBar
import it.cammino.gestionecomunita.util.setupNavBarColor

abstract class ThemeableActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {

        setupNavBarColor()
        updateStatusBarLightMode(true)

        setTaskDescription(this.createTaskDescription(TAG ?: "TAG"))

        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        updateStatusBarLightMode(true)
    }

    fun updateStatusBarLightMode(auto: Boolean) {
        setLigthStatusBar(if (auto) !isDarkMode else false)
    }

    companion object {
        internal val TAG = ThemeableActivity::class.java.canonicalName
    }

}
