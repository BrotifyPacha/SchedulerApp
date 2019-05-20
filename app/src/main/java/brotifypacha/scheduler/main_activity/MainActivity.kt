package brotifypacha.scheduler.main_activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.R
import brotifypacha.scheduler.auth_activity.AuthActivity

class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.simpleName

    val CODE_AUTH_ACTIVITY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_list)

        //Проверка авторизованности пользователя
        val pref = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (!pref.getBoolean(Constants.SP_IS_AUTHORIZED, false) or !(pref.getString(Constants.KEY_TOKEN, null) != null)) {
            startActivityForResult(Intent(this, AuthActivity::class.java), CODE_AUTH_ACTIVITY)
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_AUTH_ACTIVITY){
            Log.d(TAG, "finished authorization")
        }
    }
}
