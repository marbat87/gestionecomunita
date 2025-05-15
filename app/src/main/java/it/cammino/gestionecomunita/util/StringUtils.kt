package it.cammino.gestionecomunita.util

import kotlin.random.Random

object StringUtils {

    const val ND = "N.D."
    const val ITINERANTI = "itineranti"
    const val EMPTY_STRING = ""
    const val DASH = "-"
    private const val CODE_LENGTH = 16
    const val PREFERENCE_BACKUP_CODE = "backup_code"
    internal const val SHARED_AXIS = "shared_axis"
    val RESPONSABILE = arrayOf("responsabile", "resp")
    val VICE_RESPONSABILE = arrayOf("vice responsabile", "vice-responsabile", "vice", "viceresp")

    const val EMPTY = ""
    const val UNEXPECTED_CREDENTIAL = "Unexpected type of credential"

    fun generateRandomCode(): String {
        val chars = ("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk"
                + "lmnopqrstuvwxyz!@#$%&")
        val sb = StringBuilder(CODE_LENGTH)
        (0 until CODE_LENGTH).forEach { i ->
            sb.append(chars[Random.nextInt(chars.length)])
        }
        return sb.toString()
    }

}