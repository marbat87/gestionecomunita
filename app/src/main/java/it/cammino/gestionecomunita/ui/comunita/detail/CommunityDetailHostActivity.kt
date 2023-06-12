package it.cammino.gestionecomunita.ui.comunita.detail

import android.os.Bundle
import androidx.fragment.app.commit
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.ActivityCommunityDetailBinding
import it.cammino.gestionecomunita.ui.ThemeableActivity
import it.cammino.gestionecomunita.util.setEnterTransition

class CommunityDetailHostActivity : ThemeableActivity() {

    private lateinit var binding: ActivityCommunityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setEnterTransition()
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