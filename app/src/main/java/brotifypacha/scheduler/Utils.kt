package brotifypacha.scheduler

import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class Utils {

    companion object {

        fun formatDate(date: Long): String{
            return SimpleDateFormat("dd.MM.yy").format(date)
        }

        fun isSameDate(date1: Long, date2: Long) : Boolean {
            return formatDate(date1) == formatDate(date2)
        }

        fun getDayOfWeek(date: Long): Int{
            val calendar = Calendar.getInstance()
            return if (calendar.get(Calendar.DAY_OF_WEEK) == 1 ) 6 else calendar.get(Calendar.DAY_OF_WEEK)-2
        }
    }

}