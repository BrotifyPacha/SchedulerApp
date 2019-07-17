package brotifypacha.scheduler.Modals

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.R;
import brotifypacha.scheduler.SchedulerApiService
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.data_models.ResultModel
import kotlinx.android.synthetic.main.fragment_subscribe_modal.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

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
class SubscribeModal : BottomSheetDialogFragment() {

    val TAG = SubscribeModal::class.java.simpleName
    private var mListener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_subscribe_modal, container, false)

        val responseLiveData = MutableLiveData<ResultModel<Unit>>()
        responseLiveData.observe(viewLifecycleOwner, Observer {
            if (it.result == Constants.SUCCESS){
                if (mListener != null) (mListener as Listener).onSubscribed()
            } else {
                when (it.type){
                    Constants.NETWORK_ERROR -> Toast.makeText(context!!, "Для этого действия требуется подключение к сети", Toast.LENGTH_LONG).show()
                    "not found" -> Toast.makeText(context!!, "Расписание с предоставленным идентификатором не найдено", Toast.LENGTH_LONG).show()
                }
            }
            dismiss()
        })

        view.positive_button.setOnClickListener {
            val alias = view.alias_edit.text.toString()
            val api = SchedulerApiService.build()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = api.subscribe(
                        Utils.getToken(context!!.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)),
                        alias
                    ).await()
                    withContext(Dispatchers.Main) {
                        responseLiveData.value = response
                    }
                } catch (e: Exception){
                    Log.e(TAG, e.toString())
                    withContext(Dispatchers.Main) {
                        responseLiveData.value = ResultModel("error", Unit, Constants.NETWORK_ERROR)
                    }
                }
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

    class Listener(val listener: () -> Unit) {
        fun onSubscribed() = listener()
    }

    fun setOnSubscribedListener(listener: () -> Unit){
        mListener = Listener(listener)
    }


}
