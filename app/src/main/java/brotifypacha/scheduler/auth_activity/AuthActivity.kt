package brotifypacha.scheduler.auth_activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import androidx.lifecycle.ViewModelProviders
import androidx.transition.*
import androidx.transition.Fade.IN
import androidx.transition.Fade.OUT
import brotifypacha.scheduler.R
import brotifypacha.scheduler.Constants
import brotifypacha.scheduler.schedule_list_activity.ScheduleListActivity

class AuthActivity : AppCompatActivity() {

    val TAG = AuthActivity::class.java.simpleName

    lateinit var authViewModel: AuthViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        authViewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)

        //TODO: Н
    }

    fun onToSignUpFragment() {
        val signUpFragment = SignUpFragment.newInstance().apply {
            setEnterTransition(Fade().apply {
                addTarget(R.id.to_sign_in)
                addTarget(R.id.to_sign_up)
            })
            val shared_transition = TransitionSet().apply {
                addTransition(ChangeBounds().apply { setDuration(100) })
                setOrdering(TransitionSet.ORDERING_TOGETHER)
            }
            setSharedElementEnterTransition(shared_transition)
            setSharedElementReturnTransition(shared_transition)

            setReturnTransition(Fade().apply {
                addTarget(R.id.to_sign_in)
                addTarget(R.id.to_sign_up)
            })
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, signUpFragment)
            .addSharedElement(findViewById(R.id.main_button), "main_button")
            .addSharedElement(findViewById(R.id.later_button), "later_button")
            .addSharedElement(findViewById(R.id.username_layout), "username_layout")
            .addSharedElement(findViewById(R.id.password_layout), "password_layout")
            .addToBackStack(null)
            .commit()
    }

    fun onToSignInFragment() {
        supportFragmentManager.popBackStack()
    }

    fun onLaterButtonClick() {
        getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(Constants.SP_IS_AUTHORIZED, false)
            .apply()
        startActivity(Intent(this, ScheduleListActivity::class.java))
    }
}
