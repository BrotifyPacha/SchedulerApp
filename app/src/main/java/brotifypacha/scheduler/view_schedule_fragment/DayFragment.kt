package brotifypacha.scheduler.view_schedule_fragment


import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import brotifypacha.scheduler.AnimUtils.Companion.animateTextViewChange
import brotifypacha.scheduler.R
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.databinding.FragmentDayBinding
import java.util.*

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
        return bind.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val parentViewModel = ViewModelProviders.of(parentFragment!!).get(ViewScheduleViewModel::class.java)
        val dayOfWeek: Int = arguments!!.getInt(ARG_NUMBER)

        parentViewModel.getOnWeekSelectedEvent().observe(viewLifecycleOwner, Observer {
            val dayData = it.currentWeek[dayOfWeek]
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = dayData.date

            bind.date.animateTextViewChange(Utils.formatDate(calendar.timeInMillis))
            if (DateUtils.isToday(dayData.date)) {
                bind.date.setTextColor(ContextCompat.getColor(context!!, R.color.primaryColor))
            }
            val nameTextViews = listOf(bind.lessonName1, bind.lessonName2, bind.lessonName3, bind.lessonName4, bind.lessonName5, bind.lessonName6, bind.lessonName7, bind.lessonName8, bind.lessonName9)
            for (i in 0..8) {
                nameTextViews[i].animateTextViewChange(dayData.lessons[i])
            }
        })
    }

    data class DayFragmentData(val date: Long, val currentWeek: List<List<String>>)
}
