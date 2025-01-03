package com.sports2i.trainer.ui.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.activity.viewModels
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.User
import com.sports2i.trainer.databinding.ActivityLoginBinding
import com.sports2i.trainer.ui.dialog.CustomEmailCheckDialog
import com.sports2i.trainer.ui.dialog.CustomRecordDialogFragment
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity: BaseActivity<ActivityLoginBinding>({ActivityLoginBinding.inflate(it)}) {
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }
    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            val email = binding.userEmail.text.toString()
            val password = binding.password.text.toString()
            isInputValid(email, password)
        }
    }

    private fun setPasswordFindButton() {
        binding.tvFindPassword.setOnClickListener {
            val intent = Intent(this, PasswordFind1Activity::class.java)
            startActivity(intent)
        }
    }

        private fun isInputValid(email: String, password: String) {

            if(email.isEmpty()) {
                binding.userEmail.error = "이메일을 입력해주세요"
                customLoginCheckDialog("이메일을 확인해주세요.")
                return
            }
            if(password.isEmpty()) {
                binding.password.error = "비밀번호를 입력해주세요"
                customLoginCheckDialog("비밀번호를 확인해주세요.")

                return
            }
            if(email.isEmpty() && password.isEmpty()){
                customLoginCheckDialog("email 또는 password 를 확인해주세요.")
                return
            }

            viewModel.requestLogin(email, password)
        }

        private fun handleSuccess(userResponse: User) {

            handlerLoading(false)

            Global.myInfo = userResponse

            Preferences.put(Preferences.KEY_AUTO_LOGIN, binding.cbAutoLogin.isChecked)
            Preferences.put(Preferences.KEY_ACCESS_TOKEN, userResponse.accessToken ?: "")
            Preferences.put(Preferences.KEY_REFRESH_TOKEN, userResponse.refreshToken ?: "")
            Preferences.put(Preferences.KEY_USER_ID, userResponse.userId ?: "")
            Preferences.put(Preferences.KEY_USER_NAME, userResponse.userName ?: "")
            Preferences.put(Preferences.KEY_EMAIL, userResponse.email ?: "")
            Preferences.put(Preferences.KEY_AUTHORITY, userResponse.authority ?: "")
            Preferences.put(Preferences.KEY_BIO_ACTIVATED, userResponse.bioActivated ?: false)
            Preferences.put(Preferences.KEY_ORGANIZATION_ID, userResponse.organizationId ?: "")
            Preferences.put(Preferences.KEY_GROUP_ID, userResponse.groupId ?: "")
            Preferences.put(Preferences.KEY_PROFILE_URL, userResponse.profileUrl ?: "")

            Log.e("TAG", "login: ${userResponse.accessToken}")
            Log.e("TAG", "login: ${userResponse.refreshToken}")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Optional, close the LoginActivity if needed
            next()
        }

        private fun handleError(errorMessage: String?) {
            handlerLoading(false)
            errorMessage?.let {
                customLoginCheckDialog(getString(R.string.login_fail))
                Log.e("TAG", "Error: $it")
            }
        }

        private fun showLoading() {
        }

    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun handlerLoading(isLoading: Boolean) {
        if(_binding != null) {
            if (isLoading) {
                binding.llProgressBar.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.llProgressBar.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun customLoginCheckDialog(title:String){
        val confirmClickListener = DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
        }
        val dialogFragment = CustomEmailCheckDialog.newInstance(confirmClickListener,title)
        dialogFragment.show(supportFragmentManager, CustomRecordDialogFragment.TAG)
    }

    private fun init() {
        Preferences.init(this, Preferences.DB_USER_INFO)
        setupLoginButton()
        setPasswordFindButton()

        val reissue = intent.getBooleanExtra("reissue",false)

        if(reissue){
            customLoginCheckDialog("토큰이 만료되었습니다. 다시 로그인해주세요.")
        }

        viewModel.loginState.observe(this) { loginState ->
            when (loginState) {
                is NetworkState.Success -> { handleSuccess(loginState.data.data) }
                is NetworkState.Error -> { handleError(loginState.message) }
                is NetworkState.Loading -> { handlerLoading(true) }
            }
        }
    }
}