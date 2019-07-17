package brotifypacha.scheduler.database

import android.util.Log
import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import brotifypacha.scheduler.data_models.ChangeModel
import brotifypacha.scheduler.data_models.ScheduleModel
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "schedules_table")
data class Schedule(
    @PrimaryKey
    val _id : String,
    val alias : String,
    val name : String,
    val creator : String,
    @ColumnInfo(name = "subscribed_users")
    val subscribedUsers : String,
    @ColumnInfo(name = "first_day")
    val firstDay : Long,
    val schedule: String,
    val changes: String
){

    companion object {
        fun fromModel(model: ScheduleModel): Schedule {
            val subscribedUsersIds: ArrayList<String> = ArrayList()
            model.subscribed_users.forEach {
                subscribedUsersIds.add(it._id)
            }
            return Schedule(
                model._id,
                model.alias,
                model.name,
                model.creator,
                listToStr(subscribedUsersIds),
                model.first_day,
                scheduleToStr(model.schedule),
                changesToStr(model.changes)
            )
        }
        fun listToStr(list: List<String>): String{
            val listJson = Gson().toJson(list)
            return listJson.toString()
        }

        fun scheduleToStr(schedule: List<List<List<String>>>): String {
            val scheduleJson = Gson().toJson(schedule)
            return scheduleJson.toString()
        }

        fun changesToStr(changes: List<ChangeModel>): String{
            val changesJson = Gson().toJson(changes)
            return changesJson.toString()
        }

        fun getDefaultEmptyScheduleString() : String{
            val schedule = ArrayList<ArrayList<ArrayList<String>>>()
            schedule.add(getEmptyWeek())
            return scheduleToStr(schedule)
        }
        fun getEmptyWeek(): ArrayList<ArrayList<String>>{
            val week = ArrayList<ArrayList<String>>()
            for (i in 0..6) {
                week.add(getEmptyDay())
            }
            return week
        }
        fun getEmptyDay(): ArrayList<String>{
            return ArrayList<String>(listOf("", "", "", "", "", "", "", "", ""))
        }
        fun generateId() : String {
            val charpool = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            val randomString = (1..17)
                .map { i -> kotlin.random.Random.nextInt(0, charpool.length) }
                .map(charpool::get)
                .joinToString("");
            return randomString
        }
    }

    fun getScheduleAsList() : ArrayList<ArrayList<ArrayList<String>>> {
        val scheduleJson = JSONArray(schedule)
        val schedule: ArrayList<ArrayList<ArrayList<String>>> = ArrayList()
        for (w in 0..scheduleJson.length()-1){
            val weekJson = scheduleJson.getJSONArray(w)
            val week = ArrayList<ArrayList<String>>()
            for (d in 0..weekJson.length()-1){
                val dayJson = weekJson.getJSONArray(d)
                val day = ArrayList<String>()
                for (l in 0..dayJson.length()-1){
                    day.add(dayJson.getString(l))
                }
                week.add(day)
            }
            schedule.add(week)
        }
        return schedule
    }

    fun getChangesAsList() : List<ChangeModel>{
        val changesJson = JSONArray(changes)
        val changes: ArrayList<ChangeModel> = ArrayList()
        for (i in 0..changesJson.length()-1){
            val changeJson = changesJson.getJSONObject(i)
            val lessonsJson = changeJson.getJSONArray("change")
            val lessons: ArrayList<String> = ArrayList()
            for (l in 0..lessonsJson.length()-1){
                lessons.add(lessonsJson.getString(l))
            }
            val change = ChangeModel(changeJson.getLong("date"), lessons)
            changes.add(change)
        }
        return changes
    }

    fun toJsonString() : String{
        val jsonSerializer = Gson()
        return jsonSerializer.toJson(this)
    }

}