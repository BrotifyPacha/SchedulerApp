package brotifypacha.scheduler.main_activity

import android.content.*
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.R
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.auth_activity.AuthActivity
import com.google.android.gms.ads.MobileAds

class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.simpleName

    val CODE_AUTH_ACTIVITY = 0

    lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Инициализация AdMob
        MobileAds.initialize(this, Constants.ADMOB_APP_ID)

        //Проверка авторизованности пользователя
        val pref = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (!Utils.isAuthorized(pref)) {
            startActivityForResult(Intent(this, AuthActivity::class.java), CODE_AUTH_ACTIVITY)
        }

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        viewModel.getUserData()
        viewModel.getEventRequestAuth().observe(this, Observer {
            if (it){
                clearAuthorization(pref)
                viewModel.setEventRequestAuthCaptured()
            }
        })
        viewModel.getEventError().observe(this, Observer {
            if (it != null) {
                when (it) {
                    Constants.NETWORK_ERROR -> {}//Toast.makeText(this, "Нет доступа к сети", Toast.LENGTH_LONG).show()
                }
                viewModel.setEventErrorCaptured()
            }
        })
    }

    fun clearAuthorization(pref: SharedPreferences){
        Log.d(TAG, "supposed to happen")
        pref.edit()
            .remove(Constants.PREF_KEY_TOKEN)
            .remove(Constants.PREF_KEY_ID)
            .apply()
        startActivityForResult(Intent(this, AuthActivity::class.java), CODE_AUTH_ACTIVITY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_AUTH_ACTIVITY){
            Log.d(TAG, "finished authorization")
        }
    }
}
