package com.google.mediapipe.examples.poselandmarker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.sports2i.trainer.R
import kotlin.math.max
import kotlin.math.min

class OverlaySquatView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()
    private var arcPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private val SQUAT_ANGLE_NORMAL_THRESHOLD = 32 // 스쿼트 시작 기준 각도
    private val SQUAT_ANGLE_TRANSITION_THRESHOLD = 35 // 스쿼트 동작 중 각도
    private val SQUAT_ANGLE_PASS_THRESHOLD = 65 // 스쿼트 통과 기준 각도
    private val SQUAT_ANGLE_END_THRESHOLD = 75 // 스쿼트 종료 기준 각도
    private val SQUAT_ANGLE_DEEP_THRESHOLD = 95 // 스쿼트 너무 깊은 각도

    // 스쿼트 감지를 위한 각도 기준치 설정
    private var squatCorrectCount = 0
    private var squatInCorrectCount = 0
    private var incorrectText = ""

    // 초기 상태는 스쿼트 준비 상태로 설정
    // private var squatsState = SquatState.READY
    private var squatState = SquatPhase.S1
    private var S1_CHECK = false
    private var S2_CHECK = false
    private var S2_LOWER_CHECK = false
    private var S3_CHECK = false
    private var S3_DEEP_CHECK = false
    private var isRight = false
    fun getSquatCount(): Int {
        return squatCorrectCount
    }
    fun getSquatInCorrectCount(): Int {
        return squatInCorrectCount
    }

    fun getIncorrectText(): String {
        return incorrectText
    }
    fun clearSquatCount():Int{
        squatCorrectCount = 0
        return squatCorrectCount
    }
    fun clearSquatInCorrectCount():Int{
        squatInCorrectCount = 0
        return squatInCorrectCount
    }
    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        arcPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color = ContextCompat.getColor(context!!, R.color.icActive)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        arcPaint.color = ContextCompat.getColor(context!!, R.color.pose_message)
        arcPaint.strokeWidth = LANDMARK_ARC_STROKE_WIDTH
        arcPaint.style = Paint.Style.STROKE

        pointPaint.color = resources.getColor(R.color.primary)
        pointPaint.strokeWidth = LANDMARK_POINT_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val hipIndex = POSE_LANDMARK_LEFT_HIP
        val kneeIndex = POSE_LANDMARK_LEFT_KNEE
        val ankleIndex = POSE_LANDMARK_LEFT_ANKLE

        results?.let { poseLandmarkerResult ->
            for (landmark in poseLandmarkerResult.landmarks()) {

                val hip = landmark.get(hipIndex)
                val knee = landmark.get(kneeIndex)
                val ankle = landmark.get(ankleIndex)

                // 힙과 무릎 사이의 각도 계산
                val squatAngle = calculateSideSquat(hip, knee)
                detectSquat(squatAngle.toInt())

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

                // 반원형 그리기
                val arcCx = (knee.x() * imageWidth * scaleFactor)
                val arcCy = (knee.y() * imageHeight * scaleFactor)

                val radius = 60f // 반지름 설정
//                val startAngle = 270f // 시작 각도 (위치 상단부터 시작하기 때문에 270도)
//                val startAngle = if (isRight) 180f + hipKneeAngle.toFloat()  else hipKneeAngle.toFloat()
//                val startAngle = if (isRight) 90f else 90f // 시작 각도 (위치 상단부터 시작하기 때문에 270도)
//                val sweepAngle = hipKneeAngle.toFloat() // 그릴 각도
//                val sweepAngle = if (isRight) -hipKneeAngle.toFloat() else hipKneeAngle.toFloat() // 그릴 각도

//              발목과 무릎 사이의 각도 계산
//                val kneeAnkleLineAngle = Math.toDegrees(Math.atan2((ankle.y() - knee.y()).toDouble(), (ankle.x() - knee.x()).toDouble()))
//                val startAngle = if (isRight) kneeAnkleLineAngle.toFloat() else kneeAnkleLineAngle.toFloat()
//                val sweepAngle = if (isRight) -squatAngle.toFloat() else squatAngle.toFloat() // 그릴 각도
//                val hipKneeLineAngle = Math.toDegrees(Math.atan2((knee.y() - hip.y()).toDouble(), (knee.x() - hip.x()).toDouble()))

                val hipKneeLineAngle = Math.toDegrees(Math.atan2((knee.y() - hip.y()).toDouble(), (knee.x() - hip.x()).toDouble()))
                val hipAnkleLineAngle = Math.toDegrees(Math.atan2((ankle.y() - hip.y()).toDouble(), (ankle.x() - hip.x()).toDouble()))

                val kneeAnkleLineAngle = Math.toDegrees(Math.atan2((ankle.y() - knee.y()).toDouble(), (ankle.x() - knee.x()).toDouble()))

                // 엉덩이, 무릎, 발목 간의 각도 계산
                val hipKneeAnkleAngle = hipAnkleLineAngle - hipKneeLineAngle

                val startAngle = if (isRight) kneeAnkleLineAngle.toFloat() + 360 else kneeAnkleLineAngle.toFloat()
                val sweepAngle = if (isRight) hipKneeAnkleAngle.toFloat() else hipKneeAnkleAngle.toFloat() // 엉덩이와 무릎 사이의 각도에서 발목의 각도를 뺀 값

                // 선의 크기와 색상 설정
                canvas.drawArc(
                    arcCx - radius,
                    arcCy - radius,
                    arcCx + radius,
                    arcCy + radius,
                    startAngle,
                    sweepAngle,
                    true, // 반원 형태로 그리기 위해 true로 설정
                    arcPaint)

                // 시작점과 끝점의 위치 계산
//                val startX = cx - radius
//                val startY = cy
//                val endX = cx + radius
//                val endY = cy
//                // 시작점에서 끝점까지 선을 그립니다.
//                canvas.drawLine(startX, startY, endX, endY, arcPaint)


                // 텍스트 그리기
                val cx = (hip.x() * imageWidth * scaleFactor + if(isRight) 80 else -80)
                val cy = (hip.y() * imageHeight * scaleFactor + 100)
                val angleText = "${squatAngle.toInt()}°"
                val textPaint = Paint()
                textPaint.color = ContextCompat.getColor(context!!, R.color.white)
                textPaint.textSize = 42f
                textPaint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                val textWidth = textPaint.measureText(angleText)
                val textX = cx - textWidth / 2
                val textY = cy + 60 // 텍스트를 원 위에 놓기 위해 조정

                canvas.drawText(angleText, textX, textY, textPaint)


                // 힙과 무릎 사이의 각도 텍스트 표시
//                val angleText = "${hipKneeAngle.toInt()}°"
//                val textPaint = Paint()
//                textPaint.color = ContextCompat.getColor(context!!, R.color.white)
//                textPaint.textSize = 42f
//                textPaint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
//                val textX = (knee.x() * imageWidth * scaleFactor + 70)
//                val textY = (knee.y() * imageHeight * scaleFactor + 40)
//                canvas.drawText(angleText, textX, textY, textPaint)
            }
        }
    }

    private fun calculateSideSquat(hip: NormalizedLandmark, knee: NormalizedLandmark): Double {
        // 허벅지-무릎 선과 수직선 사이의 각도 계산
        val hipKneeLineAngle = Math.toDegrees(Math.atan2((knee.y() - hip.y()).toDouble(), (knee.x() - hip.x()).toDouble()))
        val verticalLineAngle = 90.0 // 수직선의 각도는 90도

        var kneeVerticalAngle = hipKneeLineAngle - verticalLineAngle

        if (convertToPositiveAngle(kneeVerticalAngle) > 180.0 ) {
            kneeVerticalAngle = 360 - convertToPositiveAngle(kneeVerticalAngle)
        }

        isRightAngle(kneeVerticalAngle)
        return convertToPositiveAngle(kneeVerticalAngle)
    }

    private var stateSequence: MutableList<SquatPhase> = mutableListOf()

    private fun detectSquat(angleValue: Int) {
        when (squatState) {
            SquatPhase.S1 -> handleS1State(angleValue)
            SquatPhase.S2 -> handleS2State(angleValue)
            SquatPhase.S3 -> handleS3State(angleValue)
        }
    }

    private fun handleS1State(angleValue: Int) {
        if (angleValue < SQUAT_ANGLE_NORMAL_THRESHOLD) {
            when(stateSequence.size){
                1 -> {}
                2 -> {
                    if(S1_CHECK && S2_CHECK && S2_LOWER_CHECK) {
                        squatInCorrectCount++
                        incorrectText = " Lower Your Hips ! "
                    }
                }
                3 -> {
                    if(S1_CHECK && S2_CHECK && S3_CHECK && !S3_DEEP_CHECK) {
                        squatCorrectCount++
                        incorrectText = " Good ! "
                    }
                    else if(S1_CHECK && S2_CHECK && S3_CHECK && S3_DEEP_CHECK) {
                        squatInCorrectCount++
                        incorrectText = " Too Deep Squat ! "
                    }
                }
            }

            S1_CHECK = false
            S2_CHECK = false
            S2_LOWER_CHECK = false
            S3_CHECK = false
            S3_DEEP_CHECK = false
            stateSequence.clear()

        } else if(angleValue > SQUAT_ANGLE_NORMAL_THRESHOLD && !S1_CHECK && !S2_CHECK && !S3_CHECK) {
            // 각도가 32 이상일때 스쿼트 자세 진입
            S1_CHECK = true
            incorrectText = ""
            stateSequence.clear()
            stateSequence.add(SquatPhase.S1)
            squatState = SquatPhase.S2
        }
    }

    private fun handleS2State(angleValue: Int) {

        if(angleValue < SQUAT_ANGLE_PASS_THRESHOLD && angleValue > SQUAT_ANGLE_TRANSITION_THRESHOLD) {
            // 각도 35이상 65이하
            S2_CHECK = true
            S2_LOWER_CHECK = true
        }
        if(angleValue > SQUAT_ANGLE_PASS_THRESHOLD) {
            // 각도 65이상
            S2_CHECK = true
            S2_LOWER_CHECK = false
        }

        if(angleValue > SQUAT_ANGLE_PASS_THRESHOLD && S2_CHECK && !S2_LOWER_CHECK) {
            // 적절한 스쿼트 65각도 이상으로 통과 S3로 이동
            stateSequence.add(SquatPhase.S2)
            squatState = SquatPhase.S3
        }
        else if(angleValue < SQUAT_ANGLE_NORMAL_THRESHOLD && S2_CHECK && S2_LOWER_CHECK) {
            // 적절한 스쿼트 각도를 통과하지 못하고 처음 준비자세 이전으로 돌아왔을때 S1 으로 이동 (Lower Your Hips)
            stateSequence.add(SquatPhase.S2)
            squatState = SquatPhase.S1
        }

    }
    private fun handleS3State(angleValue: Int) {

        if (angleValue > SQUAT_ANGLE_END_THRESHOLD && angleValue < SQUAT_ANGLE_DEEP_THRESHOLD) {
            // 각도 75 ~ 95 이하
            S3_CHECK = true
        }
        if( angleValue > SQUAT_ANGLE_DEEP_THRESHOLD) {
            // 각도 95이상
            S3_DEEP_CHECK = true
        }

        if( angleValue < SQUAT_ANGLE_PASS_THRESHOLD && S3_CHECK && !S3_DEEP_CHECK) {
            // 적절한 스쿼트로 각도가 65로 다시 이동될때 S1 으로 이동
            stateSequence.add(SquatPhase.S3)
            squatState = SquatPhase.S1
        }
        else if(angleValue < SQUAT_ANGLE_PASS_THRESHOLD && S3_CHECK && S3_DEEP_CHECK) {
            // 너무 깊은 각도의 스쿼트로 각도가 65로 다시 이동될때 S1 으로 이동
            stateSequence.add(SquatPhase.S3)
            squatState = SquatPhase.S1
        }
    }

private fun isRightAngle(angle:Double) {
     if (angle < -10) {
        isRight = false
    } else if(angle > 10) {
        isRight = true
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
        private const val LANDMARK_ARC_STROKE_WIDTH = 6F
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
    private enum class SquatPhase {
        S1,     // Angle < 32°
        S2,     // 35° <= Angle <= 65°
        S3      // 75° <= Angle <= 95°
    }
}
