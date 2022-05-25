package it.cammino.gestionecomunita

import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import it.cammino.gestionecomunita.databinding.ActivityMainBinding
import it.cammino.gestionecomunita.ui.ThemeableActivity
import it.cammino.gestionecomunita.ui.comunita.CommunityDetailHostActivity

class MainActivity : ThemeableActivity() {

//    private val mViewModel: MainActivityViewModel by viewModels()

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
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        binding.extendedFab.setOnClickListener {
//            val fragment = CommunityDetailFragment()
//            val args = Bundle()
//            args.putBoolean(CommunityDetailFragment.EDIT_MODE, true)
//            fragment.arguments = args
//            supportFragmentManager.commit {
//                replace(
//                    R.id.nav_host_fragment_community_detail,
//                    fragment,
//                    R.id.navigation_home.toString()
//                )
//            }
            binding.extendedFab.transitionName = "shared_element_comunita"
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                binding.extendedFab,
                "shared_element_comunita" // The transition name to be matched in Activity B.
            )
            val intent = Intent(this, CommunityDetailHostActivity::class.java)
            startActivity(intent, options.toBundle())
        }
    }

//    fun getFab(): ExtendedFloatingActionButton {
//        return binding.extendedFab
//    }

    fun setTabVisible(visible: Boolean) {
        binding.materialTabs.isVisible = visible
    }

//    fun expandToolbar() {
//        binding.appBarLayout.setExpanded(true, true)
//    }

    fun getMaterialTabs(): TabLayout {
        return binding.materialTabs
    }
}