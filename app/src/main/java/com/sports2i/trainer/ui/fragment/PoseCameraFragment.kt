package com.sports2i.trainer.ui.fragment

import android.annotation.SuppressLint
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import android.provider.MediaStore
import androidx.lifecycle.lifecycleScope
import androidx.core.util.Consumer
import com.sports2i.trainer.utils.PoseLandmarkerHelper
import com.sports2i.trainer.viewmodel.PoseVideoViewModel
import com.sports2i.trainer.R
import com.sports2i.trainer.databinding.FragmentPoseCameraBinding

class PoseCameraFragment : Fragment(), PoseLandmarkerHelper.LandmarkerListener {
    // 카메라 UI 상태 및 입력
    enum class UiState {
        IDLE,       // 녹화하지 않음, 모든 UI 컨트롤이 활성화됨
        RECORDING,  // 카메라가 녹화 중, 일시 중지/재개 및 중지 버튼만 표시됨
        FINALIZED,  // 녹화 완료, 녹화 중 UI 컨트롤을 모두 비활성화함
    }

    private var _fragmentPoseCameraBinding: FragmentPoseCameraBinding? = null
    private val fragmentPoseCameraBinding get() = _fragmentPoseCameraBinding!!

    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper
    private val viewModel: PoseVideoViewModel by activityViewModels()

    private val recordLiveStatus = MutableLiveData<String>()
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_BACK
    private var cameraSelector: CameraSelector? = null

    private lateinit var backgroundExecutor: ExecutorService

    private lateinit var videoCapture: androidx.camera.video.VideoCapture<Recorder>
    private var currentRecording: Recording? = null
    private lateinit var recordingState:VideoRecordEvent

    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(requireContext()) }
    private var enumerationDeferred:Deferred<Unit>? = null


    val args: PoseCameraFragmentArgs by navArgs()

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
//        if (!hasPermissions(requireContext())) {
//            Navigation.findNavController(
//                requireActivity(), R.id.fragment_container
//            ).navigate(R.id.action_camera_to_cagegory)
//        }

        // Start the PoseLandmarkerHelper again when users come back
        // to the foreground.
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
        if(this::poseLandmarkerHelper.isInitialized) {
            // Close the PoseLandmarkerHelper and release resources
            viewModel.setMinPoseDetectionConfidence(poseLandmarkerHelper.minPoseDetectionConfidence)
            viewModel.setMinPoseTrackingConfidence(poseLandmarkerHelper.minPoseTrackingConfidence)
            viewModel.setMinPosePresenceConfidence(poseLandmarkerHelper.minPosePresenceConfidence)
            viewModel.setDelegate(poseLandmarkerHelper.currentDelegate)
            backgroundExecutor.execute { poseLandmarkerHelper.clearPoseLandmarker() }
        }
    }

    override fun onDestroyView() {
        _fragmentPoseCameraBinding = null
        super.onDestroyView()

        // Shut down our background executor
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentPoseCameraBinding =
            FragmentPoseCameraBinding.inflate(inflater, container, false)

        return fragmentPoseCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()

        fragmentPoseCameraBinding.viewFinder.post {
            setUpCamera()
        }
        // Create the PoseLandmarkerHelper that will posele the inference
        backgroundExecutor.execute {
            poseLandmarkerHelper = PoseLandmarkerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                minPoseDetectionConfidence = viewModel.currentMinPoseDetectionConfidence,
                minPoseTrackingConfidence = viewModel.currentMinPoseTrackingConfidence,
                minPosePresenceConfidence = viewModel.currentMinPosePresenceConfidence,
                currentDelegate = viewModel.currentDelegate,
                poseLandmarkerHelperListener = this
            )
        }
    }

    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                initializeUI()
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

