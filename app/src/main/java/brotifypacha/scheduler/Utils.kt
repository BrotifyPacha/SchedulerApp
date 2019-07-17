package brotifypacha.scheduler

import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class Utils {

    companion object {

        fun isAuthorizedWithToken(pref: SharedPreferences): Boolean {
            if (pref.contains(Constants.PREF_KEY_ID) && !(pref.getString(Constants.PREF_KEY_ID, Constants.PREF_VALUE_OFFLINE)!!.equals(Constants.PREF_VALUE_OFFLINE))){
                return true
            }
            return false
        }

        fun isAuthorized(pref: SharedPreferences): Boolean {
            if (pref.contains(Constants.PREF_KEY_ID) && pref.contains(Constants.PREF_KEY_TOKEN)) {
                return true
            }
            return false
        }

        fun getToken(pref: SharedPreferences): String {
            return pref.getString(Constants.PREF_KEY_TOKEN, Constants.PREF_VALUE_OFFLINE)!!
        }

        fun setAuthorizedOffline(pref: SharedPreferences) {
            pref.edit().clear()
                .putString(Constants.PREF_KEY_TOKEN, Constants.PREF_VALUE_OFFLINE)
                .putString(Constants.PREF_KEY_ID, Constants.PREF_VALUE_OFFLINE)
                .apply()
        }


        fun formatDate(date: Long): String{
            return SimpleDateFormat("dd.MM.yy").format(date)
        }

        fun getDayOfWeek(date: Long): Int{
            val calendar = Calendar.getInstance()
            return if (calendar.get(Calendar.DAY_OF_WEEK) == 1 ) 6 else calendar.get(Calendar.DAY_OF_WEEK)-2
        }
    }

}