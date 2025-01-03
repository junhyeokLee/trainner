package com.sports2i.trainer.ui.activity

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController.Companion.createRequestPermissionResultContract
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.UserResponse
import com.sports2i.trainer.databinding.ActivityMainBinding
import com.sports2i.trainer.ui.dialog.CustomDialogClickListener
import com.sports2i.trainer.ui.fragment.group.GroupFragment
import com.sports2i.trainer.ui.fragment.groupmember.GroupMemberFragment
import com.sports2i.trainer.ui.fragment.ingredient.IngredientFragment
import com.sports2i.trainer.ui.fragment.myexercise.MyExerciseFragment
import com.sports2i.trainer.ui.fragment.myprofile.MyProfileFragment
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG_GROUP = "group_fragment"
private const val TAG_GROUP_MEMBER = "group_member_fragment"
private const val TAG_MY_ROOM = "my_exercise_fragment"
private const val TAG_INGREDIENT = "ingredient_fragment"
private const val TAG_MY_PROFILE = "my_profile_fragment"

@AndroidEntryPoint
class MainActivity: BaseActivity<ActivityMainBinding>({ActivityMainBinding.inflate(it)}),
    TopBarHandler, BottomNavHandler, FragmentManager.OnBackStackChangedListener,CustomDialogClickListener  {

    private var selectedMenuItemId = 0 // 이전에 선택된 바텀 네비게이션 아이템의 ID를 저장하는 변수

    private val PERMISSION_REQUEST_FOREGROUND_SERVICE = 1001

    private var userId: String = ""
    private var authority: String = ""
    private var userInfo : UserResponse = UserResponse()
    private val viewModel: UserViewModel by viewModels()

    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView
    var topbar: Toolbar? = null
    var topbarLayout: AppBarLayout? = null

    private var topBarBackClickListener: View.OnClickListener? = null
    private var profileIconClicked = false

        companion object {
            val PERMISSIONS = setOf(
                HealthPermission.getReadPermission(StepsRecord::class),
                HealthPermission.getReadPermission(HeartRateRecord::class),
                HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
                HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
                HealthPermission.getReadPermission(DistanceRecord::class),
                HealthPermission.getReadPermission(SpeedRecord::class),
                HealthPermission.getReadPermission(SleepSessionRecord::class),
                HealthPermission.getReadPermission(ExerciseSessionRecord::class),
                HealthPermission.getWritePermission(ExerciseSessionRecord::class))
         }

    // 위치권한 설정 요청 함수
    private fun requestRejectLocationPermission() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("위치 권한 필요")
            .setMessage("이 앱에서는 위치 권한이 필요합니다. 권한을 설정하시겠습니까?")
            .setPositiveButton("확인") { dialog, which ->
                // 사용자가 확인 버튼을 클릭하면 앱 설정으로 이동
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("취소") { dialog, which ->
                // 사용자가 취소 버튼을 클릭하면 다른 조치를 취하지 않음
            }
            .show()
    }

    // 알림 권한 거부 시 사용자에게 안내하는 메서드를 추가
    private fun requestRejectNotificationPermission() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("알림 권한 필요")
            .setMessage("이 앱에서는 알림 권한이 필요합니다. 권한을 설정하시겠습니까?")
            .setPositiveButton("확인") { dialog, which ->
                presentNotificationSetting(this)
            }
            .setNegativeButton("취소") { dialog, which ->
            }.show()
    }

    // 헬스커넥트 설정 요청 함수
    private fun requestRejectHealthConnectPermission() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("위치 권한 필요")
            .setMessage("이 앱에서는 위치 권한이 필요합니다. 권한을 설정하시겠습니까?")
            .setPositiveButton("확인") { dialog, which ->
                // 사용자가 확인 버튼을 클릭하면 앱 설정으로 이동
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("취소") { dialog, which ->
                // 사용자가 취소 버튼을 클릭하면 다른 조치를 취하지 않음
            }
            .show()
    }
    fun presentNotificationSetting(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationSettingOreo(context)
        } else {
            notificationSettingOreoLess(context)
        }
        try {
            context.startActivity(intent)
        }catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun notificationSettingOreo(context: Context): Intent {
        return Intent().also { intent ->
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun notificationSettingOreoLess(context: Context): Intent {
        return Intent().also { intent ->
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", context.packageName)
            intent.putExtra("app_uid", context.applicationInfo?.uid)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    // 알림 권한
    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            setLocationPermission()
        } else {
            setLocationPermission()
//            requestRejectNotificationPermission() // 알림권한 거부시 들어올때마다 확인
        }
    }

    // 위치 권한
    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            setHealthConnection()
        } else {
            setHealthConnection()
//          requestRejectLocationPermission()
        }
    }

    // 헬스커넥트 권한
    val healthConnectPermissionsLauncher = registerForActivityResult(createRequestPermissionResultContract()) { granted ->
        if (granted.containsAll(PERMISSIONS)) {
        } else {
//            requestRejectHealthConnectPermission()
        }
    }

    private fun checkNotificationPermission() :Boolean {
        return NotificationManagerCompat.from(this).areNotificationsEnabled()
    }
    private fun requestNotificationPermission() {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setupBottomNavMenu(Global.myInfo.authority)
        networkStatus()
        setTopBar()

        // 백스택 변경 리스너 설정
        supportFragmentManager.addOnBackStackChangedListener(this)
    }

    fun init(){
        // FOREGROUND_SERVICE 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android Q 이상에서는 Foreground 서비스를 시작하려면 권한이 필요합니다.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                // Foreground 서비스 권한이 없으면 요청
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.FOREGROUND_SERVICE), PERMISSION_REQUEST_FOREGROUND_SERVICE)
            }
        }

        setNotificationPermission()
        setLocationPermission()
        setHealthConnection()

        Preferences.init(this, Preferences.DB_USER_INFO)
        userId = Preferences.string(Preferences.KEY_USER_ID)
        authority = Preferences.string(Preferences.KEY_AUTHORITY)
        viewModel.getUserInfo(userId)
    }

    // 가정: 사용자 권한을 확인하여 그에 맞게 메뉴를 구성
    fun setupBottomNavMenu(authority: String) {

        bottomNav = binding.bottomNavigationView
        val bottomNavMenu = bottomNav.menu
        bottomNavMenu.clear()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.groupNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.my_nav) // XML 네비게이션 그래프 파일을 지정합니다

        when (authority) {
            "ADMIN" -> {
                navGraph.setStartDestination(R.id.fragment_group)
                bottomNavMenu.add(0, R.id.fragment_group, 0, "그룹별").setIcon(R.drawable.ic_bottom_navigation_tab1)
                bottomNavMenu.add(0, R.id.fragment_group_member, 0, "그룹원별").setIcon(R.drawable.ic_bottom_navigation_tab2)
                bottomNavMenu.add(0, R.id.fragment_ingredient, 0, "성분검색").setIcon(R.drawable.ic_bottom_navigation_tab4)
                bottomNavMenu.add(0, R.id.fragment_my_profile, 0, "MY").setIcon(R.drawable.ic_bottom_navigation_tab5)

                setFragment(TAG_GROUP, GroupFragment())
            }

            else -> {
                navGraph.setStartDestination(R.id.fragment_my_exercise)
                bottomNavMenu.add(0, R.id.fragment_group, 0, "그룹별").setIcon(R.drawable.ic_bottom_navigation_tab1)
                bottomNavMenu.add(0, R.id.fragment_group_member, 0, "그룹원별").setIcon(R.drawable.ic_bottom_navigation_tab2)
                bottomNavMenu.add(0, R.id.fragment_my_exercise, 0, "내 운동룸").setIcon(R.drawable.ic_bottom_navigation_tab3)
                bottomNavMenu.add(0, R.id.fragment_ingredient, 0, "성분검색").setIcon(R.drawable.ic_bottom_navigation_tab4)
                bottomNavMenu.add(0, R.id.fragment_my_profile, 0, "MY").setIcon(R.drawable.ic_bottom_navigation_tab5)

                setFragment(TAG_MY_ROOM, MyExerciseFragment())
            }
        }

        navController.graph = navGraph // 시작 목적지를 설정
        bottomNav.setupWithNavController(navController)
        bottomNav.setItemIconTintList(null)

