package brotifypacha.scheduler.view_schedule_fragment

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter

class WeekViewPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return 7
    }

    override fun getItem(position: Int): Fragment {
        return DayFragment.newInstance(position)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")[position]
    }


}