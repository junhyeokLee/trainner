package com.sports2i.trainer.ui.fragment.myprofile

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.UserResponse
import com.sports2i.trainer.databinding.FragmentMyProfileBinding
import com.sports2i.trainer.ui.activity.LoginActivity
import com.sports2i.trainer.ui.activity.MainActivity
import com.sports2i.trainer.ui.activity.EditProfileActivity
import com.sports2i.trainer.ui.fragment.BaseFragment
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.utils.TrainingTimerService
import com.sports2i.trainer.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class MyProfileFragment : BaseFragment<FragmentMyProfileBinding>() {
    companion object { private const val TRAINER_CACHE_FILE = ".trainer" }
    private val fragmentMyProfileBinding
        get() = binding!!

    private val viewModel: UserViewModel by viewModels()
    private lateinit var userInfo : UserResponse
    private var userId = ""

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMyProfileBinding {
        return FragmentMyProfileBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
        setContent()
        setFunction()
        networkStatus()
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        // 저장된 상태를 사용하여 데이터 초기화
//        if (savedInstanceState != null) {
//            userId = savedInstanceState.getString("userId", "")
//            userInfo = savedInstanceState.getParcelable("userInfo") ?: UserResponse() // 예시로 UserResponse의 기본 생성자 사용
//        } else {
//            // 저장된 상태가 없는 경우 초기화 로직 수행
//            Preferences.init(requireContext(), Preferences.DB_USER_INFO)
//            userId = Preferences.string(Preferences.KEY_USER_ID)
//            viewModel.getUserInfo(userId)
//        }
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 프래그먼트 상태 저장
        outState.putString("userId", userId)
        outState.putParcelable("userInfo", userInfo)
    }

    private fun init(savedInstanceState: Bundle?) {
        Preferences.init(requireContext(), Preferences.DB_USER_INFO)

        if (savedInstanceState != null) {
            Log.e("저정된 유저 정보","trye")
            userId = savedInstanceState.getString("userId", "")
            userInfo = savedInstanceState.getParcelable("userInfo") ?: UserResponse()
            updateUIWithData(userInfo)
        } else {
            Log.e("비어잇는 저장된 데이터","trye")
            userId = Preferences.string(Preferences.KEY_USER_ID)
            viewModel.getUserInfo(userId)
        }
    }

    override fun setContent() {
        showBottomNavigation()
        hideTopBar()
        showCacheUsage(requireContext())
    }

    override fun setFunction() {
        logout()
        fragmentMyProfileBinding.imgBtnEdit.setOnClickListener {
            val intent = Intent(context, EditProfileActivity::class.java)
            startActivity(intent)
        }
        fragmentMyProfileBinding.tvCacheDelete.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        fragmentMyProfileBinding.swAlarm.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                startTimerService()
            }
            else {
                stopTimerService()
            }

        }
    }

    // TrainingTimerService 시작
    private fun startTimerService() {
        val serviceIntent = Intent(requireContext(), TrainingTimerService::class.java)
        requireContext().startService(serviceIntent)
    }

    // TrainingTimerService 중지
    private fun stopTimerService() {
        val serviceIntent = Intent(requireContext(), TrainingTimerService::class.java)
        requireContext().stopService(serviceIntent)
    }

    override fun networkStatus() {
        viewModel.userState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Success -> { handlerUserSuccess(it.data.data) }
                is NetworkState.Error -> handleError(it.message)
                is NetworkState.Loading -> showLoading()
            }
        }
        viewModel.passwordResetState.observe(viewLifecycleOwner, { passwordResetState ->
            when (passwordResetState) {
                is NetworkState.Success -> handleSuccess(passwordResetState.data)
                is NetworkState.Error -> handleError(passwordResetState.message)
                is NetworkState.Loading -> showLoading()
            }
        })
    }

    private fun handlerUserSuccess(userInfo: UserResponse) {
        this.userInfo = userInfo
        updateUIWithData(userInfo)
    }

    private fun updateUIWithData(userInfo: UserResponse) {
        // UI 업데이트 코드는 handlerUserSuccess와 onCreate에서 공통으로 사용됩니다.
        // 이를 별도의 메서드로 분리하여 중복을 제거합니다.
        val name = userInfo.userName
        val email = userInfo.email
        val firstCharacter = if (name.isEmpty()) "Empty" else name[0]

        fragmentMyProfileBinding.userEmail.text = email
        fragmentMyProfileBinding.tvName.text = name
        fragmentMyProfileBinding.tvVersion.text = getAppVersion(requireContext())

        if (userInfo.profileUrl.isNullOrEmpty()) {
            fragmentMyProfileBinding.logoImg.visibility = View.VISIBLE
            fragmentMyProfileBinding.ivProfileImg.visibility = View.GONE
            fragmentMyProfileBinding.logoImg.text = firstCharacter.toString()
        } else {
            fragmentMyProfileBinding.logoImg.visibility = View.GONE
            fragmentMyProfileBinding.ivProfileImg.visibility = View.VISIBLE

            Glide.with(this)
                .load(userInfo.profileUrl)
                .centerCrop()
                .error(com.google.android.material.R.drawable.mtrl_ic_error)
                .into(fragmentMyProfileBinding.ivProfileImg)
        }
    }

    private fun handleSuccess(successMessage: String?) {
        successMessage?.let {
            Global.showBottomSnackBar(binding.root,getString(R.string.password_change))

            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
            this.activity?.finish()
        }
    }

    private fun handleError(errorMessage: String?) {
        errorMessage?.let {
            Global.showBottomSnackBar(binding.root,it)
        }
    }

    private fun showLoading() {
//        Global.progressON(context)
    }

    private fun logout(){
        fragmentMyProfileBinding.logOutView.setOnClickListener {
            context?.let { it1 -> Preferences.init(it1, Preferences.DB_USER_INFO) }
            Preferences.clear()
            // 이후 로그인 화면 또는 다른 필요한 화면으로 이동
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish() // 현재 화면 종료
        }
    }

    private fun checkCurrentPassword(userId: String, currentPassword: String): Boolean {
        // TODO: 현재 비밀번호 체크 로직을 구현
        // 현재 비밀번호가 맞으면 true, 아니면 false 반환
        return true // 임시로 true 반환하도록 설정
    }

    private fun showToast(message: String) {
        Global.showBottomSnackBar(binding.root,message)
    }

    override fun onResume() {
        super.onResume()
        showCacheUsage(requireContext())
//        viewModel.getUserInfo(userId) // 유저 정보 가져오기 (이미지 변경시 bottomNavigation 프로필 이미지 변경을 위해)
    }

    override fun onPause() {
        super.onPause()
    }

    fun getAppVersion(context: Context): String {
        try {
            val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return "N/A" // 에러 발생 시 기본값이나 에러 메시지를 반환하거나 처리할 수 있습니다.
        }
    }

    // 저장소 사용량을 형식화하는 함수
