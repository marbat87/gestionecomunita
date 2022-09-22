package it.cammino.gestionecomunita

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.*
import it.cammino.gestionecomunita.databinding.ActivityMainBinding
import it.cammino.gestionecomunita.dialog.*
import it.cammino.gestionecomunita.dialog.large.LargeAddNotificationDialogFragment
import it.cammino.gestionecomunita.dialog.large.LargeEditMeetingDialogFragment
import it.cammino.gestionecomunita.dialog.small.SmallAddNotificationDialogFragment
import it.cammino.gestionecomunita.dialog.small.SmallEditMeetingDialogFragment
import it.cammino.gestionecomunita.ui.ThemeableActivity
import it.cammino.gestionecomunita.ui.comunita.detail.CommunityDetailFragment
import it.cammino.gestionecomunita.ui.comunita.detail.CommunityDetailHostActivity
import it.cammino.gestionecomunita.ui.incontri.IncontriFragment
import it.cammino.gestionecomunita.ui.seminario.detail.SeminarioDetailHostActivity
import it.cammino.gestionecomunita.ui.vocazione.detail.VocazioneDetailHostActivity
import it.cammino.gestionecomunita.util.OSUtils
import it.cammino.gestionecomunita.util.StringUtils
import it.cammino.gestionecomunita.util.getTypedValueResId
import it.cammino.gestionecomunita.database.serializer.DateTimeDeserializer
import it.cammino.gestionecomunita.database.serializer.DateTimeSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.sql.Date
import java.util.concurrent.ExecutionException


