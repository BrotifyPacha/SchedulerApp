package brotifypacha.scheduler.main_activity

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.database.SchedulerDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class MainActivityViewModel(val app: Application) : AndroidViewModel(app) {

    val TAG = MainActivityViewModel::class.java.simpleName

    private var pref: SharedPreferences
    private val eventDataLoaded: MutableLiveData<Boolean> = MutableLiveData()
    private val eventError: MutableLiveData<String> = MutableLiveData()
    private val eventRequestAuth: MutableLiveData<Boolean> = MutableLiveData()

    init {
        pref = app.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun getEventRequestAuth(): LiveData<Boolean> {
        return eventRequestAuth
    }
    fun setEventRequestAuth() {
        eventRequestAuth.value = true
    }
    fun setEventRequestAuthCaptured() {
        eventRequestAuth.value = false
    }

    fun getEventDataLoaded() : LiveData<Boolean> {
        return eventDataLoaded
    }
    private fun setEventDataLoaded() {
        eventDataLoaded.value = true
    }
    fun setEventDataLoadedCaptured() {
        eventDataLoaded.value = false
    }

    fun getEventError(): MutableLiveData<String>{
        return eventError
    }
    fun setErrorEvent(type: String) {
        eventError.value = type
    }
    fun setEventErrorCaptured() {
        eventError.value = null
    }



}