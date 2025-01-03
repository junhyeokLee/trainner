package com.sports2i.trainer.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import android.widget.*
import com.google.mediapipe.examples.poselandmarker.getNameString
import kotlinx.coroutines.*
import java.util.*
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.core.util.Consumer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.sports2i.trainer.utils.PoseLandmarkerHelper
import com.sports2i.trainer.viewmodel.PoseVideoViewModel
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.TrackingData
import com.sports2i.trainer.data.model.TrainingSubDetailInsert
import com.sports2i.trainer.data.model.TrainingSubResponse
import com.sports2i.trainer.data.model.WeatherData
import com.sports2i.trainer.databinding.ActivityPoseCameraBinding
import com.sports2i.trainer.databinding.FragmentPoseCameraBinding
import com.sports2i.trainer.ui.dialog.CancelTrackingDialog
import com.sports2i.trainer.ui.dialog.CustomQualityDialogFragment
import com.sports2i.trainer.utils.ACTION_PAUSE_SERVICE
import com.sports2i.trainer.utils.DateTimeUtil
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NCPvideoUpload
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class PoseCameraActivity : BaseActivity<ActivityPoseCameraBinding>({ActivityPoseCameraBinding.inflate(it)}), PoseLandmarkerHelper.LandmarkerListener {
    // 카메라 UI 상태 및 입력
    enum class UiState {
        IDLE,       // 녹화하지 않음, 모든 UI 컨트롤이 활성화됨
        RECORDING,  // 카메라가 녹화 중, 일시 중지/재개 및 중지 버튼만 표시됨
        FINALIZED,  // 녹화 완료, 녹화 중 UI 컨트롤을 모두 비활성화함
    }

    private val poseViewModel: PoseVideoViewModel by viewModels()
    private val trainingViewModel: TrainingViewModel by viewModels()

    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper

    private lateinit var videoCapture: VideoCapture<Recorder>
    private var currentRecording: Recording? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var recordingState:VideoRecordEvent
    private lateinit var backgroundExecutor: ExecutorService
    private var cameraFacing = CameraSelector.LENS_FACING_BACK
    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(this) }
    private var cameraSelector: CameraSelector? = null
    private var preview: Preview? = null
    private var camera: Camera? = null
    private lateinit var outputDirectory: File

    private val recordLiveStatus = MutableLiveData<String>()
    private var imageAnalyzer: ImageAnalysis? = null

    private var enumerationDeferred:Deferred<Unit>? = null

    private var exerciseId = ""
    private var userId = ""
    private var selectedDate = ""
    private var trainingTime:String = ""
    private var trainingStartTime: String = ""
    private var trainingEndTime: String = ""

    private var recordingStartTime: Long? = null
    private var recordingEndTime: Long? = null

    private var startLongitude: Double? = 0.0
    private var startLatitude: Double? = 0.0
    private var endLongitude: Double? = 0.0
    private var endLatitude: Double? = 0.0
    private var weatherData: WeatherData? = null
    private var weather : String = ""
    private var weatherDetail: String = ""
    private var temp: Double = 0.0
    private var humidity: Int = 0
    private var wind: Double = 0.0
    private var videoUrl: String = ""

    private var exerciseCount: Int = 0

    private var startTimeMillis: Long = 0
    private var elapsedTimeMillis: Long = 0
    private var timerRunning: Boolean = false

    private var poseSuccess: Boolean = false
    private var trainingSub = TrainingSubResponse()

    private var duration = 0
    private val videoPathList = ArrayList<Uri>()

    private var selectedRatio = HD_RATIO
    private var selected_bit_rate = HD_BIT_RATE
    private var selected_quality = HD_QUALITY

    private val putIntent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        backgroundExecutor = Executors.newSingleThreadExecutor()

        exerciseId = intent.getStringExtra("exerciseId")?:""
        userId = intent.getStringExtra("userId")?:""
        trainingTime = intent.getStringExtra("trainingTime")?:""
        selectedDate = intent.getStringExtra("selectedDate")?:""

        if (!arePermissionsGranted()) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        else {
            setUpCamera()
            observe()
        }
        backgroundExecutor.execute {
            poseLandmarkerHelper = PoseLandmarkerHelper(
                context = this,
                runningMode = RunningMode.LIVE_STREAM,
                minPoseDetectionConfidence = poseViewModel.currentMinPoseDetectionConfidence,
                minPoseTrackingConfidence = poseViewModel.currentMinPoseTrackingConfidence,
                minPosePresenceConfidence = poseViewModel.currentMinPosePresenceConfidence,
                currentDelegate = poseViewModel.currentDelegate,
                poseLandmarkerHelperListener = this
            )
        }
        this@PoseCameraActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun arePermissionsGranted(): Boolean {
        for (permission in PoseCameraActivity.REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PoseCameraActivity.REQUEST_CODE_PERMISSIONS) {
            if (arePermissionsGranted()) {
                setUpCamera()
                observe()
            } else {
                // Handle denied permissions
            }
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if(poseSuccess) {
                putIntent.putExtra("trainingSub",trainingSub)
                putIntent.putExtra("poseSuccess", poseSuccess)
                putIntent.putExtra("exerciseCount",exerciseCount)
                setResult(Activity.RESULT_OK, putIntent)
                back()
                finish()
            }
            else {
                back()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (currentRecording != null && !(recordingState is VideoRecordEvent.Finalize)) {
            currentRecording?.resume()
            resumeTimer()
        }

        backgroundExecutor.execute {
            if(this::poseLandmarkerHelper.isInitialized) {
                if (poseLandmarkerHelper.isClose()) {
                    poseLandmarkerHelper.setupPoseLandmarker()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        if (currentRecording != null && !(recordingState is VideoRecordEvent.Finalize)) {
            currentRecording?.pause()
            pauseTimer()
        }

        if(this::poseLandmarkerHelper.isInitialized) {
            // Close the PoseLandmarkerHelper and release resources
            poseViewModel.setMinPoseDetectionConfidence(poseLandmarkerHelper.minPoseDetectionConfidence)
            poseViewModel.setMinPoseTrackingConfidence(poseLandmarkerHelper.minPoseTrackingConfidence)
            poseViewModel.setMinPosePresenceConfidence(poseLandmarkerHelper.minPosePresenceConfidence)
            poseViewModel.setDelegate(poseLandmarkerHelper.currentDelegate)
            backgroundExecutor.execute { poseLandmarkerHelper.clearPoseLandmarker() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (currentRecording != null && !(recordingState is VideoRecordEvent.Finalize)) {
            // 녹화 중이면 녹화를 중지하고 리소스를 해제.
            currentRecording?.stop()
            currentRecording = null
            stopTimer() // 녹화가 중지되면 타이머를 중지.
        }
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val qualityText = binding.qualityText

        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                initializeUI()
                bindCameraUseCases("HD")
            }, ContextCompat.getMainExecutor(this))

            qualityText.setOnClickListener {
                val dialogFragment = CustomQualityDialogFragment.newInstance()
                dialogFragment.setQualityClickListener(object :
                    CustomQualityDialogFragment.QualityClickListener {
                    override fun onQualityClicked(quality: String) {
                        bindCameraUseCases(quality)
                        qualityText.text = quality
                    }
                })
                dialogFragment.show(supportFragmentManager, CustomQualityDialogFragment.TAG)
            }
        }

//     Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases(qualityValue: String) {
    cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
    cameraSelector = CameraSelector.Builder().requireLensFacing(cameraFacing).build()

    // 디폴트 설정
    when (qualityValue) {
        "SD" -> {
            selectedRatio = SD_RATIO
            selected_bit_rate = SD_BIT_RATE
            selected_quality = SD_QUALITY
        }
        "HD" -> {
            // HD 디폴트
            selectedRatio = HD_RATIO
            selected_bit_rate = HD_BIT_RATE
            selected_quality = HD_QUALITY
        }
        "FHD" -> {
            selectedRatio = FHD_RATIO
            selected_bit_rate = FHD_BIT_RATE
            selected_quality = FHD_QUALITY
        }
        "FHD2" -> {
            selectedRatio = FHD2_RATIO
            selected_bit_rate = FHD2_BIT_RATE
            selected_quality = FHD2_QUALITY
        }
        else -> {
            selectedRatio = HD_RATIO
            selected_bit_rate = HD_BIT_RATE
            selected_quality = HD_QUALITY
        }
    }

    // Preview. Only using the 4:3 ratio because this is the closest to our models
    preview = Preview.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
        .setTargetRotation(binding.viewFinder.display.rotation)
        .build()
        .also {
            it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }
    // VideoCapture 설정
    val recorder = Recorder.Builder().apply {
        setTargetVideoEncodingBitRate(selected_bit_rate)
        setAspectRatio(selectedRatio)
        setQualitySelector(QualitySelector.from(selected_quality))
    }.build()

    // ImageAnalysis. Using RGBA 8888 to match how our models work
    imageAnalyzer = ImageAnalysis.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
        .setTargetRotation(binding.viewFinder.display.rotation)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .build()
        .also {
            it.setAnalyzer(backgroundExecutor) { imageProxy ->
                detectPose(imageProxy)
            }
        }

    // VideoCapture 설정
    videoCapture = VideoCapture.withOutput(recorder)

    // Must unbind the use-cases before rebinding them
    try {
        cameraProvider?.unbindAll()
        camera = cameraProvider?.bindToLifecycle(
            this, cameraSelector!!, preview, videoCapture
        )
        // Attach the viewfinder's surface provider to preview use case
        preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
    } catch (exc: Exception) {
        Log.e(TAG, "Use case binding failed", exc)
    }
    enableUI(true)
}

    private fun detectPose(imageProxy: ImageProxy) {
        if(this::poseLandmarkerHelper.isInitialized) {
            poseLandmarkerHelper.detectLiveStream(
                imageProxy = imageProxy,
                isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            binding.viewFinder.display.rotation
    }

    @SuppressLint("MissingPermission")
    private fun startRecording() {
        recordingStartTime = System.currentTimeMillis()
        getCurrentLocationAndShowOnMap("start")
        // create MediaStoreOutputOptions for our recorder: resulting our recording!
        val name = "CameraX-recording-" + SimpleDateFormat(FILENAME_FORMAT, Locale.KOREA).format(System.currentTimeMillis()) + ".mp4"

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        }

        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            this.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        cameraProvider?.unbindAll()
        cameraProvider?.bindToLifecycle(this, cameraSelector!!, preview,videoCapture,imageAnalyzer)

        // configure Recorder and Start recording to the mediaStoreOutput.
        currentRecording = videoCapture.output
            .prepareRecording(this, mediaStoreOutput)
            .apply { withAudioEnabled() }
            .start(mainThreadExecutor, recordListener)
    }

    private val recordListener = Consumer<VideoRecordEvent> { event ->
        // cache the recording state
        if (event !is VideoRecordEvent.Status)
            recordingState = event
            updateUI(event)
     }
    @SuppressLint("ClickableViewAccessibility", "MissingPermission")
    private fun initializeUI() {
        binding.captureButton.apply {
            setOnClickListener {
                handleCaptureButtonClick()
            }
            isEnabled = false
        }

        binding.stopButton.apply {
            setOnClickListener {
                handleStopButtonClick()
            }
            // ensure the stop button is initialized disabled & invisible
            visibility = View.INVISIBLE
            isEnabled = false
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
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
        }.addOnFailureListener { e -> }
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

                showSaveTrackingDialog(startLatitude!!,startLongitude!!,endLatitude!!,endLongitude!!)
            }
            else ->{
                Global.showBottomSnackBar(binding.root, "잘못된 위치 정보입니다.")
            }
        }
    }

    private fun mapAndWeatherInsert(startLatitude : Double,startLongitude:Double,endLatitude:Double,endLongitude:Double) {
        val apiKey = "4f424c8f88afd109f42c3a4c89832f7b"
        val apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=$endLatitude&lon=$endLongitude&units=metric&appid=$apiKey"
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
                        exerciseId!!, userId!!, trainingTime!!, selectedDate!!, trainingStartTime, trainingEndTime, videoUrl,
                        startLongitude, startLatitude, endLongitude, endLatitude, weather, weatherDetail, temp, humidity.toDouble(), wind)

                    trainingViewModel.requestTrainingSubInsert(trainingSubInsert) // 트레이닝 서브 데이터 저장
                }
                else {
                    Global.showBottomSnackBar(binding.root, "등록 실패하였습니다")
                }
            }
        })
    }

    private fun handleCaptureButtonClick() {
        if (!this@PoseCameraActivity::recordingState.isInitialized || recordingState is VideoRecordEvent.Finalize) {
            enableUI(false)  // Our eventListener will turn on the Recording UI.
            startRecording()
            startTimer()
        } else {
            when (recordingState) {
                is VideoRecordEvent.Start -> {
                    handleRecordingStartPause()
                    binding.stopButton.visibility = View.VISIBLE
                    pauseTimer()
                }
                is VideoRecordEvent.Pause -> {
                    currentRecording?.resume()
                    resumeTimer()
                }
                is VideoRecordEvent.Resume -> {
                    currentRecording?.pause()
                    pauseTimer()
                }
                else -> throw IllegalStateException("recordingState in unknown state")
            }
        }
    }

    private fun handleRecordingStartPause() {
        currentRecording?.pause()
        binding.captureButton.setImageResource(R.drawable.ic_pause)
    }

    private fun handleStopButtonClick() {
        binding.stopButton.visibility = View.INVISIBLE
        if (currentRecording == null || recordingState is VideoRecordEvent.Finalize) {
            return
        }
        recordingEndTime = System.currentTimeMillis()

        val recording = currentRecording
        if (recording != null) {
            recording.stop()
            currentRecording = null
        }
        stopTimer() // Stop the timer when recording stops
        binding.tvTime.text = "00:00:00" // Reset timer display
        binding.tvMiliSeconds.text = ""
        binding.captureButton.setImageResource(R.drawable.ic_start)
    }


    private fun updateUI(event: VideoRecordEvent) {
//        val parameter = args.parameter // 전달된 parameter 값을 가져옵니다.
        val state = if (event is VideoRecordEvent.Status) recordingState.getNameString()
        else event.getNameString()

        when (event) {
            is VideoRecordEvent.Status -> {}
            is VideoRecordEvent.Start -> {
                showUI(UiState.RECORDING, event.getNameString())

                when(exerciseId) {
                    "E02" -> { // 푸시업
                        binding.overlayPushUp.visibility = View.VISIBLE
                        binding.overlaySquat.visibility = View.GONE
                        binding.overlayDeadLift.visibility = View.GONE
                        binding.overlayLunge.visibility = View.GONE
                    }
                    "E03" -> { // 스쿼트
                        binding.overlaySquat.visibility = View.VISIBLE
                        binding.overlayPushUp.visibility = View.GONE
                        binding.overlayDeadLift.visibility = View.GONE
                        binding.overlayLunge.visibility = View.GONE
                    }
                    "E04" -> { // 데드리스프
                        binding.overlayDeadLift.visibility = View.VISIBLE
                        binding.overlayPushUp.visibility = View.GONE
                        binding.overlaySquat.visibility = View.GONE
                        binding.overlayLunge.visibility = View.GONE
                    }
                    "E05" -> {  // 런지
                        binding.overlayLunge.visibility = View.VISIBLE
                        binding.overlayPushUp.visibility = View.GONE
                        binding.overlaySquat.visibility = View.GONE
                        binding.overlayDeadLift.visibility = View.GONE
                    }
                }
            }
            is VideoRecordEvent.Finalize-> {
                showUI(UiState.FINALIZED, event.getNameString())
//                duration = duration(event.outputResults.outputUri)?.toInt()?: 600000
                videoPathList.add(event.outputResults.outputUri)
                handlerLoading(true)
                saveAndSendVideo(event.outputResults.outputUri)
            }
            is VideoRecordEvent.Pause -> {
                binding.captureButton.setImageResource(R.drawable.ic_resume)
                binding.overlaySquat.visibility = View.GONE
                binding.overlayPushUp.visibility = View.GONE
                binding.overlayDeadLift.visibility = View.GONE
                binding.overlayLunge.visibility = View.GONE
            }
            is VideoRecordEvent.Resume -> {
                binding.captureButton.setImageResource(R.drawable.ic_pause)
                if(exerciseId == "E02"){
                    binding.overlayPushUp.visibility = View.VISIBLE
                    binding.overlaySquat.visibility = View.GONE
                    binding.overlayDeadLift.visibility = View.GONE
                    binding.overlayLunge.visibility = View.GONE
                }
                else if(exerciseId == "E03"){
                    binding.overlaySquat.visibility = View.VISIBLE
                    binding.overlayPushUp.visibility = View.GONE
                    binding.overlayDeadLift.visibility = View.GONE
                    binding.overlayLunge.visibility = View.GONE
                }
                else if(exerciseId == "E04"){
                    binding.overlayDeadLift.visibility = View.VISIBLE
                    binding.overlaySquat.visibility = View.GONE
                    binding.overlayPushUp.visibility = View.GONE
                    binding.overlayLunge.visibility = View.GONE
                }
                else if(exerciseId == "E05"){
                    binding.overlayLunge.visibility = View.VISIBLE
                    binding.overlaySquat.visibility = View.GONE
                    binding.overlayPushUp.visibility = View.GONE
                    binding.overlayDeadLift.visibility = View.GONE
                }
            }
        }

        val stats = event.recordingStats
        val size = stats.numBytesRecorded / 1000
        val time = java.util.concurrent.TimeUnit.NANOSECONDS.toSeconds(stats.recordedDurationNanos)
        var text = "${state}: recorded ${size}KB, in ${time}second"
        if(event is VideoRecordEvent.Finalize)
            text = "${text}\nFile saved to: ${event.outputResults.outputUri}"

        recordLiveStatus.value = text
    }

    private fun enableUI(enable: Boolean) {
        arrayOf(binding.captureButton, binding.stopButton).forEach {
            it.isEnabled = enable
        }
    }

    private fun showUI(state: UiState, status:String = "idle") {

        if (_binding == null) _binding = bindingFactory(layoutInflater)

        binding.let {
            when(state) {
                UiState.IDLE -> {
                    binding.qualityText.visibility = View.VISIBLE
                    it.captureButton.setImageResource(R.drawable.ic_start)
                    it.stopButton.visibility = View.INVISIBLE
                }
                UiState.RECORDING -> {
                    binding.qualityText.visibility = View.GONE
                    it.captureButton.setImageResource(R.drawable.ic_pause)
                    it.captureButton.isEnabled = true
                    it.stopButton.visibility = View.VISIBLE
                    it.stopButton.isEnabled = true
                    it.countText.visibility = View.VISIBLE
                    it.incorrectCountText.visibility = View.VISIBLE
                }

                UiState.FINALIZED -> {
                    binding.qualityText.visibility = View.VISIBLE
                    it.captureButton.setImageResource(R.drawable.ic_start)
                    it.stopButton.visibility = View.INVISIBLE
                    it.countText.visibility = View.GONE
                    it.incorrectCountText.visibility = View.GONE
                    it.overlayPushUp.visibility = View.GONE
                    it.overlaySquat.visibility = View.GONE
                    it.overlayDeadLift.visibility = View.GONE
                    it.overlayLunge.visibility = View.GONE

                    binding.overlayPushUp.clearPushUpCount()
                    binding.overlaySquat.clearSquatCount()
                    binding.overlayDeadLift.clearDeadLiftCount()
                    binding.overlayLunge.clearLungeCount()

                    binding.overlayPushUp.clearPushUpInCorrectCount()
                    binding.overlaySquat.clearSquatInCorrectCount()
                    binding.overlayDeadLift.clearDeadLiftInCorrectCount()
                    binding.overlayLunge.clearLungeInCorrectCount()
                }
                else -> {
                    val errorMsg = "Error: showUI($state) is not supported"
                    Log.e(TAG, errorMsg)
                    return
                }
            }
        }
    }

    override fun onResults(
        resultBundle: PoseLandmarkerHelper.ResultBundle
    ) {
//        val parameter = args.parameter // 전달된 parameter 값을 가져옵니다.
        var incorrectCount = 0
        var categoryText = ""
        var incorrectText = ""

        this?.runOnUiThread {
            if (_binding != null) {

                if(exerciseId == "E02") { // 턱걸이지만 푸쉬업테스트
                    binding.overlayPushUp.setResults(
                        resultBundle.results.first(),
                        resultBundle.inputImageHeight,
                        resultBundle.inputImageWidth,
                        RunningMode.LIVE_STREAM,
                    )
                    exerciseCount = binding.overlayPushUp.getPushUpCount()
                    incorrectCount = binding.overlayPushUp.getPushUpInCorrectCount()
                    categoryText = "푸쉬업 "
                    incorrectText = binding.overlayPushUp.getIncorrectText()
                }
                else if(exerciseId == "E03") { // 로잉머신 이지만 스쿼트 테스트
                    binding.overlaySquat.setResults(
                        resultBundle.results.first(),
                        resultBundle.inputImageHeight,
                        resultBundle.inputImageWidth,
                        RunningMode.LIVE_STREAM,
                    )
                    exerciseCount = binding.overlaySquat.getSquatCount()
                    incorrectCount = binding.overlaySquat.getSquatInCorrectCount()
                    categoryText = "스쿼트 "
                    incorrectText = binding.overlaySquat.getIncorrectText()
                }
                else if(exerciseId == "E04") { // 로잉머신 이지만 컨벤션 데드리프트 테스트
                    binding.overlayDeadLift.setResults(
                        resultBundle.results.first(),
                        resultBundle.inputImageHeight,
                        resultBundle.inputImageWidth,
                        RunningMode.LIVE_STREAM,
                    )
                    exerciseCount = binding.overlayDeadLift.getDeadLiftCount()
                    incorrectCount = binding.overlayDeadLift.getDeadLiftInCorrectCount()
                    categoryText = "데드리프트 "
                    incorrectText = binding.overlayDeadLift.getIncorrectText()
                }
                else if(exerciseId == "E05") { // 로잉머신 이지만 런지 테스트
                    binding.overlayLunge.setResults(
                        resultBundle.results.first(),
                        resultBundle.inputImageHeight,
                        resultBundle.inputImageWidth,
                        RunningMode.LIVE_STREAM,
                    )
                    exerciseCount = binding.overlayLunge.getLungeCount()
                    incorrectCount = binding.overlayLunge.getLungeInCorrectCount()
                    categoryText = "런지 "
                    incorrectText = binding.overlayLunge.getIncorrectText()
                }

                binding.countText.text = "COUNT: $exerciseCount"
                binding.incorrectCountText.text = "FAIL: $incorrectCount"
                if(incorrectText.isNotEmpty()){
                    binding.incorrectText.visibility = View.VISIBLE
                    binding.incorrectText.text = incorrectText
                }
                else binding.incorrectText.visibility = View.GONE

                // Force a redraw
                binding.overlayPushUp.invalidate()
                binding.overlaySquat.invalidate()
                binding.overlayDeadLift.invalidate()
                binding.overlayLunge.invalidate()
            }
        }
    }


    private fun saveAndSendVideo(outputUri: Uri) {
        outputDirectory = getOutputDirectory()
        val videoFile = createVideoFile(outputDirectory)
        copyFile(outputUri, videoFile)

            if (recordingStartTime != null && recordingEndTime != null) {
                NCPvideoUpload(this@PoseCameraActivity, videoFile.absolutePath).execute()
                val endPoint = Global.endPoint
                val bucketName = Global.bucketName + "/video"
                videoUrl = endPoint + "/" + bucketName + "/" + videoFile.name

                val startTimeFormattedInsert = DateTimeUtil.formatTimeTrainingSubDetail(recordingStartTime!!)
                val endTimeFormattedInsert = DateTimeUtil.formatTimeTrainingSubDetail(recordingEndTime!!)

                trainingStartTime = startTimeFormattedInsert
                trainingEndTime = endTimeFormattedInsert

                putIntent.putExtra("exerciseCount",exerciseCount) // 운동 자동기입 카운트
                getCurrentLocationAndShowOnMap("end")
            }  else {
                handlerLoading(false)
            }
        }

    private fun showSaveTrackingDialog(startLatitude: Double, startLongitude: Double, endLatitude: Double, endLongitude: Double) {
        CancelTrackingDialog().apply {
            setYesListener {
                Log.e("YES exerciseCount",exerciseCount.toString())
                mapAndWeatherInsert(startLatitude,startLongitude,endLatitude,endLongitude)
            }
            setNoListener {
                this.dismiss()
            }
        }.show(supportFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }



    private fun createVideoFile(directory: File): File {
        return File.createTempFile("${UUID.randomUUID()}", ".mp4", directory).apply {
            deleteOnExit()
        }
    }

    private fun copyFile(sourceUri: Uri, destinationFile: File) {
        contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            destinationFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun observe(){
        this.trainingViewModel.trainingSubInsertState.observe(this){
            when (it) {
                is NetworkState.Success -> {
                    handlerTrainingSubInsertSuccess(it.data.data)
                    poseSuccess = true
                    trainingSub = it.data.data

                    putIntent.putExtra("trainingSub",trainingSub)
                    putIntent.putExtra("poseSuccess", poseSuccess)
                    setResult(Activity.RESULT_OK, putIntent)
                    finish()
                    back()
                    Global.showBottomSnackBar(binding.root, "운동종료")

//            if(exerciseId.equals("E02") || exerciseId.equals("E03")) {
//            intent.putExtra("exerciseCount",exerciseCount)
//            }
                }
                is NetworkState.Error -> {
                    handlerLoading(false)
                    Global.showBottomSnackBar(binding.root, "등록 실패하였습니다")
                    finish()
                }
                is NetworkState.Loading -> {
                    handlerLoading(it.isLoading)
                }
            }
        }
    }

    private fun handlerTrainingSubInsertSuccess(trainingSubResponse: TrainingSubResponse) {
        if(trainingSubResponse == null) {
            Global.showBottomSnackBar(binding.root, "등록 실패하였습니다")
            finish()
        }
    }


    private fun startTimer() {
        startTimeMillis = System.currentTimeMillis() - elapsedTimeMillis
        timerRunning = true
        lifecycleScope.launch(Dispatchers.Main) {
            while (timerRunning) {
                val currentTimeMillis = System.currentTimeMillis()
                elapsedTimeMillis = currentTimeMillis - startTimeMillis
                val formattedTime = formatTime(elapsedTimeMillis)
                val formattedSecond = formatMiliSecond(elapsedTimeMillis)
                binding.tvTime.text = formattedTime
                binding.tvMiliSeconds.text = formattedSecond
                delay(10) // Update every second
            }
        }
    }

    private fun pauseTimer() {
        timerRunning = false
    }

    private fun resumeTimer() {
        timerRunning = true
        startTimer()
    }

    private fun stopTimer() {
        timerRunning = false
        elapsedTimeMillis = 0 // Reset elapsed time when stopping the timer
    }

    override fun onBackPressed() {
        if (currentRecording != null && !(recordingState is VideoRecordEvent.Finalize)) {
            // 녹화 중이면 녹화를 중지하고 리소스를 해제합니다.
            currentRecording?.stop()
            currentRecording = null
            stopTimer() // 녹화가 중지되면 타이머를 중지합니다.
        } else {
            super.onBackPressed() // 녹화 중이 아니면 기본 동작을 수행합니다.
        }
    }

    private fun formatTime(elapsedTimeMillis: Long): String {
        val seconds = (elapsedTimeMillis / 1000) % 60
        val minutes = (elapsedTimeMillis / (1000 * 60)) % 60
        val hours = (elapsedTimeMillis / (1000 * 60 * 60)) % 24
        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }
    private fun formatMiliSecond(elapsedTimeMillis: Long):String{
        val milliseconds = (elapsedTimeMillis / 10) % 100
        return String.format(".%02d",milliseconds)
    }

    private fun handlerLoading(isLoading: Boolean) {
        if (_binding != null) {
            if (isLoading) {
                binding.llProgress.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.llProgress.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun duration(path: Uri): Long? {
        return try {
            val context = this@PoseCameraActivity
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, path)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

            retriever.release()

            time?.toLong()
        } catch (e: Exception) {
            600000L
        }
    }

    override fun onError(error: String, errorCode: Int) {
        this.runOnUiThread {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val DEFAULT_QUALITY_IDX = 0
        val TAG:String = PoseCameraActivity::class.java.simpleName
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH:mm.ss"
        private const val REQUEST_CODE_PERMISSIONS = 11
        private val REQUIRED_PERMISSIONS =
            mutableListOf (Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { add(Manifest.permission.WRITE_EXTERNAL_STORAGE) } }.toTypedArray()

        private const val SD_BIT_RATE = 500000
        private const val HD_BIT_RATE = 1500000
        private const val FHD_BIT_RATE = 4000000
        private const val FHD2_BIT_RATE = 8000000

        private const val SD_RATIO = AspectRatio.RATIO_4_3
        private const val HD_RATIO = AspectRatio.RATIO_4_3
        private const val FHD_RATIO = AspectRatio.RATIO_16_9
        private const val FHD2_RATIO = AspectRatio.RATIO_16_9

        private val SD_QUALITY = Quality.SD
        private val HD_QUALITY = Quality.HD
        private val FHD_QUALITY = Quality.FHD
        private val FHD2_QUALITY = Quality.FHD

    }
}
