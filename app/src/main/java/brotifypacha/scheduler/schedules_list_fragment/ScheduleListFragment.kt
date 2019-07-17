package brotifypacha.scheduler.schedules_list_fragment

import android.content.Context
import android.net.Uri
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
import androidx.work.*
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
import brotifypacha.scheduler.Modals.SubscribeModal
import brotifypacha.scheduler.add_change_fragment.AddChangeFragment
import brotifypacha.scheduler.work_manager.CommitScheduleWorker
import brotifypacha.scheduler.work_manager.UnsubscribeWorker
import java.util.concurrent.TimeUnit

class ScheduleListFragment : Fragment() {

    val TAG = ScheduleListFragment::class.java.simpleName

    lateinit var activityViewModel: MainActivityViewModel

    companion object {
        fun newInstance() = ScheduleListFragment()
    }

    private lateinit var viewModel: ScheduleListViewModel
    private lateinit var bind: FragmentScheduleListBinding

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
        bind.refreshLayout.setOnRefreshListener {
            if (Utils.isAuthorizedWithToken(activity!!.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE))){
                viewModel.refreshScheduleList()
            } else {
                bind.refreshLayout.isRefreshing = false
            }
        }

        bind.newScheduleBtn.setOnClickListener {
            viewModel.onCreateNewScheduleClick()
        }
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(bind.bar)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val menuItem = menu.add(0, 0, 0, "Меню")
        menuItem.setIcon(R.drawable.ic_more_vert_black_24dp)
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> {
                val subscribeModal = SubscribeModal()
                subscribeModal.setOnSubscribedListener {
                    viewModel.refreshScheduleList()
                }
                subscribeModal.show(childFragmentManager, "subscribe")
            }
            0 -> {
                val isAuthWithToken  = Utils.isAuthorizedWithToken(context!!.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE))
                val contextMenuModal = ContextMenuModal.newInstance(listOf(
                    ContextMenuModal.ModalMenuItem(0, if (isAuthWithToken) "Выйти" else "Авторизоваться", R.drawable.ic_exit_to_app_black_24dp)
                ))
                contextMenuModal.setOnItemClickListener {
                    contextMenuModal.dismiss()
                    if (isAuthWithToken){
                        if (it == 0 ){
                            val confirmationModal = ConfirmationModal.newInstance("Вы уверены что хотите выйти?", "Выйти", "Остаться")
                            confirmationModal.setOnItemClickListener({
                                activityViewModel.setEventRequestAuth()
                                confirmationModal.dismiss()
                            },{
                                confirmationModal.dismiss()
                            })
                            confirmationModal.show(childFragmentManager, ConfirmationModal.FRAGMENT_TAG)
                        }
                    } else {
                        activityViewModel.setEventRequestAuth()
                    }
                }
                contextMenuModal.show(childFragmentManager, ContextMenuModal.FRAGMENT_TAG)
            }

        }
        return true
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
            bind.refreshLayout.isRefreshing = false
        })

        viewModel.getSchedulesLoadedEvent().observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.setSchedulesLoadedEventHandled()
                bind.refreshLayout.isRefreshing = false
            }
        })
        viewModel.getOnScheduleClickEvent().observe(viewLifecycleOwner, Observer { id ->
            if (id != null) {
                this.findNavController().navigate(R.id.viewScheduleFragment, Bundle().apply {
                    putString(ViewScheduleFragment.ARG_SCHEDULE_ID, id)
                })
                viewModel.setScheduleClickHandled()
            }
        })
        viewModel.getOnScheduleLongClickEvent().observe(viewLifecycleOwner, Observer { data ->
            if (data != null){
                if (data.isCreator){
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
                                findNavController().navigate(R.id.addChange, Bundle().apply {
                                    putString(AddChangeFragment.ARG_SCHEDULE_ID, data.scheduleId)
                                })
                            }
                            2 -> {
                                this.findNavController().navigate(R.id.editScheduleFragment, Bundle().apply {
                                    putString(EditScheduleFragment.ARG_SCHEDULE_ID, data.scheduleId)
                                })
                            }
                        }
                        bottomDialog.dismiss()
                    }
                    bottomDialog.show(childFragmentManager, ContextMenuModal.FRAGMENT_TAG)

                } else {
                    val bottomDialog = ContextMenuModal.newInstance(
                        listOf(
                            ContextMenuModal.ModalMenuItem(0, "Отписаться", R.drawable.ic_outline_delete_24px )
                        )
                    )
                    bottomDialog.setOnItemClickListener {
                        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<UnsubscribeWorker>()
                            .setConstraints(
                                Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                            )
                            .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                10,
                                TimeUnit.SECONDS
                            )
                            .setInputData(
                                Data.Builder()
                                .putString(UnsubscribeWorker.DATA_KEY_SCHEDULE_ID, data.scheduleId)
                                .build())
                            .build()
                        WorkManager.getInstance()
                            .beginUniqueWork("unsubscribing from ${data.scheduleId}", ExistingWorkPolicy.REPLACE, oneTimeWorkRequest)
                            .enqueue()
                        bottomDialog.dismiss()
                    }
                    bottomDialog.show(childFragmentManager, "unsubscribed_modal")
                }
                viewModel.setScheduleLongClickEventHandled()
            }
        })

        viewModel.getCreateNewScheduleEvent().observe(viewLifecycleOwner, Observer {
            if (it.clicked == false) return@Observer
            val createNewScheduleDialog = ManageScheduleDataModal.newInstance(it.authorizedWithToken, ManageScheduleDataModal.MODE_CREATE, null, null)
            createNewScheduleDialog.setOnNextButtonClickListener { name, alias ->
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
