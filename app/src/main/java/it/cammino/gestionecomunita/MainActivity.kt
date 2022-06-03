package it.cammino.gestionecomunita

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import it.cammino.gestionecomunita.databinding.ActivityMainBinding
import it.cammino.gestionecomunita.dialog.AddNotificationDialogFragment
import it.cammino.gestionecomunita.dialog.large.LargeAddNotificationDialogFragment
import it.cammino.gestionecomunita.dialog.small.SmallAddNotificationDialogFragment
import it.cammino.gestionecomunita.ui.ThemeableActivity
import it.cammino.gestionecomunita.ui.comunita.detail.CommunityDetailFragment
import it.cammino.gestionecomunita.ui.comunita.detail.CommunityDetailHostActivity
import java.sql.Date


class MainActivity : ThemeableActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)

        val navView: BottomNavigationView = binding.navView

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.getOrCreateBadge(R.id.navigation_notifications)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> {
                    binding.extendedFab?.isVisible = true
                    binding.extendedFabPromemoria?.isVisible = false
                }
                R.id.navigation_dashboard -> {
                    binding.extendedFab?.isVisible = false
                    binding.extendedFabPromemoria?.isVisible = false
                }
                R.id.navigation_notifications -> {
                    binding.extendedFab?.isVisible = false
                    binding.extendedFabPromemoria?.isVisible = true
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

        subscribeUiChanges()

    }

    private fun subscribeUiChanges() {
        viewModel.itemsResult?.observe(this) { promemoria ->
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
    }

//    fun getFab(): ExtendedFloatingActionButton {
//        return binding.extendedFab
//    }

//    fun setTabVisible(visible: Boolean) {
//        binding.materialTabs.isVisible = visible
//    }

//    fun expandToolbar() {
//        binding.appBarLayout.setExpanded(true, true)
//    }

//    fun getMaterialTabs(): TabLayout {
//        return binding.materialTabs
//    }
}