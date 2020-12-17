package brotifypacha.scheduler.edit_schedule_fragment

import android.animation.LayoutTransition
import android.app.DownloadManager
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import brotifypacha.scheduler.AnimUtils
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.Modals.ConfirmationModal

import brotifypacha.scheduler.R
import brotifypacha.scheduler.confirmation_modal.FirstDayPickerModal
import brotifypacha.scheduler.Modals.ContextMenuModal
import brotifypacha.scheduler.databinding.FragmentViewEditScheduleBinding
import brotifypacha.scheduler.Modals.ManageScheduleDataModal

class EditScheduleFragment : Fragment() {

    val TAG = EditScheduleFragment::class.java.simpleName

    companion object {
        val ARG_SCHEDULE_ID = "schedule_id"
        fun newInstance(scheduleId: String) = EditScheduleFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_SCHEDULE_ID, scheduleId)
            }
        }
    }

    private lateinit var viewModel: EditScheduleViewModel
    private lateinit var scheduleId: String
    private lateinit var bind: FragmentViewEditScheduleBinding
    private lateinit var currentWeekMenuItem: MenuItem
    private lateinit var save: MenuItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_view_edit_schedule, container, false)
        bind.title = "Редактирование расписания"

        (bind.root as SwipeRefreshLayout).isEnabled = false
        bind.appbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        bind.appbar.setNavigationOnClickListener {
            val confirmationModal = ConfirmationModal.newInstance("Вы уверены что хотите выйти не сохранив изменения?", "Выйти", "Отмена")
            confirmationModal.setOnItemClickListener({
                findNavController().popBackStack()
            }, {
                confirmationModal.dismiss()
            })
        }

        bind.viewPager.adapter = EditSchedulePagerAdapter(childFragmentManager)
        bind.tabLayout.setupWithViewPager(bind.viewPager)

        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(bind.appbar)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        currentWeekMenuItem = menu.add(0, 0, 0,"Выбранная неделя")
        currentWeekMenuItem.setIcon(R.drawable.ic_week_1)
        currentWeekMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        currentWeekMenuItem.setVisible(viewModel.editedSchedule.getScheduleAsList().size > 1)


        save = menu.add(0, 1, 1, "Вставить")
        save.setIcon(R.drawable.ic_content_paste_black_24dp)
        save.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        save.setVisible(false)

        val menuItem = menu.add(0, 2, 2, "Меню")
        menuItem.setIcon(R.drawable.ic_more_vert_black_24dp)
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> findNavController().popBackStack()
            2 ->  viewModel.onMenuClick()
            1 ->  viewModel.onPasteData(bind.tabLayout.selectedTabPosition)
            0 ->  viewModel.setCurrentWeekPlusOne()
        }
        return true
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        scheduleId = arguments!!.getString(ARG_SCHEDULE_ID)!!
        viewModel = ViewModelProviders.of(this, EditScheduleViewModel.Factory(activity!!.application, scheduleId)).get(EditScheduleViewModel::class.java)

        viewModel.getOnSavedChangesEvent().observe(viewLifecycleOwner, Observer {
            if (it){
                findNavController().popBackStack()
            }
        })
        viewModel.getOnMenuClickEvent().observe(viewLifecycleOwner, Observer {
            if (it){
                val itemList = arrayListOf(
                    ContextMenuModal.ModalMenuItem(0, "Изменить название или индентификатор", R.drawable.ic_text_fields_black_24dp)
                )
                if (viewModel.editedSchedule.getScheduleAsList().size > 1){
                    itemList.add(ContextMenuModal.ModalMenuItem(1, "Изменить дату начала первой недели", R.drawable.ic_outline_event_24px))
                }
                if (viewModel.editedSchedule.getScheduleAsList().size < 5){
                    itemList.add(ContextMenuModal.ModalMenuItem(2, "Добавить неделю", R.drawable.ic_plus_one_24px))
                }
                if (viewModel.editedSchedule.getScheduleAsList().size > 1){
                    itemList.add(ContextMenuModal.ModalMenuItem(3, "Удалить неделю", R.drawable.ic_minus_one_24px))
                }
                if (viewModel.editedSchedule.getScheduleAsList().size > 1){
                    itemList.add(ContextMenuModal.ModalMenuItem(4, "Скопировать неделю", R.drawable.ic_outline_file_copy_24px))
                }
                itemList.add(ContextMenuModal.ModalMenuItem(5, "Скопировать день", R.drawable.ic_content_copy_black_24dp))
                itemList.add(ContextMenuModal.ModalMenuItem(6, "Сохранить", R.drawable.ic_done_black_24dp))

                val contextMenuModal = ContextMenuModal.newInstance(itemList)
                contextMenuModal.setOnItemClickListener {
                    when (it){
                        0 -> showChangeNameOrAliasModal()
                        1 -> {
                            val modal = FirstDayPickerModal.newInstance(false)
                            modal.setOnItemClickListener{ date: Long ->
                                viewModel.onFirstDaySet(date)
                            }
                            modal.show(childFragmentManager, "tag")
                        }
                        2 -> viewModel.onAddWeek()
                        3 -> viewModel.onRemoveWeek()
                        4 -> {
                            viewModel.onCopyCurrentWeek()
                            Toast.makeText(context, "Неделя скопирована", Toast.LENGTH_SHORT).show()
                            save.setVisible(true)
                        }
                        5 -> {
                            viewModel.onCopyCurrentDay(bind.tabLayout.selectedTabPosition)
                            Toast.makeText(context, "День скопирован", Toast.LENGTH_SHORT).show()
                            save.setVisible(true)
                        }
                        6 -> viewModel.onSaveChangesClick()
                    }
                    contextMenuModal.dismiss()
                }
                contextMenuModal.show(childFragmentManager, ContextMenuModal.FRAGMENT_TAG)
            }
        })


        viewModel.getOnWeekCountChange().observe(viewLifecycleOwner, Observer {
            if (::currentWeekMenuItem.isInitialized)
                currentWeekMenuItem.setVisible(it > 1)

        })
        viewModel.getCurrentWeekLiveData().observe(viewLifecycleOwner, Observer {
            Log.d("SDADASD", "current week $it")
            if (::currentWeekMenuItem.isInitialized)
                currentWeekMenuItem.setIcon(
                when (it){
                    0 -> R.drawable.ic_week_1
                    1 -> R.drawable.ic_week_2
                    2 -> R.drawable.ic_week_3
                    3 -> R.drawable.ic_week_4
                    else -> R.drawable.ic_week_5
                })
        })

        viewModel.getEventRequireFirstDaySet().observe(viewLifecycleOwner, Observer {
            val modal = FirstDayPickerModal.newInstance(true)
            modal.setOnItemClickListener{ date: Long ->
                viewModel.onFirstDaySet(date)
                viewModel.onSaveChangesClick()
            }
            modal.show(childFragmentManager, "tag")
        })

    }


    private fun showChangeNameOrAliasModal() {

        val editScheduleModal = ManageScheduleDataModal.newInstance(
            ManageScheduleDataModal.MODE_EDIT,
            viewModel.editedSchedule.name
        )
        editScheduleModal.setOnNextButtonClickListener { name ->
            viewModel.onChangeNameOrAlias(name)
        }
        editScheduleModal.show(childFragmentManager, ManageScheduleDataModal.FRAGMENT_TAG)
    }

}
