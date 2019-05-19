package brotifypacha.scheduler.auth_activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import brotifypacha.scheduler.Constants

import brotifypacha.scheduler.R
import brotifypacha.scheduler.afterTextChanged
import brotifypacha.scheduler.databinding.FragmentSignUpBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignUpFragment : Fragment() {

    val TAG = SignUpFragment::class.java.simpleName

    lateinit var binding : FragmentSignUpBinding

    companion object {
        fun newInstance() = SignUpFragment()
    }

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)

        binding.toSignIn.setOnClickListener {
            viewModel.setUsername(binding.usernameEdit.text.toString())
            viewModel.setPassword(binding.passwordEdit.text.toString())

            if (binding.usernameEdit.hasFocus()){
                viewModel.setFocusedField("username")
            }
            if (binding.passwordEdit.hasFocus()){
                viewModel.setFocusedField("password")
            }

            (activity as AuthActivity).onToSignInFragment()
        }

        binding.mainButton.setOnClickListener {
            viewModel.signUp(binding.usernameEdit.text.toString(), binding.passwordEdit.text.toString())
        }

        binding.usernameEdit.afterTextChanged {
            val reult = viewModel.evaluateUsername(it)
            viewModel.reactToUsername(reult)
            binding.mainButton.isEnabled =
                (viewModel.evaluateUsername(it) == Constants.SUCCESS) and (viewModel.evaluatePassword(binding.passwordEdit.text.toString()) == Constants.SUCCESS)
        }
        binding.passwordEdit.afterTextChanged {
            val result = viewModel.evaluatePassword(it)
            viewModel.reactToPassword(result)
            binding.mainButton.isEnabled =
                (viewModel.evaluateUsername(binding.usernameEdit.text.toString()) == Constants.SUCCESS) and (viewModel.evaluatePassword(it) == Constants.SUCCESS)
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(AuthViewModel::class.java)
        viewModel.getUsername().observe(viewLifecycleOwner, Observer<String>{
            binding.usernameLayout.isHintAnimationEnabled = false
            binding.usernameEdit.setText(it)
            binding.usernameEdit.setSelection(it.length)
            binding.usernameLayout.isHintAnimationEnabled = true
        })
        viewModel.getPassword().observe(viewLifecycleOwner, Observer<String>{
            binding.passwordLayout.isHintAnimationEnabled = false
            binding.passwordEdit.setText(it)
            binding.passwordEdit.setSelection(it.length)
            binding.passwordLayout.isHintAnimationEnabled = true
        })

        viewModel.getFocusedField().observe(viewLifecycleOwner, Observer<String>{
            when (it){
                "username" -> {
                    binding.usernameEdit.requestFocus()
                    binding.usernameEdit.setSelection(binding.usernameEdit.text.toString().length)
                }
                "password" -> {
                    binding.passwordEdit.requestFocus()
                    binding.passwordEdit.setSelection(binding.passwordEdit.text.toString().length)
                }
            }
        })

        viewModel.getUsernameHelperMsg().observe(viewLifecycleOwner, Observer<String?> {
            if (binding.usernameLayout.helperText != it) {
                binding.usernameLayout.helperText = it
            }
        })
        viewModel.getPasswordHelperMsg().observe(viewLifecycleOwner, Observer<String?> {
            if (binding.passwordLayout.helperText != it){
                binding.passwordLayout.helperText = it
            }
        })
    }
}
