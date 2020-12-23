package brotifypacha.scheduler.schedules_list_fragment

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.database.Schedule
import brotifypacha.scheduler.database.SchedulerDataBase
import brotifypacha.scheduler.database.SchedulesDao
import kotlinx.coroutines.*

class ScheduleListRepository(val app: Application, val scope: CoroutineScope, val errorEvent: MutableLiveData<String>) {

    val TAG = ScheduleListRepository::class.java.simpleName

    val schedulesDao : SchedulesDao
    val pref: SharedPreferences
    val db: SchedulerDataBase



    init {
        db = SchedulerDataBase.getInstance(app)
        schedulesDao = db.getSchedulesDao()
        pref = app.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun getSchedules() : LiveData<List<Schedule>> {
        return schedulesDao.getSchedulesLiveData()
    }

    fun createNewSchedule(name: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                db.getSchedulesDao().insert(
                    Schedule(
                        Schedule.generateId(),
                        name,
                        firstDay = 0,
                        schedule = Schedule.getDefaultEmptyScheduleString(),
                        changes = Schedule.changesToStr(ArrayList())
                    )
                )
            }
        }
    }

    fun deleteSchedule(scheduleId: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                db.getSchedulesDao().delete(db.getSchedulesDao().getSchedule(scheduleId))
            }
        }
    }

}