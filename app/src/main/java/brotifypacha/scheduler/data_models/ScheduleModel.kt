package brotifypacha.scheduler.data_models

import org.json.JSONArray


data class ScheduleModel (
    val _id: String,
    val name: String,
    val alias: String,
    val first_day: Long,
    val creator: String,
    val subscribed_users: List<UserModel>,
    val schedule: List<List<List<String>>>,
    val changes: List<ChangeModel>
) {

}

