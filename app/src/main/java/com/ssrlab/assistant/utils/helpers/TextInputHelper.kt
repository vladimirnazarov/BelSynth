package com.ssrlab.assistant.utils.helpers

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentLoginBinding
import com.ssrlab.assistant.databinding.FragmentRegisterBinding

class TextInputHelper(private val context: Context) {

    /**
     * 1 - OK;
     * 2 - Empty login;
     * 3 - Empty password;
     * 4 - Both empty
     */
    fun checkSignEmptiness(loginET: AppCompatEditText, passwordET: AppCompatEditText, onResult: (Int) -> Unit) {
        if (loginET.text!!.isNotEmpty() && passwordET.text!!.isNotEmpty()) onResult(1)
        else if (loginET.text!!.isEmpty() && passwordET.text!!.isNotEmpty()) onResult(2)
        else if (loginET.text!!.isNotEmpty() && passwordET.text!!.isEmpty()) onResult(3)
        else if (loginET.text!!.isEmpty() && passwordET.text!!.isEmpty()) onResult(4)
    }

    private fun createEditTextListener(editText: AppCompatEditText, errorView: View) {
        editText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (errorView.visibility == View.VISIBLE) fadeAnim(errorView, true)
                editText.background = ContextCompat.getDrawable(context, R.drawable.background_sign_et)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setEditTextError(editText: AppCompatEditText, textView: TextView, errorHolder: View, message: String) {
        fadeAnim(errorHolder)
        textView.text = message

        editText.background = ContextCompat.getDrawable(context, R.drawable.background_sign_et_error)
        createEditTextListener(editText, errorHolder)
    }

    private fun setEditTextError(editText1: AppCompatEditText, editText2: AppCompatEditText, textView1: TextView, textView2: TextView, errorHolder1: View, errorHolder2: View, message: String, binding: ViewBinding) {
        defineBinding(binding, {
            fadeAnim(errorHolder1)
            fadeAnim(errorHolder2)
        }, {
            fadeAnim(errorHolder1)
            fadeAnim(errorHolder2)
        })
        textView1.text = message
        textView2.text = message

        editText1.background = ContextCompat.getDrawable(context, R.drawable.background_sign_et_error)
        editText2.background = ContextCompat.getDrawable(context, R.drawable.background_sign_et_error)
        createEditTextListener(editText1, errorHolder1)
        createEditTextListener(editText2, errorHolder2)
    }

    private fun defineBinding(binding: ViewBinding, onRegister: (FragmentRegisterBinding) -> Unit, onLogin: (FragmentLoginBinding) -> Unit) {
        when (binding) {
            is FragmentRegisterBinding -> onRegister(binding)
            is FragmentLoginBinding -> onLogin(binding)
        }
    }

    fun handleErrorTypes(message: String, type: Int, textView1: TextView? = null, textView2: TextView? = null, binding: ViewBinding) {
        defineBinding(binding, {
            val emailErrorHolder = it.registerEmailErrorHolder
            val passwordErrorHolder = it.registerPasswordErrorHolder
            handleErrorTypesByBinding(type, message, it.registerEmailInput, it.registerPasswordInput, textView1, textView2, emailErrorHolder, passwordErrorHolder, binding)
        }, {
            val emailErrorHolder = it.loginEmailErrorHolder
            val passwordErrorHolder = it.loginPasswordErrorHolder
            handleErrorTypesByBinding(type, message, it.loginEmailInput, it.loginPasswordInput, textView1, textView2, emailErrorHolder, passwordErrorHolder, binding)
        })
    }

    /**
     * 0 - FireAuth error;
     * 1 - Login error;
     * 2 - Password error;
     * 3 - Both login & password error
     */
    private fun handleErrorTypesByBinding(
        type: Int,
        message: String,
        login: AppCompatEditText,
        password: AppCompatEditText,
        textView1: TextView? = null,
        textView2: TextView? = null,
        emailErrorView: View,
        passwordErrorView: View,
        binding: ViewBinding
    ) {
        when (type) {
            0 -> showErrorSnack(message, binding.root)
            1 -> {
                if (textView1 != null)
                    setEditTextError(login, textView1, emailErrorView, message)
            }
            2 -> {
                if (textView2 != null)
                    setEditTextError(password, textView2, passwordErrorView, message)
            }
            3 -> {
                if (textView1 != null && textView2 != null)
                    setEditTextError(login, password, textView1, textView2, emailErrorView, passwordErrorView, message, binding)
            }
        }
    }

    private fun showErrorSnack(errorMessage: String, view: View) {
        val snack = Snackbar.make(view, errorMessage, Snackbar.LENGTH_SHORT)
        snack.apply {
            setTextColor(ContextCompat.getColor(context, R.color.snack_text))
            setBackgroundTint(ContextCompat.getColor(context, R.color.error))
            show()
        }
    }

    private fun fadeAnim(view: View, isOut: Boolean = false) {
        val alphaInAnim = AnimationUtils.loadAnimation(context, R.anim.medium_alpha_in)
        val alphaOutAnim = AnimationUtils.loadAnimation(context, R.anim.medium_alpha_out)
        if (isOut) {
            view.startAnimation(alphaOutAnim)
            view.visibility = View.INVISIBLE
        } else {
            view.startAnimation(alphaInAnim)
            view.visibility = View.VISIBLE
        }
    }

    fun showPasswordAction(editText: AppCompatEditText, button: ImageButton) {
        button.setOnClickListener {
            if (editText.transformationMethod == PasswordTransformationMethod.getInstance()) {
                editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                button.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_hide_password))
            }
            else {
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
                button.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_show_password))
            }
        }
    }
}