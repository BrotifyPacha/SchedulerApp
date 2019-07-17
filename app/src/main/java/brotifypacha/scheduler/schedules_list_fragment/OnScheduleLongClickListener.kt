package brotifypacha.scheduler.schedules_list_fragment

import android.view.View
import brotifypacha.scheduler.database.Schedule

class OnScheduleLongClickListener(val clickListener: (String) -> Boolean){

    fun onClick(schedule: Schedule): Boolean = clickListener(schedule._id)
}

