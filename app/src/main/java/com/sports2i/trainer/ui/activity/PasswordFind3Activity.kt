package com.sports2i.trainer.ui.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.RequestUserResetPassword
import com.sports2i.trainer.databinding.ActivityPasswordFind3Binding
import com.sports2i.trainer.ui.dialog.CustomEmailCheckDialog
import com.sports2i.trainer.ui.dialog.CustomRecordDialogFragment
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.SignUtil
import com.sports2i.trainer.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasswordFind3Activity : BaseActivity<ActivityPasswordFind3Binding>({ ActivityPasswordFind3Binding.inflate(it)}) {
    private val viewModel: UserViewModel by viewModels()
    private var email: String = ""
    private var passwordFlag = true
    private var passwordCheckFlag = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        init()
        setupFindButton()
        clickBack()
        this@PasswordFind3Activity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun setupFindButton() {
        val editPaswword: TextInputEditText = binding.editPwd
        val editPaswwordConfirm: TextInputEditText = binding.editPwdConfirm
        val editPasswordLayout: TextInputLayout = binding.llPwd
        val editPasswordConfirmLayout: TextInputLayout = binding.llPwdConfirm

        val passwordListener = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(editPaswword.text.toString().equals(editPaswwordConfirm.text.toString())){
                    editPasswordLayout.error = resources.getString(R.string.equal_password)
                    editPasswordLayout.hintTextColor = resources.getColorStateList(R.color.primary)
                    editPasswordLayout.setErrorTextColor(resources.getColorStateList(R.color.primary))
                    editPasswordLayout.boxStrokeErrorColor = resources.getColorStateList(R.color.primary)
                    // 가입하기 버튼 활성화
                    passwordFlag=true
                }
                else{
                    editPasswordLayout.error = resources.getString(R.string.fail_password)
                    editPasswordLayout.hintTextColor = resources.getColorStateList(R.color.red)
                    editPasswordLayout.setErrorTextColor(resources.getColorStateList(R.color.red))
                    editPasswordLayout.boxStrokeErrorColor = resources.getColorStateList(R.color.red)
                    // 가입하기 버튼 비활성화
                    passwordFlag=false
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    when {
                        editPaswword.text.toString().isEmpty() -> {
                            editPasswordLayout.error = "비밀번호를 입력해주세요."
                            editPasswordLayout.error = resources.getString(R.string.fail_password)
                            editPasswordLayout.hintTextColor = resources.getColorStateList(R.color.red)
                            editPasswordLayout.setErrorTextColor(resources.getColorStateList(R.color.red))
                            editPasswordLayout.boxStrokeErrorColor = resources.getColorStateList(R.color.red)
                            passwordFlag = false
                        }
                        !editPaswword.text.toString().equals(editPaswwordConfirm.text.toString()) -> {
                            editPasswordLayout.error = resources.getString(R.string.fail_password)
                            editPasswordLayout.hintTextColor = resources.getColorStateList(R.color.red)
                            editPasswordLayout.setErrorTextColor(resources.getColorStateList(R.color.red))
                            editPasswordLayout.boxStrokeErrorColor = resources.getColorStateList(R.color.red)
                            passwordCheckFlag = false
                        }
                        editPaswword.text.toString().equals(editPaswwordConfirm.text.toString()) -> {
                            editPasswordLayout.error = null
                            editPasswordLayout.hintTextColor = resources.getColorStateList(R.color.primary)
                            editPasswordLayout.setErrorTextColor(resources.getColorStateList(R.color.primary))
                            editPasswordLayout.boxStrokeErrorColor = resources.getColorStateList(R.color.primary)
                            editPasswordConfirmLayout.error = null
                            editPasswordConfirmLayout.hintTextColor = resources.getColorStateList(R.color.primary)
                            editPasswordConfirmLayout.setErrorTextColor(resources.getColorStateList(R.color.primary))
                            editPasswordConfirmLayout.boxStrokeErrorColor = resources.getColorStateList(R.color.primary)
                            passwordFlag = true
                            passwordCheckFlag = true
                        }
                    }
                }
            }
        }

        val passwordCheckedListener = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if(editPaswword.text.toString().equals(editPaswwordConfirm.text.toString())){
                    editPasswordConfirmLayout.error = resources.getString(R.string.equal_password)
                    editPasswordConfirmLayout.hintTextColor = resources.getColorStateList(R.color.primary)
                    editPasswordConfirmLayout.setErrorTextColor(resources.getColorStateList(R.color.primary))
                    editPasswordConfirmLayout.boxStrokeErrorColor = resources.getColorStateList(R.color.primary)
                    // 가입하기 버튼 활성화
                    passwordCheckFlag=true
                }
                else{
                    editPasswordConfirmLayout.error = resources.getString(R.string.fail_password)
                    editPasswordConfirmLayout.hintTextColor = resources.getColorStateList(R.color.red)
                    editPasswordConfirmLayout.setErrorTextColor(resources.getColorStateList(R.color.red))
                    editPasswordConfirmLayout.boxStrokeErrorColor = resources.getColorStateList(R.color.red)
                    // 가입하기 버튼 비활성화
                    passwordCheckFlag=false
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    when {
                        editPaswwordConfirm.text.toString().isEmpty() -> {
                            editPasswordConfirmLayout.error = "비밀번호를 입력해주세요."
                            editPasswordConfirmLayout.hintTextColor = resources.getColorStateList(R.color.red)
                            editPasswordConfirmLayout.setErrorTextColor(resources.getColorStateList(R.color.red))
                            editPasswordConfirmLayout.boxStrokeErrorColor = resources.getColorStateList(R.color.red)
                            passwordCheckFlag = false
                        }
                        !editPaswwordConfirm.text.toString().equals(editPaswword.text.toString()) -> {
                            editPasswordConfirmLayout.error = resources.getString(R.string.fail_password)
                            editPasswordConfirmLayout.hintTextColor = resources.getColorStateList(R.color.red)
                            editPasswordConfirmLayout.setErrorTextColor(resources.getColorStateList(R.color.red))
                            editPasswordConfirmLayout.boxStrokeErrorColor = resources.getColorStateList(R.color.red)
                            passwordCheckFlag = false
                        }
                        editPaswwordConfirm.text.toString().equals(editPaswword.text.toString()) -> {
                            editPasswordConfirmLayout.error = resources.getString(R.string.equal_password)
                            editPasswordConfirmLayout.hintTextColor = resources.getColorStateList(R.color.primary)
                            editPasswordConfirmLayout.setErrorTextColor(resources.getColorStateList(R.color.primary))
                            editPasswordConfirmLayout.boxStrokeErrorColor = resources.getColorStateList(R.color.primary)
                            editPasswordLayout.error = null
                            editPasswordLayout.hintTextColor = resources.getColorStateList(R.color.primary)
                            editPasswordLayout.setErrorTextColor(resources.getColorStateList(R.color.primary))
                            editPasswordLayout.boxStrokeErrorColor = resources.getColorStateList(R.color.primary)
                            passwordCheckFlag = true
                            passwordFlag = true
                        }
                    }
                }
            }
        }

        editPaswword.addTextChangedListener(passwordListener)
        editPaswwordConfirm.addTextChangedListener(passwordCheckedListener)

        binding.positiveButton.setOnClickListener {
            if (flagCheck()) {

                val editPassword = editPaswword.text.toString()
                val editPasswordConfirm = editPaswwordConfirm.text.toString()

                if(SignUtil.isPasswordValid(editPasswordConfirm)) {
                    val requestUserResetPassword = RequestUserResetPassword(email, editPassword)
                    viewModel.requestUserResetPassword(requestUserResetPassword)
                } else{
                    Global.showBottomSnackBar(it, "비밀번호는 8자 이상의 영문,숫자,특수문자로 구성되어야 합니다.")
                }
            } else {
                // passwordFlag와 passwordCheckFlag가 false일 때 처리 추가 (예: 에러 메시지 표시 등)
                return@setOnClickListener
            }
        }

        binding.negativeButton.setOnClickListener {
            finish()
            back()
        }
    }

    fun flagCheck() : Boolean {
        return passwordFlag && passwordCheckFlag
    }
    private fun handleSuccess(passwordChange: String) {
//        if(passwordChange.equals("success")){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            Global.showBottomSnackBar(binding.root, getString(R.string.password_change_success))
            finish()
            back()
//        }

//        else customPasswordChangeDialog(getString(R.string.password_change_fail))

    }

    private fun customPasswordChangeDialog(title:String){
        val confirmClickListener = DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
        }
        val dialogFragment = CustomEmailCheckDialog.newInstance(confirmClickListener,title)
        dialogFragment.show(supportFragmentManager, CustomRecordDialogFragment.TAG)
    }

    private fun handleError(errorMessage: String?) {
        customPasswordChangeDialog(getString(R.string.password_change_fail))
        errorMessage?.let {
            Log.e("TAG", "Error: $it")
        }
    }

    private fun hold(){}
    private fun init(){

        email = intent.getStringExtra("email").toString()

        viewModel.passwordResetState.observe(this) { passwordReset ->
            when (passwordReset) {
                is NetworkState.Success -> handleSuccess(passwordReset.data)
                is NetworkState.Error -> handleError(passwordReset.message)
                is NetworkState.Loading -> hold()
            }
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
            back()
        }
    }
    private fun clickBack() {
        binding.layoutBack.setOnClickListener {
            finish()
            back()
        }
    }

}