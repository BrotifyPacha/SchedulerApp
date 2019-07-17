package brotifypacha.scheduler.edit_schedule_fragment

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import androidx.work.*
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.SchedulerApiService
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.data_models.ResultModel
import brotifypacha.scheduler.database.Schedule
import brotifypacha.scheduler.database.SchedulerDataBase
import brotifypacha.scheduler.work_manager.CommitScheduleWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.util.concurrent.TimeUnit
import java.util.prefs.NodeChangeEvent

class EditScheduleRepository(val app: Application, val viewModelScope: CoroutineScope, val errorEvent: MutableLiveData<String>) {

    val db = SchedulerDataBase.getInstance(app.applicationContext)
    val api = SchedulerApiService.build()
    val pref = app.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    

    fun saveChanges(scheduleId: String, editedSchedule: Schedule, saveChangeEvent: MutableLiveData<Boolean>) {
        viewModelScope.launch{
            if (Utils.isAuthorizedWithToken(pref)){
                val oneTimeWorkRequest = OneTimeWorkRequestBuilder<CommitScheduleWorker>()
                    .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)

                        .build()
                    )
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        10,
                        TimeUnit.SECONDS
                    )
                    .setInputData(Data.Builder()
                        .putString(CommitScheduleWorker.DATA_KEY_SCHEDULE_ID, scheduleId)
                        .putString(CommitScheduleWorker.DATA_KEY_SCHEDULE_JSON, editedSchedule.toJsonString())
                        .build())
                    .build()
                WorkManager.getInstance()
                    .beginUniqueWork("Saving changes ${scheduleId}", ExistingWorkPolicy.REPLACE, oneTimeWorkRequest)
                    .enqueue()
            }
            withContext(Dispatchers.IO){
                db.getSchedulesDao().update(editedSchedule)
            }
            saveChangeEvent.value = true
        }

    }

    fun verifyAlias(scheduleId: String, alias: String, event: MutableLiveData<ResultModel<Unit>>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                try {
                    val response = api.get_schedules(
                        "{'_id': '${scheduleId}', 'alias': '${alias}'}",
                        "[]").await()
                    withContext(Dispatchers.Main) {
                        when (response.result){
                            Constants.SUCCESS -> if (response.data.size <= 1)
                                event.value = ResultModel(Constants.SUCCESS, Unit)
                            else event.value = ResultModel(Constants.ERROR, Unit, type = "field", field = "alias", description = "error_4")
                        }
                    }
                } catch (e: ConnectException){
                    withContext(Dispatchers.Main) {
                        event.value = ResultModel(Constants.NETWORK_ERROR, Unit)
                    }
                }
            }
        }
    }

}
