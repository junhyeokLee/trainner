/*
 * TensorFlow Authors의 저작권 © 2023
 *
 * Apache License, Version 2.0 (the "License")에 따라 라이선스가 부여됩니다.
 * 라이선스에 따라 이 파일을 사용할 수 있습니다.
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * 라이선스에 따라 특정 규정을 준수하지 않으면 라이선스 사용이 제한될 수 있습니다.
 */
package com.sports2i.trainer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.camera.core.ImageProxy
import com.google.mediapipe.formats.proto.LandmarkProto
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseLandmarkerHelper(
    var minPoseDetectionConfidence: Float = DEFAULT_POSE_DETECTION_CONFIDENCE,
    var minPoseTrackingConfidence: Float = DEFAULT_POSE_TRACKING_CONFIDENCE,
    var minPosePresenceConfidence: Float = DEFAULT_POSE_PRESENCE_CONFIDENCE,
    var currentModel: Int = MODEL_POSE_LANDMARKER_FULL,
    var currentDelegate: Int = DELEGATE_CPU,
    var runningMode: RunningMode = RunningMode.IMAGE,
    val context: Context,
    val poseLandmarkerHelperListener: LandmarkerListener? = null // 라이브 스트림 모드일 때 결과를 리스닝하기 위한 랜드마커 리스너
) {

    // 이 예제에서는 변경 사항이 있을 때마다 재설정해야하므로 var로 선언합니다.
    // 랜드마커가 변경되지 않는다면 lazy val을 사용하는 것이 좋습니다.
    private var poseLandmarker: PoseLandmarker? = null
    // 평활화를 위한 변수 및 상수 정의
    private var smoothedLandmarks: List<List<LandmarkProto.NormalizedLandmark>> = emptyList()
    private val SMOOTHING_FACTOR = 0.5f

    init {
        setupPoseLandmarker()
    }


    // 랜드마커를 닫습니다.
    fun clearPoseLandmarker() {
        poseLandmarker?.close()
        poseLandmarker = null
    }

    // PoseLandmarkerHelper의 러닝 상태를 반환합니다.
    fun isClose(): Boolean {
        return poseLandmarker == null
    }

    // 현재 설정을 기반으로 랜드마커를 초기화합니다.
    fun setupPoseLandmarker() {
        // 일반적인 포즈 랜드마커 옵션을 설정합니다.
        val baseOptionBuilder = BaseOptions.builder()

        // 지정된 하드웨어를 사용하여 모델을 실행합니다. 기본적으로 CPU를 사용합니다.
        when (currentDelegate) {
            DELEGATE_CPU -> {
                baseOptionBuilder.setDelegate(Delegate.CPU)
            }
            DELEGATE_GPU -> {
                baseOptionBuilder.setDelegate(Delegate.GPU)
            }
        }

        val modelName =
            when (currentModel) {
                MODEL_POSE_LANDMARKER_FULL -> "pose_landmarker_full.task"
//                MODEL_POSE_LANDMARKER_LITE -> "pose_landmarker_lite.task"
//                MODEL_POSE_LANDMARKER_HEAVY -> "pose_landmarker_heavy.task"
                else -> "pose_landmarker_full.task"
            }

        baseOptionBuilder.setModelAssetPath(modelName)

        // runningMode와 poseLandmarkerHelperListener가 일치하는지 확인합니다.
        when (runningMode) {
            RunningMode.LIVE_STREAM -> {
                if (poseLandmarkerHelperListener == null) {
                    throw IllegalStateException(
                        "poseLandmarkerHelperListener must be set when runningMode is LIVE_STREAM."
                    )
                }
            }
            else -> {
                // 무시합니다.
            }
        }

        try {
            val baseOptions = baseOptionBuilder.build()
            // 베이스 옵션과 랜드마커를 위한 특정 옵션을 사용하여 옵션 빌더를 만듭니다.
            val optionsBuilder =
                PoseLandmarker.PoseLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setMinPoseDetectionConfidence(minPoseDetectionConfidence)
                    .setMinTrackingConfidence(minPoseTrackingConfidence)
                    .setMinPosePresenceConfidence(minPosePresenceConfidence)
                    .setRunningMode(runningMode)

            // ResultListener와 ErrorListener는 LIVE_STREAM 모드에서만 사용됩니다.
            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder
                    .setResultListener(this::returnLivestreamResult)
                    .setErrorListener(this::returnLivestreamError)
            }

            val options = optionsBuilder.build()
            poseLandmarker =
                PoseLandmarker.createFromOptions(context, options)
        } catch (e: IllegalStateException) {
            poseLandmarkerHelperListener?.onError(
                "Pose Landmarker failed to initialize. See error logs for " +
                        "details"
            )
            Log.e(
                TAG, "MediaPipe failed to load the task with error: " + e
                    .message
            )
        } catch (e: RuntimeException) {
            // 모델이 GPU를 지원하지 않는 경우 발생합니다.
            poseLandmarkerHelperListener?.onError(
                "Pose Landmarker failed to initialize. See error logs for " +
                        "details", GPU_ERROR
            )
            Log.e(
                TAG,
                "Image classifier failed to load model with error: " + e.message
            )
        }
    }

    // ImageProxy를 MPImage로 변환하여 PoseLandmarkerHelper에 제공합니다.
    fun detectLiveStream(
        imageProxy: ImageProxy,
        isFrontCamera: Boolean
    ) {
        if (runningMode != RunningMode.LIVE_STREAM) {
            throw IllegalArgumentException(
                "Attempting to call detectLiveStream" +
                        " while not using RunningMode.LIVE_STREAM"
            )
        }
        val frameTime = SystemClock.uptimeMillis()

        // 프레임에서 RGB 비트를 비트맵 버퍼로 복사합니다.
        val bitmapBuffer =
            Bitmap.createBitmap(
                imageProxy.width,
                imageProxy.height,
                Bitmap.Config.ARGB_8888
            )

        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
        imageProxy.close()

        val matrix = Matrix().apply {
            // 카메라에서 받은 프레임을 표시될 방향과 동일하게 회전합니다.
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

            // 전면 카메라를 사용하는 경우 이미지를 뒤집습니다.
            if (isFrontCamera) {
                postScale(
                    -1f,
                    1f,
                    imageProxy.width.toFloat(),
                    imageProxy.height.toFloat()
                )
            }
        }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
            matrix, true
        )

        // 입력 Bitmap 개체를 MPImage 개체로 변환하여 추론을 실행합니다.
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        detectAsync(mpImage, frameTime)
    }

    // MediaPipe Pose Landmarker API를 사용하여 포즈 랜드마커를 실행합니다.
    @VisibleForTesting
    fun detectAsync(mpImage: MPImage, frameTime: Long) {
        poseLandmarker?.detectAsync(mpImage, frameTime)
        // 러닝 모드가 LIVE_STREAM인 경우 랜드마커 결과는 returnLivestreamResult 함수에서 반환됩니다.
    }

    // 사용자 갤러리에서 로드한 비디오 파일의 URI를 받아와 포즈 랜드마커 추론을 실행하고 결과를 번들에 첨부하여 반환합니다.
    fun detectVideoFile(
        videoUri: Uri,
        inferenceIntervalMs: Long
    ): ResultBundle? {
        if (runningMode != RunningMode.VIDEO) {
            throw IllegalArgumentException(
                "Attempting to call detectVideoFile" +
                        " while not using RunningMode.VIDEO"
            )
        }

        // 추론 시간은 프로세스의 시작과 끝 시스템 시간의 차이입니다.
        val startTime = SystemClock.uptimeMillis()

        var didErrorOccurred = false

        // 비디오에서 프레임을 로드하고 포즈 랜드마커를 실행합니다.
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val videoLengthMs =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong()

        // 참고: MediaRetriever는 비디오 파일의 실제 차원보다 작은 프레임을 반환하기 때문에 프레임에서 너비/높이를 읽어옵니다.
        val firstFrame = retriever.getFrameAtTime(0)
        val width = firstFrame?.width
        val height = firstFrame?.height

        // 비디오가 유효하지 않은 경우 결과를 null로 반환합니다.
        if ((videoLengthMs == null) || (width == null) || (height == null)) return null

        // 다음으로, frameInterval ms마다 하나의 프레임을 가져온 다음 이러한 프레임에서 추론을 실행합니다.
        val resultList = mutableListOf<PoseLandmarkerResult>()
        val numberOfFrameToRead = videoLengthMs.div(inferenceIntervalMs)

        for (i in 0..numberOfFrameToRead) {
            val timestampMs = i * inferenceIntervalMs // ms

            retriever
                .getFrameAtTime(
                    timestampMs * 1000, // ms에서 마이크로초로 변환
                    MediaMetadataRetriever.OPTION_CLOSEST
                )
                ?.let { frame ->
                    // MediaPipe에서 요구하는 ARGB_8888로 비디오 프레임을 변환합니다.
                    val argb8888Frame =
                        if (frame.config == Bitmap.Config.ARGB_8888) frame
                        else frame.copy(Bitmap.Config.ARGB_8888, false)

                    // 입력 Bitmap 개체를 MPImage 개체로 변환하여 추론을 실행합니다.
                    val mpImage = BitmapImageBuilder(argb8888Frame).build()

                    // MediaPipe Pose Landmarker API를 사용하여 포즈 랜드마커를 실행합니다.
                    poseLandmarker?.detectForVideo(mpImage, timestampMs)
                        ?.let { detectionResult ->
                            resultList.add(detectionResult)
                        } ?: {
                        didErrorOccurred = true
                        poseLandmarkerHelperListener?.onError(
                            "ResultBundle could not be returned" +
                                    " in detectVideoFile"
                        )
                    }
                }
                ?: run {
                    didErrorOccurred = true
                    poseLandmarkerHelperListener?.onError(
                        "Frame at specified time could not be" +
                                " retrieved when detecting in video."
                    )
                }
        }

        retriever.release()

        val inferenceTimePerFrameMs =
            (SystemClock.uptimeMillis() - startTime).div(numberOfFrameToRead)

        return if (didErrorOccurred) {
            null
        } else {
            ResultBundle(
                resultList, inferenceTimePerFrameMs, height, width
            )
        }
    }

    // Bitmap을 받아와 포즈 랜드마커 추론을 실행하고 결과를 번들에 첨부하여 반환합니다.
    fun detectImage(image: Bitmap): ResultBundle? {
        if (runningMode != RunningMode.IMAGE) {
            throw IllegalArgumentException(
                "Attempting to call detectImage" +
                        " while not using RunningMode.IMAGE"
            )
        }

        // 추론 시간은 프로세스의 시작과 끝 시스템 시간의 차이입니다.
        val startTime = SystemClock.uptimeMillis()

        // 입력 Bitmap 개체를 MPImage 개체로 변환하여 추론을 실행합니다.
        val mpImage = BitmapImageBuilder(image).build()

        // MediaPipe Pose Landmarker API를 사용하여 포즈 랜드마커를 실행하고 결과를 반환합니다.
        poseLandmarker?.detect(mpImage)?.also { landmarkResult ->
            val inferenceTimeMs = SystemClock.uptimeMillis() - startTime
            return ResultBundle(
                listOf(landmarkResult),
                inferenceTimeMs,
                image.height,
                image.width
            )
        }

        // poseLandmarker?.detect()가 null을 반환하는 경우 오류로 간주합니다. null을 반환하여 이를 표시합니다.
        poseLandmarkerHelperListener?.onError(
            "Pose Landmarker failed to detect."
        )
        return null
    }

    // 포즈 랜드마커 결과를 사용하여 평활화 기법을 적용
    fun smoothLandmarks(
        newLandmarks: List<List<LandmarkProto.NormalizedLandmark>>,
        smoothedLandmarks: MutableList<List<LandmarkProto.NormalizedLandmark>>,
        smoothingFactor: Float
    ) {
        if (smoothedLandmarks.isEmpty()) {
            smoothedLandmarks.addAll(newLandmarks.map { it.toMutableList() })
        } else {
            for (i in newLandmarks.indices) {
                val smoothedLandmarkList = smoothedLandmarks[i].toMutableList()
                val newLandmarkList = newLandmarks[i]

                for (j in smoothedLandmarkList.indices) {
                    val smoothedLandmark = smoothedLandmarkList[j]
                    val prevX = smoothedLandmark.x
                    val prevY = smoothedLandmark.y

                    val newLandmark = newLandmarkList[j]
                    val newX = newLandmark.x
                    val newY = newLandmark.y

                    val smoothedX = prevX + smoothingFactor * (newX - prevX)
                    val smoothedY = prevY + smoothingFactor * (newY - prevY)

                    val smoothedNormalizedLandmark =
                        LandmarkProto.NormalizedLandmark.newBuilder()
                            .setX(smoothedX)
                            .setY(smoothedY)
                            .build()

                    smoothedLandmarkList[j] = smoothedNormalizedLandmark
                }
                smoothedLandmarks[i] = smoothedLandmarkList
            }
        }
    }

    // 랜드마커 결과를 이 PoseLandmarkerHelper의 호출자에게 반환합니다.
    private fun returnLivestreamResult(
        result: PoseLandmarkerResult,
        input: MPImage
    ) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()

        poseLandmarkerHelperListener?.onResults(
            ResultBundle(
                listOf(result),
                inferenceTime,
                input.height,
                input.width
            )
        )
    }

    // 포즈 랜드마커 실행 중 발생한 오류를 이 PoseLandmarkerHelper의 호출자에게 반환합니다.
    private fun returnLivestreamError(error: RuntimeException) {
        poseLandmarkerHelperListener?.onError(
            error.message ?: "An unknown error has occurred"
        )
    }

    companion object {
        const val TAG = "PoseLandmarkerHelper"

        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DEFAULT_POSE_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_POSE_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_POSE_PRESENCE_CONFIDENCE = 0.5F
        const val DEFAULT_NUM_POSES = 1
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1
        const val MODEL_POSE_LANDMARKER_FULL = 0
//        const val MODEL_POSE_LANDMARKER_LITE = 1
//        const val MODEL_POSE_LANDMARKER_HEAVY = 2
    }

    data class ResultBundle(
        val results: List<PoseLandmarkerResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )

    interface LandmarkerListener {
        fun onError(error: String, errorCode: Int = OTHER_ERROR)
        fun onResults(resultBundle: ResultBundle)
    }
}