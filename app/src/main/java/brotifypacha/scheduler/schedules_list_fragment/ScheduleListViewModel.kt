package brotifypacha.scheduler.schedules_list_fragment

import android.app.Application
import androidx.lifecycle.*
import brotifypacha.scheduler.BaseRepository
import brotifypacha.scheduler.data_models.ResultModel
import brotifypacha.scheduler.database.Schedule

class ScheduleListViewModel(private val app: Application) : AndroidViewModel(app) {

    val TAG = ScheduleListViewModel::class.java.simpleName


    private var repository: ScheduleListRepository
    private var scheduleList = MediatorLiveData<List<Schedule>>()
    private val eventSchedulesLoaded = MutableLiveData<Boolean>()
    private val eventScheduleClicked = MutableLiveData<String>()
    private val eventCreateNewScheduleClicked = MutableLiveData<CreateNewScheduleData>()
    private val eventScheduleLongClicked = MutableLiveData<ScheduleLongClickEventData>()
    val createNewScheduleResponseLiveData = MutableLiveData<ResultModel<Unit>>()
    private lateinit var query : LiveData<List<Schedule>>

    private val errorEvent = MutableLiveData<String>()

    data class ScheduleLongClickEventData(val scheduleId: String)


    init {
        repository = ScheduleListRepository(app, viewModelScope, errorEvent)
        refreshScheduleList()
    }

    fun refreshScheduleList(){
        if (::query.isInitialized) scheduleList.removeSource(query)
        query = repository.getSchedules()
        scheduleList.addSource(query, {
            scheduleList.value = it
        })
        eventSchedulesLoaded.value = true
    }

    fun getErrorEvent() : LiveData<String>{
        return errorEvent
    }

    fun getSchedule(scheduleId: String): Schedule{
        return BaseRepository(app).getSchedule(scheduleId)
    }

    fun deleteSchedule(scheduleId: String){
        repository.deleteSchedule(scheduleId)
    }

    fun getOnScheduleClickEvent() : LiveData<String>{
        return eventScheduleClicked
    }
    fun onScheduleClick(id: String) {
        eventScheduleClicked.value = id
    }
    fun setScheduleClickHandled(){
        eventScheduleClicked.value = null
    }

    fun getSchedulesLiveData() : LiveData<List<Schedule>>{
        return scheduleList
    }

    fun getSchedulesLoadedEvent(): LiveData<Boolean>{
        return eventSchedulesLoaded
    }

    fun setSchedulesLoadedEventHandled(){
        eventSchedulesLoaded.value = false
    }


    fun getOnScheduleLongClickEvent(): LiveData<ScheduleLongClickEventData>{
        return eventScheduleLongClicked
    }

    fun onScheduleLongClick(id: String) {
        eventScheduleLongClicked.value = ScheduleLongClickEventData(id)
    }

    fun setScheduleLongClickEventHandled(){
        eventScheduleLongClicked.value = null
    }

    data class CreateNewScheduleData(val clicked: Boolean)

    fun getCreateNewScheduleEvent() : LiveData<CreateNewScheduleData>{
        return eventCreateNewScheduleClicked
    }
    fun onCreateNewScheduleClick() {
        eventCreateNewScheduleClicked.value = CreateNewScheduleData(true)
    }
    fun setCreateNewScheduleEventHandled() {
        eventCreateNewScheduleClicked.value = CreateNewScheduleData(false)
    }

    fun createNewSchedule(name: String) {
        repository.createNewSchedule(name, createNewScheduleResponseLiveData)
    }

}
