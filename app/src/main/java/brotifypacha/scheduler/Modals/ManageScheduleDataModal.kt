package brotifypacha.scheduler.Modals

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import brotifypacha.scheduler.AnimUtils.Companion.animateViewWiggle
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.R;
import brotifypacha.scheduler.data_models.ResultModel
import brotifypacha.scheduler.databinding.FragmentCreateScheduleModalBinding
import brotifypacha.scheduler.edit_schedule_fragment.EditScheduleViewModel
import brotifypacha.scheduler.schedules_list_fragment.ScheduleListViewModel
import java.util.regex.Pattern

// TODO: Customize parameter argument names
const val ARG_ITEMS = "item_count"

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    ContextMenuModal.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 *
 * You activity (or fragment) needs to implement [ContextMenuModal.Listener].
 */

class ManageScheduleDataModal : BottomSheetDialogFragment() {

    val TAG = ManageScheduleDataModal::class.java.simpleName
    private var mListener: Listener? = null

    companion object {

        //TODO GOTTA REFACTOR THIS CLASS!
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

        binding.nextButton.setOnClickListener {
            if (mListener != null){
                if (evaluateName()){
                    val name =  binding.nameEdit.text.toString()
                    if (mode == MODE_CREATE) {
                        (parentViewModel as ScheduleListViewModel).createNewSchedule(name)
                    }
                }
            }
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        if (mode!! == MODE_CREATE){
            val parentViewModel = ViewModelProviders.of(parentFragment!!).get(ScheduleListViewModel::class.java)
            parentViewModel.createNewScheduleResponseLiveData.observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    if (it.result == ResultModel.CODE_SUCCESS) {
                        if (mListener != null) {
                            (mListener as Listener).onNextButtonClick(binding.nameEdit.text.toString())
                        }
                        parentViewModel.createNewScheduleResponseLiveData.value = null
                    }
                }
            })
        } else {
            val parentViewModel = ViewModelProviders.of(parentFragment!!).get(EditScheduleViewModel::class.java)
            parentViewModel.getEventAliasVerified().observe(viewLifecycleOwner, Observer {
                if (it != null){
                    when (it.result){
                        ResultModel.CODE_SUCCESS -> {
                            if (mListener != null){
                                (mListener as Listener).onNextButtonClick(
                                    binding.nameEdit.text.toString()
                                )
                                dismiss()
                            }
                        }
                    }
                    parentViewModel.setEventAliasVerifiedHandled()
                }
            })
        }
    }
    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    class Listener(val listener: (name: String) -> Unit) {
        fun onNextButtonClick(name: String) = listener(name)
    }
    fun setOnNextButtonClickListener(listener: (name: String) -> Unit){
        mListener = Listener(listener)
    }

    fun evaluateName() : Boolean {
        if (binding.nameEdit.text.length < 3) return false
        return true
    }


}
