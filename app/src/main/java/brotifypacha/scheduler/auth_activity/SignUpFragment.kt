package brotifypacha.scheduler.auth_activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController

import brotifypacha.scheduler.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignUpFragment : Fragment() {

    val TAG = SignUpFragment::class.java.simpleName

    companion object {
        fun newInstance() = SignUpFragment()
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
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)
        usernameEdit = view.findViewById(R.id.username_edit)
        passwordEdit = view.findViewById(R.id.password_edit)
        usernameLayout = view.findViewById(R.id.username_layout)
        passwordLayout = view.findViewById(R.id.password_layout)

        view.findViewById<TextView>(R.id.to_sign_in).setOnClickListener {
            viewModel.setUsername(usernameEdit.text.toString())
            viewModel.setPassword(passwordEdit.text.toString())
            view.findNavController().popBackStack()
        }


        usernameEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when (viewModel.evaluateUsername(usernameEdit.text.toString())){

                }

            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
        passwordEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(AuthViewModel::class.java)
        viewModel.getUsername().observe(viewLifecycleOwner, Observer<String>{
            usernameLayout.isHintAnimationEnabled = false
            usernameEdit.setText(it)
            usernameLayout.isHintAnimationEnabled = true
        })
        viewModel.getPassword().observe(viewLifecycleOwner, Observer<String>{
            passwordLayout.isHintAnimationEnabled = false
            passwordEdit.setText(it)
            passwordLayout.isHintAnimationEnabled = true
        })
    }
}
