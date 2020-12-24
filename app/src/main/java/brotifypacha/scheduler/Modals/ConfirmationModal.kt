package brotifypacha.scheduler.Modals

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import brotifypacha.scheduler.R;
import kotlinx.android.synthetic.main.fragment_confirmation_modal.view.*

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
class ConfirmationModal : BottomSheetDialogFragment() {

    val TAG = ConfirmationModal::class.java.simpleName
    private var mListener: ActionListener? = null

    companion object {
        val FRAGMENT_TAG = "confirmation_modal"
        val ARG_QUESTION = "question"
        val ARG_POSITIVE_LABEL = "positive_label"
        val ARG_NEGATIVE_LABEL = "negative_label"
        val ARG_NEUTRAL_LABEL = "neutral_label"

        fun newInstance(question: String, positiveLabel: String, negativeLabel: String, neutralLabel: String): ConfirmationModal =
            ConfirmationModal().apply {
                arguments = Bundle().apply {
                    putString(ARG_QUESTION, question)
                    putString(ARG_POSITIVE_LABEL, positiveLabel)
                    putString(ARG_NEGATIVE_LABEL, negativeLabel)
                    putString(ARG_NEUTRAL_LABEL, neutralLabel)
                }
            }
        fun newInstance(question: String, positiveLabel: String, negativeLabel: String): ConfirmationModal =
            ConfirmationModal().apply {
                arguments = Bundle().apply {
                    putString(ARG_QUESTION, question)
                    putString(ARG_POSITIVE_LABEL, positiveLabel)
                    putString(ARG_NEGATIVE_LABEL, negativeLabel)
                }
            }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_confirmation_modal, container, false)
        view.question_text.text = requireArguments().getString(ARG_QUESTION)
        view.positive_button.text = requireArguments().getString(ARG_POSITIVE_LABEL)
        view.negative_button.text = requireArguments().getString(ARG_NEGATIVE_LABEL)
        view.positive_button.setOnClickListener {
            if (mListener != null) mListener!!.onPositiveButtonClick()
        }
        view.negative_button.setOnClickListener {
            if (mListener != null) mListener!!.onNegativeButtonClick()
        }
        if (requireArguments().containsKey(ARG_NEUTRAL_LABEL)){
            view.neutral_button.text = requireArguments().getString(ARG_NEUTRAL_LABEL)
            view.neutral_button.setOnClickListener {
                if (mListener != null) mListener!!.onNeutralButtonClick()
            }
        }
        return view
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    class ActionListener(val positiveListener: ()-> Unit, val negativeListener: () ->  Unit, val neutralListener: () -> Unit = {}){
        fun onPositiveButtonClick() = positiveListener()
        fun onNegativeButtonClick() = negativeListener()
        fun onNeutralButtonClick() = neutralListener()
    }

    fun setOnItemClickListener( positive: () -> Unit, negative: () -> Unit, neutral: () -> Unit){
        mListener = ActionListener(positive, negative, neutral)
    }
    fun setOnItemClickListener( positive: () -> Unit, negative: () -> Unit){
        mListener = ActionListener(positive, negative)
    }

}
