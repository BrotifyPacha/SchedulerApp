package brotifypacha.scheduler.edit_schedule_fragment

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import androidx.work.*
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.database.Schedule
import brotifypacha.scheduler.database.SchedulerDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.util.concurrent.TimeUnit

class EditScheduleRepository(val app: Application, val viewModelScope: CoroutineScope, val errorEvent: MutableLiveData<String>) {

    val db = SchedulerDataBase.getInstance(app.applicationContext)
    val pref = app.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    

    fun saveChanges(scheduleId: String, editedSchedule: Schedule, saveChangeEvent: MutableLiveData<Boolean>) {
        viewModelScope.launch{
            withContext(Dispatchers.IO){
                db.getSchedulesDao().update(editedSchedule)
            }
            saveChangeEvent.value = true
        }

    }
}
