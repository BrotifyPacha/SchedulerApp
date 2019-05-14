package brotifypacha.scheduler.data_models

data class ScheduleModel (
    val _id: String,
    val name: String,
    val alias: String,
    val availability: String,
    val first_day: Long,
    val creator: UserModel,
    val moderators: List<UserModel>,
    val invited_users: List<UserModel>,
    val subscribed_users: List<UserModel>,
    val schedule: List<List<List<String>>>,
    val changes: List<ChangeModel>
)

