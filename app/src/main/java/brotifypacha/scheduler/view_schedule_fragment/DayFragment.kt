package brotifypacha.scheduler.view_schedule_fragment


import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

import brotifypacha.scheduler.R
import brotifypacha.scheduler.databinding.FragmentDayBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_NUMBER = "lessons"

/**
 * A simple [Fragment] subclass.
 * Use the [DayFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class DayFragment : Fragment() {

    private lateinit var bind: FragmentDayBinding

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DayFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(dayOfWeek: Int) =
            DayFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_NUMBER, dayOfWeek )
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_day, container, false)
        bind.recycler.layoutManager = LinearLayoutManager(context)
        bind.recycler.adapter = LessonsAdapter(ArrayList())
        return bind.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val parentViewModel = ViewModelProviders.of(parentFragment!!).get(ViewScheduleViewModel::class.java)
        val dayOfWeek: Int = arguments!!.getInt(ARG_NUMBER)

        parentViewModel.getOnWeekSelectedEvent().observe(viewLifecycleOwner, Observer {

            val dayData = it.currentWeek[dayOfWeek]
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = dayData.date

            val format = SimpleDateFormat("dd.MM.yy")

            if (DateUtils.isToday(dayData.date)) {
                bind.date.setText(format.format(calendar.time)+" (сегодня)")
            } else {
                bind.date.setText(format.format(calendar.time))
            }
            (bind.recycler.adapter as LessonsAdapter).setData(dayData.lessons)
        })
        super.onActivityCreated(savedInstanceState)
    }

    data class DayFragmentData(val date: Long, val currentWeek: List<List<String>>)
}
