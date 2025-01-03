package com.sports2i.trainer.ui.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Outline
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.health.connect.client.records.ExerciseRoute
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.ExerciseRecentAchievementGraph
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.HealthData
import com.sports2i.trainer.data.model.TrainingGroupStatus
import com.sports2i.trainer.data.model.TrainingInfoResponse
import com.sports2i.trainer.data.model.TrainingSubDetailInsert
import com.sports2i.trainer.data.model.TrainingSubResponse
import com.sports2i.trainer.data.model.WeatherData
import com.sports2i.trainer.databinding.ActivityTrainingExerciseDetailMyBinding
import com.sports2i.trainer.ui.adapter.groupmember.HealthConnectDataAdapter
import com.sports2i.trainer.ui.adapter.groupmember.TrainingExerciseDetailUnitAdapter
import com.sports2i.trainer.ui.adapter.groupmember.TrainingRecentAchievementGraphAdapter
import com.sports2i.trainer.ui.dialog.CustomDialog
import com.sports2i.trainer.ui.dialog.CustomEmailCheckDialog
import com.sports2i.trainer.ui.dialog.CustomExerciseUpdateDialogFragment
import com.sports2i.trainer.ui.dialog.CustomRPEDialogFragment
import com.sports2i.trainer.ui.dialog.CustomRecordDialogFragment
import com.sports2i.trainer.ui.dialog.CustomRecordStartDialogFragment
import com.sports2i.trainer.ui.fragment.PoseCameraFragment
import com.sports2i.trainer.ui.view.CommentBottomSheet
import com.sports2i.trainer.ui.view.DateSelectionView
import com.sports2i.trainer.utils.DateTimeUtil
import com.sports2i.trainer.utils.DateTimeUtil.check5MinuteTimeDifference
import com.sports2i.trainer.utils.DateTimeUtil.formatDateSelection
import com.sports2i.trainer.utils.DateTimeUtil.formatTimeTrainingSubDetail
import com.sports2i.trainer.utils.FileUtil
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.HealthConnectViewModel
import com.sports2i.trainer.viewmodel.GroupViewModel
import com.sports2i.trainer.viewmodel.TrackingViewModel
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class TrainingExerciseDetailMyActivity : BaseActivity<ActivityTrainingExerciseDetailMyBinding>({ActivityTrainingExerciseDetailMyBinding.inflate(it)}),
    OnMapReadyCallback {

    private val groupViewModel: GroupViewModel by viewModels()
    private val trainingViewModel: TrainingViewModel by viewModels()
    private val healthConnectViewModel: HealthConnectViewModel by viewModels()
    private val trackingViewModel: TrackingViewModel by viewModels()

    private var userList: MutableList<GroupUser> = mutableListOf()
    private var exerciseDetailList : MutableList<TrainingInfoResponse> = mutableListOf()
    private var healthDataList = mutableListOf<HealthData>()

    private lateinit var trainingRecentAchivementGraphAdapter : TrainingRecentAchievementGraphAdapter
    private lateinit var trainingExerciseUnitAdapter : TrainingExerciseDetailUnitAdapter
    private lateinit var healthConnectDataAdapter : HealthConnectDataAdapter
    private var exoPlayer: ExoPlayer? = null

    var selectedGroup : GroupInfo? = null
    var selectedUserId : String? = null
    var selectedDateTime : String? = null

    private var trainingStartTime: String = ""
    private var trainingEndTime: String = ""
    private var trainingTime: String? = ""
    private var exerciseName: String? = ""
    private var exerciseId: String? = ""

    private var trainingSub : TrainingSubResponse = TrainingSubResponse()
    private var weatherData: WeatherData? = null
    private var googleMap: GoogleMap? = null
    private var bottomSheetFragment :CommentBottomSheet? = null

    private var recordingStartTime: Long? = null
    private var recordingEndTime: Long? = null
    private var startLongitude: Double? = 0.0
    private var startLatitude: Double? = 0.0
    private var endLongitude: Double? = 0.0
    private var endLatitude: Double? = 0.0
    private var weather : String = ""
    private var weatherDetail: String = ""
    private var temp: Double = 0.0
    private var humidity: Int = 0
    private var wind: Double = 0.0
    private var rpe: Int = 0
    private var tss: Int = 0
    private var videoUrl: String = ""

    private var dateSelectionView : DateSelectionView? = null

    private var updateData = false

    private var exerciseCount = 0

//  네트워크 상태 관찰자를 합쳐서 로딩 상태를 처리
    val networkStateObservers = listOf(
        ::observeGroupInfo,
        ::observeGroupUser,
        ::observeTrainingDetailExercise,
        ::observeTrainingExerciseUpdate,
        ::observeTrainingSubSearch,
        ::observeTrainingSubUpdate,
        ::observeTrainingSubInsert
    )

    val callBackActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val trainingSub = result.data?.getParcelableExtra<TrainingSubResponse>("trainingSub")
            val trackingSuccess = result.data?.getBooleanExtra("trackingSuccess", false)
            val poseSuccess = result.data?.getBooleanExtra("poseSuccess", false)
            val count = result.data?.getIntExtra("exerciseCount",0)

            if (trackingSuccess != null) {
                if (trackingSuccess) saveHealthData(trainingSub!!)
            }
            else {
                dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: DateTimeUtil.getCurrentDate())
            }

            if (poseSuccess != null) {
                exerciseCount = count!!
                if (poseSuccess) {
                    exerciseCount = count!!
                    saveHealthData(trainingSub!!)
                    checkEmptyData(trainingSub!!)
                }
            }
            else {
                dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: DateTimeUtil.getCurrentDate())
            }
        }
    }
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        init()
        setFunction()
        binding.root.post {
            setupRecyclerView()
            setupMap()
            observe()
            initializePlayer()
            setLiveData()
        }
        clickBack()
        this@TrainingExerciseDetailMyActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun init() {
        // 각종 뷰 및 UI 초기화 작업
        Preferences.init(this, Preferences.DB_USER_INFO)
        groupViewModel.getSelectedGroup(Preferences.string(Preferences.KEY_GROUP_ID))
        groupViewModel.requestGroupUser(Preferences.string(Preferences.KEY_GROUP_ID))

        // Intent로 전달된 데이터 추출
        selectedDateTime = intent.getStringExtra("dateTime")
        selectedUserId = intent.getStringExtra("userId")
        trainingTime = intent.getStringExtra("trainingTime")
        exerciseName = intent.getStringExtra("exerciseName")
        exerciseId = intent.getStringExtra("exerciseId")

        when(trainingTime){
            "T1" -> binding.trainingTime.text = "새벽"
            "T2" -> binding.trainingTime.text = "오전"
            "T3" -> binding.trainingTime.text = "오후"
            "T4" -> binding.trainingTime.text = "저녁"
            "T5" -> binding.trainingTime.text = "야간"
        }
    }

    private fun setLiveData() {
        dateSelectionView = DateSelectionView(this)
        binding.dateSelectionView.apply { addView(dateSelectionView) }
        dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: DateTimeUtil.getCurrentDate())
        dateSelectionView!!.selectedDate.observe(this, ::handleSelectedDate)
    }
    private fun handleSelectedDate(selectedDate: String) {
        selectedDateTime = formatDateSelection(selectedDate)
        trainingViewModel.getExerciseDetail(selectedUserId!!, selectedDateTime!!,exerciseId!!)
        trainingViewModel.getTrainingSubDetail(selectedUserId!!,exerciseId!!,selectedDateTime!!,trainingTime!!)
    }

    private fun getPoseCamera(){
        val intent = Intent(this, PoseCameraActivity::class.java)
        intent.putExtra("exerciseId", exerciseId)
        intent.putExtra("userId",selectedUserId)
        intent.putExtra("trainingTime",trainingTime)
        intent.putExtra("selectedDate",selectedDateTime)
        callBackActivityResultLauncher.launch(intent)
    }

    private fun setFunction() {
        refreshing()

        binding.btnComment.setOnClickListener {
            bottomSheetFragment?.show(supportFragmentManager, "bottomSheetFragment")
        }

        binding.tvRecord.setOnClickListener {
            if(exerciseId.isNullOrEmpty()) {
                Global.showBottomSnackBar(binding.root, "등록된 운동이 없습니다.")
                return@setOnClickListener
            }

            val videoDataRecordClickListener = DialogInterface.OnClickListener { dialog, _ ->
                when(exerciseId) {
                    "E02" -> { // 턱걸이 운동 이지만 테스트로 푸쉬업 카운팅 영상으로
                        getPoseCamera()
                        dialog.dismiss()
                    }
                    "E03" -> { // 로잉머신 이지만 테스트로 스쿼트 카운팅 영상으로
                        getPoseCamera()
                        dialog.dismiss()
                    }
                    "E04" -> { // 벤치프레스 이지만 테스트로 컨벤션 데드리스프
                        getPoseCamera()
                        dialog.dismiss()
                    }
                    "E05" -> { // 수영 이지만 테스트로 런지
                        getPoseCamera()
                        dialog.dismiss()
                    }

                    else -> {
                        val intent = Intent(this, VideoCaptureActivity::class.java)
                        intent.putExtra("exerciseId", exerciseId)
                        intent.putExtra("userId",selectedUserId)
                        intent.putExtra("trainingTime",trainingTime)
                        intent.putExtra("selectedDate",selectedDateTime)
                        callBackActivityResultLauncher.launch(intent)
                        dialog.dismiss()
                    }
                }

            }

            val dataRecordClickListener = DialogInterface.OnClickListener { dialog, _ ->

                if(!checkLocationPermission()) {
                    Global.showBottomSnackBar(binding.root, "위치 권한이 필요합니다.")
                    return@OnClickListener
                }
                getCurrentLocationAndShowOnMap("start")

                when(exerciseId) {
                    "E01" -> { // 달리기 일때
                        val intent = Intent(this, TrackingActivity::class.java)
                        intent.putExtra("exerciseId", exerciseId)
                        intent.putExtra("userId",selectedUserId)
                        intent.putExtra("trainingTime",trainingTime)
                        intent.putExtra("selectedDate",selectedDateTime)
                        callBackActivityResultLauncher.launch(intent)
                        dialog.dismiss()
                    }

                    else -> {
                        val pauseClickListener = DialogInterface.OnClickListener { dialog, _ ->
                            Global.showBottomSnackBar(binding.root, "일시중지")
                            dialog.dismiss()
                        }
                        val finishClickListener = DialogInterface.OnClickListener { dialog, _ ->
                            recordingEndTime = System.currentTimeMillis()
                            finishedRecordData()
                            dialog.dismiss()
                        }

                        val dialogDataRecordFragment = CustomRecordStartDialogFragment.newInstance(pauseClickListener, finishClickListener)
                        dialogDataRecordFragment.show(supportFragmentManager, CustomRecordDialogFragment.TAG)
                        dialog.dismiss()
                    }
                }
            }

            val dialogFragment = CustomRecordDialogFragment.newInstance(
                videoDataRecordClickListener, dataRecordClickListener
            )

            dialogFragment.show(supportFragmentManager, CustomRecordDialogFragment.TAG)
            recordingStartTime = System.currentTimeMillis() // Capture start time

        }

        binding.btnRpe.setOnClickListener {

            if(trainingSub.startDate.isEmpty() && trainingSub.endDate.isEmpty()){
                showStartTimeEmptyDialog()
                return@setOnClickListener
            }

            val cancelClickListener = DialogInterface.OnClickListener { dialog, _ ->
                Global.showBottomSnackBar(binding.root, getString(R.string.cancel))
                dialog.dismiss()
            }

            val saveClickListener = DialogInterface.OnClickListener { dialog, _ ->
                Global.showBottomSnackBar(binding.root, getString(R.string.save))
                dialog.dismiss()
            }

            val dialogFragment = CustomRPEDialogFragment.newInstance(
                cancelClickListener, saveClickListener
            )

            dialogFragment.mProgressValueDialogListener = object : CustomRPEDialogFragment.progressValueDialogListener {
                override fun setProgress(progress: Int) {
                    val trainingSub = TrainingSubResponse(trainingSub.exerciseId,trainingSub.userId,trainingSub.trainingTime,
                       trainingSub.trainingDate,progress,0,"","","",0.0,0.0,0.0,0.0,
                      "","",0.0,0.0,0.0,TrainingSubResponse.HealthData())
                    trainingViewModel.requestTrainingSubUpdate(trainingSub,"rpe")
                    updateData = true
                }
            }
            dialogFragment.show(supportFragmentManager, CustomRecordDialogFragment.TAG)
        }


        binding.tvDataDeleteRecord.setOnClickListener {
            val confirmClickListener = DialogInterface.OnClickListener { dialog, _ ->
                val trainingSub = TrainingSubResponse(trainingSub.exerciseId,trainingSub.userId,trainingSub.trainingTime,
                    trainingSub.trainingDate,0,0,"","","",0.0,0.0,0.0,0.0,
                    "","",0.0,0.0,0.0,TrainingSubResponse.HealthData())
                trainingViewModel.requestTrainingSubUpdate(trainingSub,"url")
                dialog.dismiss()
            }
            val dialogFragment = CustomEmailCheckDialog.newInstance(confirmClickListener,"동영상을 삭제하시겠습니까?")
            dialogFragment.show(supportFragmentManager, CustomRecordDialogFragment.TAG)
        }

        binding.tvDataEditRecord.setOnClickListener {
            val intent = Intent(this, VideoCaptureActivity::class.java)
            intent.putExtra("editVideo",true)
            intent.putExtra("exerciseId", exerciseId)
            intent.putExtra("userId",selectedUserId)
            intent.putExtra("trainingTime",trainingTime)
            intent.putExtra("selectedDate",selectedDateTime)
            callBackActivityResultLauncher.launch(intent)
        }
    }


    private fun finishedRecordData() {
        // Format the start and end times
        val startTimeFormattedInsert = formatTimeTrainingSubDetail(recordingStartTime!!)
        val endTimeFormattedInsert = formatTimeTrainingSubDetail(recordingEndTime!!)

        trainingStartTime = startTimeFormattedInsert
        trainingEndTime = endTimeFormattedInsert

        getCurrentLocationAndShowOnMap("end") // 서브 등록 성공시 구글맵 날씨 업데이트

//        if(check5MinuteTimeDifference(recordingStartTime!!,recordingEndTime!!)) {
//            // 5분 이하
//            val alertDialogBuilder = AlertDialog.Builder(this@TrainingExerciseDetailMyActivity)
//            alertDialogBuilder.setMessage("운동 시간이 짧을경우 헬스데이터 등록 어려움")
//            alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
//                dialog.dismiss()
//            }
//            val alertDialog = alertDialogBuilder.create()
//            alertDialog.show()
//        } else {
//            // 5분 이상
//            getCurrentLocationAndShowOnMap("end") // 서브 등록 성공시 구글맵 날씨 업데이트
//        }
    }

    private fun setupRecyclerView() {

        val healthRecyclerView = binding.rvBioData
        healthConnectDataAdapter = HealthConnectDataAdapter(this, healthDataList)
        healthRecyclerView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        healthRecyclerView.adapter = healthConnectDataAdapter

        val trainingExerciseUnitRecyclerView = binding.rvExerciseUnit
        trainingExerciseUnitAdapter = TrainingExerciseDetailUnitAdapter(this, mutableListOf())
        trainingExerciseUnitRecyclerView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        trainingExerciseUnitRecyclerView.adapter = trainingExerciseUnitAdapter

        trainingExerciseUnitAdapter.mListener = object : TrainingExerciseDetailUnitAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, trainingExerciseList: List<TrainingInfoResponse>) {
                // TrainingInfo 를 사용해서 업데이트 해야하는데 (training/update DTO) TrainingInfoResponse 를 받은 리스트들임
                val positiveButtonClickListener = DialogInterface.OnClickListener { dialog, _ ->
                    dialog.dismiss()
                }
                val negativeButtonClickListener = DialogInterface.OnClickListener { dialog, _ ->
                    // Negative 버튼 클릭 시 처리할 로직 작성
                    dialog.dismiss()
                }
                val dialogFragment = CustomExerciseUpdateDialogFragment.newInstance(
                    trainingExerciseList as MutableList<TrainingInfoResponse>,
                    positiveButtonClickListener, negativeButtonClickListener)

                if(trainingSub.startDate.isEmpty() && trainingSub.endDate.isEmpty()) showStartTimeEmptyDialog()
                else dialogFragment.show(supportFragmentManager, CustomExerciseUpdateDialogFragment.TAG)

                dialogFragment.mListener = object : CustomExerciseUpdateDialogFragment.OnSaveClickListener {
                    override fun onSaveClick(trainingExerciseList: List<TrainingInfoResponse>) {
                        // 사용자 groupID와 organizationID 설정
                        val modifiedTrainingInfoList = trainingExerciseList.map { trainingInfoResponse ->
                            // 특정 값으로 groupID와 organizationID 설정
                            val modifiedTrainingInfo = trainingInfoResponse.copy(
                                organizationId = Preferences.string(Preferences.KEY_ORGANIZATION_ID),
                                groupId = Preferences.string(Preferences.KEY_GROUP_ID)
                            )
                            modifiedTrainingInfo
                        }
                        trainingViewModel.requestExerciseUpdate(modifiedTrainingInfoList)
                        updateData = true
                    }
                }
            }
        }

        val recentActivityRecyclerView = binding.rvGraph1
        trainingRecentAchivementGraphAdapter = TrainingRecentAchievementGraphAdapter(this, mutableListOf())
        recentActivityRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        recentActivityRecyclerView.adapter = trainingRecentAchivementGraphAdapter

        bottomSheetFragment = CommentBottomSheet(this@TrainingExerciseDetailMyActivity, selectedUserId!!, selectedDateTime!!, trainingViewModel)
    }

    private fun setupMap() {
        // Map 설정
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

     }

    private fun checkEmptyExerciseData(exerciseDetailList: List<TrainingInfoResponse>) {
//            binding.layoutTrainingTime.visibility = if (exerciseDetailList.size > 0) View.VISIBLE else View.GONE
            updateExerciseList(exerciseDetailList)
            recentExerciseGraph(exerciseDetailList)
    }

    private fun saveHealthData(trainingSub:TrainingSubResponse) {
        handlerGetHealthLoading(true) // 로딩 시작
        Handler(mainLooper).postDelayed({
            if (_binding != null) {
                binding.cvBioData.apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    setContent {
                        HealthConnectData(healthConnectViewModel, trainingSub)
                    }
                }
            }
            handlerGetHealthLoading(false) // 5초 후에 로딩 종료
        }, 5000)
    }
    private fun checkEmptyData(trainingSub:TrainingSubResponse){

        when(trainingSub.healthData.energy_total){
            0.0 -> {
                if(trainingSub.startDate.isEmpty() && trainingSub.endDate.isEmpty()) {
                    binding.tvBioDataEmpty.visibility = View.VISIBLE
                    binding.rvBioData.visibility = View.GONE
                } else {
                    binding.tvBioDataEmpty.visibility = View.VISIBLE
                    binding.rvBioData.visibility = View.GONE
                    binding.cvBioData.apply {
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            HealthConnectData(healthConnectViewModel, trainingSub)
                        }
                    }
                }
            }
            else -> {
                binding.tvBioDataEmpty.visibility = View.GONE
                binding.rvBioData.visibility = View.VISIBLE

                healthDataList.clear()
                // Add data to the list
                healthDataList.add(HealthData(0, "energy_total", trainingSub.healthData.energy_total.toString()))
                healthDataList.add(HealthData(1, "speed_avg", String.format("%.02f", trainingSub.healthData.speed_avg)))
                healthDataList.add(HealthData(2, "speed_max", String.format("%.02f", trainingSub.healthData.speed_max)))
                healthDataList.add(HealthData(3, "speed_min", String.format("%.02f", trainingSub.healthData.speed_min)))
                healthDataList.add(HealthData(4, "distance_total", String.format("%.02f", trainingSub.healthData.distance_total)))
                healthDataList.add(HealthData(5, "bpm_avg", trainingSub.healthData.bpm_avg.toString()))
                healthDataList.add(HealthData(6, "bpm_max", trainingSub.healthData.bpm_max.toString()))
                healthDataList.add(HealthData(7, "bpm_min", trainingSub.healthData.bpm_min.toString()))

                val healthRecyclerView = binding.rvBioData
                healthConnectDataAdapter = HealthConnectDataAdapter(this, healthDataList)
                healthRecyclerView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
                healthRecyclerView.adapter = healthConnectDataAdapter
            }
        }

        when(trainingSub.url) {
            "" -> {
                videoUrl = ""
                if(trainingSub.startDate.isEmpty() && trainingSub.endDate.isEmpty()){
                    binding.layoutDataEmpty.visibility = View.VISIBLE
                    binding.layoutVideo.visibility = View.GONE
                    binding.layoutData.visibility = View.GONE
                    binding.layoutEditRecord.visibility = View.GONE
                }
                else {
                    binding.layoutData.visibility = View.VISIBLE

                    binding.layoutEditRecord.visibility = View.VISIBLE
                    binding.tvDataEditRecord.visibility = View.GONE
                    binding.tvDataDeleteRecord.visibility = View.GONE

                    binding.layoutDataEmpty.visibility = View.GONE
                    binding.layoutVideo.visibility = View.GONE
                }
            }
            else -> {
                videoUrl = trainingSub.url!!
                binding.layoutData.visibility = View.GONE
                binding.layoutDataEmpty.visibility = View.GONE
                binding.layoutVideo.visibility = View.VISIBLE

                binding.layoutEditRecord.visibility = View.VISIBLE
                binding.tvDataDeleteRecord.visibility = View.VISIBLE
                binding.tvDataEditRecord.visibility = View.GONE

                initializePlayer()
            }
        }

        when(trainingSub.weather) {
            "" -> {
                binding.tvWeatherDataEmpty.visibility = View.VISIBLE
                binding.weatherCard.visibility = View.GONE
            }
            else -> {
                binding.tvWeatherDataEmpty.visibility = View.GONE
                binding.weatherCard.visibility = View.VISIBLE

                val tempString = trainingSub.temp.toString() + " °C"
                val humidityString = trainingSub.humidity.toString() + " %"
                val windSpeedString = String.format("%.02f", trainingSub.wind) + " m/s"

                binding.tvWeather.text = trainingSub.weather
                binding.tvWeatherDescription.text = trainingSub.weatherDetail
                binding.tvWeatherTemp.text = tempString
                binding.tvWeatherHumidity.text = humidityString
                binding.tvWeatherWind.text = windSpeedString
            }
        }

        when(trainingSub.start_latitude){
            0.0 -> {
                binding.tvMapDataEmpty.visibility = View.VISIBLE
                binding.llGoogleMap.visibility = View.GONE
            }
            else -> {
                binding.tvMapDataEmpty.visibility = View.GONE
                binding.llGoogleMap.visibility = View.VISIBLE

                trackingViewModel.getTrackingData(exerciseId!!, selectedUserId!!, trainingTime!!, selectedDateTime!!)

                googleMap!!.uiSettings.isZoomControlsEnabled = false // 줌 컨트롤 숨기기
                googleMap!!.uiSettings.isScrollGesturesEnabled = false // 스크롤 제스처 비활성화

                val distance = trainingSub.healthData.distance_total ?: 0
                val caloriesBurned = trainingSub.healthData.energy_total ?: 0
                val speed = trainingSub.healthData.speed_avg ?: 0

                trackingViewModel.trackingData.observe(this) { trackingData ->
                    if (trackingData != null) {
                            // 경로 그리기
                            val path = PolylineOptions()
                            path.width(10f)
                            path.color(getColor(R.color.primary))
                            for (point in trackingData.pathPoints.last()) {
                                path.add(point)
                            }
                            googleMap?.addPolyline(path)

                            // 시작점과 마지막점에 마커 추가
                            if (trackingData.pathPoints.isNotEmpty()) {
                                // 시작점
                                val startPoint = trackingData.pathPoints.last().first()
                                val startMarkerOptions = MarkerOptions().position(startPoint).title("시작점")
                                val startBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(FileUtil.resizeBitmap(FileUtil.getBitmapFromVector(this, R.drawable.ic_circle_hollow), 50, 50))
                                startMarkerOptions.icon(startBitmapDescriptor)
                                googleMap?.addMarker(startMarkerOptions)

                                // 도착점
                                val endPoint = trackingData.pathPoints.last().last()
                                val endMarkerOptions = MarkerOptions().position(endPoint).title("도착점")
                                val endBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(FileUtil.resizeBitmap(FileUtil.getBitmapFromVector(this, R.drawable.ic_tracking_finish), 50, 50))
                                endMarkerOptions.icon(endBitmapDescriptor)
                                googleMap?.addMarker(endMarkerOptions)

                                // 지도 화면 이동
                                val boundsBuilder = LatLngBounds.builder()
                                for (pointList in trackingData.pathPoints) {
                                    for (point in pointList) {
                                        boundsBuilder.include(point)
                                    }
                                }
//                                val bounds = boundsBuilder.build()
//                                val padding = resources.getDimensionPixelSize(R.dimen.google_map_padding) // 마커가 화면에 보이도록 여백 추가
//                                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
//                                googleMap?.moveCamera(cameraUpdate)

                                googleMap?.let { map ->
                                    val bounds = boundsBuilder.build()
                                    val padding = resources.getDimensionPixelSize(R.dimen.google_map_padding) // 마커가 화면에 보이도록 여백 추가
                                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                                    map.setOnMapLoadedCallback {
                                        map.moveCamera(cameraUpdate)
                                    }
                                }


                                googleMap?.setOnMapClickListener {
                                    val intent = Intent(this@TrainingExerciseDetailMyActivity, TrackingMapActivity::class.java)
                                    intent.putExtra("exerciseId", exerciseId)
                                    intent.putExtra("userId",selectedUserId)
                                    intent.putExtra("trainingTime",trainingTime)
                                    intent.putExtra("selectedDate",selectedDateTime)
                                    intent.putExtra("distance",distance)
                                    intent.putExtra("caloriesBurned",caloriesBurned)
                                    intent.putExtra("speed",speed)
                                    startActivity(intent)
                                }
                            }

                    } else {
                        val startBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(FileUtil.resizeBitmap(FileUtil.getBitmapFromVector(this, R.drawable.ic_circle_hollow), 50, 50))
                        val endBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(FileUtil.resizeBitmap(FileUtil.getBitmapFromVector(this, R.drawable.ic_tracking_finish), 50, 50))

                        val startMarkerOptions = MarkerOptions().position(LatLng(trainingSub.start_latitude, trainingSub.start_longitude)).title("시작점")
                        val endMarkerOptions = MarkerOptions().position(LatLng(trainingSub.end_latitude, trainingSub.end_longitude)).title("도착점")
                        startMarkerOptions.icon(startBitmapDescriptor)
                        endMarkerOptions.icon(endBitmapDescriptor)
                        googleMap?.setOnMapClickListener {

                        }
                        googleMap?.addMarker(startMarkerOptions)
                        googleMap?.addMarker(endMarkerOptions)
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(trainingSub.start_latitude, trainingSub.start_longitude), 17f))
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(trainingSub.end_latitude, trainingSub.end_longitude), 17f))

                        googleMap?.setOnMapClickListener {
                            val intent = Intent(this@TrainingExerciseDetailMyActivity, TrackingMapActivity::class.java)
                            intent.putExtra("exerciseId", exerciseId)
                            intent.putExtra("userId",selectedUserId)
                            intent.putExtra("trainingTime",trainingTime)
                            intent.putExtra("selectedDate",selectedDateTime)
                            intent.putExtra("startLatitude",trainingSub.start_latitude)
                            intent.putExtra("startLongitude",trainingSub.start_longitude)
                            intent.putExtra("endLatitude",trainingSub.end_latitude)
                            intent.putExtra("endLongitude",trainingSub.end_longitude)
                            intent.putExtra("distance",distance)
                            intent.putExtra("caloriesBurned",caloriesBurned)
                            intent.putExtra("speed",speed)
                            startActivity(intent)
                        }
                    }
                }
            }
        }



        when(trainingSub.rpe){
            0 -> {
                binding.btnRpe.visibility = View.VISIBLE
                binding.layoutRpe.visibility = View.GONE
            }
            else -> {
                binding.btnRpe.visibility = View.GONE
                binding.layoutRpe.visibility = View.VISIBLE
                binding.tvRpe.text = "운동 자각도: "+trainingSub.rpe.toString()
                binding.tvTss.text = "훈련부하: "+trainingSub.tss.toString()
            }
        }
    }

    private fun getCurrentLocationAndShowOnMap(time: String) {
        if(!checkLocationPermission()) {
            Global.showBottomSnackBar(binding.root, "위치 권한이 필요합니다.")
            return
        }
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val time = time
                    showLocationOnMap(it.latitude, it.longitude,time)
                } ?: run {}
            }
            .addOnFailureListener { e -> }
    }

    private fun showLocationOnMap(latitude: Double, longitude: Double,time:String) {
        // 운동시작할때의 위도경도 마커 지정
        when(time){
            "start" -> {
                startLatitude = latitude
                startLongitude = longitude
            }
            "end" -> {
                endLatitude = latitude
                endLongitude = longitude

                // 날씨 데이터 저장
                mapAndWeatherInsert(latitude, longitude,startLatitude!!,startLongitude!!,endLatitude!!,endLongitude!!)
                // 운동 끝날때의 위도 경도로 날씨 데이터 요청 하면서 완료시 subDetail insert API 호출
            }
            else ->{
                Global.showBottomSnackBar(binding.root, "잘못된 위치 정보입니다.")
            }
        }
    }

    private fun updateExerciseList(selectedList: List<TrainingInfoResponse>) {
        // Clear all adapters'
        if(selectedList.isEmpty()) {
            binding.exerciseNameTextview.text = ""
            trainingExerciseUnitAdapter.clearData()
            binding.layoutTrainingTime.visibility = View.GONE
            binding.tvTrainingTimeEmpty.visibility = View.VISIBLE
        }
        else {
            binding.layoutTrainingTime.visibility = View.VISIBLE
            binding.tvTrainingTimeEmpty.visibility = View.GONE
            val trainingDateToExerciseListMap = selectedList.groupBy { it.trainingDate }
            exerciseDetailList = (trainingDateToExerciseListMap[selectedDateTime] ?: emptyList()) as MutableList<TrainingInfoResponse>
            binding.exerciseNameTextview.text = exerciseDetailList.firstOrNull()?.exerciseName ?: ""

            val trainingDateToExerciseListMdap = exerciseDetailList.groupBy { it.trainingTime }
            exerciseDetailList = (trainingDateToExerciseListMdap[trainingTime] ?: emptyList()) as MutableList<TrainingInfoResponse>

            trainingExerciseUnitAdapter.addListData(exerciseDetailList)

//            for(trainingInfoResponse in exerciseDetailList) {
//                val trainingDateToExerciseListMap = exerciseDetailList.groupBy { it.trainingTime }
//                exerciseDetailList = (trainingDateToExerciseListMap[trainingTime] ?: emptyList()) as MutableList<TrainingInfoResponse>
//                trainingExerciseUnitAdapter.addListData(exerciseDetailList)
//                Log.e("ㅇㅇㅇ",exerciseDetailList.toString())
//            }
        }
    }

    private fun recentExerciseGraph(trainingInfoGraphList: List<TrainingInfoResponse>) {

        if (trainingInfoGraphList.isEmpty()) {
            binding.rvGraph1.removeAllViews()
            trainingRecentAchivementGraphAdapter.clearData()
            binding.rvGraph1.visibility = View.GONE
            binding.tvGraph1Empty.visibility = View.VISIBLE

            return
        } else {
            binding.rvGraph1.visibility = View.VISIBLE
            binding.tvGraph1Empty.visibility = View.GONE
            // 그룹화하기 전에 exerciseId로 중복된 항목들의 totalAchieveRate를 리스트로 만들기
            val groupedExerciseList = trainingInfoGraphList.groupBy { it.exerciseUnitName }
                .map { (exerciseUnitName, exerciseGraphList) ->
                    ExerciseRecentAchievementGraph(
                        trainingUnitName = exerciseUnitName!!,
                        trainingUnit = exerciseGraphList[0].exerciseUnit!!,
                        trainingDates = exerciseGraphList.mapNotNull { it.trainingDate },
                        achieveRates = exerciseGraphList.mapNotNull { it.achieveRate?.toInt() ?: 0 }
                    )
                }
//        exerciseDetailList = groupedExerciseList as MutableList<TrainingInfoResponse>
            // 어댑터에 그룹화된 아이템과 achieveRateList 전달
            trainingRecentAchivementGraphAdapter.addListData(groupedExerciseList)
        }
    }

    private fun refreshing() {
        binding.refreshLayout?.setOnRefreshListener {
//            handleSelectedDate(dateSelectionView!!.getSelectedDate())
            dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: DateTimeUtil.getCurrentDate())
            binding.refreshLayout?.isRefreshing = false
        }
    }

    private fun initializePlayer() {

        if (exoPlayer == null && videoUrl.isNotEmpty()) {

            val layoutVideo = binding.layoutVideo
            val playerView = binding.playerView

            playerView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(0, 0, view!!.getWidth(), view!!.getHeight(), 20f)
                }
            }
            playerView.setClipToOutline(true);

            binding.layoutVideo.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    layoutVideo.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val lp = playerView.layoutParams
                    lp.width = layoutVideo.width
                    lp.height = layoutVideo.height
                    playerView.layoutParams = lp
                }
            })

            val mediaDataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this)
            val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUrl))
            val mediaSourceFactory = DefaultMediaSourceFactory(mediaDataSourceFactory)

            exoPlayer = ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()

            exoPlayer!!.addMediaSource(mediaSource)
            exoPlayer!!.prepare()
            exoPlayer!!.repeatMode = Player.REPEAT_MODE_ONE
            exoPlayer!!.playWhenReady = true
            binding.playerView.player = exoPlayer
            binding.playerView.requestFocus()
        }
    }


    private fun releasePlayer() {
        if (exoPlayer != null && videoUrl.isNotEmpty()) {
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    private inline fun <reified T : Any> observeNetworkState(
        stateLiveData: LiveData<NetworkState<T>>,
        crossinline onSuccess: (NetworkState.Success<T>) -> Unit,
        crossinline onError: (NetworkState.Error) -> Unit
    ) {
        stateLiveData.observe(this) { state ->
            when (state) {
                is NetworkState.Success -> {
                    onSuccess(state)
                    Handler(mainLooper).postDelayed({ handlerLoading(false) }, 200)
                }
                is NetworkState.Error -> {
                    handlerError(state.message)
                    onError(state)
                    Handler(mainLooper).postDelayed({ handlerLoading(false) }, 200)
                }
                is NetworkState.Loading -> {
                    handlerLoading(state.isLoading)
                }
            }
        }
    }

    private fun observeGroupInfo() = observeNetworkState(
        groupViewModel.selectedGroupInfoState,{handlerSelectedGroupInfoSuccess(it.data.data)},
        { Log.d("error", it.message) })

    private fun observeGroupUser() = observeNetworkState(
        groupViewModel.groupUserState, { handlerGroupUserSuccess(it.data.data)},
        { Log.d("error", it.message)})

    private fun observeTrainingDetailExercise() = observeNetworkState(
        trainingViewModel.trainingDetailExerciseState, { handlerTrainingDetailExerciseSuccess(it.data.data)},
        {
//            exerciseDetailList.clear()
//            checkEmptyExerciseData(exerciseDetailList)
        })
    private fun observeTrainingExerciseUpdate() = observeNetworkState(
        trainingViewModel.trainingExerciseUpdateState, { handlerTrainingExerciseUpdateSuccess(it.data)},
        { Log.d("error", it.message) })

    private fun observeTrainingSubSearch() = observeNetworkState(
        trainingViewModel.trainingSubSearchState, { handlerTrainingSubDetailSuccess(it.data.data)},
        {
            trainingSub = TrainingSubResponse()
            checkEmptyData(trainingSub)
            Log.e("trainingSubSearch error : ", it.message)
        })

    private fun observeTrainingSubUpdate() = observeNetworkState(
        trainingViewModel.trainingSubDetailUpdateState, { handlerTrainingSubUpdateSuccess(it.data.data!!)},
        { Log.d("error", it.message) })

    private fun observeTrainingSubInsert() = observeNetworkState(
        trainingViewModel.trainingSubInsertState, { handlerTrainingSubInsertSuccess(it.data.data)},
        { Log.d("error", it.message) })

    private fun observe() {
        networkStateObservers.forEach { observer ->
            observer.invoke()
        }
    }

    private fun handlerSelectedGroupInfoSuccess(groupInfos: GroupInfo) {
        selectedGroup = groupInfos
    }

    private fun handlerGroupUserSuccess(groupUsers: MutableList<GroupUser>) {
        userList = groupUsers
        selectedUserId = Preferences.string(Preferences.KEY_USER_ID)
    }

    private fun handlerTrainingDetailExerciseSuccess(trainingDetailExercise: List<TrainingInfoResponse>) {
        exerciseDetailList.clear()
        exerciseDetailList.addAll(trainingDetailExercise)


        checkEmptyExerciseData(exerciseDetailList)
    }

    private fun handlerTrainingSubInsertSuccess(trainingSubResponse: TrainingSubResponse) {
        trainingSub = trainingSubResponse
//      checkEmptyData(trainingSub)
//      운동 종료시 헬스데이터 업데이트
        saveHealthData(trainingSub)
    }
    private fun handlerTrainingExerciseUpdateSuccess(sucessMessage: String) {
        val message = sucessMessage
        trainingViewModel.getExerciseDetail(selectedUserId!!, selectedDateTime!!,exerciseId!!)
    }
    private fun handlerTrainingSubDetailSuccess(trainingSubResponse: TrainingSubResponse) {
        trainingSub = trainingSubResponse
        checkEmptyData(trainingSub)
    }
    private fun handlerTrainingSubUpdateSuccess(trainingSubResponse: TrainingSubResponse) {
        trainingSub = trainingSubResponse
        checkEmptyData(trainingSub)
    }

    private fun handlerError(errorMessage: String?) {
        errorMessage?.let {
//            Log.e("TAG", "Error: $it")
        }
    }

    @Composable
    fun HealthConnectData(healthConnectViewModel: HealthConnectViewModel,trainingSubResponse: TrainingSubResponse) {

        val startDateTime = trainingSubResponse.startDate.replace("T", " ")
        val endDateTime = trainingSubResponse.endDate.replace("T", " ")

        // SimpleDateFormat을 사용하여 String 형식의 날짜를 Date 객체로 변환
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val startTime: Date = sdf.parse(startDateTime)
        val endTime: Date = sdf.parse(endDateTime)
        // 시작 시간과 종료 시간을 분 단위로 변환.
        val diffInMillies = endTime.time - startTime.time
        // 분처리
        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillies)

        val healthDataList = mutableListOf<HealthData>()

        healthConnectViewModel.fetchData(startDateTime, endDateTime)

        val calories by healthConnectViewModel.flowCalories.collectAsState()
        val speedAvg by healthConnectViewModel.flowSpeedAvg.collectAsState()
        val speedMax by healthConnectViewModel.flowSpeedMax.collectAsState()
        val speedMin by healthConnectViewModel.flowSpeedMin.collectAsState()
        val distance by healthConnectViewModel.flowDistance.collectAsState()
        val heartRateAvg by healthConnectViewModel.flowHeartRateAvg.collectAsState()
        val heartRateMax by healthConnectViewModel.flowHeartRateMax.collectAsState()
        val heartRateMin by healthConnectViewModel.flowHeartRateMin.collectAsState()
        val exerciseRoute by healthConnectViewModel.flowExerciseRoute.collectAsState()
//          val sleepTime by healthConnectViewModel.flowSleepTime.collectAsState()

        // 칼로리와 거리 까지만 체크 후 0일때 저장하지 않고 리턴
        if(calories == 0 && distance == 0.0) {
            binding.tvBioDataEmpty.visibility = View.VISIBLE
            binding.rvBioData.visibility = View.GONE
            return

        } else {

            healthDataList.add(HealthData(0, "energy_total", String.format("%d", calories)))
            healthDataList.add(HealthData(1, "speed_avg", String.format("%.02f", speedAvg)))
            healthDataList.add(HealthData(2, "speed_max", String.format("%.02f", speedMax)))
            healthDataList.add(HealthData(3, "speed_min", String.format("%.02f", speedMin)))
            healthDataList.add(HealthData(4, "distance_total", String.format("%.02f", distance)))
            healthDataList.add(HealthData(5, "bpm_avg", heartRateAvg.toString()))
            healthDataList.add(HealthData(6, "bpm_max", heartRateMax.toString()))
            healthDataList.add(HealthData(7, "bpm_min", heartRateMin.toString()))
            healthConnectDataAdapter.addItems(healthDataList)

            binding.tvBioDataEmpty.visibility = View.GONE
            binding.rvBioData.visibility = View.VISIBLE

            val healthData = TrainingSubResponse.HealthData(
                String.format("%d", calories).toDouble(), String.format("%.02f", speedAvg).toDouble(),
                String.format("%.02f", speedMax).toDouble(), String.format("%.02f", speedMin).toDouble(),
                String.format("%.02f", distance).toDouble(),
                heartRateAvg!!.toDouble(),
                heartRateMax!!.toDouble(),
                heartRateMin!!.toDouble())

            val trainingSubDetailRequest = TrainingSubResponse(
                exerciseId!!,
                selectedUserId!!,
                trainingTime!!,
                selectedDateTime!!,
                trainingSubResponse.rpe,
                trainingSubResponse.tss,
                trainingSubResponse.startDate,
                trainingSubResponse.endDate,
                trainingSubResponse.url,
                trainingSubResponse.start_longitude,
                trainingSubResponse.start_latitude,
                trainingSubResponse.end_longitude,
                trainingSubResponse.end_latitude,
                trainingSubResponse.weather,
                trainingSubResponse.weatherDetail,
                trainingSubResponse.temp,
                trainingSubResponse.humidity,
                trainingSubResponse.wind,
                healthData)


            val organizationId = Preferences.string(Preferences.KEY_ORGANIZATION_ID)
            val groupId = Preferences.string(Preferences.KEY_GROUP_ID)

            when(exerciseId) {
                "E01" -> { // 달리기일 경우 헬스커넥트 데이터 자동기입
                    val trainingInfoList: MutableList<TrainingInfoResponse> = mutableListOf()
                    exerciseDetailList.forEach {
                        // 거리
                        if(it.exerciseUnitId == "U01") {
                            trainingInfoList.add(
                                TrainingInfoResponse(organizationId,groupId,it.trainingId,selectedUserId,selectedDateTime,trainingTime,exerciseId,
                                    it.exerciseName,it.exerciseUnitId,it.exerciseUnit,it.exerciseUnitName,
                                    it.goalValue,String.format("%.02f", distance).toDouble(),it.achieveRate,it.exerciseAchieveRate,it.totalAchieveRate))
                        }
                        // 분
                        if(it.exerciseUnitId == "U04") {
                            trainingInfoList.add(
                                TrainingInfoResponse(organizationId,groupId,it.trainingId,selectedUserId,selectedDateTime,trainingTime,exerciseId,
                                    it.exerciseName,it.exerciseUnitId,it.exerciseUnit,it.exerciseUnitName,
                                    it.goalValue,diffInMinutes.toDouble(),it.achieveRate,it.exerciseAchieveRate,it.totalAchieveRate))
                        }
                    }

                    if (trainingInfoList.isNotEmpty()) {
                        trainingViewModel.requestExerciseUpdate(trainingInfoList)
                        updateData = true
                    }
                }

                "E02" -> { // 턱길이지만 푸쉬업 자동기입
                    val trainingInfoList: MutableList<TrainingInfoResponse> = mutableListOf()
                    exerciseDetailList.forEach {
                        // 분
                        if(it.exerciseUnitId == "U04") {
                            trainingInfoList.add(
                                TrainingInfoResponse(organizationId,groupId,it.trainingId,selectedUserId,selectedDateTime,trainingTime,exerciseId,
                                    it.exerciseName,it.exerciseUnitId,it.exerciseUnit,it.exerciseUnitName,
                                    it.goalValue,diffInMinutes.toDouble(),it.achieveRate,it.exerciseAchieveRate,it.totalAchieveRate))
                        }
                        // 횟수
                        if(it.exerciseUnitId == "U05") {
                            trainingInfoList.add(
                                TrainingInfoResponse(organizationId,groupId,it.trainingId,selectedUserId,selectedDateTime,trainingTime,exerciseId,
                                    it.exerciseName,it.exerciseUnitId,it.exerciseUnit,it.exerciseUnitName,
                                    it.goalValue,exerciseCount.toDouble(),it.achieveRate,it.exerciseAchieveRate,it.totalAchieveRate))
                        }
                    }

                    if (trainingInfoList.isNotEmpty()) {
                        trainingViewModel.requestExerciseUpdate(trainingInfoList)
                        updateData = true
                    }
                }

                "E03" -> { // 로잉머신 이지만 스쿼트 자동기입
                    val trainingInfoList: MutableList<TrainingInfoResponse> = mutableListOf()
                    exerciseDetailList.forEach {
                        // 분
                        if(it.exerciseUnitId == "U04") {
                            trainingInfoList.add(
                                TrainingInfoResponse(organizationId,groupId,it.trainingId,selectedUserId,selectedDateTime,trainingTime,exerciseId,
                                    it.exerciseName,it.exerciseUnitId,it.exerciseUnit,it.exerciseUnitName,
                                    it.goalValue,diffInMinutes.toDouble(),it.achieveRate,it.exerciseAchieveRate,it.totalAchieveRate))
                        }
                        // 횟수
                        if(it.exerciseUnitId == "U05") {
                            trainingInfoList.add(
                                TrainingInfoResponse(organizationId,groupId,it.trainingId,selectedUserId,selectedDateTime,trainingTime,exerciseId,
                                    it.exerciseName,it.exerciseUnitId,it.exerciseUnit,it.exerciseUnitName,
                                    it.goalValue,exerciseCount.toDouble(),it.achieveRate,it.exerciseAchieveRate,it.totalAchieveRate))
                        }
                    }

                    if (trainingInfoList.isNotEmpty()) {
                        trainingViewModel.requestExerciseUpdate(trainingInfoList)
                        updateData = true
                    }
                }
            }

            trainingViewModel.requestTrainingSubUpdate(trainingSubDetailRequest,"health")
        }
    }


    fun displayExerciseRoute(route: ExerciseRoute?) {
        // Handle displaying exercise route
        route?.let { exerciseRoute ->
            val routeLocations = exerciseRoute.route.orEmpty()
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

            mapFragment?.getMapAsync { googleMap ->
                val polylineOptions = PolylineOptions()
                polylineOptions.color(Color.BLUE)
                polylineOptions.width(5f)

                for (location in routeLocations) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    polylineOptions.add(latLng)
                }

                googleMap.clear()
                googleMap.addPolyline(polylineOptions)

                // Move camera to the first location in the route
                if (routeLocations.isNotEmpty()) {
                    val firstLocation = routeLocations.first()
                    val firstLatLng = LatLng(firstLocation.latitude, firstLocation.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 15f))
                }
            }
        }
    }


    private fun mapAndWeatherInsert(latitude: Double, longitude: Double,startLatitude : Double,startLongitude:Double,endLatitude:Double,endLongitude:Double) {
        val apiKey = "4f424c8f88afd109f42c3a4c89832f7b"
        val apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&appid=$apiKey"
        val client = OkHttpClient()
        val request = Request.Builder().url(apiUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val weatherGson = Gson().fromJson(responseBody, WeatherData::class.java)
                    weatherData = weatherGson
                    weather = weatherData!!.weather[0].main
                    weatherDetail = weatherData!!.weather[0].description
                    temp = weatherData!!.main.temp
                    humidity = weatherData!!.main.humidity
                    wind = weatherData!!.wind.speed

                    val trainingSubInsert = TrainingSubDetailInsert(
                        exerciseId!!, selectedUserId!!, trainingTime!!, selectedDateTime!!, trainingStartTime, trainingEndTime, videoUrl,
                        startLongitude, startLatitude, endLongitude, endLatitude, weather, weatherDetail, temp, humidity.toDouble(), wind)

                    trainingViewModel.requestTrainingSubInsert(trainingSubInsert) // 트레이닝 서브 데이터 저장
                    Global.showBottomSnackBar(binding.root, "운동종료")


                } else {
                    Global.showBottomSnackBar(binding.root, "등록 실패하였습니다")

                }
            }
        })
    }


    public override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    public override fun onResume() {
        super.onResume()
        initializePlayer()
    }

    public override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    public override fun onStop() {
        super.onStop()
        releasePlayer()
    }


    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // "뒤로 가기" 버튼 처리
            completeWorkAndFinish()
        }
    }
    private fun clickBack() {
        binding.layoutBack.setOnClickListener {
            completeWorkAndFinish()
        }
    }
    private fun completeWorkAndFinish() {
        if(updateData) { // 업데이트 했을때만 결과 설정
            val resultCode = Activity.RESULT_OK
            val resultIntent = Intent()
            setResult(resultCode, resultIntent) }

            finish()
            back()
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
    }

    private fun handlerLoading(isLoading: Boolean) {
    if(_binding != null) {
        if (isLoading) {
            binding.llProgressBar.visibility = View.VISIBLE
            binding.loadingProgressBar.visibility = View.VISIBLE
        } else {
            binding.llProgressBar.visibility = View.GONE
            binding.loadingProgressBar.visibility = View.GONE
             }
        }
    }

    private fun handlerGetHealthLoading(isLoading:Boolean) {
        if(_binding != null) {
            if (isLoading) {
                binding.llHealthDataProgressBar.visibility = View.VISIBLE
                binding.loadingHealthDataProgressBar.visibility = View.VISIBLE
                binding.loadingHealthData.visibility = View.VISIBLE
            } else {
                binding.llHealthDataProgressBar.visibility = View.GONE
                binding.loadingHealthDataProgressBar.visibility = View.GONE
                binding.loadingHealthData.visibility = View.GONE
             }
          }
      }

    private fun showStartTimeEmptyDialog(){
        val message = "운동 기록 후 입력이 가능합니다."
        val alertDialog = AlertDialog.Builder(this@TrainingExerciseDetailMyActivity)
            .setMessage(message)
            .setPositiveButton("확인") { _, _ -> }
            .create()

        alertDialog.show()
    }
}
