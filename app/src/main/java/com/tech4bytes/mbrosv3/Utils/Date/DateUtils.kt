package com.tech4bytes.mbrosv3.Utils.Date

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class DateUtils {
    companion object {
        var datePattern = "yyyy-MM-dd HH:mm:ss"
        var timeZone = "Asia/Kolkata"

        fun getDateString(datetime: String): String {
            return if (timeZoneConversionRequired(datetime)) {
                convertTimeZones(datetime)
            } else {
                datetime
            }
        }

        fun getCurrentDateInString(): String {
            return DateFormat.getDateTimeInstance().format(Date())
        }

        fun getDateFormat(): SimpleDateFormat {
            return SimpleDateFormat(datePattern)
        }

        fun getCurrentTimestamp(): String {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern(datePattern)
            return current.format(formatter)
        }

        fun getCurrentDate(pattern: String): String {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern(pattern)
            return current.format(formatter)
        }

        private fun convertTimeZones(sourceTime: String): String {
            val sourceFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            sourceFormat.timeZone = TimeZone.getTimeZone("UTC")
            val parsed = sourceFormat.parse(sourceTime)

            val destFormat = SimpleDateFormat(datePattern)
            destFormat.timeZone = TimeZone.getTimeZone(timeZone)

            return destFormat.format(parsed)
        }

        fun getSelectedValidTimeOrCurrentTime(str: String): String {
            return if (!isTimestamp(str)) {
                getCurrentTimestamp()
            } else {
                str
            }
        }

        fun isTimestamp(str: String): Boolean {
            return try {
                getCalendar(str)
                true
            } catch (e: ParseException) {
                false
            }
        }

        fun getDate(datetime: String): Date? {
            return SimpleDateFormat(datePattern).parse(getDateString(datetime))
        }

        fun getCalendar(datetime: String): Calendar {
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat(datePattern, Locale.ENGLISH)
            cal.time = sdf.parse(datetime)
            return cal
        }

        fun getMonthName(date: Date): String {
            val month_date = SimpleDateFormat("MMM")
            val month_name = month_date.format(date)
            return month_name
        }

        fun getYear(date: Date): Int {
            val month_date = SimpleDateFormat("YY")
            val month_name = month_date.format(date)
            return month_name.toInt()
        }

        fun getTwoDigitYear(date: Date): String {
            val month_date = SimpleDateFormat("YY")
            val twoDigitYear = month_date.format(date)
            return twoDigitYear
        }

        fun getDateInFormat(date: Date, format: String): String {
            val format = SimpleDateFormat(format)
            val formattedDate = format.format(date)
            return formattedDate
        }

        fun getDateInFormat(format: String): String {
            val format = SimpleDateFormat(format)
            return format.format(Date())
        }

        fun timeZoneConversionRequired(datetime: String): Boolean {
            return datetime.endsWith("Z")
        }

        fun getNextTimeOccurrenceTimestamp(hourIn24Hour: Int): LocalDateTime {
            val currentDateTime: LocalDateTime? = LocalDateTime.now()
            val todayAt4Pm: LocalDateTime? = currentDateTime!!.toLocalDate().atTime(hourIn24Hour, 0)
            val tomorrowAt4Pm: LocalDateTime? =
                currentDateTime.toLocalDate().plusDays(1).atTime(hourIn24Hour, 0)

            if (todayAt4Pm!!.isAfter(currentDateTime)) {
                return todayAt4Pm
            }
            return tomorrowAt4Pm!!
        }
    }
}