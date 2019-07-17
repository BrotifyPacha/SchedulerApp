package brotifypacha.scheduler.schedules_list_fragment

import brotifypacha.scheduler.database.Schedule

class OnScheduleClickListener(val clickListener: (scheduleId: String) -> Unit){
    fun onClick(schedule: Schedule) = clickListener(schedule._id)
}
