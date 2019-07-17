package brotifypacha.scheduler.work_manager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.SchedulerApiService
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.data_models.ChangeModel
import brotifypacha.scheduler.database.Schedule
import brotifypacha.scheduler.database.SchedulerDataBase
import com.google.gson.Gson


class UnsubscribeWorker(val context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters){

    val TAG = UnsubscribeWorker::class.java.simpleName

    companion object {
        val DATA_KEY_SCHEDULE_ID = "schedule_id"
    }
    /**
     * Override this method to do your actual background processing.  This method is called on a
     * background thread - you are required to **synchronously** do your work and return the
     * [androidx.work.ListenableWorker.Result] from this method.  Once you return from this
     * method, the Worker is considered to have finished what its doing and will be destroyed.  If
     * you need to do your work asynchronously on a thread of your own choice, see
     * [ListenableWorker].
     *
     *
     * A Worker is given a maximum of ten minutes to finish its execution and return a
     * [androidx.work.ListenableWorker.Result].  After this time has expired, the Worker will
     * be signalled to stop.
     *
     * @return The [androidx.work.ListenableWorker.Result] of the computation; note that
     * dependent work will not execute if you use
     * [androidx.work.ListenableWorker.Result.failure] or
     * [androidx.work.ListenableWorker.Result.failure]
     */
    override suspend fun doWork(): Result {
        val db = SchedulerDataBase.getInstance(context)
        val api = SchedulerApiService.build()
        val pref = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        val scheduleId = inputData.getString(DATA_KEY_SCHEDULE_ID)!!

        val scheduleBeforeEdit = db.getSchedulesDao().getSchedule(scheduleId)

        db.getSchedulesDao().delete(scheduleBeforeEdit)
        if (Utils.isAuthorized(pref)) {
            val response = api.unsubscribe(
                Utils.getToken(pref),
                scheduleBeforeEdit.alias
            ).await()
            if (response.result != Constants.SUCCESS) {
                Result.retry()
            }
        }
        return Result.success()

    }

}
