package brotifypacha.scheduler.view_schedule_fragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import brotifypacha.scheduler.BaseRepository
import brotifypacha.scheduler.database.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class ViewScheduleViewModel(val id: String, val app: Application) : AndroidViewModel(app) {

    val TAG = ViewScheduleViewModel::class.java.simpleName

    class Factory(val schedule_id: String, val app: Application): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ViewScheduleViewModel(schedule_id, app) as T
        }
    }

    private val scheduleMediatorLiveData: MediatorLiveData<Schedule> = MediatorLiveData()
    private lateinit var scheduleQuery: LiveData<Schedule>

    private val errorEvent: MutableLiveData<String> = MutableLiveData()
    private val refreshedEvent: MutableLiveData<Boolean> = MutableLiveData()
    private val onMenuClickEvent: MutableLiveData<MenuClickedEventData> = MutableLiveData()
    private val repository: ViewScheduleRepository

    var selectedWeekDate: Long = -1

    private val onWeekSelectedEvent: MutableLiveData<WeekSelectedEventData> = MutableLiveData()

    init {
        repository = ViewScheduleRepository(app, viewModelScope, errorEvent)
        refreshSchedule()
    }

    fun refreshSchedule(){
        if (::scheduleQuery.isInitialized) scheduleMediatorLiveData.removeSource(scheduleQuery)
        viewModelScope.launch(Dispatchers.IO) {
            scheduleQuery = repository.getSchedule(id)
            withContext(Dispatchers.Main){
                scheduleMediatorLiveData.addSource(scheduleQuery, Observer {
                    scheduleMediatorLiveData.value = it
                })
                refreshedEvent.value = true
            }
        }
    }


    fun getRefreshedEvent() : LiveData<Boolean>{
        return refreshedEvent
    }
    fun setRefreshedEventHandled(){
        refreshedEvent.value = false
    }

    fun getErrorEvent(): LiveData<String> {
        return errorEvent
    }
    fun setErrorEventHandled(){
        errorEvent.value = null
    }

    fun getScheduleLiveData(): LiveData<Schedule> {
        return scheduleMediatorLiveData
    }

    fun getOnWeekSelectedEvent() : LiveData<WeekSelectedEventData>{
        return onWeekSelectedEvent
    }

    data class MenuClickedEventData(val scheduleId: String)

    fun onMenuClickEvent(){
        onMenuClickEvent.value = MenuClickedEventData(id)
    }
    fun getOnMenuClickEvent() : LiveData<MenuClickedEventData>{
        return onMenuClickEvent
    }
    fun setOnMenuClickEventHandled() {
        onMenuClickEvent.value = null
    }

    data class DayData( val date: Long, val lessons: List<String>)

    data class WeekSelectedEventData(val currentDay: Int, val currentWeek: List<DayData>)


    fun setSelectedWeekByDate(date: Long){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                selectedWeekDate = date
                val dayOfWeek = getDayOfWeek(date)
                val arrayList = ArrayList<DayData>()
                for (i in 0..6){
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = date + (i - dayOfWeek) * 1000*60*60*24
                    val lessons = getScheduleForDate(calendar.timeInMillis)
                    arrayList.add(DayData(calendar.timeInMillis, lessons))
                }
                withContext(Dispatchers.Main){
                    onWeekSelectedEvent.value = WeekSelectedEventData(dayOfWeek, arrayList)
                }
            }
        }
    }

    /**
     * Возвращает расписание на неделю содержающую дату "date"
     */
    private fun getScheduleForDate(date: Long) : List<String>{
        val scheduleData = scheduleMediatorLiveData.value!!.getScheduleAsList()
        val changesData = scheduleMediatorLiveData.value!!.getChangesAsList()
        val dateDate = Calendar.getInstance()
        dateDate.timeInMillis = date

        val now = Calendar.getInstance()
        val changeDate = now

        val changeForDate = changesData.filter { isSameDate(it.date, date) }.getOrNull(0)
        if (changeForDate != null){
            return changeForDate.change
        }

        var currentWeek = 0
        if (scheduleData.size > 1){
            val deltaDate = date - scheduleMediatorLiveData.value!!.firstDay
            val deltaWeeks: Int = (deltaDate / 1000 / 60 / 60 / 24 / 7).toInt() // Переводим миллисекунды в Недели
            currentWeek = deltaWeeks % scheduleData.size
        }
        val dayOfWeek = getDayOfWeek(date)
        return scheduleData[currentWeek][dayOfWeek]
    }

    fun isSameDate(dateOne: Long, dateTwo: Long): Boolean{
        val one = Calendar.getInstance()
        val two = Calendar.getInstance()
        one.timeInMillis = dateOne
        two.timeInMillis = dateTwo

        if (one.get(Calendar.YEAR) != two.get(Calendar.YEAR)) return false
        if (one.get(Calendar.MONTH) != two.get(Calendar.MONTH)) return false
        if (one.get(Calendar.DAY_OF_MONTH) != two.get(Calendar.DAY_OF_MONTH)) return false
        return true
    }


    /***
     * Возвращает номер дня недели, где понедельник = 0, а воскресенье = 6
     */
    private fun getDayOfWeek(date: Long) : Int{
        val dateDate = Calendar.getInstance()
        dateDate.timeInMillis = date
        var dayOfWeek = dateDate.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SUNDAY) {
            dayOfWeek = 6
        } else {
            dayOfWeek -= 2
        }
        return dayOfWeek
    }

    fun getSchedule(scheduleId: String): Schedule{
        return BaseRepository(app).getSchedule(scheduleId)
    }

    fun deleteSchedule(scheduleId: String) {
        repository.deleteSchedule(scheduleId)
    }

}
