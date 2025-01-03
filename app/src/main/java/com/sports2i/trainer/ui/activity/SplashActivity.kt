package com.sports2i.trainer.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.sports2i.trainer.R
import com.sports2i.trainer.databinding.ActivitySplashBinding
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.TokenViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>({ActivitySplashBinding.inflate(it)}) {

    private val tokenViewModel: TokenViewModel by viewModels()
    private var executor: Executor? = null
    private var biometricPrompt: BiometricPrompt? = null
    private var promptInfo: BiometricPrompt.PromptInfo? = null

    private var autoLogin = false
    private var accessToken = ""
    private var refreshToken = ""
    private var userId = ""
    private var userName = ""
    private var email = ""
    private var authority = ""
    private var bioActivated = false
    private var organizationId = ""
    private var groupId = ""
    private var profileUrl = ""


    private val loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "registerForActivityResult - result : $result")
            if (result.resultCode == Activity.RESULT_OK) {
                authenticateToEncrypt()  //생체 인증 가능 여부확인 다시 호출
            } else {
                Log.d(TAG, "registerForActivityResult - NOT RESULT_OK")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init(){
        Preferences.init(this, Preferences.DB_USER_INFO)
        Global.setMode()

        autoLogin = Preferences.boolean(Preferences.KEY_AUTO_LOGIN)
        accessToken = Preferences.string(Preferences.KEY_ACCESS_TOKEN)
        refreshToken = Preferences.string(Preferences.KEY_REFRESH_TOKEN)
        userId = Preferences.string(Preferences.KEY_USER_ID)
        userName = Preferences.string(Preferences.KEY_USER_NAME)
        email = Preferences.string(Preferences.KEY_EMAIL)
        authority = Preferences.string(Preferences.KEY_AUTHORITY)
        bioActivated = Preferences.boolean(Preferences.KEY_BIO_ACTIVATED)
        organizationId = Preferences.string(Preferences.KEY_ORGANIZATION_ID)
        groupId = Preferences.string(Preferences.KEY_GROUP_ID)
        profileUrl = Preferences.string(Preferences.KEY_PROFILE_URL)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        biometricPrompt = setBiometricPrompt()
        promptInfo = setPromptInfo()

        startTrainer()

        // 자동 로그인 일때 토큰 요청 및 성공시 메인 생체인증 후 메인화면으로 이동
        tokenViewModel.tokenState.observe(this@SplashActivity){
            when(it) {
                is NetworkState.Loading -> {
                    Log.d(TAG, "tokenState.observe - Loading")
                }
                is NetworkState.Success -> {
                    Log.d(TAG, "tokenState.observe - Success")
                    val token = it.data
                    Preferences.put(Preferences.KEY_ACCESS_TOKEN, token.data.accessToken)
                    Global.myInfo.accessToken = token.data.accessToken
                    authenticateToEncrypt()
//                    startTrainer()
                }
                is NetworkState.Error -> {
                    Log.d(TAG, "tokenState.observe - Error")
                    // 토큰 요청 실패시 로그인 화면으로 이동
                    this?.let { it1 -> Preferences.init(it1, Preferences.DB_USER_INFO) }
                    Preferences.clear()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.putExtra("reissue",true)
                    this.startActivity(intent)
//                    Global.showBottomSnackBar(binding.root, getString(R.string.network_error))
                }
            }
        }

    }

    private fun startTrainer(){

        if (autoLogin) {

            Global.myInfo.accessToken = accessToken
            Global.myInfo.refreshToken = refreshToken
            Global.myInfo.userId = userId
            Global.myInfo.userName = userName
            Global.myInfo.email = email
            Global.myInfo.authority = authority
            Global.myInfo.bioActivated = bioActivated
            Global.myInfo.organizationId = organizationId
            Global.myInfo.profileUrl = profileUrl

            // 토큰 체크
            tokenViewModel.requestToken(refreshToken)

            }
        else {
                Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
                }, 1000) // 1000ms = 1초
            }
         }

    private fun setPromptInfo(): BiometricPrompt.PromptInfo {

        val promptBuilder: BiometricPrompt.PromptInfo.Builder = BiometricPrompt.PromptInfo.Builder()
        promptBuilder.setTitle("Biometric login for Trainer")
        promptBuilder.setSubtitle("Log in using your biometric credential")
        promptBuilder.setNegativeButtonText("Use account password")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { //  안면인식 ap사용 android 11부터 지원
//            promptBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            promptBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG) // DEVICE_CREDENTIAL 제거
//        }

        promptInfo = promptBuilder.build()
        return promptInfo as BiometricPrompt.PromptInfo
    }


    private fun setBiometricPrompt(): BiometricPrompt {
        executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this@SplashActivity, executor!!, object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // Use account password 클릭시 로그인 화면으로 이동
                if(errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON){
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Global.showBottomSnackBar(binding.root, getString(R.string.biometric_fail))
            }

        } )
        return biometricPrompt as BiometricPrompt
    }

    /*
* 생체 인식 인증을 사용할 수 있는지 확인
* */
    fun authenticateToEncrypt() = with(binding) {
        var textStatus = ""
        val biometricManager = BiometricManager.from(this@SplashActivity)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
//        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {

            //생체 인증 가능
//            BiometricManager.BIOMETRIC_SUCCESS -> textStatus = "App can authenticate using biometrics."

            //기기에서 생체 인증을 지원하지 않는 경우
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> textStatus = getString(R.string.biometric_no_hardware)

            //현재 생체 인증을 사용할 수 없는 경우
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> textStatus = getString(R.string.biometric_not_available)

            //생체 인식 정보가 등록되어 있지 않은 경우
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                textStatus = getString(R.string.biometric_un_enroll)

                val dialogBuilder = AlertDialog.Builder(this@SplashActivity)
                dialogBuilder
                    .setTitle(getString(R.string.app_name))
                    .setMessage(getString(R.string.biometric_move_to_enroll))
                    .setPositiveButton("확인") { dialog, which -> goBiometricSettings() }
                    .setNegativeButton("취소") {dialog, which -> dialog.cancel() }
                dialogBuilder.show()
            }

            //기타 실패
            else ->  {
                textStatus = "Fail Biometric facility"
                goAuthenticate()
                return@with
            }

        }
        notifyUser(textStatus)

        //인증 실행하기
        goAuthenticate()
    }


    override fun onResume() {
        super.onResume()
        init()
    }

    /*
        * 생체 인식 인증 실행
        * */
    private fun goAuthenticate() {
        Log.d(TAG, "goAuthenticate - promptInfo : $promptInfo")
        promptInfo?.let {
            biometricPrompt?.authenticate(it);  //인증 실행
        }
    }

    /*
    * 지문 등록 화면으로 이동
    * */
    fun goBiometricSettings() {
        val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
            putExtra(
                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        }
        loginLauncher.launch(enrollIntent)
    }

    private fun notifyUser(message: String){
        Global.showBottomSnackBar(binding.root, message)
    }
}

