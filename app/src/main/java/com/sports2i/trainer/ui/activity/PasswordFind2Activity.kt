package com.sports2i.trainer.ui.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.VerifyRequest
import com.sports2i.trainer.databinding.ActivityPasswordFind2Binding
import com.sports2i.trainer.ui.dialog.CustomEmailCheckDialog
import com.sports2i.trainer.ui.dialog.CustomRecordDialogFragment
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasswordFind2Activity : BaseActivity<ActivityPasswordFind2Binding>({ ActivityPasswordFind2Binding.inflate(it)}) {
    private val viewModel: UserViewModel by viewModels()
    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        init()
        observe()
        setupFindButton()
        clickBack()
        this@PasswordFind2Activity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

    }

    private fun setupFindButton() {
        binding.nextButton.setOnClickListener {
            val codeValue = binding.etCode.text.toString()
            if(codeValue.isNotEmpty() || codeValue.isNotBlank()) {
                val requestVerify = VerifyRequest(email = email, verificationCode = codeValue)
                viewModel.requestVerify(requestVerify)
            } else {
                customEmailCheckDialog(getString(R.string.empty_code))
            }
        }
    }


    private fun handleSuccess(emailCheck: Boolean) {
        Log.e("TAG", "handleSuccess: $emailCheck")
    }
    private fun handleVerifySuccess(success: Boolean) {
        if(success){
            val intent = Intent(this, PasswordFind3Activity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }else{
            customEmailCheckDialog(getString(R.string.fault_code))
        }
    }

    private fun customEmailCheckDialog(title:String){
        val confirmClickListener = DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
        }
        val dialogFragment = CustomEmailCheckDialog.newInstance(confirmClickListener,title)
        dialogFragment.show(supportFragmentManager, CustomRecordDialogFragment.TAG)
    }

    private fun handleError(errorMessage: String?) {
        customEmailCheckDialog(getString(R.string.fault_code))
        errorMessage?.let {
            Log.e("TAG", "Error: $it")
        }
    }

    private fun hold(){}
    private fun init(){

        email = intent.getStringExtra("email").toString()

        val requestEmailSend = VerifyRequest(email = email, verificationCode = "")
        viewModel.requestEmailSend(requestEmailSend)

    }

    private fun observe(){
        viewModel.emailSendState.observe(this) { emailcheck ->
            when (emailcheck) {
                is NetworkState.Success -> handleSuccess(emailcheck.data)
                is NetworkState.Error ->  handleError(emailcheck.message)
                is NetworkState.Loading ->  hold()
            }
        }

        viewModel.verifyState.observe(this) {
            when(it) {
                is NetworkState.Success -> handleVerifySuccess(it.data.data)
                is NetworkState.Error ->  handleError(it.message)
                is NetworkState.Loading ->  hold()
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