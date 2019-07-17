package brotifypacha.scheduler.work_manager

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.SchedulerApiService
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.database.SchedulerDataBase

class DeleteScheduleWorker(val context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {

    companion object {
        val DATA_KEY_ALIAS = "alias"
        val DATA_KEY_SCHEDULE_ID = "schedule_id"
    }
    val TAG = DeleteScheduleWorker::class.java.simpleName

    override suspend fun doWork(): Result {

        val db = SchedulerDataBase.getInstance(context)
        val api = SchedulerApiService.build()
        val pref = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val alias = inputData.getString(DATA_KEY_ALIAS)!!
        val scheduleId = inputData.getString(DATA_KEY_SCHEDULE_ID)!!
        if (Utils.isAuthorizedWithToken(pref)) {
            if (alias.length > 0) {
                val response = api.deleteSchedule(Utils.getToken(pref), alias, alias).await()
                if (!response.result.equals(Constants.SUCCESS)) {
                    return Result.retry()
                }
            }
        }
        //Закоментировал потому что по сути, на этом этапе распиания уже нет в локальной БД
        //db.getSchedulesDao().deleteIfExists(db.getSchedulesDao().getSchedule(scheduleId))
        return Result.success()
    }

}
