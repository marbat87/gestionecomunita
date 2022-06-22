package it.cammino.gestionecomunita.ui.vocazione.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.ActivityVocazioneDetailBinding
import it.cammino.gestionecomunita.ui.ThemeableActivity
import it.cammino.gestionecomunita.util.OSUtils

class VocazioneDetailHostActivity : ThemeableActivity() {

    private lateinit var binding: ActivityVocazioneDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!OSUtils.isObySamsung()) {
            // Set the transition name, which matches Activity A’s start view transition name, on
            // the root view.
            findViewById<View>(android.R.id.content).transitionName = "shared_element_vocazione"

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
        }
        super.onCreate(savedInstanceState)

        binding = ActivityVocazioneDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val fragment = VocazioneDetailFragment()
            val args = Bundle()
            args.putBoolean(
                VocazioneDetailFragment.EDIT_MODE,
                this.intent.extras?.getBoolean(VocazioneDetailFragment.EDIT_MODE) ?: true
            )
            args.putBoolean(
                VocazioneDetailFragment.CREATE_MODE,
                this.intent.extras?.getBoolean(VocazioneDetailFragment.CREATE_MODE) ?: true
            )
            args.putLong(
                VocazioneDetailFragment.ARG_ITEM_ID,
                this.intent.extras?.getLong(VocazioneDetailFragment.ARG_ITEM_ID) ?: -1
            )
            fragment.arguments = args
            supportFragmentManager.commit {
                replace(
                    R.id.nav_host_fragment_vocazione_detail,
                    fragment,
                    R.id.vocazione_detail_fragment.toString()
                )
            }
        }

    }

}