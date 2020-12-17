package brotifypacha.scheduler.schedules_list_fragment

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import brotifypacha.scheduler.BaseRepository
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.data_models.ResultModel
import brotifypacha.scheduler.database.Schedule
import brotifypacha.scheduler.database.SchedulerDataBase
import brotifypacha.scheduler.database.SchedulesDao
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

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

    fun createNewSchedule(name: String, responseLiveData: MutableLiveData<ResultModel<Unit>>) {
        scope.launch {
            withContext(Dispatchers.IO) {
                Log.d("TAG", "Inserted schedule!")
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
            responseLiveData.value = ResultModel(ResultModel.CODE_SUCCESS, Unit)
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