package it.cammino.gestionecomunita.ui.vocazione.detail

import android.os.Bundle
import androidx.fragment.app.commit
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.ActivityVocazioneDetailBinding
import it.cammino.gestionecomunita.ui.ThemeableActivity
import it.cammino.gestionecomunita.util.setEnterTransition

class VocazioneDetailHostActivity : ThemeableActivity() {

    private lateinit var binding: ActivityVocazioneDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setEnterTransition()
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