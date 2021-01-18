package brotifypacha.scheduler.changes_list_fragment

import android.app.Application
import androidx.lifecycle.*
import brotifypacha.scheduler.BaseRepository
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.database.Change
import brotifypacha.scheduler.database.Schedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChangesListViewModel(val scheduleId: String, val app: Application) : AndroidViewModel(app) {

    class Factory(val schedule_id: String, val app: Application): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ChangesListViewModel(schedule_id, app) as T
        }
    }

    private val baseRepository: BaseRepository = BaseRepository(app)
    private val changes: MediatorLiveData<ArrayList<Change>> = MediatorLiveData()
    private val schedule = baseRepository.getScheduleLiveData(scheduleId)

    init {
        changes.addSource(schedule, Observer {
            changes.value = it.getChangesAsList()
        })
    }

    fun getChangesLiveData(): LiveData<ArrayList<Change>> {
        return changes
    }

    fun removeChange(date: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val scheduleBeforeEdit = baseRepository.db.getSchedulesDao().getSchedule(scheduleId)
            val changeList = ArrayList(scheduleBeforeEdit.getChangesAsList().filterNot{changeModel -> Utils.formatDate(changeModel.date) == date })
            changeList.sortBy { change ->  change.date}
            baseRepository.db.getSchedulesDao().update(scheduleBeforeEdit.copy(changes = Schedule.changesToStr(changeList)))
        }
    }

}