package brotifypacha.scheduler.data_models


data class UserModel (
    val _id: String,
    val username: String,
    val schedules: List<ScheduleModel>
)