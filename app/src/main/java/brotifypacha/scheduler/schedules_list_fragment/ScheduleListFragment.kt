package brotifypacha.scheduler.schedules_list_fragment

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import brotifypacha.scheduler.Constants

import brotifypacha.scheduler.R
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.Modals.ConfirmationModal
import brotifypacha.scheduler.Modals.ContextMenuModal
import brotifypacha.scheduler.view_schedule_fragment.ViewScheduleFragment
import brotifypacha.scheduler.databinding.FragmentScheduleListBinding
import brotifypacha.scheduler.edit_schedule_fragment.EditScheduleFragment
import brotifypacha.scheduler.main_activity.MainActivityViewModel
import brotifypacha.scheduler.Modals.ManageScheduleDataModal
import brotifypacha.scheduler.add_change_fragment.AddChangeFragment

class ScheduleListFragment : Fragment() {

    val TAG = ScheduleListFragment::class.java.simpleName

    companion object {
        fun newInstance() = ScheduleListFragment()
    }

    private lateinit var viewModel: ScheduleListViewModel
    private lateinit var bind: FragmentScheduleListBinding
    lateinit var activityViewModel: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule_list, container, false)

        bind.recycler.layoutManager = LinearLayoutManager(inflater.context)
        bind.recycler.setHasFixedSize(true)
        bind.recycler.adapter = ScheduleListAdapter(
            OnScheduleClickListener {id ->
                viewModel.onScheduleClick(id)
            },
            OnScheduleLongClickListener { id ->
                viewModel.onScheduleLongClick(id)
                return@OnScheduleLongClickListener true

            })

        bind.newScheduleBtn.setOnClickListener {
            viewModel.onCreateNewScheduleClick()
        }
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(bind.bar)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activityViewModel = ViewModelProviders.of(activity!!).get(MainActivityViewModel::class.java)
        viewModel = ViewModelProviders.of(this).get(ScheduleListViewModel::class.java)

        viewModel.getErrorEvent().observe(viewLifecycleOwner, Observer {
            activityViewModel.setErrorEvent(it)
        })

        viewModel.getSchedulesLiveData().observe(viewLifecycleOwner, Observer {
            (bind.recycler.adapter as ScheduleListAdapter).setSchedules(it)
        })

        viewModel.getSchedulesLoadedEvent().observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.setSchedulesLoadedEventHandled()
            }
        })
        viewModel.getOnScheduleClickEvent().observe(viewLifecycleOwner, Observer { id ->
            if (id != null) {
                findNavController().navigate(R.id.action_view_schedule, Bundle().apply {
                    putString(ViewScheduleFragment.ARG_SCHEDULE_ID, id)
                })
                viewModel.setScheduleClickHandled()
            }
        })
        viewModel.getOnScheduleLongClickEvent().observe(viewLifecycleOwner, Observer { data ->
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
                                        confirmationModal.dismiss()
                                    }, negative = {
                                confirmationModal.dismiss()
                            })
                            confirmationModal.show(childFragmentManager, ConfirmationModal.FRAGMENT_TAG)
                        }
                        1 -> {
                            findNavController().navigate(R.id.action_add_change, Bundle().apply {
                                putString(AddChangeFragment.ARG_SCHEDULE_ID, data.scheduleId)
                            })
                        }
                        2 -> {
                            findNavController().navigate(R.id.action_edit_schedule, Bundle().apply {
                                putString(EditScheduleFragment.ARG_SCHEDULE_ID, data.scheduleId)
                            })
                        }
                    }
                    bottomDialog.dismiss()
                }
                bottomDialog.show(childFragmentManager, ContextMenuModal.FRAGMENT_TAG)
                viewModel.setScheduleLongClickEventHandled()
            }
        })

        viewModel.getCreateNewScheduleEvent().observe(viewLifecycleOwner, Observer {
            if (it.clicked == false) return@Observer
            val createNewScheduleDialog = ManageScheduleDataModal.newInstance(ManageScheduleDataModal.MODE_CREATE, null)
            createNewScheduleDialog.setOnNextButtonClickListener { name ->
                createNewScheduleDialog.dismiss()
                viewModel.refreshScheduleList()
            }
            createNewScheduleDialog.show(childFragmentManager, "create_new_schedule_dialog")
            viewModel.setCreateNewScheduleEventHandled()
        })

    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshScheduleList()
    }


}
