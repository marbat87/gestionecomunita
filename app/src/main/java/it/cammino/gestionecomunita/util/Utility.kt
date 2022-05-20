package it.cammino.gestionecomunita.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.elevation.SurfaceColors
import java.sql.Date
import java.text.ParseException
import java.text.SimpleDateFormat

object Utility {

    const val CLICK_DELAY: Long = 2000
    private val TAG = Utility::class.java.canonicalName

    fun getDateFromString(ctx: Context, inputString: String): Date? {
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

}
