package brotifypacha.scheduler.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Schedule::class), version = 1, exportSchema = false)
abstract class SchedulerDataBase : RoomDatabase() {

    abstract fun getSchedulesDao() : SchedulesDao

    companion object {

        @Volatile
        private var INSTANCE: SchedulerDataBase? = null

        fun getInstance(context : Context) : SchedulerDataBase{
            synchronized(this){
                var instance = INSTANCE
                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SchedulerDataBase::class.java,
                        "scheduler_db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
                INSTANCE = instance
                return instance
            }
        }
    }

}