package brotifypacha.scheduler.add_change_fragment

import android.animation.LayoutTransition
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.work.*
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.R
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.afterTextChanged
import brotifypacha.scheduler.data_models.ChangeModel
import brotifypacha.scheduler.database.Schedule
import brotifypacha.scheduler.database.SchedulerDataBase
import brotifypacha.scheduler.work_manager.CommitChangeWorker
import brotifypacha.scheduler.work_manager.CommitScheduleWorker
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_add_change_modal.view.*
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class AddChangeFragment: Fragment(){

    companion object{
        val ARG_SCHEDULE_ID = "schedule_id"
        fun newInstance(scheduleId: String) = AddChangeFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_SCHEDULE_ID, scheduleId)
            }
        }
    }

    val editListId = listOf(
        R.id.lesson_edit_1,
        R.id.lesson_edit_2,
        R.id.lesson_edit_3,
        R.id.lesson_edit_4,
        R.id.lesson_edit_5,
        R.id.lesson_edit_6,
        R.id.lesson_edit_7,
        R.id.lesson_edit_8,
        R.id.lesson_edit_9
    )
    var date: Long = tomorrowMillis()
    val lessons: ArrayList<String> = arrayListOf("", "", "", "", "", "", "", "", "")


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_add_change_modal, container, false)

        val db = SchedulerDataBase.getInstance(context!!)
        val pref = context!!.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val scheduleId = arguments!!.getString(ARG_SCHEDULE_ID)

        editListId.forEachIndexed { id, it ->
            view.findViewById<EditText>(it).afterTextChanged {
                lessons[id] = it
            }
        }

        val datePicker = view.date_picker
        datePicker.setText(Utils.formatDate(tomorrowMillis()))

        datePicker.setOnClickListener {
            val tomorrowCalendar = Calendar.getInstance()
            tomorrowCalendar.timeInMillis = tomorrowMillis()
            DatePickerDialog( context, R.style.AppTheme_DatePickerDialog, { view, year, month, dayOfMonth ->
                val changeDate = Calendar.getInstance()
                changeDate.set(year, month, dayOfMonth)

                if (changeDate.timeInMillis < tomorrowMillis()){
                    Toast.makeText(context, "Выберите будущую дату", Toast.LENGTH_LONG).show()
                } else {
                    date = changeDate.timeInMillis
                    datePicker.setText(Utils.formatDate(date))
                }
            },
                tomorrowCalendar.get(Calendar.YEAR),
                tomorrowCalendar.get(Calendar.MONTH),
                tomorrowCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        view.positive_button.setOnClickListener{

            CoroutineScope(Dispatchers.IO).launch {
                val scheduleBeforeEdit = db.getSchedulesDao().getSchedule(scheduleId)
                val changeList = ArrayList(scheduleBeforeEdit.getChangesAsList())
                changeList.add(ChangeModel(date, lessons))
                db.getSchedulesDao().update(scheduleBeforeEdit.copy(changes = Schedule.changesToStr(changeList)))
            }

            if (Utils.isAuthorizedWithToken(pref)) {
                val oneTimeWorkRequest = OneTimeWorkRequestBuilder<CommitChangeWorker>().run {
                    setConstraints(
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
                                .putString(CommitChangeWorker.DATA_KEY_SCHEDULE_ID, scheduleId)
                                .putString(
                                    CommitChangeWorker.DATA_KEY_CHANGE_JSON,
                                    ChangeModel(date, lessons).toString()
                                )
                                .build()
                        )
                        .build()
                }
                WorkManager.getInstance()
                    .beginUniqueWork("Saving changes ${scheduleId}", ExistingWorkPolicy.REPLACE, oneTimeWorkRequest)
                    .enqueue()
            }
            findNavController().popBackStack()
        }
        view.negative_buttton.setOnClickListener{
            findNavController().popBackStack()
        }


        val request = AdRequest.Builder().build()
        view.adView.loadAd(request)
        view.adView.adListener = object: AdListener(){
            override fun onAdFailedToLoad(p0: Int) {
                view.adView.visibility = View.GONE
                val adLoader = AdLoader.Builder(context!!, "ca-app-pub-3940256099942544/2247696110")
                    .forUnifiedNativeAd { ad : UnifiedNativeAd ->
                        val adView = layoutInflater.inflate(R.layout.native_ad_layout, null) as UnifiedNativeAdView
                        view.adViewPlaceholder.layoutTransition.enableTransitionType(LayoutTransition.APPEARING)
                        view.screenContainer.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
                        adView.findViewById<TextView>(R.id.ad_header).setText(ad.headline)
                        adView.findViewById<TextView>(R.id.ad_body).setText(ad.body)


                        if (ad.advertiser != null && ad.advertiser.isNotBlank())
                            adView.findViewById<TextView>(R.id.ad_advertiser).setText(ad.advertiser)


                        adView.headlineView = adView.findViewById(R.id.ad_header)
                        adView.bodyView = adView.findViewById(R.id.ad_body)
                        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
                        adView.adChoicesView = adView.findViewById(R.id.ad_choices)


                        view.adViewPlaceholder.removeAllViews()
                        view.adViewPlaceholder.addView(adView)
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(errorCode: Int) {
                            view.adViewPlaceholder.visibility = View.GONE
                        }
                    })
                    .withNativeAdOptions(
                        NativeAdOptions.Builder()
                            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                            // Methods in the NativeAdOptions.Builder class can be
                            // used here to specify individual options settings.
                            .build())
                    .build()
                adLoader.loadAd(AdRequest.Builder()
                    .build())
            }
        }

        return view
    }

    fun tomorrowMillis(): Long {
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_MONTH, 1)
        return tomorrow.timeInMillis
    }

}