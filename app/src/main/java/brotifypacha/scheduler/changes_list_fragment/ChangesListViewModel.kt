package brotifypacha.scheduler.changes_list_fragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import brotifypacha.scheduler.BaseRepository
import brotifypacha.scheduler.database.Change
import brotifypacha.scheduler.database.Schedule
import brotifypacha.scheduler.view_schedule_fragment.ViewScheduleViewModel

class ChangesListViewModel(val schedule_id: String, val app: Application) : AndroidViewModel(app) {

    class Factory(val schedule_id: String, val app: Application): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ChangesListViewModel(schedule_id, app) as T
        }
    }

    private val baseRepository: BaseRepository = BaseRepository(app)
    private val changes: MutableLiveData<ArrayList<Change>> = MutableLiveData()

    init {
        val schedule = baseRepository.getSchedule(schedule_id)
        changes.value = schedule.getChangesAsList()
    }

    fun getChangesLiveData(): LiveData<ArrayList<Change>> {
        return changes
    }

}