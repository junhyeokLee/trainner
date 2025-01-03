package com.sports2i.trainer.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.RequestUserResetPassword
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.utils.SignUtil
import com.sports2i.trainer.viewmodel.TrainingViewModel
import com.sports2i.trainer.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomPasswordChangeDialog : DialogFragment() {

    private var negativeButtonClickListener: DialogInterface.OnClickListener? = null
    private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
    private val trainingViewModel: TrainingViewModel by viewModels()
    private var passwordFlag = true
    private var passwordCheckFlag = true
    private val viewModel: UserViewModel by viewModels()

    companion object {
        const val TAG = "CustomRPEDialogFragment"

        fun newInstance(
            negativeButtonClickListener: DialogInterface.OnClickListener,
            positiveButtonClickListener: DialogInterface.OnClickListener): CustomPasswordChangeDialog {
            val fragment = CustomPasswordChangeDialog()
            fragment.negativeButtonClickListener = negativeButtonClickListener
            fragment.positiveButtonClickListener = positiveButtonClickListener
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        Preferences.init(requireContext(), Preferences.DB_USER_INFO)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.custom_password_change_dailog, null)

        val negativeButton: Button = view.findViewById(R.id.negativeButton)
        val positiveButton: Button = view.findViewById(R.id.positiveButton)
        val editPaswword: TextInputEditText = view.findViewById(R.id.edit_pwd)
        val editPaswwordConfirm: TextInputEditText = view.findViewById(R.id.edit_pwd_confirm)
        val editPasswordLayout: TextInputLayout = view.findViewById(R.id.ll_pwd)
        val editPasswordConfirmLayout: TextInputLayout = view.findViewById(R.id.ll_pwd_confirm)

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

        positiveButton.setOnClickListener {
            if (flagCheck()) {
                val userId = Preferences.string(Preferences.KEY_USER_ID)
                val userEmail = Preferences.string(Preferences.KEY_EMAIL)
                val editPassword = editPaswword.text.toString()
                val editPasswordConfirm = editPaswwordConfirm.text.toString()

                if(SignUtil.isPasswordValid(editPasswordConfirm)) {
                    val requestUserResetPassword = RequestUserResetPassword(userEmail, editPassword)
                    viewModel.requestUserResetPassword(requestUserResetPassword)
                    positiveButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
                } else{
                    Global.showBottomSnackBar(view, "비밀번호는 8자 이상의 영문,숫자,특수문자로 구성되어야 합니다.")
                }
            } else {
                // passwordFlag와 passwordCheckFlag가 false일 때 처리 추가 (예: 에러 메시지 표시 등)
                return@setOnClickListener
            }
        }

        negativeButton.setOnClickListener {
            negativeButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_NEGATIVE)
            dismiss()
        }

        builder.setView(view)
        val dialog = builder.create()

        // 영역 바깥을 터치해도 다이얼로그가 닫히지 않도록 설정
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    fun flagCheck() : Boolean {
       return passwordFlag && passwordCheckFlag
    }



    interface progressValueDialogListener {
        fun setProgress(progress: Int)
    }

}