//        bottomNav.setOnItemReselectedListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.fragment_group_member -> {}
//                R.id.fragment_group -> {}
//                R.id.fragment_my_exercise -> {}
//                R.id.fragment_ingredient -> {}
//                R.id.fragment_my_profile -> {}
//            }
//        }

        bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.fragment_group_member -> {
                    // 그룹원별 프래그먼트로 이동
                    profileIconClicked = false
                    profileIconBackgroundChange()
//                    navController.navigate(R.id.fragment_group_member)
                    setFragment(TAG_GROUP_MEMBER, GroupMemberFragment())
                    true
                }
                R.id.fragment_group -> {
                    // 그룹별 프래그먼트로 이동
                    profileIconClicked = false
                    profileIconBackgroundChange()
//                    navController.navigate(R.id.fragment_group)
                    setFragment(TAG_GROUP, GroupFragment())
                    true
                }
                R.id.fragment_my_exercise -> {
                    // 내 운동룸 프래그먼트로 이동
                    profileIconClicked = false
                    profileIconBackgroundChange()
//                    navController.navigate(R.id.fragment_my_exercise)
                    setFragment(TAG_MY_ROOM, MyExerciseFragment())
                    true
                }
                R.id.fragment_ingredient -> {
                    // 성분검색 프래그먼트로 이동
                    profileIconClicked = false
                    profileIconBackgroundChange()
//                    navController.navigate(R.id.fragment_ingredient)
                    setFragment(TAG_INGREDIENT, IngredientFragment())
                    true
                }
                R.id.fragment_my_profile -> {
                    // 프로필 프래그먼트로 이동
                    profileIconClicked = true
                    profileIconBackgroundChange()
//                    navController.navigate(R.id.fragment_my_profile)
                    setFragment(TAG_MY_PROFILE, MyProfileFragment())

                    true
                }
                else -> false
            }
        }
    }

    fun setFragment(tag: String, fragment: Fragment){
        val manager: FragmentManager = supportFragmentManager
        val ft: FragmentTransaction = manager.beginTransaction()

        //트랜잭션에 tag로 전달된 fragment가 없을 경우 add
        if(manager.findFragmentByTag(tag) == null) {
            ft.add(R.id.groupNavHostFragment, fragment, tag)
            ft.addToBackStack(tag) // 백스택에 추가
        }
        //작업이 수월하도록 manager에 add되어있는 fragment들을 변수로 할당해둠
        val group = manager.findFragmentByTag(TAG_GROUP)
        val groupMember = manager.findFragmentByTag(TAG_GROUP_MEMBER)
        val myRoom = manager.findFragmentByTag(TAG_MY_ROOM)
        val ingredient = manager.findFragmentByTag(TAG_INGREDIENT)
        val myProfile = manager.findFragmentByTag(TAG_MY_PROFILE)

        //모든 프래그먼트 hide
        if(group!=null){
            ft.hide(group)
        }
        if(groupMember!=null){
            ft.hide(groupMember)
        }
        if(myRoom!=null){
            ft.hide(myRoom)
        }
        if(ingredient!=null){
            ft.hide(ingredient)
        }
        if(myProfile!=null){
            ft.hide(myProfile)
        }

        // 선택한 항목에 따라 그에 맞는 프래그먼트만 show
        when (tag) {
            TAG_GROUP -> if (group != null) ft.show(group)
            TAG_GROUP_MEMBER -> if (groupMember != null) ft.show(groupMember)
            TAG_MY_ROOM -> if (myRoom != null) ft.show(myRoom)
            TAG_INGREDIENT -> if (ingredient != null) ft.show(ingredient)
            TAG_MY_PROFILE -> if (myProfile != null) ft.show(myProfile)
        }
        ft.commitAllowingStateLoss()
        //ft.commit()
    }

    private fun profileIconBackgroundChange() {
        val bottomNavMenu = bottomNav.menu
        val myProfileMenuItem = bottomNavMenu.findItem(R.id.fragment_my_profile)

        if (userInfo.profileUrl.isNullOrEmpty()) {

        val firstInitial = userInfo.userName.firstOrNull()?.toString()?.toUpperCase() ?: "?"
        val backgroundColor = if (profileIconClicked) {
            getColor(R.color.green) // 클릭되었을 때의 배경색
        } else {
            getColor(R.color.green_alpha) // 클릭되지 않았을 때의 배경색
        }
        // 원형 배경을 그립니다.
        val bitmapSize = 100 // 원하는 크기로 변경
        val textSize = 50f // 텍스트 크기를 원하는 크기로 변경

        val bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
//        paint.color = getColor(R.color.green) // 배경색을 green으로 설정하는
            paint.color = backgroundColor // 배경색 설정
        canvas.drawCircle(bitmapSize / 2f, bitmapSize / 2f, bitmapSize / 2f, paint)

        paint.color = Color.WHITE // 텍스트 색상을 흰색으로 설정
        paint.textSize = textSize // 텍스트 크기를 설정
        paint.textAlign = Paint.Align.CENTER // 가운데 정렬
        val xPos = bitmapSize / 2f
        val yPos = (bitmapSize / 2f - (paint.descent() + paint.ascent()) / 2)
        canvas.drawText(firstInitial, xPos, yPos, paint)

        val drawable = BitmapDrawable(resources, bitmap)
        myProfileMenuItem.icon = drawable

        }
    }

    private fun setTopBar() {
        topbar = binding.topBar
        topbarLayout = binding.topBarLayout
        setSupportActionBar(topbar)
        supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setDisplayShowCustomEnabled(true)
        }
        binding.topBarBackButton.setOnClickListener { navController.navigateUp() }
    }

    private fun networkStatus(){
        viewModel.userState.observe(this) {
            when (it) {
                is NetworkState.Success -> { handlerUserSuccess(it.data.data) }
                is NetworkState.Error -> handleError(it.message)
                is NetworkState.Loading -> showLoading()
            }
        }
    }
    private fun handlerUserSuccess(user: UserResponse) {
        userInfo = user
        val bottomNavMenu = bottomNav.menu
        val myProfileMenuItem = bottomNavMenu.findItem(R.id.fragment_my_profile)
        val myProfileUrl = userInfo.profileUrl ?: null

        if (userInfo.profileUrl.isNullOrEmpty()) {
            profileIconBackgroundChange()
        } else {
            Glide.with(this)
                .asBitmap()
                .load(myProfileUrl)
                .apply(RequestOptions.circleCropTransform().placeholder(com.google.android.material.R.drawable.mtrl_ic_error))
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                        val drawable = BitmapDrawable(resources, resource)
                        myProfileMenuItem.icon = drawable   // 프로필 이미지로 아이콘을 설정
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        myProfileMenuItem.icon = placeholder // 프로필 이미지가 없을 경우 기본 이미지로 아이콘을 설정
                    }
                })
             }
          }
    private fun handleError(errorMessage: String?) {
        errorMessage?.let {
            Global.showBottomSnackBar(binding.root, it)
            Log.e("TAG", "Error: $it")
        }
    }
    private fun showLoading() {
//        Global.progressON(context)
    }

    private fun setNotificationPermission(){
        if(!checkNotificationPermission()) requestNotificationPermission()
//            requestRejectNotificationPermission() // 알림권한 체크
    }

    private fun setLocationPermission(){
        if (!checkLocationPermission()) requestLocationPermission() // 위치권한 체크
    }

    private fun setHealthConnection() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val healthConnectClient = HealthConnectClient.getOrCreate(this)
        lifecycleScope.launch {
            checkPermissionsAndRun(healthConnectClient)
        }
    }
    else {
            getHealthConnectCheck()
        }
    }

    suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(PERMISSIONS)) {
            // Permissions already granted; proceed with inserting or reading data
        } else {
            healthConnectPermissionsLauncher.launch(PERMISSIONS)
        }
    }

