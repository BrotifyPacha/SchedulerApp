package brotifypacha.scheduler

import android.util.Log
import androidx.work.*
import brotifypacha.scheduler.work_manager.SaveChangeWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.TimeUnit

class SchedulerNotificationService: FirebaseMessagingService(){

    override fun onMessageReceived(msg: RemoteMessage?) {
        super.onMessageReceived(msg)
        Log.d("Notification serciece", "caought message ${msg!!.data}")

        if (msg != null && msg.data.isNotEmpty()){
            Log.d("TAG", msg.data.toString())

            val saveChangeWorker = OneTimeWorkRequestBuilder<SaveChangeWorker>()
                .setInputData(Data.Builder()
                    .putString(SaveChangeWorker.DATA_KEY_CHANGE_INFO, msg.data["change_data"]!!)
                    .build())
                .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance()
                .beginUniqueWork("save change ${msg.data}", ExistingWorkPolicy.REPLACE, saveChangeWorker)
                .enqueue()
        }

    }

}