package brotifypacha.scheduler.database

import com.google.gson.Gson

data class Change(
        val date: Long,
        val change: List<String>
){
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