//    헬스커넥트 설치 다이얼로그 표시


    private fun getHealthConnectCheck(){
        val availabilityStatus = HealthConnectClient.getSdkStatus(this, packageName)
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            return // early return as there is no viable integration
        }
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            val dialogBuilder = AlertDialog.Builder(this@MainActivity)
            dialogBuilder
                .setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.healthconnect_install_message))
                .setPositiveButton("확인") { dialog, which ->
//                    val uriString = "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding"

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setPackage("com.android.vending")
                        data = Uri.parse("market://details").buildUpon()
                            .appendQueryParameter("id", "com.google.android.apps.healthdata")
                            .appendQueryParameter("url", "healthconnect://onboarding")
                            .build()
                    }
                    this.startActivity(intent)
                }
                .setNegativeButton("취소") {dialog, which ->
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // 현재 Activity를 종료하려면 추가합니다.
                }
            dialogBuilder.show()
            return
        }
        val healthConnectClient = HealthConnectClient.getOrCreate(this)
        lifecycleScope.launch {
            checkPermissionsAndRun(healthConnectClient)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUserInfo(userId) // 유저 정보 가져오기 (이미지 변경시 bottomNavigation 프로필 이미지 변경을 위해)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun showTopBar() {
        topbar?.visibility = View.VISIBLE
    }
    override fun hideTopBar() {
        topbar?.visibility = View.GONE
    }
    override fun setTopBarTitle(title: String) {
        binding.topBarTitle.text = title
    }
    override fun showTopBarTitle() {
        binding.topBarTitle.visibility = View.VISIBLE
    }
    override fun hideTopBarTitle() {
        binding.topBarTitle.visibility = View.GONE
    }
    override fun showTopBarBack() {
        binding.topBarBackButton.visibility = View.VISIBLE
    }
    override fun hideTopBarBack() {
        binding.topBarBackButton.visibility = View.GONE
    }

    override fun topbarBackTrainingEnroll(previewLayout: View, enrollLayout: View) {
        binding.topBarBackButton.setOnClickListener {
            if (previewLayout.visibility == View.VISIBLE) {
                previewLayout.visibility = View.GONE
                enrollLayout.visibility = View.VISIBLE
            } else {
//                navController.navigateUp()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // 현재 Activity를 종료하려면 추가합니다.
            }
        }
    }

    override fun topBarBackClickListener() {
        binding.topBarBackButton.setOnClickListener {
            topBarBackClickListener?.onClick(it)
        }
    }
    override fun setTopBarBackClickListener(listener: View.OnClickListener) {
        topBarBackClickListener = listener
    }

    override fun showBottomNav() {
        bottomNav.visibility = View.VISIBLE
    }
    override fun hideBottomNav() {
        bottomNav.visibility = View.GONE
    }

    // 이전에 선택된 바텀 네비게이션 아이템을 설정
    override fun onBackStackChanged() {
        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        if (backStackEntryCount == 0) { // 백스택이 비어 있는 경우
            showDialog("앱을 종료 하시겠습니까?")
        } else {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.groupNavHostFragment)
            currentFragment?.let { fragment ->
                val itemId = when (fragment) {
                    is GroupMemberFragment -> R.id.fragment_group_member
                    is GroupFragment -> R.id.fragment_group
                    is MyExerciseFragment -> R.id.fragment_my_exercise
                    is IngredientFragment -> R.id.fragment_ingredient
                    is MyProfileFragment -> R.id.fragment_my_profile
                    else -> 0
                }
                if (itemId != 0 && itemId != selectedMenuItemId) {
                    selectedMenuItemId = itemId
                    bottomNav.selectedItemId = itemId
                }
            }
        }
    }

    private fun showDialog(title:String) {
        Global.customDialog(title,true, supportFragmentManager, this)
    }
    override fun onPositiveButtonClick() {
        finish() // 앱 종료
    }
        override fun onNegativeButtonClick() {
            when(authority)
            {
                "ADMIN" -> setFragment(TAG_GROUP, GroupFragment())
                else -> setFragment(TAG_MY_ROOM, MyExerciseFragment())
            }
        }
    }

