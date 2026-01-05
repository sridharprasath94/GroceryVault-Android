package com.flash.groceryVault.ui.util

import com.flash.groceryVault.ui.util.DateFormats.LIST_DATE_WITH_SECONDS
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormats {
    const val LIST_DATE_TIME = "dd MMM • HH:mm"
    const val LIST_DATE_TIME_WITH_YEAR = "dd MMM yyyy • HH:mm"
    const val LIST_DATE_WITH_SECONDS = "dd MMM yyyy • HH:mm:ss"
}

fun Long.toFormattedDateTimeLegacy(
    pattern: String = LIST_DATE_WITH_SECONDS
): String =
    SimpleDateFormat(pattern, Locale.getDefault())
        .format(Date(this))