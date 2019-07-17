package brotifypacha.scheduler.edit_schedule_fragment


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.*
import androidx.lifecycle.Observer

import brotifypacha.scheduler.R
import brotifypacha.scheduler.afterTextChanged
import brotifypacha.scheduler.database.Schedule
import com.google.gson.Gson
import org.json.JSONArray
import kotlin.collections.ArrayList

private const val ARG_DAY_OF_WEEK = "day_of_week"
private const val ARG_SCHEDULE_ID = "schedule_id"

class EditDayFragment : Fragment() {

    val TAG = EditDayFragment::class.java.simpleName

    private var dayOfweek: Int? = null

    companion object {
        @JvmStatic
        fun newInstance(dayOfWeek: Int) =
            EditDayFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_DAY_OF_WEEK, dayOfWeek)
                }
            }
    }

    private lateinit var parentViewModel: EditScheduleViewModel
    private lateinit var editList: List<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dayOfweek = it.getInt(ARG_DAY_OF_WEEK)
        }
    }

    var manualyUpdating = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_day, container, false)

        editList = listOf(
            view.findViewById(R.id.lesson_edit_1),
            view.findViewById(R.id.lesson_edit_2),
            view.findViewById(R.id.lesson_edit_3),
            view.findViewById(R.id.lesson_edit_4),
            view.findViewById(R.id.lesson_edit_5),
            view.findViewById(R.id.lesson_edit_6),
            view.findViewById(R.id.lesson_edit_7),
            view.findViewById(R.id.lesson_edit_8),
            view.findViewById(R.id.lesson_edit_9)
        )
        editList.forEachIndexed { i, edit ->
            edit.afterTextChanged {
                if (edit.hasFocus()) {
                    val scheduleArrayList = scheduleToArrayList(parentViewModel.editedSchedule)
                    scheduleArrayList[parentViewModel.currentWeekLiveData.value!!][dayOfweek!!][i] = it
                    parentViewModel.editedSchedule =
                        parentViewModel.editedSchedule.copy(schedule = Schedule.scheduleToStr(scheduleArrayList))
                }
            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        parentViewModel = ViewModelProviders.of(parentFragment!!).get(EditScheduleViewModel::class.java)
        parentViewModel.getEventPasteData().observe(viewLifecycleOwner, Observer { pendingUpdateList ->
            if (pendingUpdateList[dayOfweek!!]){
                updateEdittexts(parentViewModel.editedSchedule.getScheduleAsList(), parentViewModel.currentWeekLiveData.value!! )
                Log.d( TAG , pendingUpdateList.toString())
                parentViewModel.setEventPasteDataHandled(dayOfweek!!)
            }
        })
        parentViewModel.getCurrentWeekLiveData().observe(viewLifecycleOwner, Observer {
            updateEdittexts(parentViewModel.editedSchedule.getScheduleAsList(), it)
        })



    }

    override fun onResume() {
        super.onResume()
        updateEdittexts(parentViewModel.editedSchedule.getScheduleAsList(), parentViewModel.currentWeekLiveData.value!!)
    }


    fun updateEdittexts(schedule: ArrayList<ArrayList<ArrayList<String>>>, currentWeek: Int){
        Log.d(TAG, "updating edittexts")
        editList.forEachIndexed { index, editText ->
            editText.clearFocus()
            editText.setText(schedule[currentWeek][dayOfweek!!][index])
        }
        editList[0].requestFocus()
    }

    fun scheduleToArrayList(schedule: Schedule): ArrayList<ArrayList<ArrayList<String>>>{
        val scheduleArrayList = ArrayList<ArrayList<ArrayList<String>>>()
        schedule.getScheduleAsList().forEach { weekList ->
            val weekArrayList = ArrayList<ArrayList<String>>()
            for (day in weekList){
                weekArrayList.add(ArrayList(day))
            }
            scheduleArrayList.add(weekArrayList)
        }
        return scheduleArrayList
    }

    fun currentDayToString(): String{
        val day = ArrayList<String>()
        for (edit in editList){
            day.add(edit.text.toString())
        }
        return Gson().toJson(day)
    }


}
