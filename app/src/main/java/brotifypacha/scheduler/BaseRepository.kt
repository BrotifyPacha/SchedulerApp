package brotifypacha.scheduler

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import brotifypacha.scheduler.database.Schedule
import brotifypacha.scheduler.database.SchedulerDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class BaseRepository(app: Application) {

    val db = SchedulerDataBase.getInstance(app.applicationContext)
    val pref = app.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getSchedule(scheduleId: String) = runBlocking{
        withContext(Dispatchers.IO){
            db.getSchedulesDao().getSchedule(scheduleId)
        }
    }

    fun getScheduleLiveData(scheduleId: String): LiveData<Schedule>{
        return db.getSchedulesDao().getScheduleLiveData(scheduleId)
    }

    /**
     * @return Schedule object if found or NULL
     */
    fun findScheduleByName(name: String) : Schedule? {
        return db.getSchedulesDao().getSchedules().filter { it -> it.name == name }.firstOrNull()
    }
}