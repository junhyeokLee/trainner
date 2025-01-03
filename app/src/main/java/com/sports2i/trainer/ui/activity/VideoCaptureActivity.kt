package com.sports2i.trainer.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.lifecycleScope
import com.arthenica.mobileffmpeg.Config
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.mediapipe.examples.poselandmarker.getNameString
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.TrainingSubDetailInsert
import com.sports2i.trainer.data.model.TrainingSubResponse
import com.sports2i.trainer.data.model.WeatherData
import com.sports2i.trainer.databinding.ActivityVideoCaptureBinding
import com.sports2i.trainer.ui.dialog.CustomPresetDialogFragment
import com.sports2i.trainer.ui.dialog.CustomQualityDialogFragment
import com.sports2i.trainer.utils.DateTimeUtil
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NCPvideoUpload
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class VideoCaptureActivity : BaseActivity<ActivityVideoCaptureBinding>({ ActivityVideoCaptureBinding.inflate(it)}) {

    enum class UiState {
        IDLE,       // 녹화하지 않음, 모든 UI 컨트롤이 활성화됨
        RECORDING,  // 카메라가 녹화 중, 일시 중지/재개 및 중지 버튼만 표시됨
        FINALIZED,  // 녹화 완료, 녹화 중 UI 컨트롤을 모두 비활성화함
    }
    private val trainingViewModel: TrainingViewModel by viewModels()
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

    private var editVideo = false
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

    private var startTimeMillis: Long = 0
    private var elapsedTimeMillis: Long = 0
    private var timerRunning: Boolean = false

    private var duration = 0
    private val videoPathList = ArrayList<Uri>()

    private var selectedRatio = HD_RATIO
    private var selected_bit_rate = HD_BIT_RATE
    private var selected_quality = HD_QUALITY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        backgroundExecutor = Executors.newSingleThreadExecutor()

        if (!arePermissionsGranted()) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        } else {
            binding.viewFinder.post {
                setUpCamera()
                observe()
            }
        }
        extractData()

        try {
            logCallback()
        }
        catch (e: Exception){

        }
    }

    private fun arePermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (arePermissionsGranted()) {
                setUpCamera()
            } else {
                // Handle denied permissions
            }
        }
    }

    override fun onResume() {
        super.onResume()
    if (currentRecording != null && !(recordingState is VideoRecordEvent.Finalize)) {
        currentRecording?.resume()
        resumeTimer()
         }
    }

    override fun onPause() {
        super.onPause()
    if (currentRecording != null && !(recordingState is VideoRecordEvent.Finalize)) {
        currentRecording?.pause()
        pauseTimer()
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


    private fun extractData() {
        // Intent로 전달된 데이터 추출
        exerciseId = intent.getStringExtra("exerciseId")?:""
        userId = intent.getStringExtra("userId")?:""
        trainingTime = intent.getStringExtra("trainingTime")?:""
        selectedDate = intent.getStringExtra("selectedDate")?:""
        editVideo = intent.getBooleanExtra("editVideo",false)
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
        preview = Preview.Builder()
            .setTargetAspectRatio(selectedRatio)
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

        videoCapture = VideoCapture.withOutput(recorder)

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



    @SuppressLint("MissingPermission")
    private fun startRecording() {
        recordingStartTime = System.currentTimeMillis()
        getCurrentLocationAndShowOnMap("start")

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
            cameraProvider?.bindToLifecycle(this, cameraSelector!!, preview,videoCapture)

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

                // 날씨 데이터 저장
                mapAndWeatherInsert(latitude, longitude,startLatitude!!,startLongitude!!,endLatitude!!,endLongitude!!)
                // 운동 끝날때의 위도 경도로 날씨 데이터 요청 하면서 완료시 subDetail insert API 호출
            }
            else ->{
                Global.showBottomSnackBar(binding.root, "잘못된 위치 정보입니다.")
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
                        exerciseId!!, userId!!, trainingTime!!, selectedDate!!, trainingStartTime, trainingEndTime, videoUrl,
                        startLongitude, startLatitude, endLongitude, endLatitude, weather, weatherDetail, temp, humidity.toDouble(), wind)
                    trainingViewModel.requestTrainingSubInsert(trainingSubInsert) // 트레이닝 서브 데이터 저장
                }
                else {
                    Global.showBottomSnackBar(binding.root, "등록 실패하였습니다")
                    finish()
                }

            }
        })
    }

    private fun handleCaptureButtonClick() {
        if (!this::recordingState.isInitialized || recordingState is VideoRecordEvent.Finalize) {
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
        // stopping: hide it after getting a click before we go to viewing fragment
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

        val state = if (event is VideoRecordEvent.Status) recordingState.getNameString()
        else event.getNameString()

        when (event) {
            is VideoRecordEvent.Status -> {}
            is VideoRecordEvent.Start -> showUI(UiState.RECORDING, event.getNameString())
            is VideoRecordEvent.Finalize-> {
                showUI(UiState.FINALIZED, event.getNameString())
                // 비동기적으로 ViewModel에 요청을 보냄
                duration = duration(event.outputResults.outputUri)?.toInt()?: 600000
                videoPathList.add(event.outputResults.outputUri)
                Log.e("videoPathList", "${videoPathList}")

//                saveAndSendVideo(event.outputResults.outputUri)

                lifecycleScope.launch {
                    handlerLoading(true)
                    saveAndSendVideo(event.outputResults.outputUri)
                }
            }
            is VideoRecordEvent.Pause -> binding.captureButton.setImageResource(R.drawable.ic_resume)
            is VideoRecordEvent.Resume -> binding.captureButton.setImageResource(R.drawable.ic_pause)
        }

        val stats = event.recordingStats
        val size = stats.numBytesRecorded / 1000
        val time = java.util.concurrent.TimeUnit.NANOSECONDS.toSeconds(stats.recordedDurationNanos)
        var text = "${state}: recorded ${size}KB, in ${time}second"
        if(event is VideoRecordEvent.Finalize)
            text = "${text}\nFile saved to: ${event.outputResults.outputUri}"
    }

    private fun enableUI(enable: Boolean) {
        arrayOf(binding.captureButton, binding.stopButton).forEach {
            it.isEnabled = enable
        }
    }

    private fun showUI(state: UiState, status:String = "idle") {

        if (_binding == null) _binding = bindingFactory(layoutInflater)

        _binding?.let {
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
                }

                UiState.FINALIZED -> {
                    binding.qualityText.visibility = View.VISIBLE
                    it.captureButton.setImageResource(R.drawable.ic_start)
                    it.stopButton.visibility = View.INVISIBLE
                }
                else -> {
                    val errorMsg = "Error: showUI($state) is not supported"
                    Log.e(TAG, errorMsg)
                    return
                }
            }
        }
    }
    private suspend fun saveAndSendVideo(outputUri: Uri) {

//        Global.videoProgressON(this@VideoCaptureActivity, isValue = true)

//        val imageMaker = TrainerVideoMaket(this@VideoCaptureActivity)
//        val file = imageMaker.makeCacheFile(MP4_EXE)
//        val cmd = imageMaker.videoCmd(videoPathList, file)
//      val thumbnailBitmap = ThumbnailUtils.createVideoThumbnail("$selectPath[0]", MediaStore.Images.Thumbnails.MINI_KIND)

//        FFmpeg.executeAsync(cmd) { _, returnCode ->
//            if (returnCode == 0) {
//                val result = file.absolutePath
//                val fileName = file.name
//
//                if (recordingStartTime != null && recordingEndTime != null) {
//                    NCPvideoUpload(this@VideoCaptureActivity, result).execute()
//                    val endPoint = Global.endPoint
//                    val bucketName = Global.bucketName + "/video"
//                    videoUrl = endPoint + "/" + bucketName + "/" + fileName
//
//                    val startTimeFormattedInsert = DateTimeUtil.formatTimeTrainingSubDetail(recordingStartTime!!)
//                    val endTimeFormattedInsert = DateTimeUtil.formatTimeTrainingSubDetail(recordingEndTime!!)
//
//                    trainingStartTime = startTimeFormattedInsert
//                    trainingEndTime = endTimeFormattedInsert
//                    getCurrentLocationAndShowOnMap("end")
//                } else {
////                    handlerLoading(false)
//                }
//            }
//        }

        outputDirectory = getOutputDirectory()
        val videoFile = createVideoFile(outputDirectory)
        copyFile(outputUri, videoFile)

        withContext(Dispatchers.IO) {
        if (recordingStartTime != null && recordingEndTime != null) {
                NCPvideoUpload(this@VideoCaptureActivity, videoFile.absolutePath).execute()
                val endPoint = Global.endPoint
                val bucketName = Global.bucketName + "/video"
                Log.e("filename", "" + videoFile.name)
                videoUrl = endPoint + "/" + bucketName + "/" + videoFile.name

                val startTimeFormattedInsert = DateTimeUtil.formatTimeTrainingSubDetail(recordingStartTime!!)
                val endTimeFormattedInsert = DateTimeUtil.formatTimeTrainingSubDetail(recordingEndTime!!)

                trainingStartTime = startTimeFormattedInsert
                trainingEndTime = endTimeFormattedInsert

//            if(editVideo){
//                val trainingSub = TrainingSubResponse(exerciseId,userId,trainingTime,
//                    selectedDate,0,0,"","","",0.0,0.0,0.0,0.0,
//                    "","",0.0,0.0,0.0,TrainingSubResponse.HealthData())
//                trainingViewModel.requestTrainingSubUpdate(trainingSub,"url")
//            }

            getCurrentLocationAndShowOnMap("end")
        }  else {
            // Handle null case if needed
            handlerLoading(false)
              }
        }
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
                    Global.videoProgressOff()
                    handlerLoading(false)
                    setResult(Activity.RESULT_OK, intent)
                    Global.showBottomSnackBar(binding.root, "운동종료")
                    finish()
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

    private fun stopRecordingAndReleaseResources() {
        // 현재 녹화가 진행 중인지 확인하고 녹화를 중지합니다.
        if (currentRecording != null && !(recordingState is VideoRecordEvent.Finalize)) {
            currentRecording?.stop()
            currentRecording = null
        }
        // UI 비활성화
        enableUI(false)
        // 백그라운드 스레드 종료
        backgroundExecutor.shutdown()
        try {
            backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
        } catch (e: InterruptedException) {
            Log.e(TAG, "Error while shutting down backgroundExecutor", e)
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
            val context = this@VideoCaptureActivity
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, path)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

            retriever.release()

            time?.toLong()
        } catch (e: Exception) {
            600000L
        }
    }

    private var progressJob: Job? = null

    private fun logCallback() {
        Config.enableStatisticsCallback { newStatistics ->
            try {
                val completePercentage =
                    BigDecimal(newStatistics.time).multiply(BigDecimal(100)).divide(BigDecimal(duration), 0, BigDecimal.ROUND_HALF_UP).toString()
                Log.e("로그콜백", "${duration} / ${newStatistics.time} / ${completePercentage}")

                progressJob?.cancel()
                progressJob = GlobalScope.launch(Dispatchers.Main) {
                    while ( true ) {
                        val current = Global.getVideoProgress()
                        if ( current >= completePercentage.toFloat()) {
                            cancel()
                        } else {
                            Global.setVideoProgress(current + 1)
                        }

                        delay(100)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "$e")
            }
        }
    }

    companion object {
        private const val TAG = "TrainingRecording"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { add(Manifest.permission.WRITE_EXTERNAL_STORAGE) } }.toTypedArray()

        // 비트레이트 설정 (500kbps) = 500000 , SD(720 x 480) 비율 4:3

        //SD (720 x 480):
        //해상도: 720 x 480
        //비율: 4:3
        //비트레이트: 500kbps (500000 bps)

        //HD (1280 x 720):
        //해상도: 1280 x 720
        //비율: 16:9
        //비트레이트: 800kbps (800000 bps)

        //FHD - 1 (1920 x 1080):
        //해상도: 1920 x 1080
        //비율: 16:9
        //비트레이트: 1200kbps (1200000 bps)

        //FHD - 2 (1920 x 1080):
        //해상도: 1920 x 1080
        //비율: 16:9
        //비트레이트: 1500kbps (1500000 bps)

        //SD (720 x 480): 적절한 비트레이트: 약 500kbps (최소 300kbps ~ 최대 800kbps)
        //HD (1280 x 720): 적절한 비트레이트: 약 1500kbps (최소 800kbps ~ 최대 2500kbps)
        //FHD (1920 x 1080): 적절한 비트레이트: 약 4000kbps (최소 2500kbps ~ 최대 6000kbps)
        //FHD (1920 x 1080) - 고화질: 적절한 비트레이트: 약 8000kbps (최소 6000kbps ~ 최대 10000kbps)

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

//        setQualitySelector(QualitySelector.from(Quality.LOWEST)) // 화질 설정 (낮은 퀄리티)
    }
}