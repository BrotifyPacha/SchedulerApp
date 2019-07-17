package brotifypacha.scheduler.work_manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.R
import brotifypacha.scheduler.SchedulerApiService
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.data_models.ChangeModel
import brotifypacha.scheduler.database.Schedule
import brotifypacha.scheduler.database.SchedulerDataBase
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class SaveChangeWorker(val context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters){

    val TAG = SaveChangeWorker::class.java.simpleName
    val weekdays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    companion object {

        data class ChangeInfo(
            @SerializedName("schedule_id")
            val scheduleId: String,
            val change: ArrayList<String>,
            val date: Long
        )

        val DATA_KEY_CHANGE_INFO = "change_info"
    }

    override suspend fun doWork(): Result {
        val db = SchedulerDataBase.getInstance(context)
        val api = SchedulerApiService.build()
        val pref = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        Log.d(TAG, "worker triggered")
        val change = Gson().fromJson(inputData.getString(DATA_KEY_CHANGE_INFO)!!, ChangeInfo::class.java)

        val scheduleBeforeEdit = db.getSchedulesDao().getSchedule(change.scheduleId)
        val changesList = ArrayList(scheduleBeforeEdit.getChangesAsList())
        changesList.add(ChangeModel(change.date, change.change))
        val schedule = scheduleBeforeEdit.copy(changes = Schedule.changesToStr(changesList))
        db.getSchedulesDao().insert(schedule)

        showNotification(ChangeModel(change.date, change.change), schedule.name)

        return Result.success()


    }

    private fun showNotification(change: ChangeModel, name: String) {
        val manager = getSystemService(context, NotificationManager::class.java) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(Constants.CHANNEL_ID, "Канал изменений",
                NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Используется для сообщения об изменение в расписании на которое вы подписаны");
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.enableVibration(false);
            manager.createNotificationChannel(channel);
        }

        val simpleDateFormatter = SimpleDateFormat("dd.MM.yy")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = change.date
        val dateStr = simpleDateFormatter.format(calendar.time)



        Log.d(TAG, "something good")
        val notification = NotificationCompat.Builder(applicationContext, Constants.CHANNEL_ID)
        notification.apply {
            setContentTitle(name)
            setContentText("Изменение в расписании")
            setStyle(NotificationCompat.BigTextStyle().bigText(
                formatChange(change)
            ))
            //setContentText("Дата: $dateStr - ${weekdays[Utils.getDayOfWeek(calendar.timeInMillis)]}")
            setShowWhen(true)
            setSmallIcon(R.drawable.ic_outline_event_24px)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setVibrate(longArrayOf(0L, 100L, 50L, 100L))
            setLights(Color.GREEN, 250, 100)
        }

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            Log.d(TAG, "notified")
            Log.d(TAG, notification.build().toString())
            notify(112, notification.build())
        }
    }

    fun formatChange(change: ChangeModel): String{
        var result = "Дата: ${Utils.formatDate(change.date)} - ${weekdays[Utils.getDayOfWeek(change.date)]}\n"
        val hasLessons = change.change.filter { it.isNotBlank() }.isNotEmpty()
        if (!hasLessons) return "${result}Занятий нет"

        var lastLesson = 0
        change.change.forEachIndexed{ id, it ->
            if (it.isNotBlank()) lastLesson = id
        }
        change.change.forEachIndexed{id, it ->
            if (id <= lastLesson) result = result + "${id+1}. $it"
            if (id < lastLesson) result += "\n"

        }
        return result
    }


}
