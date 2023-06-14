package it.cammino.gestionecomunita.util

import android.content.Context
import android.util.Log
import org.joda.time.LocalDate
import org.joda.time.Years
import org.joda.time.format.DateTimeFormat
import java.sql.Date
import java.text.ParseException
import java.text.SimpleDateFormat

object Utility {

    const val CLICK_DELAY: Long = 2000
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

}
