package brotifypacha.scheduler.add_change_fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import brotifypacha.scheduler.R
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.afterTextChanged
import brotifypacha.scheduler.data_models.ChangeModel
import brotifypacha.scheduler.database.Schedule
import brotifypacha.scheduler.database.SchedulerDataBase
import kotlinx.android.synthetic.main.fragment_add_change_modal.view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class AddChangeFragment: Fragment(){

    companion object{
        val ARG_SCHEDULE_ID = "schedule_id"
        fun newInstance(scheduleId: String) = AddChangeFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_SCHEDULE_ID, scheduleId)
            }
        }
    }

    val editListId = listOf(
        R.id.lesson_edit_1,
        R.id.lesson_edit_2,
        R.id.lesson_edit_3,
        R.id.lesson_edit_4,
        R.id.lesson_edit_5,
        R.id.lesson_edit_6,
        R.id.lesson_edit_7,
        R.id.lesson_edit_8,
        R.id.lesson_edit_9
    )
    val lessons: ArrayList<String> = arrayListOf("", "", "", "", "", "", "", "", "")
    var selectedDate = getTodayInMillis()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_add_change_modal, container, false)

        val db = SchedulerDataBase.getInstance(context!!)
        val scheduleId = arguments!!.getString(ARG_SCHEDULE_ID)

        editListId.forEachIndexed { id, it ->
            view.findViewById<EditText>(it).afterTextChanged {
                lessons[id] = it
            }
        }

        val datePicker = view.date_picker
        datePicker.setText(Utils.formatDate(getTodayInMillis()))

        datePicker.setOnClickListener {
            val todayCalendar = Calendar.getInstance()
            todayCalendar.timeInMillis = getTodayInMillis()
            val datePickerDialog = DatePickerDialog( context, R.style.AppTheme_DatePickerDialog, { view, year, month, dayOfMonth ->
                val changeDate = Calendar.getInstance()
                changeDate.set(year, month, dayOfMonth)
                selectedDate = changeDate.timeInMillis
                datePicker.setText(Utils.formatDate(selectedDate))
            }, todayCalendar.get(Calendar.YEAR), todayCalendar.get(Calendar.MONTH), todayCalendar.get(Calendar.DAY_OF_MONTH))

            datePickerDialog.datePicker.minDate = getTodayInMillis()
            datePickerDialog.show()
        }
        view.positive_button.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch {
                val scheduleBeforeEdit = db.getSchedulesDao().getSchedule(scheduleId)
                val changeList = ArrayList(scheduleBeforeEdit.getChangesAsList())
                changeList.add(ChangeModel(selectedDate, lessons))
                db.getSchedulesDao().update(scheduleBeforeEdit.copy(changes = Schedule.changesToStr(changeList)))
            }
            findNavController().popBackStack()
        }
        view.negative_buttton.setOnClickListener{
            findNavController().popBackStack()
        }
        return view
    }

    fun getTodayInMillis(): Long {
        return Calendar.getInstance().timeInMillis
    }

}