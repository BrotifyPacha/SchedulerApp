package brotifypacha.scheduler.view_schedule_fragment

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.data_models.ResultModel
import brotifypacha.scheduler.data_models.ScheduleModel
import brotifypacha.scheduler.database.Schedule
import brotifypacha.scheduler.database.SchedulerDataBase
import kotlinx.coroutines.*

class ViewScheduleRepository(val app: Application, val scope: CoroutineScope, val errorEvent: MutableLiveData<String>){

    val TAG = ViewScheduleRepository::class.java.simpleName

    val db: SchedulerDataBase = SchedulerDataBase.getInstance(app.applicationContext)
    val pref = app.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    //fun getSchedule(scheduleId: String, mediatorLiveData: MediatorLiveData<Schedule>, completedEvent: MutableLiveData<Boolean>) {
    //    scope.launch {
    //        withContext(Dispatchers.IO){
    //            if (Utils.isAuthorizedWithToken(pref)){
    //                downloadScheduleInfo(Utils.getToken(pref), scheduleId)
    //            }
    //            getScheduleFromLocalStorage(scheduleId, mediatorLiveData)
    //        }
    //        completedEvent.value = true
    //    }
    //}

    fun getSchedule(scheduleId: String) : LiveData<Schedule> {
        return db.getSchedulesDao().getScheduleLiveData(scheduleId)
    }

    fun deleteSchedule(scheduleId: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                db.getSchedulesDao().delete(db.getSchedulesDao().getSchedule(scheduleId))
            }
        }
    }


}