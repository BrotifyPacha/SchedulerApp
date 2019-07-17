package brotifypacha.scheduler.auth_activity

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.WindowDecorActionBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.transition.*
import brotifypacha.scheduler.R
import brotifypacha.scheduler.Constants
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {

    val TAG = AuthActivity::class.java.simpleName

    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auth)

        viewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)
        viewModel.getErrorEvent().observe(this, Observer {
            if (it != null) {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.setErrorShown()
            }
        })
        viewModel.getEventAuthenticated().observe(this, Observer {
            if (it != null) {
                Log.d(TAG, "supposed to finish")
                finish()
                viewModel.setAuthenticationEventHandled()
            }
        })
        if (supportFragmentManager.fragments.isEmpty()) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, SignInFragment.newInstance().apply {
                    setExitTransition(TransitionSet().apply {
                        addTransition(TransitionSet().apply {
                            addTransition(ChangeBounds())
                            addTransition(ChangeTransform())
                            addTarget(R.id.title_sign_in)
                            addTarget(R.id.title_sign_up)
                        })
                        addTransition(TransitionSet().apply {
                            addTransition(Fade(Fade.MODE_OUT).apply {
                                addTarget(R.id.to_sign_up)
                                addTarget(R.id.title_sign_in)
                            })
                            addTransition(Fade(Fade.MODE_IN).apply {
                                addTarget(R.id.to_sign_in)
                                addTarget(R.id.title_sign_up)
                            })
                            setOrdering(TransitionSet.ORDERING_SEQUENTIAL)
                        })
                        setOrdering(TransitionSet.ORDERING_TOGETHER)
                    })
                })
                .commit()
        }

    }

    fun onToSignUpFragment() {
        val signUpFragment = SignUpFragment.newInstance().apply {
            val shared_transition = TransitionSet().apply {
                setDuration(200)
                addTransition(Fade(Fade.MODE_OUT))
                addTransition(ChangeBounds())
                addTransition(ChangeTransform())
                setOrdering(TransitionSet.ORDERING_TOGETHER)
            }
            setSharedElementEnterTransition(shared_transition)
            setSharedElementReturnTransition(shared_transition)

            setReturnTransition(TransitionSet().apply {
                addTransition(TransitionSet().apply {
                    addTransition(ChangeBounds())
                    addTransition(ChangeTransform())
                    addTarget(R.id.title_sign_in)
                    addTarget(R.id.title_sign_up)
                })
                addTransition(TransitionSet().apply {
                    addTransition(Fade(Fade.MODE_OUT).apply {
                        addTarget(R.id.to_sign_in)
                        addTarget(R.id.title_sign_up)
                    })
                    addTransition(Fade(Fade.MODE_IN).apply {
                        addTarget(R.id.to_sign_up)
                        addTarget(R.id.title_sign_in)
                    })
                    setOrdering(TransitionSet.ORDERING_SEQUENTIAL)
                })
                setOrdering(TransitionSet.ORDERING_TOGETHER)

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
        viewModel.setAuthorizationVariables(Constants.PREF_VALUE_OFFLINE, Constants.PREF_VALUE_OFFLINE)
        finish()
    }

    override fun onBackPressed() {
        finishAffinity()
        //super.onBackPressed()
    }

}