//     Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
    cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
    cameraSelector = CameraSelector.Builder().requireLensFacing(cameraFacing).build()

    // Preview. Only using the 4:3 ratio because this is the closest to our models
    preview = Preview.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
        .setTargetRotation(fragmentPoseCameraBinding.viewFinder.display.rotation)
        .build()
        .also {
            it.setSurfaceProvider(fragmentPoseCameraBinding.viewFinder.surfaceProvider)
        }

    // ImageAnalysis. Using RGBA 8888 to match how our models work
    imageAnalyzer = ImageAnalysis.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
        .setTargetRotation(fragmentPoseCameraBinding.viewFinder.display.rotation)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .build()
        .also {
            it.setAnalyzer(backgroundExecutor) { imageProxy ->
                detectPose(imageProxy)
            }
        }

    // VideoCapture 설정
    val recorder = Recorder.Builder().build()
    videoCapture = VideoCapture.withOutput(recorder)

    // Must unbind the use-cases before rebinding them

    try {
        cameraProvider?.unbindAll()
        camera = cameraProvider?.bindToLifecycle(
            this, cameraSelector!!, preview, videoCapture
        )
        // Attach the viewfinder's surface provider to preview use case
        preview?.setSurfaceProvider(fragmentPoseCameraBinding.viewFinder.surfaceProvider)
    } catch (exc: Exception) {
        Log.e(TAG, "Use case binding failed", exc)
    }
    enableUI(true)
}


    private fun bindRecordAnalysis(){

//        val cameraProvider = cameraProvider
//            ?: throw IllegalStateException("Camera initialization failed.")

//        val cameraSelector = CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentPoseCameraBinding.viewFinder.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(backgroundExecutor) { imageProxy ->
                    detectPose(imageProxy)
                }
            }



        //  - 녹화 시작하기 위해 레코딩 생성
        val recorder = Recorder.Builder()
            .build()

        videoCapture = VideoCapture.withOutput(recorder)

        // Must unbind the use-cases before rebinding them

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                this, cameraSelector!!, preview,imageAnalyzer
            )
            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentPoseCameraBinding.viewFinder.surfaceProvider)
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
            fragmentPoseCameraBinding.viewFinder.display.rotation
    }


    @SuppressLint("MissingPermission")
    private fun startRecording() {


        // create MediaStoreOutputOptions for our recorder: resulting our recording!
        val name = "CameraX-recording-" + SimpleDateFormat(FILENAME_FORMAT, Locale.KOREA).format(System.currentTimeMillis()) + ".mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            requireActivity().contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()


        cameraProvider?.unbindAll()
        cameraProvider?.bindToLifecycle(this, cameraSelector!!, preview,videoCapture,imageAnalyzer)



        // configure Recorder and Start recording to the mediaStoreOutput.
        currentRecording = videoCapture.output
            .prepareRecording(requireActivity(), mediaStoreOutput)
            .apply { withAudioEnabled() }
            .start(mainThreadExecutor, recordListener)
//        bindRecordAnalysis()



        Log.i(TAG, "Recording started")
    }

    private val recordListener = Consumer<VideoRecordEvent> { event ->
        // cache the recording state
        if (event !is VideoRecordEvent.Status)
            recordingState = event
        updateUI(event)
        if (event is VideoRecordEvent.Finalize) {
            // display the captured video
            viewLifecycleOwner.lifecycleScope.launch {
                Log.e(TAG, "Finalize: ${event.outputResults.outputUri}")
                //영상 종료 시점
//                navController.navigate(
//                    CameraFragmentDirections.actionCaptureToVideoViewer(
//                        event.outputResults.outputUri
//                    )
//                )
            }
        }
    }
    @SuppressLint("ClickableViewAccessibility", "MissingPermission")
    private fun initializeUI() {
        fragmentPoseCameraBinding.captureButton.apply {
            setOnClickListener {
                handleCaptureButtonClick()
            }
            isEnabled = false
        }

        fragmentPoseCameraBinding.stopButton.apply {
            setOnClickListener {
                handleStopButtonClick()
            }
            // ensure the stop button is initialized disabled & invisible
            visibility = View.INVISIBLE
            isEnabled = false
        }
    }

    private fun handleCaptureButtonClick() {
        if (!this@PoseCameraFragment::recordingState.isInitialized || recordingState is VideoRecordEvent.Finalize) {
            enableUI(false)  // Our eventListener will turn on the Recording UI.
            startRecording()
        } else {
            when (recordingState) {
                is VideoRecordEvent.Start -> {
                    handleRecordingStartPause()
                    fragmentPoseCameraBinding.stopButton.visibility = View.VISIBLE
                }
                is VideoRecordEvent.Pause -> currentRecording?.resume()
                is VideoRecordEvent.Resume -> currentRecording?.pause()
                else -> throw IllegalStateException("recordingState in unknown state")
            }
        }
    }

    private fun handleRecordingStartPause() {
        currentRecording?.pause()
        fragmentPoseCameraBinding.captureButton.setImageResource(R.drawable.ic_pause)
    }

    private fun handleStopButtonClick() {
        // stopping: hide it after getting a click before we go to viewing fragment
        fragmentPoseCameraBinding.stopButton.visibility = View.INVISIBLE
        if (currentRecording == null || recordingState is VideoRecordEvent.Finalize) {
            return
        }
        val recording = currentRecording
        if (recording != null) {
            recording.stop()
            currentRecording = null
        }
        fragmentPoseCameraBinding.captureButton.setImageResource(R.drawable.ic_start)
    }


    private fun updateUI(event: VideoRecordEvent) {
        val parameter = args.parameter // 전달된 parameter 값을 가져옵니다.
        val state = if (event is VideoRecordEvent.Status) recordingState.getNameString()
        else event.getNameString()
        when (event) {
            is VideoRecordEvent.Status -> {
                // placeholder: we update the UI with new status after this when() block,
                // nothing needs to do here.
            }
            is VideoRecordEvent.Start -> {
                showUI(UiState.RECORDING, event.getNameString())
                if(parameter == 1) { // 푸시업
                    fragmentPoseCameraBinding.overlayPushUp.visibility = View.VISIBLE
                    fragmentPoseCameraBinding.overlaySquat.visibility = View.GONE
                } else if(parameter == 2) { // 스쿼트
                    fragmentPoseCameraBinding.overlaySquat.visibility = View.VISIBLE
                    fragmentPoseCameraBinding.overlayPushUp.visibility = View.GONE
                }
            }
            is VideoRecordEvent.Finalize-> {
                showUI(UiState.FINALIZED, event.getNameString())
            }
            is VideoRecordEvent.Pause -> {
                fragmentPoseCameraBinding.captureButton.setImageResource(R.drawable.ic_resume)
                fragmentPoseCameraBinding.overlaySquat.visibility = View.GONE
                fragmentPoseCameraBinding.overlayPushUp.visibility = View.GONE
            }
            is VideoRecordEvent.Resume -> {
                fragmentPoseCameraBinding.captureButton.setImageResource(R.drawable.ic_pause)
                if(parameter == 1){
                    fragmentPoseCameraBinding.overlayPushUp.visibility = View.VISIBLE
                    fragmentPoseCameraBinding.overlaySquat.visibility = View.GONE
                } else if(parameter == 2){
                    fragmentPoseCameraBinding.overlaySquat.visibility = View.VISIBLE
                    fragmentPoseCameraBinding.overlayPushUp.visibility = View.GONE
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
        Log.i(TAG, "recording event: $text")
    }

    private fun enableUI(enable: Boolean) {
        arrayOf(fragmentPoseCameraBinding.captureButton,
            fragmentPoseCameraBinding.stopButton).forEach {
            it.isEnabled = enable
        }
    }

    private fun showUI(state: UiState, status:String = "idle") {

        fragmentPoseCameraBinding.let {
            when(state) {
                UiState.IDLE -> {
                    it.captureButton.setImageResource(R.drawable.ic_start)
                    it.stopButton.visibility = View.INVISIBLE
                }
                UiState.RECORDING -> {
                    it.captureButton.setImageResource(R.drawable.ic_pause)
                    it.captureButton.isEnabled = true
                    it.stopButton.visibility = View.VISIBLE
                    it.stopButton.isEnabled = true
                    it.countText.visibility = View.VISIBLE
                    it.incorrectCountText.visibility = View.VISIBLE
                }

                UiState.FINALIZED -> {
                    it.captureButton.setImageResource(R.drawable.ic_start)
                    it.stopButton.visibility = View.INVISIBLE
                    it.countText.visibility = View.GONE
                    it.incorrectCountText.visibility = View.GONE
                    it.overlayPushUp.visibility = View.GONE
                    it.overlaySquat.visibility = View.GONE
                    fragmentPoseCameraBinding.overlayPushUp.clearPushUpCount()
                    fragmentPoseCameraBinding.overlaySquat.clearSquatCount()
                    fragmentPoseCameraBinding.overlayPushUp.clearPushUpInCorrectCount()
                    fragmentPoseCameraBinding.overlaySquat.clearSquatInCorrectCount()
                }
                else -> {
                    val errorMsg = "Error: showUI($state) is not supported"
                    Log.e(TAG, errorMsg)
                    return
                }
            }
        }
    }

    private fun resetUIandState(reason: String) {
        enableUI(true)
        showUI(UiState.IDLE, reason)

//        cameraIndex = 0
//        qualityIndex = DEFAULT_QUALITY_IDX
//        audioEnabled = true
    }

    // Update UI after pose have been detected. Extracts original
    // image height/width to scale and place the landmarks properly through
    // OverlayView
    override fun onResults(
        resultBundle: PoseLandmarkerHelper.ResultBundle
    ) {
        val parameter = args.parameter // 전달된 parameter 값을 가져옵니다.
        var count = 0
        var incorrectCount = 0
        var categoryText = ""
        var incorrectText = ""
        activity?.runOnUiThread {
            if (_fragmentPoseCameraBinding != null) {

                if(parameter == 1) {
                    fragmentPoseCameraBinding.overlayPushUp.setResults(
                        resultBundle.results.first(),
                        resultBundle.inputImageHeight,
                        resultBundle.inputImageWidth,
                        RunningMode.LIVE_STREAM,
                    )

                    count = fragmentPoseCameraBinding.overlayPushUp.getPushUpCount()
                    incorrectCount = fragmentPoseCameraBinding.overlayPushUp.getPushUpInCorrectCount()
                    categoryText = "Push Up "
                    incorrectText = fragmentPoseCameraBinding.overlayPushUp.getIncorrectText()
                }
                else if(parameter == 2) {
                    fragmentPoseCameraBinding.overlaySquat.setResults(
                        resultBundle.results.first(),
                        resultBundle.inputImageHeight,
                        resultBundle.inputImageWidth,
                        RunningMode.LIVE_STREAM,
                    )
                    count = fragmentPoseCameraBinding.overlaySquat.getSquatCount()
                    incorrectCount = fragmentPoseCameraBinding.overlaySquat.getSquatInCorrectCount()
                    categoryText = "Squat "
                    incorrectText = fragmentPoseCameraBinding.overlaySquat.getIncorrectText()
                }

                fragmentPoseCameraBinding.countText.text = "CORRECT: $count"
                fragmentPoseCameraBinding.incorrectCountText.text = "INCORRECT: $incorrectCount"
                if(incorrectText.isNotEmpty()){
                    fragmentPoseCameraBinding.incorrectText.visibility = View.VISIBLE
                    fragmentPoseCameraBinding.incorrectText.text = incorrectText
                }
                else fragmentPoseCameraBinding.incorrectText.visibility = View.GONE

                // Force a redraw
                fragmentPoseCameraBinding.overlayPushUp.invalidate()
                fragmentPoseCameraBinding.overlaySquat.invalidate()
            }
        }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val DEFAULT_QUALITY_IDX = 0
        val TAG:String = PoseCameraFragment::class.java.simpleName
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH:mm.ss"
    }
}
