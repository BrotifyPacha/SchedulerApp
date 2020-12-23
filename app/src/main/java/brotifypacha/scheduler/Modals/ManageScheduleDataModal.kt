package brotifypacha.scheduler.Modals

import android.graphics.Color
import android.os.Bundle
import android.view.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import brotifypacha.scheduler.AnimUtils.Companion.animateViewWiggle
import brotifypacha.scheduler.R;
import brotifypacha.scheduler.databinding.FragmentCreateScheduleModalBinding
import brotifypacha.scheduler.edit_schedule_fragment.EditScheduleViewModel
import brotifypacha.scheduler.schedules_list_fragment.ScheduleListViewModel

const val ARG_ITEMS = "item_count"

class ManageScheduleDataModal : BottomSheetDialogFragment() {

    val TAG = ManageScheduleDataModal::class.java.simpleName

    interface ManageScheduleInterface{
        data class NameVerificationCode(val code: Int){
            companion object {
                val VERIFICATION_HANDLED = 0
                val SUCCESS = 1
                val ERROR_NAME_LENGTH = 2
                val ERROR_NAME_NOT_UNIQUE = 3
            }
        }
        fun getNameVerifiedEvent() : LiveData<NameVerificationCode>
        fun verifyName(name: String)
    }

    companion object {
        val FRAGMENT_TAG = "create_schedule_modal"
        val ARG_MODE = "mode"
        val ARG_NAME = "name"
        val MODE_CREATE = 0
        val MODE_EDIT = 1

        fun newInstance(mode: Int, name: String?): ManageScheduleDataModal =
            ManageScheduleDataModal().apply {
                arguments  = Bundle().apply {
                    putInt(ARG_MODE, mode)
                    if (mode == MODE_EDIT){
                        putString(ARG_NAME, name)
                    }
                }
            }
    }

    private lateinit var binding: FragmentCreateScheduleModalBinding
    private var mode: Int? = null

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mode = arguments!!.getInt(
            ARG_MODE,
            MODE_CREATE
        )
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_schedule_modal, container, false)
        val parentViewModel: ViewModel
        when (mode){
            MODE_CREATE -> {
                parentViewModel = ViewModelProviders.of(parentFragment!!).get(ScheduleListViewModel::class.java)
            }
            else -> {
                parentViewModel = ViewModelProviders.of(parentFragment!!).get(EditScheduleViewModel::class.java)
                binding.nameEdit.setText(arguments!!.getString(ARG_NAME))
            }
        }

        binding.nameEdit.requestFocus()
        (binding.root).setBackgroundColor(Color.TRANSPARENT)
        binding.doneButton.setOnClickListener {
            val name =  binding.nameEdit.text.toString()
            (parentViewModel as ManageScheduleInterface).verifyName(name)
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val parentViewModel: ViewModel
        if (mode!! == MODE_CREATE){
            parentViewModel = ViewModelProviders.of(parentFragment!!).get(ScheduleListViewModel::class.java)
        } else {
            parentViewModel = ViewModelProviders.of(parentFragment!!).get(EditScheduleViewModel::class.java)
        }
        (parentViewModel as ManageScheduleInterface).getNameVerifiedEvent().observe(viewLifecycleOwner, Observer {
            when (it.code) {
                ManageScheduleInterface.NameVerificationCode.ERROR_NAME_LENGTH -> {
                    animateViewWiggle(binding.nameLayout)
                    binding.footer.requestFocus()
                    binding.nameLayout.helperText = "Название расписания должно содержать как минимум одну букву"
                }
                ManageScheduleInterface.NameVerificationCode.ERROR_NAME_NOT_UNIQUE -> {
                    animateViewWiggle(binding.nameLayout)
                    binding.nameLayout.helperText = "У вас уже есть расписание с таким же названием"
                }
                ManageScheduleInterface.NameVerificationCode.SUCCESS -> {
                    dismiss()
                }
            }
        })
    }
}
