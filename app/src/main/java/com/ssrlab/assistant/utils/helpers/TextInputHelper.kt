package com.ssrlab.assistant.utils.helpers

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentLoginBinding
import com.ssrlab.assistant.databinding.FragmentRegisterBinding

class TextInputHelper(private val context: Context) {

    /**
     * 1 - OK
     * 2 - Empty login
     * 3 - Empty password
     * 4 - Both empty
     */
    fun checkSignEmptiness(loginET: AppCompatEditText, passwordET: AppCompatEditText, onResult: (Int) -> Unit) {
        if (loginET.text!!.isNotEmpty() && passwordET.text!!.isNotEmpty()) onResult(1)
        else if (loginET.text!!.isEmpty() && passwordET.text!!.isNotEmpty()) onResult(2)
        else if (loginET.text!!.isNotEmpty() && passwordET.text!!.isEmpty()) onResult(3)
        else if (loginET.text!!.isEmpty() && passwordET.text!!.isEmpty()) onResult(4)
    }

    private fun createEditTextListener(editText: AppCompatEditText, binding: ViewBinding) {
        editText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when (binding) {

                }
                editText.background = ContextCompat.getDrawable(context, R.drawable.background_sign_et)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setEditTextError(editText: AppCompatEditText?, message: String, binding: ViewBinding) {
        if (editText != null) {
            when (binding) {
                is FragmentRegisterBinding -> {

                }
                is FragmentLoginBinding -> {

                }
            }

            editText.background = ContextCompat.getDrawable(context, R.drawable.background_sign_et_error)
            createEditTextListener(editText, binding)
        }
    }

    private fun setEditTextError(editText1: AppCompatEditText?, editText2: AppCompatEditText?, message: String, binding: ViewBinding) {
        if (editText1 != null && editText2 != null) {
            editText1.background = ContextCompat.getDrawable(context, R.drawable.background_sign_et_error)
            editText2.background = ContextCompat.getDrawable(context, R.drawable.background_sign_et_error)
            createEditTextListener(editText1, binding)
            createEditTextListener(editText2, binding)
        }
    }

    /**
     * 0 - FireAuth error
     * 1 - Login error
     * 2 - Password error
     * 3 - Both login & password error
     */
    fun handleErrorTypes(message: String, type: Int, binding: ViewBinding) {
        val login: AppCompatEditText?
        val password: AppCompatEditText?

        when (binding) {
            is FragmentRegisterBinding -> {
                login = binding.registerEmailInput
                password = binding.registerPasswordInput
            }
            is FragmentLoginBinding -> {
                login = binding.loginEmailInput
                password = binding.loginPasswordInput
            }
            else -> {
                login = null
                password = null
            }
        }

        when (type) {
            0 -> showErrorSnack(message, binding.root)
            1 -> { setEditTextError(login, message, binding) }
            2 -> { setEditTextError(password, message, binding) }
            3 -> { setEditTextError(login, password, message, binding) }
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
}