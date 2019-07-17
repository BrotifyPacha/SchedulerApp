package brotifypacha.scheduler.confirmation_modal

import android.app.DatePickerDialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import brotifypacha.scheduler.Modals.ConfirmationModal
import brotifypacha.scheduler.R;
import brotifypacha.scheduler.Utils.Companion.formatDate
import kotlinx.android.synthetic.main.fragment_pick_first_day_modal.view.*
import java.text.SimpleDateFormat
import java.util.*

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
class FirstDayPickerModal : BottomSheetDialogFragment() {

    val TAG = ConfirmationModal::class.java.simpleName
    private var mListener: OnDatePickedListener? = null

    companion object {
        val FRAGMENT_TAG = "confirmation_modal"
        val ARG_IS_FORCED = "is_forced"

        fun newInstance(isForced: Boolean): FirstDayPickerModal =
            FirstDayPickerModal().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_FORCED, isForced)
                }
            }
    }

    private var pickedDay: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pick_first_day_modal, container, false)

        if (arguments!!.getBoolean(ARG_IS_FORCED)){
            view.helper.text = "Ваше расписание состоит из нескольких повторяющихся недель, для его корректного отображения требуется выбрать первый день первой недели (Обычно это первое сентября)"
            view.cancel_button.visibility = View.GONE
        }
        else {
            view.helper.text = "Этот параметр требуется для корректного отображения расписаний, имеющих несколько повторяющихся недель (пример такой даты - первое сентября)"
        }
        val nowDate = Calendar.getInstance()

        val datePickerText = view.date_picker
        datePickerText.setText(formatDate(nowDate.timeInMillis))
        datePickerText.setOnClickListener {
            DatePickerDialog(context, R.style.AppTheme_DatePickerDialog, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)
                datePickerText.setText(formatDate(calendar.timeInMillis))
                pickedDay = calendar.timeInMillis
            }, nowDate.get(Calendar.YEAR), nowDate.get(Calendar.MONTH), nowDate.get(Calendar.DAY_OF_MONTH)).show()

        }
        view.done_button.setOnClickListener {
            if (pickedDay != null){
                if (nowDate.after(pickedDay)) {
                    Toast.makeText(context, "Выберите дату из прошлого", Toast.LENGTH_LONG).show()
                } else if (mListener != null){
                    (mListener as OnDatePickedListener).onDatePicked(pickedDay!!)
                }
            } else {
                Toast.makeText(context, "Для продолжения, выберите дату", Toast.LENGTH_LONG).show()
            }
        }
        view.cancel_button.setOnClickListener {
            dismiss()
        }
        return view
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    class OnDatePickedListener(val listener: (date: Long) -> Unit) {
        fun onDatePicked(date: Long) = listener(date)
    }

    fun setOnItemClickListener(listener: (date: Long) -> Unit){
        mListener = OnDatePickedListener(listener)
    }


}
