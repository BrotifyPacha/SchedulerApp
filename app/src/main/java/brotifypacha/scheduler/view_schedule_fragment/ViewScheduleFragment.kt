package brotifypacha.scheduler.view_schedule_fragment

import android.app.DatePickerDialog
import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.R
import brotifypacha.scheduler.Modals.ConfirmationModal
import brotifypacha.scheduler.Modals.ContextMenuModal
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.add_change_fragment.AddChangeFragment
import brotifypacha.scheduler.databinding.FragmentViewEditScheduleBinding
import brotifypacha.scheduler.edit_schedule_fragment.EditScheduleFragment
import brotifypacha.scheduler.main_activity.MainActivityViewModel
import java.util.*


class ViewScheduleFragment : Fragment() {

    val TAG = ViewScheduleFragment::class.java.simpleName

    companion object {
        val ARG_SCHEDULE_ID = "schedule_id"

        fun newInstance(id: String) = ViewScheduleFragment().apply {
            arguments = Bundle().apply { putString(ARG_SCHEDULE_ID, id) }
        }

    }

    private lateinit var activityViewModel: MainActivityViewModel
    private lateinit var viewModel: ViewScheduleViewModel
    private lateinit var bind: FragmentViewEditScheduleBinding

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        setHasOptionsMenu(true)
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_view_edit_schedule, container, false)
        (activity!! as AppCompatActivity).setSupportActionBar(bind.appbar)
        bind.title = "Расписание"
        (bind.root as SwipeRefreshLayout).isEnabled = false
        bind.appbar.setNavigationIcon(R.drawable.ic_outline_event_24px)

        return bind.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val refresh = menu.add(0, 0, 0, "Выбрать текущий день")
        refresh.setIcon(R.drawable.ic_refresh_black_24dp)
        refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        val menuItem = menu.add(0, 1, 1, "Меню")
        menuItem.setIcon(R.drawable.ic_more_vert_black_24dp)
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = viewModel.selectedWeekDate
                DatePickerDialog(
                    context!!,
                    R.style.AppTheme_DatePickerDialog,
                    DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        viewModel.setSelectedWeekByDate(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            0 -> {
                (bind.root as SwipeRefreshLayout).isEnabled = true
                (bind.root as SwipeRefreshLayout).isRefreshing = true
                viewModel.refreshSchedule()
            }
            1 -> {
                viewModel.onMenuClickEvent()
            }
        }
        return true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val id = arguments!!.getString(ARG_SCHEDULE_ID)!!
        viewModel = ViewModelProviders.of(this, ViewScheduleViewModel.Factory(id, activity!!.application)).get(ViewScheduleViewModel::class.java)
        activityViewModel = ViewModelProviders.of(activity!!).get(MainActivityViewModel::class.java)

        bind.viewPager.adapter = WeekViewPagerAdapter(childFragmentManager)

        viewModel.getErrorEvent().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                activityViewModel.setErrorEvent(it)
                viewModel.setErrorEventHandled()
            }
        })

        viewModel.getRefreshedEvent().observe(viewLifecycleOwner, Observer{
            if (it){
                viewModel.setRefreshedEventHandled()
                (bind.root as SwipeRefreshLayout).isRefreshing = false
                (bind.root as SwipeRefreshLayout).isEnabled = false
            }
        })

        viewModel.getScheduleLiveData().observe(viewLifecycleOwner, Observer { schedule ->
            bind.title = schedule.name
            Log.d(TAG, "${viewModel.selectedWeekDate == -1.toLong()}")
            viewModel.setSelectedWeekByDate(Calendar.getInstance().timeInMillis)
        })
        viewModel.getOnWeekSelectedEvent().observe(viewLifecycleOwner, Observer {
            bind.tabLayout.setupWithViewPager(bind.viewPager, true)
            bind.viewPager.setCurrentItem(it.currentDay)

        })
        viewModel.getOnMenuClickEvent().observe(viewLifecycleOwner, Observer { data ->
            if (data != null){
                val bottomDialog = ContextMenuModal.newInstance(
                        listOf(
                                ContextMenuModal.ModalMenuItem(0, "Удалить", R.drawable.ic_outline_delete_24px ),
                                ContextMenuModal.ModalMenuItem(1, "Изменение на дату", R.drawable.ic_outline_event_24px),
                                ContextMenuModal.ModalMenuItem(2, "Редактировать", R.drawable.ic_outline_edit_24px )
                        )
                )
                bottomDialog.setOnItemClickListener {menuItemId ->
                    when (menuItemId){
                        0 -> {
                            bottomDialog.dismiss()
                            val scheduleName = viewModel.getSchedule(data.scheduleId).name
                            Log.d(TAG, scheduleName)
                            val confirmationModal = ConfirmationModal.newInstance(
                                    "Вы уверены что хотите удалить расписание \"$scheduleName\"",
                                    "Удалить",
                                    "Отмена")
                            confirmationModal.setOnItemClickListener(
                                    positive = {
                                        viewModel.deleteSchedule(data.scheduleId)
                                        findNavController().popBackStack()
                                        confirmationModal.dismiss()
                                    }, negative = {
                                confirmationModal.dismiss()
                            })
                            confirmationModal.show(childFragmentManager, ConfirmationModal.FRAGMENT_TAG)
                        }
                        1 -> {
                            findNavController().navigate(R.id.addChange, Bundle().apply {
                                putString(AddChangeFragment.ARG_SCHEDULE_ID, data.scheduleId)
                            })
                            bottomDialog.dismiss()
                        }
                        2 -> {
                            this.findNavController().navigate(R.id.editScheduleFragment, Bundle().apply {
                                putString(EditScheduleFragment.ARG_SCHEDULE_ID, data.scheduleId)
                            })
                            bottomDialog.dismiss()
                        }
                    }
                }
                bottomDialog.show(childFragmentManager, "context_menu")
                viewModel.setOnMenuClickEventHandled()
            }
        })
    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG, "refreshing")
        viewModel.refreshSchedule()
    }
}
