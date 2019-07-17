package brotifypacha.scheduler.data_models

import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

data class ChangeModel(
    val date: Long,
    val change: List<String>
){
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
