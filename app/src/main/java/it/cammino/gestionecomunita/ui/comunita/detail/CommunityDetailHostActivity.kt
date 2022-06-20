package it.cammino.gestionecomunita.ui.comunita.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.ActivityCommunityDetailBinding
import it.cammino.gestionecomunita.ui.ThemeableActivity

class CommunityDetailHostActivity : ThemeableActivity() {

    private lateinit var binding: ActivityCommunityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set the transition name, which matches Activity A’s start view transition name, on
        // the root view.
        findViewById<View>(android.R.id.content).transitionName = "shared_element_comunita"

        // Attach a callback used to receive the shared elements from Activity A to be
        // used by the container transform transition.
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        // Set this Activity’s enter and return transition to a MaterialContainerTransform
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 700L
        }

        // Keep system bars (status bar, navigation bar) persistent throughout the transition.
        window.sharedElementsUseOverlay = false
        super.onCreate(savedInstanceState)

        binding = ActivityCommunityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val fragment = CommunityDetailFragment()
            val args = Bundle()
            args.putBoolean(
                CommunityDetailFragment.EDIT_MODE,
                this.intent.extras?.getBoolean(CommunityDetailFragment.EDIT_MODE) ?: true
            )
            args.putBoolean(
                CommunityDetailFragment.CREATE_MODE,
                this.intent.extras?.getBoolean(CommunityDetailFragment.CREATE_MODE) ?: true
            )
            args.putLong(
                CommunityDetailFragment.ARG_ITEM_ID,
                this.intent.extras?.getLong(CommunityDetailFragment.ARG_ITEM_ID) ?: -1
            )
            fragment.arguments = args
            supportFragmentManager.commit {
                replace(
                    R.id.detail_fragment,
                    fragment,
                    R.id.community_detail_fragment.toString()
                )
            }
        }

    }

}