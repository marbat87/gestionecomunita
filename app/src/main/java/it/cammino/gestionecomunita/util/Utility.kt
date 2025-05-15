package it.cammino.gestionecomunita.util

import android.content.Context
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.util.Utils
import com.mikepenz.fastadapter.ui.utils.StringHolder
import org.joda.time.LocalDate
import org.joda.time.Years
import org.joda.time.format.DateTimeFormat
import java.sql.Date
import java.text.ParseException
import java.text.SimpleDateFormat

object Utility {

    const val CLICK_DELAY: Long = 2000
    internal const val SIGNED_IN = "signed_id"

    private val TAG = Utility::class.java.canonicalName

    fun getDateFromString(ctx: Context, inputString: String): Date? {
        if (inputString.isEmpty())
            return null

        val df = SimpleDateFormat("dd/MM/yyyy", ctx.resources.systemLocale)

        val date: java.util.Date?

        try {
            date = df.parse(inputString)
        } catch (e: ParseException) {
            Log.e(TAG, e.message, e)
            return null
        }

        var returnDate: Date? = null
        date?.let {
            returnDate = Date(it.time)
        }

        return returnDate
    }

    fun getStringFromDate(ctx: Context, inputDate: Date): String? {
        val df = SimpleDateFormat("dd/MM/yyyy", ctx.resources.systemLocale)

        var date: String? = null

        try {
            date = df.format(inputDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return date
    }

    fun calculateAge(birthDate: String): Int {
        val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
        return Years.yearsBetween(
            LocalDate.parse(birthDate, formatter),
            LocalDate.now()
        ).years
    }

    fun validateToken(
        idToken: String,
        clientId: String
    ): String {
        Log.d(TAG, "IDTOKEN: $idToken")
        if (idToken.isEmpty())
            return StringUtils.EMPTY

        try {
            val verifier = GoogleIdTokenVerifier.Builder(
                Utils.getDefaultTransport(),
                Utils.getDefaultJsonFactory()
            ) // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(listOf(clientId))
                .build()
            val googleIdToken: GoogleIdToken = verifier.verify(idToken)
            Log.d(TAG, "IDTOKEN SUBJECT: ${googleIdToken.payload.subject}")
            return googleIdToken.payload.subject
        }
        catch (e: Exception) {
            Log.e(TAG, "validateToken exception", e)
            return StringUtils.EMPTY
        }

    }

    fun fixSystemBarPadding(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(
            view
        ) { v, insets ->
            val innerPadding = insets.getInsets(
                // Notice we're using systemBars, not statusBar
                WindowInsetsCompat.Type.systemBars()
                        // Notice we're also accounting for the display cutouts
                        or WindowInsetsCompat.Type.displayCutout()
                // If using EditText, also add
                // "or WindowInsetsCompat.Type.ime()"
                // to maintain focus when opening the IME
            )
            v.setPadding(
                innerPadding.left,
                0,
                innerPadding.right,
                innerPadding.bottom)
            insets
        }
    }

    fun <T> helperSetString(t: T): StringHolder = when (t) {
        is String -> StringHolder(t)
        is Int -> StringHolder(t)
        else -> throw IllegalArgumentException()
    }

}
