package com.sports2i.trainer.ui.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.LoginRequest
import com.sports2i.trainer.databinding.ActivityPasswordFind1Binding
import com.sports2i.trainer.ui.dialog.CustomEmailCheckDialog
import com.sports2i.trainer.ui.dialog.CustomRecordDialogFragment
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasswordFind1Activity : BaseActivity<ActivityPasswordFind1Binding>({ ActivityPasswordFind1Binding.inflate(it)}) {
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        init()
        setupFindButton()
        clickBack()
        this@PasswordFind1Activity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

    }


    private fun setupFindButton() {
        binding.nextButton.setOnClickListener {
            val idValue = binding.etId.text.toString()
            val requestEmailCheck = LoginRequest(email = idValue, password = "", accessToken = "", refreshToken = "")
            Log.e("TAG", "setupFindButton: $requestEmailCheck")
            viewModel.requestCheckEmail(requestEmailCheck)
        }
    }


    private fun handleSuccess(emailCheck: Boolean) {
        if(emailCheck){
            val intent = Intent(this, PasswordFind2Activity::class.java)
            intent.putExtra("email", binding.etId.text.toString())
            startActivity(intent)
        }else{
            customEmailCheckDialog(getString(R.string.not_enroll_id))
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
//        handlerLoading(false)
        customEmailCheckDialog(getString(R.string.not_enroll_id))
        errorMessage?.let {
            Log.e("TAG", "Error: $it")
        }
    }

    private fun hold(){}
private fun init(){

    viewModel.emailCheckState.observe(this) { emailcheck ->
        when (emailcheck) {
            is NetworkState.Success -> { handleSuccess(emailcheck.data.data) }
            is NetworkState.Error -> { handleError(emailcheck.message) }
            is NetworkState.Loading -> { hold() }
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