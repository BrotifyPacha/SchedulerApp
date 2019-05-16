package brotifypacha.scheduler.auth_activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras

import brotifypacha.scheduler.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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

        view.findViewById<TextView>(R.id.to_sign_up).setOnClickListener {
            viewModel.setUsername(usernameEdit.text.toString())
            viewModel.setPassword(passwordEdit.text.toString())

            //val signUpFragment = SignUpFragment.newInstance().apply {
            //    setEnterTransition(Fade().apply {
            //        addTarget(R.id.to_sign_in)
            //        addTarget(R.id.to_sign_up)
            //    })
            //    val shared_transition = TransitionSet().apply {
            //        addTransition(ChangeBounds().apply { setDuration(100) })
            //        setOrdering(TransitionSet.ORDERING_TOGETHER)
            //    }
            //    setSharedElementEnterTransition(shared_transition)
            //    setSharedElementReturnTransition(shared_transition)

            //    setReturnTransition(Fade().apply {
            //        addTarget(R.id.to_sign_in)
            //        addTarget(R.id.to_sign_up)
            //    })
            //}

            //supportFragmentManager
            //    .beginTransaction()
            //    //.replace(R.id.fragment_container, signUpFragment)
            //    .addSharedElement(
            //    .addSharedElement(
            //    .addSharedElement()
            //    .addSharedElement()
            //    .addToBackStack(null)
            //    .commit()
            val extras = FragmentNavigatorExtras(
                view.findViewById<Button>(R.id.main_button) to "main_button",
                view.findViewById<Button>(R.id.later_button) to "later_button",
                view.findViewById<TextInputLayout>(R.id.username_layout) to "username_layout",
                view.findViewById<TextInputLayout>(R.id.password_layout) to "password_layout"
            )
            view.findNavController().navigate(R.id.to_sign_up_screen, null, null, extras)
        }

        view.findViewById<TextView>(R.id.later_button).setOnClickListener {
            (this.activity as AuthActivity).onLaterButtonClick()
        }

        usernameEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
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
