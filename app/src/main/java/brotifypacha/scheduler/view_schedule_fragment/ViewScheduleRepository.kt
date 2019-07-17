package brotifypacha.scheduler.view_schedule_fragment

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.SchedulerApiService
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.data_models.ResultModel
import brotifypacha.scheduler.data_models.ScheduleModel
import brotifypacha.scheduler.database.Schedule
import brotifypacha.scheduler.database.SchedulerDataBase
import kotlinx.coroutines.*

class ViewScheduleRepository(val app: Application, val scope: CoroutineScope, val errorEvent: MutableLiveData<String>){

    val TAG = ViewScheduleRepository::class.java.simpleName

    val db: SchedulerDataBase = SchedulerDataBase.getInstance(app.applicationContext)
    val api: SchedulerApiService = SchedulerApiService.build()
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
        if (Utils.isAuthorizedWithToken(pref)) {
            scope.launch {
                Log.d(TAG, "getSchedule - downloading info")
                withContext(Dispatchers.IO) {
                    downloadScheduleInfo(scheduleId)
                }
            }
        }
        return db.getSchedulesDao().getScheduleLiveData(scheduleId)
    }

    private suspend fun downloadScheduleInfo(scheduleId: String){
        try {
            val response: ResultModel<List<ScheduleModel>> = api.get_schedules("{'_id': '$scheduleId'}", null).await()
            if (response.result == Constants.SUCCESS){
                if (response.data.isEmpty()){
                    triggerError()
                    return
                }
                val scheduleModel = response.data[0]

                db.getSchedulesDao().insert(Schedule.fromModel(scheduleModel))
            } else {
                Log.e(TAG, response.type)
            }
        } catch (e: Exception){
            Log.e("$TAG downloadScheduleInfo()", e.toString())
            triggerError()
        }
    }

    private suspend fun triggerError(){
        withContext(Dispatchers.Main){
            errorEvent.value = Constants.NETWORK_ERROR
        }
    }


}