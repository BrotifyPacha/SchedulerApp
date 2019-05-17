package brotifypacha.scheduler.main_activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import brotifypacha.scheduler.R

class ScheduleListActivity : AppCompatActivity() {

    val TAG = ScheduleListActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_list)


        /*val retrofit  = Retrofit.Builder()
            .baseUrl("http://178.219.153.30:5000")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build().create(SchedulerApiService::class.java)

        val oof = retrofit.get_users(null, null, null, null).enqueue(object : Callback<ResultModel> {
            /**
             * Invoked when a network exception occurred talking to the server or when an unexpected
             * exception occurred creating the request or processing the response.
             */
            override fun onFailure(call: Call<ResultModel>, t: Throwable) {
                Log.d(TAG, t.toString())
            }

            /**
             * Invoked for a received HTTP response.
             *
             *
             * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
             * Call [Response.isSuccessful] to determine if the response indicates success.
             */
            override fun onResponse(call: Call<ResultModel>, response: Response<ResultModel>) {
                val res = response.body()
                    Log.d(TAG, res.toString())
            }
        })*/
    }
}
