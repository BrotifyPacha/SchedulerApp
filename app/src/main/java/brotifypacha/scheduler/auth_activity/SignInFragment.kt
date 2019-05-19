package brotifypacha.scheduler.auth_activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import brotifypacha.scheduler.Constants

import brotifypacha.scheduler.R
import brotifypacha.scheduler.afterTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.android.synthetic.main.fragment_sign_in.view.*

class SignInFragment : Fragment() {

    val TAG = SignInFragment::class.java.simpleName

    companion object {
        fun newInstance() = SignInFragment()
    }

    private lateinit var viewModel: AuthViewModel
    private lateinit var usernameEdit: TextInputEditText
    private lateinit var passwordEdit: TextInputEditText
    private lateinit var usernameLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)
        usernameEdit = view.findViewById(R.id.username_edit)
        passwordEdit = view.findViewById(R.id.password_edit)
        usernameLayout = view.findViewById(R.id.username_layout)
        passwordLayout = view.findViewById(R.id.password_layout)

        usernameEdit.requestFocus()

        view.findViewById<TextView>(R.id.to_sign_up).setOnClickListener {
            viewModel.setUsername(usernameEdit.text.toString())
            viewModel.setPassword(passwordEdit.text.toString())

            if (usernameEdit.hasFocus()){
                viewModel.setFocusedField("username")
            }
            if (passwordEdit.hasFocus()){
                viewModel.setFocusedField("password")
            }
            (activity as AuthActivity).onToSignUpFragment()
        }

        view.findViewById<TextView>(R.id.later_button).setOnClickListener {
            (activity as AuthActivity).onLaterButtonClick()
        }

        usernameEdit.afterTextChanged {
            main_button.isEnabled = (viewModel.evaluateUsername(it) == Constants.SUCCESS) and (viewModel.evaluatePassword(passwordEdit.text.toString()) == Constants.SUCCESS)
        }
        passwordEdit.afterTextChanged {
            main_button.isEnabled = (viewModel.evaluateUsername(usernameEdit.text.toString()) == Constants.SUCCESS) and (viewModel.evaluatePassword(it) == Constants.SUCCESS)
        }

        view.findViewById<Button>(R.id.main_button).main_button.setOnClickListener {
            Log.d(TAG, "click is invoked")
            viewModel.signIn(usernameEdit.text.toString(), passwordEdit.text.toString())
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(AuthViewModel::class.java)
        viewModel.getUsername().observe(viewLifecycleOwner, Observer<String>{
            usernameLayout.isHintAnimationEnabled = false
            usernameEdit.setText(it)
            usernameEdit.setSelection(it.length)
            usernameLayout.isHintAnimationEnabled = true
        })
        viewModel.getPassword().observe(viewLifecycleOwner, Observer<String>{
            passwordLayout.isHintAnimationEnabled = false
            passwordEdit.setText(it)
            passwordEdit.setSelection(it.length)
            passwordLayout.isHintAnimationEnabled = true
        })

        viewModel.getFocusedField().observe(viewLifecycleOwner, Observer<String>{
            when (it){
                "username" -> {
                    usernameEdit.requestFocus()
                    usernameEdit.setSelection(usernameEdit.text.toString().length)
                }
                "password" -> {
                    passwordEdit.requestFocus()
                    passwordEdit.setSelection(passwordEdit.text.toString().length)
                }
            }
        })
    }
}
