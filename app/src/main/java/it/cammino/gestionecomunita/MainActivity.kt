package it.cammino.gestionecomunita

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import it.cammino.gestionecomunita.databinding.ActivityMainBinding
import it.cammino.gestionecomunita.dialog.AddNotificationDialogFragment
import it.cammino.gestionecomunita.dialog.EditMeetingDialogFragment
import it.cammino.gestionecomunita.dialog.large.LargeAddNotificationDialogFragment
import it.cammino.gestionecomunita.dialog.large.LargeEditMeetingDialogFragment
import it.cammino.gestionecomunita.dialog.small.SmallAddNotificationDialogFragment
import it.cammino.gestionecomunita.dialog.small.SmallEditMeetingDialogFragment
import it.cammino.gestionecomunita.ui.ThemeableActivity
import it.cammino.gestionecomunita.ui.comunita.detail.CommunityDetailFragment
import it.cammino.gestionecomunita.ui.comunita.detail.CommunityDetailHostActivity
import it.cammino.gestionecomunita.ui.incontri.IncontriFragment
import it.cammino.gestionecomunita.ui.vocazione.detail.VocazioneDetailHostActivity
import java.sql.Date


class MainActivity : ThemeableActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)

        val navView: BottomNavigationView = binding.navView

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_incontri,
                R.id.navigation_notifications,
                R.id.navigation_dashboard
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.getOrCreateBadge(R.id.navigation_notifications)
        navView.getOrCreateBadge(R.id.navigation_incontri)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d(TAG, "destination.id ${destination.id}")
            when (destination.id) {
                R.id.navigation_home -> {
                    binding.extendedFab?.isVisible = true
                    binding.extendedFabPromemoria?.isVisible = false
                    binding.extendedFabIncontro?.isVisible = false
                    binding.extendedFabVocazione?.isVisible = false
                }
                R.id.navigation_incontri -> {
                    binding.extendedFab?.isVisible = false
                    binding.extendedFabPromemoria?.isVisible = false
                    binding.extendedFabIncontro?.isVisible = true
                    binding.extendedFabVocazione?.isVisible = false
                }
                R.id.navigation_notifications -> {
                    binding.extendedFab?.isVisible = false
                    binding.extendedFabPromemoria?.isVisible = true
                    binding.extendedFabIncontro?.isVisible = false
                    binding.extendedFabVocazione?.isVisible = false
                }
                R.id.navigation_dashboard -> {
                    binding.extendedFab?.isVisible = false
                    binding.extendedFabPromemoria?.isVisible = false
                    binding.extendedFabIncontro?.isVisible = false
                    binding.extendedFabVocazione?.isVisible = true
                }
                else -> {}
            }
        }

        binding.extendedFab?.let { fab ->
            fab.setOnClickListener {
                it.transitionName = "shared_element_comunita"
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    it,
                    "shared_element_comunita" // The transition name to be matched in Activity B.
                )
                val intent = Intent(this, CommunityDetailHostActivity::class.java)
                startActivity(intent, options.toBundle())
            }
        }

        binding.extendedFabPromemoria?.let { fab ->
            fab.setOnClickListener {
                val builder = AddNotificationDialogFragment.Builder(
                    this, CommunityDetailFragment.ADD_NOTIFICATION
                )
                    .setFreeMode(true)
                if (resources.getBoolean(R.bool.large_layout)) {
                    builder.positiveButton(R.string.save)
                        .negativeButton(android.R.string.cancel)
                    LargeAddNotificationDialogFragment.show(
                        builder,
                        supportFragmentManager
                    )
                } else {
                    SmallAddNotificationDialogFragment.show(
                        builder,
                        supportFragmentManager
                    )
                }
            }
        }

        binding.extendedFabIncontro?.let { fab ->
            fab.setOnClickListener {
                val builder = EditMeetingDialogFragment.Builder(
                    this, IncontriFragment.ADD_INCONTRO
                )
                if (resources.getBoolean(R.bool.large_layout)) {
                    builder.positiveButton(R.string.save)
                        .negativeButton(android.R.string.cancel)
                    LargeEditMeetingDialogFragment.show(
                        builder,
                        supportFragmentManager
                    )
                } else {
                    SmallEditMeetingDialogFragment.show(
                        builder,
                        supportFragmentManager
                    )
                }
            }
        }

        binding.extendedFabVocazione?.let { fab ->
            fab.setOnClickListener {
                it.transitionName = "shared_element_vocazione"
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    it,
                    "shared_element_vocazione" // The transition name to be matched in Activity B.
                )
                val intent = Intent(this, VocazioneDetailHostActivity::class.java)
                startActivity(intent, options.toBundle())
            }
        }

        subscribeUiChanges()

    }

    private fun subscribeUiChanges() {
        viewModel.livePromemoria?.observe(this) { promemoria ->
            val ora = Date(System.currentTimeMillis())
            val countScadute = promemoria.count { it.data != null && ora >= it.data }
            val badgeDrawable = binding.navView.getBadge(R.id.navigation_notifications)
            if (countScadute > 0) {
                badgeDrawable?.let {
                    it.isVisible = true
                    it.number = countScadute
                }
            } else {
                badgeDrawable?.let {
                    it.isVisible = false
                    it.clearNumber()
                }
            }
        }
        viewModel.liveIncontri?.observe(this) { promemoria ->
            val ora = Date(System.currentTimeMillis())
            val countScadute = promemoria.count { (it.data != null && ora >= it.data) && !it.done }
            val badgeDrawable = binding.navView.getBadge(R.id.navigation_incontri)
            if (countScadute > 0) {
                badgeDrawable?.let {
                    it.isVisible = true
                    it.number = countScadute
                }
            } else {
                badgeDrawable?.let {
                    it.isVisible = false
                    it.clearNumber()
                }
            }
        }
    }

}