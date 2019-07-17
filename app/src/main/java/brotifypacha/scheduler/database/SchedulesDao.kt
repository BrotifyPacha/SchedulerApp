package brotifypacha.scheduler.database

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
abstract class SchedulesDao{

    val TAG = SchedulesDao::class.java.simpleName

    @Ignore
    fun insert(schedule: Schedule){
        _insert(schedule)
    }
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun _insert(schedule: Schedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(schedule: List<Schedule>)

    @Update
    abstract fun update(schedule : Schedule)

    @Ignore
    fun deleteIfExists(schedule: Schedule?){
        if (schedule != null) delete(schedule)
    }

    @Delete
    abstract fun delete(
        schedule : Schedule
    )

    @Query("SELECT * FROM schedules_table WHERE _id = :id")
    abstract fun getScheduleLiveData(id: String) : LiveData<Schedule>


    @Query("SELECT * FROM schedules_table WHERE _id = :id")
    abstract fun getSchedule(id: String) : Schedule

    @Ignore
    fun getSubscribedSchedulesOfUser(id: String) : LiveData<List<Schedule>> {
        Log.d(TAG, "querying schedules user is subscribed to")
        return _getSubscribedSchedulesOfUser("%"+id+"%")
    }

    @Query("SELECT * FROM schedules_table WHERE subscribed_users LIKE :id")
    abstract fun _getSubscribedSchedulesOfUser(id: String) : LiveData<List<Schedule>>



}