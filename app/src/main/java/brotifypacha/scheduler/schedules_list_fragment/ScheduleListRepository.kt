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
import brotifypacha.scheduler.SchedulerApiService
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.data_models.ResultModel
import brotifypacha.scheduler.database.Schedule
import brotifypacha.scheduler.database.SchedulerDataBase
import brotifypacha.scheduler.database.SchedulesDao
import brotifypacha.scheduler.database.UsersDao
import brotifypacha.scheduler.work_manager.DeleteScheduleWorker
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class ScheduleListRepository(val app: Application, val scope: CoroutineScope, val errorEvent: MutableLiveData<String>) {

    val TAG = ScheduleListRepository::class.java.simpleName

    val schedulesDao : SchedulesDao
    val usersDao : UsersDao
    val api: SchedulerApiService
    val pref: SharedPreferences
    val db: SchedulerDataBase



    init {
        db = SchedulerDataBase.getInstance(app)
        schedulesDao = db.getSchedulesDao()
        usersDao = db.getUsersDao()
        api = SchedulerApiService.build()
        pref = app.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    suspend fun downloadSchedulesOfUser() {
        val token = pref.getString(Constants.PREF_KEY_TOKEN, null)
        try {
            val result = api.get_active_user(token).await()
            if (result.result == Constants.SUCCESS){
                val user = result.data
                Log.d(TAG, result.data.toString())
                withContext(Dispatchers.IO) {
                    val schedules: ArrayList<Schedule> = ArrayList()
                    user.schedules.forEach {
                        schedules.add(Schedule.fromModel(it))
                    }
                    // Использую опен хелпер чтобы БД не посылала эвент об апдейте
                    // Удаляю все расписания которые не связаныы с "оффлайн аккаунтом"
                    db.openHelper.writableDatabase.execSQL("DELETE FROM schedules_table WHERE creator != 'offline' ")
                    schedulesDao.insert(schedules)
                }
            }
        } catch (e: ConnectException){
            Log.e(TAG, e.toString())
            errorEvent.value = Constants.NETWORK_ERROR
        } catch (e: SocketTimeoutException){
            Log.d(TAG, "ITS CRASHED")
        }
    }

    fun getSchedulesOfUser() : LiveData<List<Schedule>> {
        return schedulesDao.getSubscribedSchedulesOfUser(pref.getString(Constants.PREF_KEY_ID, Constants.PREF_VALUE_OFFLINE)!!)
    }

    fun createNewSchedule(name: String, alias: String, responseLiveData: MutableLiveData<ResultModel<Unit>>) {
        scope.launch {
            withContext(Dispatchers.IO) {
                db.getSchedulesDao().insert(
                    Schedule(
                        Schedule.generateId(),
                        alias,
                        name,
                        Utils.getToken(pref),
                        subscribedUsers = Schedule.listToStr(listOf(Utils.getToken(pref))),
                        firstDay = 0,
                        schedule = Schedule.getDefaultEmptyScheduleString(),
                        changes = Schedule.changesToStr(ArrayList())
                    )
                )
            }
            var response: ResultModel<Unit>? = null
            if (Utils.isAuthorizedWithToken(pref)){
                try {
                    response = api.create_schedule(
                        header = Utils.getToken(pref),
                        name = name,
                        alias = alias,
                        schedule = Schedule.getDefaultEmptyScheduleString(),
                        first_day = "0"
                    ).await()
                } catch (e: Exception){
                    response = ResultModel(Constants.ERROR, Unit, type = Constants.NETWORK_ERROR)
                }
            }
            withContext(Dispatchers.Main) {
                if (response != null){
                    responseLiveData.value = response
                } else {
                    responseLiveData.value = ResultModel("success", Unit)
                }
            }
        }
    }

    fun deleteSchedule(scheduleId: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val oneTimeWorkRequest = OneTimeWorkRequestBuilder<DeleteScheduleWorker>()
                oneTimeWorkRequest.setConstraints(constraints)
                oneTimeWorkRequest.setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.SECONDS)
                oneTimeWorkRequest.setInputData(
                    Data.Builder()
                        .putString(DeleteScheduleWorker.DATA_KEY_ALIAS, BaseRepository(app).getSchedule(scheduleId).alias)
                        .putString(DeleteScheduleWorker.DATA_KEY_SCHEDULE_ID, scheduleId)
                        .build()
                )

                val operation = WorkManager.getInstance()
                    .beginUniqueWork(
                        "Delete schedule ${scheduleId}",
                        ExistingWorkPolicy.KEEP,
                        oneTimeWorkRequest.build()
                    )
                    .enqueue()

                db.getSchedulesDao().delete(db.getSchedulesDao().getSchedule(scheduleId))
            }
        }
    }

}