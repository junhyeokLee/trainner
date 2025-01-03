package com.google.mediapipe.examples.poselandmarker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.sports2i.trainer.R
import kotlin.math.max
import kotlin.math.min

class OverlayLungeView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private val LUNGE_ANGLE_NORMAL_THRESHOLD = 35 // 스쿼트 시작 기준 각도
    private val LUNGE_ANGLE_TRANSITION_THRESHOLD = 60 // 스쿼트 동작 중 각도
    private val LUNGE_ANGLE_PASS_THRESHOLD = 75 // 스쿼트 통과 기준 각도
    private val LUNGE_ANGLE_END_THRESHOLD = 80 // 스쿼트 종료 기준 각도

    // 스쿼트 감지를 위한 각도 기준치 설정
    private var lungeCorrectCount = 0
    private var lungeInCorrectCount = 0
    private var incorrectText = ""

    // 상태 추적을 위한 변수들
    private var lastLungeTimestamp: Long = 0
    private val LUNGE_COOLDOWN_TIME_MS = 1000L // 1초

    // 초기 상태는 스쿼트 준비 상태로 설정
//    private var squatsState = SquatState.READY
    private var lungeState = LungePhase.S1

    fun getLungeCount(): Int {
        return lungeCorrectCount
    }
    fun getLungeInCorrectCount(): Int {
        return lungeInCorrectCount
    }

    fun getIncorrectText(): String {
        return incorrectText
    }

    fun clearLungeCount():Int{
        lungeCorrectCount = 0
        return lungeCorrectCount
    }
    fun clearLungeInCorrectCount():Int{
        lungeInCorrectCount = 0
        return lungeInCorrectCount
    }

    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.icActive)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_POINT_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val hipIndex = POSE_LANDMARK_LEFT_HIP
        val kneeIndex = POSE_LANDMARK_LEFT_KNEE

        results?.let { poseLandmarkerResult ->
            for (landmark in poseLandmarkerResult.landmarks()) {

                val hip = landmark.get(hipIndex)
                val knee = landmark.get(kneeIndex)

                // 힙과 무릎 사이의 각도 계산
                val hipKneeAngle = calculateSideLunge(hip, knee)

                // 제외할 랜드마크 인덱스 (눈, 코, 입 랜드마크)
                val excludedLandmarkIndices = setOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

                PoseLandmarker.POSE_LANDMARKS.forEach {
                    if (it != null) {
                        val hip = poseLandmarkerResult.landmarks().get(0).get(it.start())
                        val knee = poseLandmarkerResult.landmarks().get(0).get(it.end())

                        // 제외할 랜드마크는 선을 그리지 않도록 처리
                        if (it.start() !in excludedLandmarkIndices && it.end() !in excludedLandmarkIndices) {
                            canvas.drawLine(
                                hip.x() * imageWidth * scaleFactor,
                                hip.y() * imageHeight * scaleFactor,
                                knee.x() * imageWidth * scaleFactor,
                                knee.y() * imageHeight * scaleFactor,
                                linePaint
                            )
                        }
                    }
                }

                // 랜드마크 그리기
                for ((index, normalizedLandmark) in landmark.withIndex()) {
                    if (index !in excludedLandmarkIndices) {
                        canvas.drawCircle(
                            normalizedLandmark.x() * imageWidth * scaleFactor,
                            normalizedLandmark.y() * imageHeight * scaleFactor,
                            LANDMARK_POINT_WIDTH / 2,
                            pointPaint
                        )
                    }
                }

                // 힙과 무릎 사이의 각도 텍스트 표시
                val angleText = "${hipKneeAngle.toInt()}°"
                val textPaint = Paint()
                textPaint.color = ContextCompat.getColor(context!!, R.color.bkPurple)
                textPaint.textSize = 42f
                textPaint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                val textX = (knee.x() * imageWidth * scaleFactor + 70)
                val textY = (knee.y() * imageHeight * scaleFactor)
                canvas.drawText(angleText, textX, textY, textPaint)

                detectLunge(poseLandmarkerResult.landmarks()[0])
            }
        }
    }

    private fun calculateSideLunge(hip: NormalizedLandmark, knee: NormalizedLandmark): Double {
        // 허벅지-무릎 선과 수직선 사이의 각도 계산
        val hipKneeLineAngle = Math.toDegrees(Math.atan2((knee.y() - hip.y()).toDouble(), (knee.x() - hip.x()).toDouble()))
        val verticalLineAngle = 90.0 // 수직선의 각도는 90도

        var kneeVerticalAngle =   hipKneeLineAngle - verticalLineAngle

        if (convertToPositiveAngle(kneeVerticalAngle) > 180.0 ) {
            kneeVerticalAngle = 360 - convertToPositiveAngle(kneeVerticalAngle)
        }
        return convertToPositiveAngle(kneeVerticalAngle)
    }

    private var stateSequence: MutableList<LungePhase> = mutableListOf()

    private fun detectLunge(landmarks: List<NormalizedLandmark>) {
        val currentTimeMillis = System.currentTimeMillis()
        val timeSinceLastLunge = currentTimeMillis - lastLungeTimestamp

        if (timeSinceLastLunge > LUNGE_COOLDOWN_TIME_MS) {

            val leftHipIndex = POSE_LANDMARK_LEFT_HIP
            val leftKneeIndex = POSE_LANDMARK_LEFT_KNEE

            val hip = landmarks[leftHipIndex]
            val knee = landmarks[leftKneeIndex]

            // 측면에서의 스쿼트 각도 계산 (왼쪽)
            val sideLungeAngle = calculateSideLunge(hip,knee)

            when (lungeState) {
                LungePhase.S1 -> {
                    if (sideLungeAngle > LUNGE_ANGLE_NORMAL_THRESHOLD) {
                        incorrectText = ""
                        stateSequence.clear()
                        stateSequence.add(LungePhase.S1)
                        lungeState = LungePhase.S2

                    }
                }
                LungePhase.S2 -> {
                    if (sideLungeAngle > LUNGE_ANGLE_PASS_THRESHOLD ) lungeState = LungePhase.S3

                    else if(sideLungeAngle < LUNGE_ANGLE_TRANSITION_THRESHOLD) {
                        // Detected incorrect squat, increment the incorrect count
                        stateSequence.clear()
                        stateSequence.addAll(listOf(LungePhase.S1,LungePhase.S2))
                        lungeState = LungePhase.S1
                    }
                    else lungeState = LungePhase.S3

                }
                LungePhase.S3 -> {
                if (sideLungeAngle < LUNGE_ANGLE_END_THRESHOLD){
                    stateSequence.clear()
                    stateSequence.addAll(listOf(LungePhase.S1,LungePhase.S2,LungePhase.S3))
                    lungeState = LungePhase.S1
                }
                else{
                    stateSequence.clear()
                    stateSequence.addAll(listOf(LungePhase.S1,LungePhase.S2))
                    lungeState = LungePhase.S1

                    incorrectText = "LUNGE TOO DEEP"

                    }
                }
            }
            if (stateSequence == mutableListOf(LungePhase.S1, LungePhase.S2)) {
                lungeInCorrectCount++
                incorrectText = "Lower Your Hips"

            }

            if (stateSequence == mutableListOf(LungePhase.S1, LungePhase.S2, LungePhase.S3)) {
                lungeCorrectCount++
                incorrectText = "Correct Lunge"

            }
            lastLungeTimestamp = currentTimeMillis
        }
    }


    private fun convertToPositiveAngle(angle: Double): Double {
        return if (angle < 0) {
            -angle
        } else {
            angle
        }
    }

    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }

        // PoseLandmarkerResult가 업데이트될 때마다 카운팅을 초기화
        // resetPushUpCount()

        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 14F
        private const val LANDMARK_POINT_WIDTH = 20F
        // 엉덩이 랜드마크 인덱스
        private const val POSE_LANDMARK_LEFT_HIP = 23
        private const val POSE_LANDMARK_RIGHT_HIP = 24
        // 무릎 랜드마크 인덱스
        private const val POSE_LANDMARK_LEFT_KNEE = 25
        private const val POSE_LANDMARK_RIGHT_KNEE = 26
        // 발목 랜드마크 인덱스
        private const val POSE_LANDMARK_LEFT_ANKLE = 27
        private const val POSE_LANDMARK_RIGHT_ANKLE = 28
        // 어깨 랜드마크 인덱스
        private const val POSE_LANDMARK_LEFT_SHOULDER = 11
        private const val POSE_LANDMARK_RIGHT_SHOULDER = 12


    }
    private enum class LungePhase {
        S1,     // Angle < 32°
        S2, // 35° <= Angle <= 65°
        S3        // 75° <= Angle <= 95°
    }
}
