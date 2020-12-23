package brotifypacha.scheduler.schedules_list_fragment

import android.app.Application
import androidx.lifecycle.*
import brotifypacha.scheduler.BaseRepository
import brotifypacha.scheduler.Modals.ManageScheduleDataModal.ManageScheduleInterface
import brotifypacha.scheduler.Modals.ManageScheduleDataModal.ManageScheduleInterface.NameVerificationCode
import brotifypacha.scheduler.database.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScheduleListViewModel(private val app: Application) : AndroidViewModel(app), ManageScheduleInterface {

    val TAG = ScheduleListViewModel::class.java.simpleName


    private var baseRepository: BaseRepository
    private var repository: ScheduleListRepository
    private var scheduleList = MediatorLiveData<List<Schedule>>()
    private val eventSchedulesLoaded = MutableLiveData<Boolean>()
    private val eventScheduleClicked = MutableLiveData<String>()
    private val eventCreateNewScheduleClicked = MutableLiveData<CreateNewScheduleData>()
    private val eventScheduleLongClicked = MutableLiveData<ScheduleLongClickEventData>()
    private val eventNameVerified =  MutableLiveData<ManageScheduleInterface.NameVerificationCode>()
    private lateinit var query : LiveData<List<Schedule>>

    private val errorEvent = MutableLiveData<String>()

    data class ScheduleLongClickEventData(val scheduleId: String)


    init {
        repository = ScheduleListRepository(app, viewModelScope, errorEvent)
        baseRepository = BaseRepository(app)
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

    override fun getNameVerifiedEvent(): LiveData<NameVerificationCode> {
        return eventNameVerified
    }

    override fun verifyName(name: String) {
        if (name.isBlank()) {
            eventNameVerified.value = NameVerificationCode(NameVerificationCode.ERROR_NAME_LENGTH)
        } else {
            viewModelScope.launch(Dispatchers.IO){
                val schedule: Schedule? = baseRepository.findScheduleByName(name)
                withContext(Dispatchers.Main) {
                    if (schedule != null) eventNameVerified.value = NameVerificationCode(NameVerificationCode.ERROR_NAME_NOT_UNIQUE)
                    else {
                        eventNameVerified.value = NameVerificationCode(NameVerificationCode.SUCCESS)
                        repository.createNewSchedule(name)
                    }
                }
            }
        }
    }
}