private fun formatMemorySize(bytes: Long): String {
    val kilobyte = bytes / 1024.0
    val megabyte = kilobyte / 1024.0
    return when {
        megabyte >= 1024 -> String.format("%.2f GB", megabyte / 1024)
        megabyte > 0 -> String.format("%.2f MB", megabyte) // 수정된 부분
        kilobyte > 0 -> String.format("%.2f KB", kilobyte)
        else -> "${bytes} B"
    }
}


// 캐시 사용량을 표시할 함수
private fun showCacheUsage(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
            val appStorageStats = storageStatsManager.queryStatsForUid(StorageManager.UUID_DEFAULT, context.applicationInfo.uid)
            val cacheBytes = appStorageStats.cacheBytes
            val formattedCacheSize = formatMemorySize(cacheBytes)
            fragmentMyProfileBinding.tvCacheSize.text = "${formattedCacheSize} 사용중"
        } catch (e: IOException) {
            e.printStackTrace()
        }
    } else {
        // Android Oreo 이상이 아닌 기기에 대한 처리
        // 캐시 사용량 추정
        val cacheUsage = estimateCacheUsage(requireContext())
        val formattedCacheUsage = formatMemorySize(cacheUsage)
        fragmentMyProfileBinding.tvCacheSize.text = "${formattedCacheUsage} 사용중"
    }
}


    // 오레오 버전 이하에서 캐시 디렉터리 스캔 및 캐시 사용량 추정 함수
    private fun estimateCacheUsage(context: Context): Long {
        var totalCacheSize: Long = 0
        // 앱 내부 캐시 디렉터리 경로
        val cacheDir = context.cacheDir
        // 캐시 디렉터리가 존재하고 디렉터리인지 확인
        if (cacheDir.exists() && cacheDir.isDirectory) {
            // 캐시 디렉터리 내의 파일 목록 가져오기
            val cacheFiles = cacheDir.listFiles()
            // 캐시 디렉터리 내의 각 파일에 대해 크기 합산
            for (file in cacheFiles) {
                // 파일 크기 합산
                totalCacheSize += file.length()
            }
        }
        // 총 캐시 사용량 반환
        return totalCacheSize
    }
}
