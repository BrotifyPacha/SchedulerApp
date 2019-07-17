package brotifypacha.scheduler

import android.app.Application
import android.content.Context
import brotifypacha.scheduler.database.Schedule
import brotifypacha.scheduler.database.SchedulerDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class BaseRepository(app: Application) {

    val db = SchedulerDataBase.getInstance(app.applicationContext)
    val api = SchedulerApiService.build()
    val pref = app.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getSchedule(scheduleId: String) = runBlocking{
        withContext(Dispatchers.IO){
            db.getSchedulesDao().getSchedule(scheduleId)
        }
    }
    fun getIsScheduleOwner(scheduleId: String) : Boolean = runBlocking {
        withContext(Dispatchers.IO) {
            db.getSchedulesDao().getSchedule(scheduleId).creator == pref.getString(
                Constants.PREF_KEY_ID,
                Constants.PREF_VALUE_OFFLINE
            )
        }
    }

}