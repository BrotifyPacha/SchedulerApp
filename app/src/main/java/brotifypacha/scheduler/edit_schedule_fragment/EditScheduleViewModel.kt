package brotifypacha.scheduler.edit_schedule_fragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import brotifypacha.scheduler.BaseRepository
import brotifypacha.scheduler.Modals.ManageScheduleDataModal.ManageScheduleInterface
import brotifypacha.scheduler.Modals.ManageScheduleDataModal.ManageScheduleInterface.NameVerificationCode
import brotifypacha.scheduler.database.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class EditScheduleViewModel(val app: Application, val scheduleId: String) : AndroidViewModel(app), ManageScheduleInterface {

    class Factory(val app: Application, val scheduleId: String): ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EditScheduleViewModel(app, scheduleId) as T
        }

    }

    val TAG = EditScheduleViewModel::class.java.simpleName
    private val baseRepository: BaseRepository
    private val repository: EditScheduleRepository

    private val errorEvent = MutableLiveData<String>()
    private val onMenuClickEvent = MutableLiveData<Boolean>()
    private val onSaveChangesEvent = MutableLiveData<Boolean>()
    private val eventPasteData = MutableLiveData<ArrayList<Boolean>>() // ArrayList of days that updated their view
    private val eventOnWeekCountChange = MutableLiveData<Int>()
    private val eventNameVerified = MutableLiveData<ManageScheduleInterface.NameVerificationCode>()
    private val eventRequireFirstDaySet = MutableLiveData<Boolean>()
    var editedSchedule: Schedule
    var schedule: Schedule
    val currentWeekLiveData = MutableLiveData<Int>()

    data class CopiedData(val type: Int, val data: Any){
        companion object {
            val DATA_TYPE_DAY = 0
            val DATA_TYPE_WEEK = 1
        }
    }
    var copiedData: CopiedData? = null

    init {
        baseRepository = BaseRepository(app)
        repository = EditScheduleRepository(app, viewModelScope, errorEvent)
        schedule = BaseRepository(app).getSchedule(scheduleId)
        editedSchedule = schedule
        currentWeekLiveData.value = 0
        eventOnWeekCountChange.value = editedSchedule.getScheduleAsList().size
        Log.d(TAG, "viewModel initiated")
    }

    fun onCopyCurrentDay(day: Int) {
        val dayStr = editedSchedule.getScheduleAsList()[currentWeekLiveData.value!!][day]
        copiedData = CopiedData(CopiedData.DATA_TYPE_DAY, dayStr)
    }
    fun onCopyCurrentWeek() {
        val currentWeekStr = editedSchedule.getScheduleAsList()[currentWeekLiveData.value!!]
        copiedData = CopiedData(CopiedData.DATA_TYPE_WEEK, currentWeekStr)
    }

    fun onPasteData(day: Int) {
        val alteredSchedule = editedSchedule.getScheduleAsList()
        if (copiedData!!.type == CopiedData.DATA_TYPE_DAY){
            alteredSchedule[currentWeekLiveData.value!!][day] = copiedData!!.data as ArrayList<String>
        } else {
            alteredSchedule[currentWeekLiveData.value!!] = copiedData!!.data as ArrayList<ArrayList<String>>
        }
        editedSchedule = editedSchedule.copy(schedule = Schedule.scheduleToStr(alteredSchedule))
        if (copiedData!!.type == CopiedData.DATA_TYPE_DAY){
            val pendingUpdateList = arrayListOf(false, false, false, false, false, false, false)
            pendingUpdateList[day] = true
            eventPasteData.value = pendingUpdateList
        } else {
            eventPasteData.value  = arrayListOf(true, true, true, true, true, true, true)
        }

    }
    fun getEventPasteData(): LiveData<ArrayList<Boolean>>{
        return eventPasteData
    }
    fun setEventPasteDataHandled(day: Int){
        val updated = eventPasteData.value!!
        updated[day] = false
        eventPasteData.value = updated
    }

    fun onAddWeek() {
        val scheduleList = editedSchedule.getScheduleAsList()
        if (scheduleList.size > 4) return
        scheduleList.add(Schedule.getEmptyWeek())
        eventOnWeekCountChange.value = scheduleList.size

        editedSchedule = editedSchedule.copy(schedule = Schedule.scheduleToStr(scheduleList))
    }

    fun onRemoveWeek() {
        val scheduleList = editedSchedule.getScheduleAsList()
        if (scheduleList.size < 2) return
        scheduleList.removeAt(currentWeekLiveData.value!!)
        eventOnWeekCountChange.value = scheduleList.size
        editedSchedule = editedSchedule.copy(schedule = Schedule.scheduleToStr(scheduleList))
        if (currentWeekLiveData.value!! > 0)
            currentWeekLiveData.value = currentWeekLiveData.value!! -1
        else currentWeekLiveData.value = 0
    }

    fun getOnWeekCountChange(): LiveData<Int>{
        return eventOnWeekCountChange
    }

    fun onMenuClick(){
        onMenuClickEvent.value = true
    }
    fun getOnMenuClickEvent() : LiveData<Boolean>{
        return onMenuClickEvent
    }
    fun setOnMenuClickEventHandled() {
        onMenuClickEvent.value = false
    }

    fun onSaveChangesClick(){
        if (editedSchedule.getScheduleAsList().size > 1 && editedSchedule.firstDay < 1000){
            eventRequireFirstDaySet.value = true
        } else repository.saveChanges(scheduleId, editedSchedule, onSaveChangesEvent)
    }

    fun getCurrentWeekLiveData(): LiveData<Int>{
        return currentWeekLiveData
    }
    fun setCurrentWeekPlusOne() {
        if (currentWeekLiveData.value!! == editedSchedule.getScheduleAsList().size-1){
            currentWeekLiveData.value = 0
        } else currentWeekLiveData.value = currentWeekLiveData.value!! + 1
        Log.d(TAG, currentWeekLiveData.value.toString())
    }

    fun getOnSavedChangesEvent(): LiveData<Boolean>{
        return onSaveChangesEvent
    }
    fun setOnSavedChangesEventHandled(){
        onSaveChangesEvent.value = false
    }

    fun onFirstDaySet(date: Long) {
        editedSchedule = editedSchedule.copy(firstDay = date)
    }

    fun getEventRequireFirstDaySet(): LiveData<Boolean> {
        return eventRequireFirstDaySet
    }
    fun setEventRequireFirstDaySetHandled(){
        eventRequireFirstDaySet.value = false
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
                        editedSchedule = editedSchedule.copy(name = name)
                    }
                }
            }
        }
    }

    fun isScheduleModified(): Boolean {
        if (schedule.name != editedSchedule.name) return true
        if (schedule.firstDay != editedSchedule.firstDay) return true
        if (schedule.schedule != editedSchedule.schedule) return true
        return false
    }


}