class MainActivity : ThemeableActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels()
    private val profileDialogViewModel: ProfileDialogFragment.DialogViewModel by viewModels()
    private val inputDialogViewModel: InputTextDialogFragment.DialogViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private var acct: GoogleSignInAccount? = null
    private var mSignInClient: GoogleSignInClient? = null
    private lateinit var auth: FirebaseAuth
    private var profileItem: MenuItem? = null
    private var profilePhotoUrl: String = StringUtils.EMPTY_STRING
    private var profileName: String = StringUtils.EMPTY_STRING
    private var profileEmail: String = StringUtils.EMPTY_STRING

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        DynamicColors.applyToActivityIfAvailable(this)
        if (!OSUtils.isObySamsung()) {
            // Attach a callback used to capture the shared elements from this Activity to be used
            // by the container transform transition
            setExitSharedElementCallback(object :
                MaterialContainerTransformSharedElementCallback() {
                override fun onSharedElementEnd(
                    sharedElementNames: MutableList<String>,
                    sharedElements: MutableList<View>,
                    sharedElementSnapshots: MutableList<View>
                ) {
                    super.onSharedElementEnd(
                        sharedElementNames,
                        sharedElements,
                        sharedElementSnapshots
                    )
                    Log.d(TAG, "onTransitionEnd")
                    if (!resources.getBoolean(R.bool.tablet_layout)) updateStatusBarLightMode(true)
                }
            })

            // Keep system bars (status bar, navigation bar) persistent throughout the transition.
            window.sharedElementsUseOverlay = false
        }
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            // Your server's client ID, not your Android client ID.
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()

        // [END configure_signin]

        // [START build_client]
        mSignInClient = GoogleSignIn.getClient(this, gso)
        // [END build_client]

        // Initialize Firebase Auth
        auth = Firebase.auth

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
                R.id.navigation_dashboard,
                R.id.navigation_seminari
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
                    binding.extendedFabSeminari?.isVisible = false
                }
                R.id.navigation_incontri -> {
                    binding.extendedFab?.isVisible = false
                    binding.extendedFabPromemoria?.isVisible = false
                    binding.extendedFabIncontro?.isVisible = true
                    binding.extendedFabVocazione?.isVisible = false
                    binding.extendedFabSeminari?.isVisible = false
                }
                R.id.navigation_notifications -> {
                    binding.extendedFab?.isVisible = false
                    binding.extendedFabPromemoria?.isVisible = true
                    binding.extendedFabIncontro?.isVisible = false
                    binding.extendedFabVocazione?.isVisible = false
                    binding.extendedFabSeminari?.isVisible = false
                }
                R.id.navigation_dashboard -> {
                    binding.extendedFab?.isVisible = false
                    binding.extendedFabPromemoria?.isVisible = false
                    binding.extendedFabIncontro?.isVisible = false
                    binding.extendedFabVocazione?.isVisible = true
                    binding.extendedFabSeminari?.isVisible = false
                }
                R.id.navigation_seminari -> {
                    binding.extendedFab?.isVisible = false
                    binding.extendedFabPromemoria?.isVisible = false
                    binding.extendedFabIncontro?.isVisible = false
                    binding.extendedFabVocazione?.isVisible = false
                    binding.extendedFabSeminari?.isVisible = true
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
                if (OSUtils.isObySamsung()) {
                    startActivity(
                        Intent(
                            this,
                            VocazioneDetailHostActivity::class.java
                        )
                    )
                } else {
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
        }

        binding.extendedFabSeminari?.let { fab ->
            fab.setOnClickListener {
                if (OSUtils.isObySamsung()) {
                    startActivity(
                        Intent(
                            this,
                            SeminarioDetailHostActivity::class.java
                        )
                    )
                } else {
                    it.transitionName = "shared_element_seminario"
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        it,
                        "shared_element_seminario" // The transition name to be matched in Activity B.
                    )
                    val intent = Intent(this, SeminarioDetailHostActivity::class.java)
                    startActivity(intent, options.toBundle())
                }
            }
        }

        subscribeUiChanges()

    }

    override fun onStart() {
        super.onStart()
        val task = mSignInClient?.silentSignIn()
        task?.let {
            if (it.isSuccessful) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Got cached sign-in")
                handleSignInResult(task)
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                showProgressDialog()

                task.addOnCompleteListener { mTask: Task<GoogleSignInAccount> ->
                    Log.d(TAG, "Reconnected")
                    handleSignInResult(mTask)
                }
            }
        }
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

        simpleDialogViewModel.state.observe(this) {
            Log.d(TAG, "simpleDialogViewModel state $it")
            if (!simpleDialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (simpleDialogViewModel.mTag) {
                            BACKUP_NEW_CODE -> {
                                simpleDialogViewModel.handled = true
                                ProgressDialogFragment.show(
                                    ProgressDialogFragment.Builder(this, BACKUP_RUNNING)
                                        .title(R.string.backup_running_title)
                                        .icon(R.drawable.cloud_upload_24px)
                                        .content(R.string.backup_running_content),
                                    supportFragmentManager
                                )
                                lifecycleScope.launch { backupDbPrefs() }
                            }
                            BACKUP_OLD_CODE -> {
                                simpleDialogViewModel.handled = true
                                ProgressDialogFragment.show(
                                    ProgressDialogFragment.Builder(this, BACKUP_RUNNING)
                                        .title(R.string.backup_running_title)
                                        .icon(R.drawable.cloud_upload_24px)
                                        .content(R.string.backup_running_content),
                                    supportFragmentManager
                                )
                                lifecycleScope.launch { backupDbPrefs(false) }
                            }
                            RESTORE_OLD_CODE -> {
                                simpleDialogViewModel.handled = true
                                ProgressDialogFragment.show(
                                    ProgressDialogFragment.Builder(this, RESTORE_RUNNING)
                                        .title(R.string.restore_running_title)
                                        .icon(R.drawable.cloud_download_24px)
                                        .content(R.string.restore_running_content),
                                    supportFragmentManager
                                )
                                lifecycleScope.launch { restoreDbPrefs(false) }
                            }
                            SIGNOUT -> {
                                simpleDialogViewModel.handled = true
                                signOut()
                            }
                            REVOKE -> {
                                simpleDialogViewModel.handled = true
                                revokeAccess()
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        simpleDialogViewModel.handled = true
                    }
                }
            }
        }

        profileDialogViewModel.state.observe(this) {
            Log.d(TAG, "profileDialogViewModel state $it")
            if (!profileDialogViewModel.handled) {
                if (it is DialogState.Positive) {
                    when (profileDialogViewModel.menuItemId) {
                        R.id.backup_old_code -> {
                            showAccountRelatedDialog(BACKUP_OLD_CODE)
                        }
                        R.id.backup_new_code -> {
                            showAccountRelatedDialog(BACKUP_NEW_CODE)
                        }
                        R.id.restore_old_code -> {
                            showAccountRelatedDialog(RESTORE_OLD_CODE)
                        }
                        R.id.restore_new_code -> {
                            showAccountRelatedDialog(RESTORE_NEW_CODE)
                        }
                        R.id.gplus_signout -> {
                            showAccountRelatedDialog(SIGNOUT)
                        }
                        R.id.gplus_revoke -> {
                            showAccountRelatedDialog(REVOKE)
                        }
                    }
                    profileDialogViewModel.handled = true
                }
            }
        }

        inputDialogViewModel.state.observe(this) {
            Log.d(TAG, "inputDialogViewModel state $it")
            if (!inputDialogViewModel.handled) {
                if (it is DialogState.Positive) {
                    when (inputDialogViewModel.mTag) {
                        RESTORE_NEW_CODE -> {
                            simpleDialogViewModel.handled = true
                            ProgressDialogFragment.show(
                                ProgressDialogFragment.Builder(this, RESTORE_RUNNING)
                                    .title(R.string.restore_running_title)
                                    .icon(R.drawable.cloud_download_24px)
                                    .content(R.string.restore_running_content),
                                supportFragmentManager
                            )
                            viewModel.backupCode = inputDialogViewModel.outputText
                            lifecycleScope.launch { restoreDbPrefs() }
                        }
                    }
                    profileDialogViewModel.handled = true
                }
            }
        }

    }

    // [START signIn]
    private fun signIn() {
        val signInIntent = mSignInClient?.signInIntent
        startSignInForResult.launch(signInIntent)
    }

    private val startSignInForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(result.data))
        }

    // [START signOut]
    private fun signOut() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit { putBoolean(SIGN_IN_REQUESTED, false) }
        FirebaseAuth.getInstance().signOut()
        mSignInClient?.signOut()?.addOnCompleteListener {
            updateUI(false)
            Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        profileItem = menu.findItem(R.id.account_manager)
        return super.onCreateOptionsMenu(menu)
    }

    // [START revokeAccess]
    private fun revokeAccess() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit { putBoolean(SIGN_IN_REQUESTED, false) }
        FirebaseAuth.getInstance().signOut()
        mSignInClient?.revokeAccess()?.addOnCompleteListener {
            updateUI(false)
            Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT)
                .show()
        }
    }

    // [START handleSignInResult]
    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        //    Log.d(getClass().getName(), "handleSignInResult:" + result.isSuccess());
        Log.d(TAG, "handleSignInResult:" + task.isSuccessful)
        if (task.isSuccessful) {
            // Signed in successfully, show authenticated UI.
            acct = GoogleSignIn.getLastSignedInAccount(this)
            firebaseAuthWithGoogle()
        } else {
            // Sign in failed, handle failure and update UI
            Log.w(TAG, "handleSignInResult:failure", task.exception)
            if (PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean(SIGN_IN_REQUESTED, false)
            )
                Toast.makeText(
                    this, getString(
                        R.string.login_failed,
                        task.exception?.message
                    ), Toast.LENGTH_SHORT
                )
                    .show()
            acct = null
            updateUI(false)
        }
    }

    private fun firebaseAuthWithGoogle() {
        Log.d(TAG, "firebaseAuthWithGoogle: ${acct?.idToken}")

        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "firebaseAuthWithGoogle:success")
                    if (viewModel.showSnackbar) {
                        Toast.makeText(
                            this,
                            getString(R.string.connected_as, acct?.displayName),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        viewModel.showSnackbar = false
                    }
                    updateUI(true)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (PreferenceManager.getDefaultSharedPreferences(this)
                            .getBoolean(SIGN_IN_REQUESTED, false)
                    )
                        Toast.makeText(
                            this, getString(
                                R.string.login_failed,
                                task.exception?.message
                            ), Toast.LENGTH_SHORT
                        )
                            .show()
                }
            }
    }

    private fun dismissProgressDialog(tag: String) {
        val sFragment = ProgressDialogFragment.findVisible(this, tag)
        sFragment?.dismiss()
    }

    private fun updateUI(signedIn: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit { putBoolean(SIGNED_IN, signedIn) }
        if (signedIn)
            PreferenceManager.getDefaultSharedPreferences(this)
                .edit { putBoolean(SIGN_IN_REQUESTED, true) }
        if (signedIn) {
            profileName = acct?.displayName.orEmpty()
            profileEmail = acct?.email.orEmpty()
            val profilePhoto = acct?.photoUrl
            if (profilePhoto != null) {
                var personPhotoUrl = profilePhoto.toString()
                Log.d(TAG, "personPhotoUrl BEFORE $personPhotoUrl")
                personPhotoUrl = personPhotoUrl.replace(OLD_PHOTO_RES, NEW_PHOTO_RES)
                Log.d(TAG, "personPhotoUrl AFTER $personPhotoUrl")
                profilePhotoUrl = personPhotoUrl
            } else {
                profilePhotoUrl = StringUtils.EMPTY_STRING
            }
        } else {
            profileName = StringUtils.EMPTY_STRING
            profileEmail = StringUtils.EMPTY_STRING
            profilePhotoUrl = StringUtils.EMPTY_STRING
        }
        updateProfileImage()
        hideProgressDialog()
    }

    fun updateProfileImage() {
        val loggedListener = View.OnClickListener {
            ProfileDialogFragment.show(
                ProfileDialogFragment.Builder(
                    this,
                    PROFILE_DIALOG
                )
                    .profileName(profileName)
                    .profileEmail(profileEmail)
                    .profileImageSrc(profilePhotoUrl),
                supportFragmentManager
            )
        }

        val notLoggedListener = View.OnClickListener {
            PreferenceManager.getDefaultSharedPreferences(this)
                .edit { putBoolean(SIGN_IN_REQUESTED, true) }
            viewModel.showSnackbar = true
            signIn()
        }

        if (profilePhotoUrl.isEmpty()) {
            profileItem?.actionView?.findViewById<ShapeableImageView>(R.id.profile_icon)
                ?.setImageResource(R.drawable.account_circle_56px)
            profileItem?.actionView?.findViewById<ShapeableImageView>(R.id.profile_icon)?.background =
                null
        } else {
            AppCompatResources.getDrawable(this, R.drawable.account_circle_56px)?.let {
                Picasso.get().load(profilePhotoUrl)
                    .placeholder(it)
                    .into(profileItem?.actionView?.findViewById<ShapeableImageView>(R.id.profile_icon))
            }
            AppCompatResources.getDrawable(
                this,
                getTypedValueResId(android.R.attr.selectableItemBackgroundBorderless)
            )?.let {
                profileItem?.actionView?.findViewById<ShapeableImageView>(R.id.profile_icon)?.background =
                    it
            }
        }

        profileItem?.actionView?.findViewById<ShapeableImageView>(R.id.profile_icon)
            ?.setOnClickListener(
                if (PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(SIGNED_IN, false)
                ) loggedListener else notLoggedListener
            )
    }

    private fun showProgressDialog() {
        binding.loadingBar.isVisible = true
    }

    private fun hideProgressDialog() {
        binding.loadingBar.isVisible = false
    }

    private suspend fun backupDbPrefs(generateNewCode: Boolean = true) {
        try {

            if (generateNewCode)
                viewModel.backupCode = StringUtils.generateRandomCode()
            else
                viewModel.backupCode = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(StringUtils.PREFERENCE_BACKUP_CODE, StringUtils.EMPTY_STRING)
                    .orEmpty()

            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                backupDatabase(viewModel.backupCode)
            }

            PreferenceManager.getDefaultSharedPreferences(this)
                .edit { putString(StringUtils.PREFERENCE_BACKUP_CODE, viewModel.backupCode) }

            dismissProgressDialog(BACKUP_RUNNING)
            BackupCodeDialogFragment.show(
                BackupCodeDialogFragment.Builder(this, BACKUP_OK).apply {
                    mTitle = R.string.backup_ok_title
                    positiveButton(R.string.ok)
                    mBackupCode = viewModel.backupCode
                },
                supportFragmentManager
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.localizedMessage, e)
            Snackbar.make(
                findViewById(android.R.id.content),
                "error: " + e.localizedMessage,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private suspend fun restoreDbPrefs(useNewCode: Boolean = true) {
        try {
            if (!useNewCode)
                viewModel.backupCode = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(StringUtils.PREFERENCE_BACKUP_CODE, StringUtils.EMPTY_STRING)
                    .orEmpty()

            var codeOk: Boolean

            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                val storageRef = Firebase.storage.reference
                codeOk = checkControlFile(storageRef, viewModel.backupCode)
            }

            if (!codeOk) {
                dismissProgressDialog(RESTORE_RUNNING)
                SimpleDialogFragment.show(
                    SimpleDialogFragment.Builder(this, RESTORE_KO)
                        .title(R.string.error_dialog_title)
                        .content(R.string.codice_errato)
                        .positiveButton(R.string.ok),
                    supportFragmentManager
                )
                return
            }

            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                restoreDatabase(viewModel.backupCode)
            }

            PreferenceManager.getDefaultSharedPreferences(this)
                .edit { putString(StringUtils.PREFERENCE_BACKUP_CODE, viewModel.backupCode) }

            dismissProgressDialog(RESTORE_RUNNING)
            SimpleDialogFragment.show(
                SimpleDialogFragment.Builder(this, RESTORE_OK)
                    .title(R.string.restore_ok_title)
                    .content(getString(R.string.restore_ok_code, viewModel.backupCode))
                    .positiveButton(R.string.ok),
                supportFragmentManager
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.localizedMessage, e)
            Snackbar.make(
                findViewById(android.R.id.content),
                "error: " + e.localizedMessage,
                Snackbar.LENGTH_LONG
            )
                .show()
        }
    }

    private fun backupDatabase(backupCode: String?) {
        Log.d(TAG, "backupDatabase $backupCode")

        if (backupCode == null)
            throw NoIdException()

        val storageRef = Firebase.storage.reference

        val backupRef = deleteControlFile(storageRef, backupCode)
        putControlFileToFirebase(backupRef)

        val comunitaDb = ComunitaDatabase.getInstance(this)

        //BACKUP COMUNITA LINK
        val comunitaRef = deleteExistingFile(storageRef, COMUNITA_FILE_NAME, backupCode)
        val comunitaList = comunitaDb.comunitaDao().allByName
        Log.d(TAG, "comunitaList size ${comunitaList.size}")
        putFileToFirebase(comunitaRef, comunitaList, COMUNITA_FILE_NAME)

        //BACKUP PASSAGGIO LINK
        val passaggioRef = deleteExistingFile(storageRef, PASSAGGIO_FILE_NAME, backupCode)
        val passaggioList = comunitaDb.passaggioDao().all
        Log.d(TAG, "localLink size ${passaggioList.size}")
        putFileToFirebase(passaggioRef, passaggioList, PASSAGGIO_FILE_NAME)

        //BACKUP PROMEMORIA LINK
        val promemoriaRef = deleteExistingFile(storageRef, PROMEMORIA_FILE_NAME, backupCode)
        val promemoriaList = comunitaDb.promemoriaDao().all
        Log.d(TAG, "promemoriaList size ${promemoriaList.size}")
        putFileToFirebase(promemoriaRef, promemoriaList, PROMEMORIA_FILE_NAME)

        //BACKUP FRATELLO LINK
        val fratelloRef = deleteExistingFile(storageRef, FRATELLO_FILE_NAME, backupCode)
        val fratelloList = comunitaDb.fratelloDao().all
        Log.d(TAG, "fratelloList size ${fratelloList.size}")
        putFileToFirebase(fratelloRef, fratelloList, FRATELLO_FILE_NAME)

        //BACKUP INCONTRO LINK
        val incontroRef = deleteExistingFile(storageRef, INCONTRO_FILE_NAME, backupCode)
        val incontroList = comunitaDb.incontroDao().all
        Log.d(TAG, "incontroList size ${incontroList.size}")
        putFileToFirebase(incontroRef, incontroList, INCONTRO_FILE_NAME)

        //BACKUP VOCAZIONE LINK
        val vocazioneRef = deleteExistingFile(storageRef, VOCAZIONE_FILE_NAME, backupCode)
        val vocazioneList = comunitaDb.vocazioneDao().all
        Log.d(TAG, "vocazioneList size ${vocazioneList.size}")
        putFileToFirebase(vocazioneRef, vocazioneList, VOCAZIONE_FILE_NAME)

        //BACKUP SEMINARIO LINK
        val seminarioRef = deleteExistingFile(storageRef, SEMINARIO_FILE_NAME, backupCode)
        val seminarioList = comunitaDb.seminarioDao().allByName
        Log.d(TAG, "seminarioList size ${seminarioList.size}")
        putFileToFirebase(seminarioRef, seminarioList, SEMINARIO_FILE_NAME)

        //BACKUP VISITASEMINARIO SEMINARIO LINK
        val visitaSeminarioRef =
            deleteExistingFile(storageRef, VISITASEMINARIO_FILE_NAME, backupCode)
        val visitaSeminarioList = comunitaDb.visitaSeminarioDao().all
        Log.d(
            TAG,
            "visitaSeminarioList size ${visitaSeminarioList.size}"
        )
        putFileToFirebase(
            visitaSeminarioRef,
            visitaSeminarioList,
            VISITASEMINARIO_FILE_NAME
        )

        //BACKUP RESPONSABILE SEMINARIO LINK
        val responsabileSeminarioRef =
            deleteExistingFile(storageRef, RESPONSABILESEMINARIO_FILE_NAME, backupCode)
        val seminarioresponsabileSeminarioList = comunitaDb.responsabileSeminarioDao().all
        Log.d(
            TAG,
            "seminarioresponsabileSeminarioList size ${seminarioresponsabileSeminarioList.size}"
        )
        putFileToFirebase(
            responsabileSeminarioRef,
            seminarioresponsabileSeminarioList,
            RESPONSABILESEMINARIO_FILE_NAME
        )

        //BACKUP SEMINARISTA LINK
        val seminaristaRef = deleteExistingFile(storageRef, SEMINARISTA_FILE_NAME, backupCode)
        val seminaristaList = comunitaDb.seminaristaDao().all
        Log.d(TAG, "seminaristaList size ${seminaristaList.size}")
        putFileToFirebase(seminaristaRef, seminaristaList, SEMINARISTA_FILE_NAME)

        //BACKUP COMUNITA SEMINARISTA LINK
        val comunitaSeminaristaRef =
            deleteExistingFile(storageRef, COMUNITASEMINARISTA_FILE_NAME, backupCode)
        val comunitaSeminaristaList = comunitaDb.comunitaSeminaristaDao().all
        Log.d(TAG, "comunitaSeminaristaList size ${comunitaSeminaristaList.size}")
        putFileToFirebase(
            comunitaSeminaristaRef,
            comunitaSeminaristaList,
            COMUNITASEMINARISTA_FILE_NAME
        )

        Log.d(TAG, "BACKUP DB COMPLETATO")
    }

    private fun deleteExistingFile(
        storageRef: StorageReference,
        fileName: String,
        backupCode: String
    ): StorageReference {
        val fileRef = storageRef.child("camminodatabase_$backupCode/$fileName.json")

        try {
            Tasks.await(fileRef.delete())
            Log.d(TAG, "Backup esistente cancellato!")
        } catch (e: ExecutionException) {
            if (e.cause is StorageException && (e.cause as? StorageException)?.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND)
                Log.d(TAG, "Backup non trovato!")
            else
                throw e
        }
        return fileRef
    }

    private fun deleteControlFile(
        storageRef: StorageReference,
        backupCode: String
    ): StorageReference {
        val fileRef = storageRef.child("camminodatabase_$backupCode/lock")

        try {
            Tasks.await(fileRef.delete())
            Log.d(TAG, "Backup esistente cancellato!")
        } catch (e: ExecutionException) {
            if (e.cause is StorageException && (e.cause as? StorageException)?.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND)
                Log.d(TAG, "Backup non trovato!")
            else
                throw e
        }
        return fileRef
    }

    private fun checkControlFile(
        storageRef: StorageReference,
        backupCode: String
    ): Boolean {
        return try {

            val backupRef = storageRef.child("camminodatabase_$backupCode/lock")
            Tasks.await(backupRef.stream)
            true

        } catch (e: ExecutionException) {
            Log.e(TAG, e.localizedMessage, e)
            if (e.cause is StorageException && (e.cause as? StorageException)?.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND)
                false
            else
                throw e
        }
    }

    private fun putControlFileToFirebase(fileRef: StorageReference) {
        val exportFile = File("${cacheDir.absolutePath}/lock")
        Log.d(TAG, "putControlFileToFirebase: exportFile = " + exportFile.absolutePath)
        val output = BufferedWriter(FileWriter(exportFile))
        output.close()

        val saveFile = Tasks.await(fileRef.putFile(exportFile.toUri()))
        Log.d(TAG, "DocumentSnapshot added with path: ${saveFile.metadata?.path}")
    }

    private fun putFileToFirebase(fileRef: StorageReference, jsonObject: Any, fileName: String) {
        val gson =
            GsonBuilder().registerTypeAdapter(Date::class.java, DateTimeSerializer()).create()
        Log.d(TAG, "=== List to JSON ===")
        val jsonList: String = gson.toJson(jsonObject)
        Log.d(TAG, jsonList)

        val exportFile = File("${cacheDir.absolutePath}/$fileName.json")
        Log.d(TAG, "listToXML: exportFile = " + exportFile.absolutePath)
        val output = BufferedWriter(FileWriter(exportFile))
        output.write(jsonList)
        output.close()

        val saveFile = Tasks.await(fileRef.putFile(exportFile.toUri()))
        Log.d(TAG, "DocumentSnapshot added with path: ${saveFile.metadata?.path}")
    }

    private fun restoreDatabase(backupCode: String?) {
        Log.d(TAG, "backupDatabase $backupCode")

        if (backupCode == null)
            throw NoIdException()

        val storageRef = FirebaseStorage.getInstance().reference
        val gson =
            GsonBuilder().registerTypeAdapter(Date::class.java, DateTimeDeserializer()).create()
        val comunitaDb = ComunitaDatabase.getInstance(this)

        //RESTORE COMUNITA
        val backupComunita: List<Comunita> = gson.fromJson(
            InputStreamReader(
                getFileFromFirebase(
                    storageRef,
                    COMUNITA_FILE_NAME,
                    backupCode
                )
            ), object : TypeToken<List<Comunita>>() {}.type
        )
        comunitaDb.comunitaDao().truncateTable()
        Log.d(TAG, "Comunita truncated!")
        comunitaDb.comunitaDao().insertComunita(backupComunita)


        //RESTORE PASSAGGIO LIST
        val backupPassaggioList: List<Passaggio> = gson.fromJson(
            InputStreamReader(
                getFileFromFirebase(
                    storageRef,
                    PASSAGGIO_FILE_NAME,
                    backupCode
                )
            ), object : TypeToken<List<Passaggio>>() {}.type
        )
        comunitaDb.passaggioDao().truncateTable()
        Log.d(TAG, "Passaggio truncated!")
        comunitaDb.passaggioDao().insertPassaggi(backupPassaggioList)


        //RESTORE PROMEMORIA LIST
        val backupPromemoriaList: List<Promemoria> = gson.fromJson(
            InputStreamReader(
                getFileFromFirebase(
                    storageRef,
                    PROMEMORIA_FILE_NAME,
                    backupCode
                )
            ), object : TypeToken<List<Promemoria>>() {}.type
        )
        comunitaDb.promemoriaDao().truncateTable()
        Log.d(TAG, "Promemoria truncated!")
        comunitaDb.promemoriaDao().insertPromemoria(backupPromemoriaList)


        //RESTORE FRATELLO LIST
        val backupFratelloList: List<Fratello> = gson.fromJson(
            InputStreamReader(
                getFileFromFirebase(
                    storageRef,
                    FRATELLO_FILE_NAME,
                    backupCode
                )
            ), object : TypeToken<List<Fratello>>() {}.type
        )
        comunitaDb.fratelloDao().truncateTable()
        Log.d(TAG, "Fratello truncated!")
        comunitaDb.fratelloDao().insertFratelli(backupFratelloList)


        //RESTORE INCONTRO LIST
        val backupIncontroList: List<Incontro> = gson.fromJson(
            InputStreamReader(
                getFileFromFirebase(
                    storageRef,
                    INCONTRO_FILE_NAME,
                    backupCode
                )
            ), object : TypeToken<List<Incontro>>() {}.type
        )
        comunitaDb.incontroDao().truncateTable()
        Log.d(TAG, "Incontro truncated!")
        comunitaDb.incontroDao().insertIncontri(backupIncontroList)


        //RESTORE VOCAZIONE PERS
        val backupVocazioni: List<Vocazione> = gson.fromJson(
            InputStreamReader(
                getFileFromFirebase(
                    storageRef,
                    VOCAZIONE_FILE_NAME,
                    backupCode
                )
            ), object : TypeToken<List<Vocazione>>() {}.type
        )
        comunitaDb.vocazioneDao().truncateTable()
        Log.d(TAG, "Vocazione truncated!")
        comunitaDb.vocazioneDao().insertVocazioni(backupVocazioni)


        //RESTORE SEMINARIO PERS
        val backupSeminari: List<Seminario> = gson.fromJson(
            InputStreamReader(
                getFileFromFirebase(
                    storageRef,
                    SEMINARIO_FILE_NAME,
                    backupCode
                )
            ), object : TypeToken<List<Seminario>>() {}.type
        )
        comunitaDb.seminarioDao().truncateTable()
        Log.d(TAG, "Seminario truncated!")
        comunitaDb.seminarioDao().insertSeminari(backupSeminari)


        //RESTORE VISITA SEMINARIO PERS
        val backupVisitaSeminaro: List<VisitaSeminario> = gson.fromJson(
            InputStreamReader(
                getFileFromFirebase(
                    storageRef,
                    VISITASEMINARIO_FILE_NAME,
                    backupCode
                )
            ), object : TypeToken<List<VisitaSeminario>>() {}.type
        )
        comunitaDb.visitaSeminarioDao().truncateTable()
        Log.d(TAG, "VisitaSeminario truncated!")
        comunitaDb.visitaSeminarioDao().insertVisite(backupVisitaSeminaro)


        //RESTORE RESPONSABILE SEMINARIO PERS
        val backupResponsabileSeminario: List<ResponsabileSeminario> = gson.fromJson(
            InputStreamReader(
                getFileFromFirebase(
                    storageRef,
                    RESPONSABILESEMINARIO_FILE_NAME,
                    backupCode
                )
            ), object : TypeToken<List<ResponsabileSeminario>>() {}.type
        )
        comunitaDb.responsabileSeminarioDao().truncateTable()
        Log.d(TAG, "ResponsabileSeminario truncated!")
        comunitaDb.responsabileSeminarioDao().insertResponsabili(backupResponsabileSeminario)


        //RESTORE SEMINARISTA PERS
        val backupSeminarista: List<Seminarista> = gson.fromJson(
            InputStreamReader(
                getFileFromFirebase(
                    storageRef,
                    SEMINARISTA_FILE_NAME,
                    backupCode
                )
            ), object : TypeToken<List<Seminarista>>() {}.type
        )
        comunitaDb.seminaristaDao().truncateTable()
        Log.d(TAG, "Seminarista truncated!")
        comunitaDb.seminaristaDao().insertSeminaristi(backupSeminarista)


        //RESTORE COMUNITA SEMINARISTA PERS
        val backupComunitaSeminarista: List<ComunitaSeminarista> = gson.fromJson(
            InputStreamReader(
                getFileFromFirebase(
                    storageRef,
                    COMUNITASEMINARISTA_FILE_NAME,
                    backupCode
                )
            ), object : TypeToken<List<ComunitaSeminarista>>() {}.type
        )
        comunitaDb.comunitaSeminaristaDao().truncateTable()
        Log.d(TAG, "ComunitaSeminarista truncated!")
        comunitaDb.comunitaSeminaristaDao().insertComunita(backupComunitaSeminarista)


        Log.d(TAG, "RESTORE DB COMPLETATO")
    }

    private fun getFileFromFirebase(
        storageRef: StorageReference,
        fileName: String,
        backupCode: String
    ): InputStream {
        try {

            val cantoRef = storageRef.child("camminodatabase_$backupCode/$fileName.json")
            val fileStream = Tasks.await(cantoRef.stream)
            return fileStream.stream

        } catch (e: ExecutionException) {
            Log.e(TAG, e.localizedMessage, e)
            if (e.cause is StorageException && (e.cause as? StorageException)?.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND)
                throw NoBackupException(resources)
            else
                throw e
        }
    }

    private fun showAccountRelatedDialog(tag: String) {
        if (tag == RESTORE_NEW_CODE) {
            InputTextDialogFragment.show(
                InputTextDialogFragment.Builder(
                    this, RESTORE_NEW_CODE
                )
                    .title(R.string.restore_code_confirm)
                    .positiveButton(R.string.restore_code_confirm)
                    .negativeButton(android.R.string.cancel), supportFragmentManager
            )
            return
        }
        SimpleDialogFragment.show(
            SimpleDialogFragment.Builder(this, tag).apply {
                when (tag) {
                    BACKUP_OLD_CODE -> {
                        title(R.string.upload_old_code_confirm)
                        content(
                            getString(
                                R.string.upload_old_code_content,
                                PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                                    .getString(
                                        StringUtils.PREFERENCE_BACKUP_CODE,
                                        StringUtils.EMPTY_STRING
                                    ).orEmpty()
                            )
                        )
                        positiveButton(R.string.upload_old_code_confirm)
                    }
                    BACKUP_NEW_CODE -> {
                        title(R.string.upload_new_code_confirm)
                        content(R.string.upload_new_code_content)
                        positiveButton(R.string.upload_new_code_confirm)
                    }
                    RESTORE_OLD_CODE -> {
                        title(R.string.restore_code_confirm)
                        content(
                            getString(
                                R.string.restore_old_code_content,
                                PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                                    .getString(
                                        StringUtils.PREFERENCE_BACKUP_CODE,
                                        StringUtils.EMPTY_STRING
                                    ).orEmpty()
                            )
                        )
                        positiveButton(R.string.restore_code_confirm)
                    }
                    SIGNOUT -> {
                        title(R.string.gplus_signout)
                        content(R.string.dialog_acc_disconn_text)
                        positiveButton(R.string.disconnect_confirm)
                    }
                    REVOKE -> {
                        title(R.string.gplus_revoke)
                        content(R.string.dialog_acc_revoke_text)
                        positiveButton(R.string.disconnect_confirm)
                    }
                }
                negativeButton(android.R.string.cancel)
            },
            supportFragmentManager
        )
    }

    class NoBackupException internal constructor(val resources: Resources) :
        Exception(resources.getString(R.string.no_restore_found))

    class NoIdException internal constructor() : Exception("no ID linked to this Account")

    companion object {
        const val SIGN_IN_REQUESTED = "sign_id_requested"
        const val SIGNED_IN = "signed_id"
        const val PROFILE_DIALOG = "PROFILE_DIALOG"
        private const val OLD_PHOTO_RES = "s96-c"
        private const val NEW_PHOTO_RES = "s400-c"
        private const val RESTORE_RUNNING = "RESTORE_RUNNING"
        private const val BACKUP_RUNNING = "BACKUP_RUNNING"
        private const val BACKUP_NEW_CODE = "backup_new_code"
        private const val BACKUP_OLD_CODE = "backup_OLD_code"
        private const val RESTORE_NEW_CODE = "restore_new_code"
        private const val RESTORE_OLD_CODE = "restore_old_code"
        private const val SIGNOUT = "SIGNOUT"
        private const val REVOKE = "REVOKE"
        private const val BACKUP_OK = "backup_ok"
        private const val RESTORE_OK = "restore_ok"
        private const val RESTORE_KO = "restore_ko"
        internal const val COMUNITA_FILE_NAME = "Comunita"
        internal const val FRATELLO_FILE_NAME = "Fratello"
        internal const val INCONTRO_FILE_NAME = "Incontro"
        internal const val PASSAGGIO_FILE_NAME = "Passaggio"
        internal const val PROMEMORIA_FILE_NAME = "Promemoria"
        internal const val VOCAZIONE_FILE_NAME = "Vocazione"
        internal const val SEMINARIO_FILE_NAME = "Seminario"
        internal const val VISITASEMINARIO_FILE_NAME = "VisitaSeminario"
        internal const val RESPONSABILESEMINARIO_FILE_NAME = "ResponsabileSeminario"
        internal const val SEMINARISTA_FILE_NAME = "Seminarista"
        internal const val COMUNITASEMINARISTA_FILE_NAME = "ComunitaSeminarista"
    }

}