interface TopBarHandler {
    fun showTopBar()
    fun hideTopBar()
    fun setTopBarTitle(title: String)
    fun showTopBarTitle()
    fun hideTopBarTitle()
    fun showTopBarBack()
    fun hideTopBarBack()

    // 훈련등록 화면 뒤로가기 버튼 클릭 시 레이아웃 visible 변경
    fun topbarBackTrainingEnroll(previewLayout: View, enrollLayout: View)

    fun topBarBackClickListener()
    fun setTopBarBackClickListener(listener: View.OnClickListener)
}
interface BottomNavHandler {
    fun showBottomNav()
    fun hideBottomNav()
}


// 운영체제(OS) 정보 가져오기
//val osVersion: String = System.getProperty("os.version")

// 디바이스 모델 정보 가져오기
//val deviceModel: String = Build.MODEL

// 네트워크 정보 가져오기
//val connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//val networkInfo = connManager.activeNetworkInfo
//if (networkInfo != null && networkInfo.isConnected) {
//    val networkType: String = networkInfo.typeName // 네트워크 유형 (예: "WIFI", "MOBILE")
//    val networkState: String = networkInfo.state.toString() // 네트워크 상태 (예: "CONNECTED", "CONNECTING")
//}

// 국가 코드 가져오기
//val countryCode: String = resources.configuration.locale.country

// IP 주소 가져오기
//fun getDeviceIpAddress(): String? {
//    try {
//        val interfaces = NetworkInterface.getNetworkInterfaces().toList()
//        for (intf in interfaces) {
//            val addrs = intf.inetAddresses.toList()
//            for (addr in addrs) {
//                if (!addr.isLoopbackAddress && addr is Inet4Address) {
//                    return addr.hostAddress
//                }
//            }
//        }
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//    return null
//}