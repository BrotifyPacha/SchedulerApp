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

        val FRAGMENT_TAG = "create_schedule_modal"
        val ARG_AUTH_WITH_TOKEN = "auth_with_token"
        val ARG_MODE = "mode"
        val ARG_NAME = "name"
        val ARG_ALIAS = "alias"
        val MODE_CREATE = 0
        val MODE_EDIT = 1

        fun newInstance(authWithToken: Boolean, mode: Int, name: String?, alias: String?): ManageScheduleDataModal =
            ManageScheduleDataModal().apply {
                arguments  = Bundle().apply {
                    putBoolean(ARG_AUTH_WITH_TOKEN, authWithToken)
                    putInt(ARG_MODE, mode)
                    if (mode == MODE_EDIT){
                        putString(ARG_NAME, name)
                        putString(ARG_ALIAS, alias)

                    }
                }
            }
    }

    private lateinit var binding: FragmentCreateScheduleModalBinding
    private var mode: Int? = null

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val authWithToken = arguments!!.getBoolean(ARG_AUTH_WITH_TOKEN, false)
        mode = arguments!!.getInt(
            ARG_MODE,
            MODE_CREATE
        )

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_schedule_modal, container, false)

        val parentViewModel: ViewModel
        when (mode){
            MODE_CREATE -> {
                parentViewModel = ViewModelProviders.of(parentFragment!!).get(ScheduleListViewModel::class.java)
                binding.nextButton.text = "Далее"
            }
            else -> {
                parentViewModel = ViewModelProviders.of(parentFragment!!).get(EditScheduleViewModel::class.java)
                binding.nextButton.text = "Готово"
                binding.nameEdit.setText(arguments!!.getString(ARG_NAME))
                binding.aliasEdit.setText(arguments!!.getString(ARG_ALIAS))
            }
        }


        if (!authWithToken) {
            binding.aliasEditLayout.visibility = View.GONE
        }
        binding.nameEdit.requestFocus()
        (binding.root).setBackgroundColor(Color.TRANSPARENT)

        binding.nextButton.setOnClickListener {
            if (mListener != null){
                if (evaluateName().equals("cool") && evaluateAlias().equals("cool")){
                    val alias =  binding.aliasEdit.text.toString()
                    val name =  binding.nameEdit.text.toString()
                    if (mode == MODE_CREATE) {
                        (parentViewModel as ScheduleListViewModel).createNewSchedule(name, alias)
                    } else {
                        (parentViewModel as EditScheduleViewModel).verifyAlias(alias)
                    }
                }
                reactToNameError(evaluateName())
                reactToAliasError(evaluateAlias())
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
                    if (it.result == Constants.SUCCESS) {
                        if (mListener != null) {
                            (mListener as Listener).onNextButtonClick(
                                binding.nameEdit.text.toString(),
                                binding.aliasEdit.text.toString()
                            )
                        }
                    } else {
                        when (it.type){
                            "field" -> {
                                if (it.field == "name"){
                                    reactToNameError(it.description)
                                } else {
                                    reactToAliasError(it.description)
                                }
                                return@Observer
                            }
                            Constants.NETWORK_ERROR -> {
                                Toast.makeText(context, "Для создания нового расписания необходим доступ к сети", Toast.LENGTH_LONG).show()
                                dismiss()
                            }
                        }
                    }
                    parentViewModel.createNewScheduleResponseLiveData.value = null
                }
            })
        } else {
            val parentViewModel = ViewModelProviders.of(parentFragment!!).get(EditScheduleViewModel::class.java)
            parentViewModel.getEventAliasVerified().observe(viewLifecycleOwner, Observer {
                if (it != null){
                    when (it.result){
                        Constants.SUCCESS -> {
                            if (mListener != null){
                                (mListener as Listener).onNextButtonClick(
                                    binding.nameEdit.text.toString(),
                                    binding.aliasEdit.text.toString()
                                )
                                dismiss()
                            }
                        }
                        Constants.ERROR -> {
                            reactToAliasError("error_4")
                        }
                        Constants.NETWORK_ERROR -> {
                            Toast.makeText(context, "Для изменения этих полей требуется доступ к сети", Toast.LENGTH_LONG).show()
                            Log.e(TAG, "network error smh")
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

    class Listener(val listener: (name: String, alias: String) -> Unit) {
        fun onNextButtonClick(name: String, alias: String) = listener(name, alias)
    }
    fun setOnNextButtonClickListener(listener: (name: String, alias: String) -> Unit){
        mListener = Listener(listener)
    }


    fun evaluateName() : String {
        if (binding.nameEdit.text.length < 3) return "error_1"
        return "cool"
    }
    fun evaluateAlias() : String {
        val alias = binding.aliasEdit.text
        if (alias.length < 3 && alias.length > 0) return "error_1"

        val secondRuleBroken = Pattern.compile("[^A-Za-z0-9_.-]+")
            .matcher(binding.aliasEdit.text.toString())
            .matches()
        if (secondRuleBroken) return "error_2"
        val thirdRuleBroken = Pattern.compile("(.*\\.\\..*|.*--.*|.*__.*)")
            .matcher(binding.aliasEdit.text.toString())
            .matches()
        if (thirdRuleBroken) return "error_3"
        else return "cool"
    }

    fun reactToNameError(error: String){
        if (error.equals("error_1")){
            animateViewWiggle(binding.nameEditLayout)
        } else {
            binding.nameHelperText.text = "*обязательно к заполнению"
        }
    }
    fun reactToAliasError(error: String){
        if (!error.equals("cool")){
            when (error) {
                "error_1" -> {
                    binding.aliasHelperText.text = "При заполнении, поле должно иметь больше двух символов"
                }
                "error_2" -> {
                    binding.aliasHelperText.text = "Может состоять лишь из следующих символов A-z, 0-9, _ . -"
                }
                "error_3" -> {
                    binding.aliasHelperText.text = "Нельзя иметь более одной . - или _ подряд"
                }
                "error_4" -> {
                    binding.aliasHelperText.text = "Этот идентификатор уже занят"
                }
            }
            animateViewWiggle(binding.aliasEditLayout)
        } else {
            binding.aliasHelperText.text = "Оставьте пустым для автозаполнения"
        }
    }


}
