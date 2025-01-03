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
import com.sports2i.trainer.ui.view.OverlayPushUpView
import kotlin.math.max
import kotlin.math.min

class OverlayDeadLiftView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()
    private var arcShoulderHipPaint = Paint()
    private var arcHipKneePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private val DEADLIFT_HIP_KNEE_ANGLE_NORMAL_THRESHOLD = 12 // 데드리프트 힙 무릎 시작 기준 각도
    private val DEADLIFT_HIP_KNEE_ANGLE_TRANSITION_THRESHOLD = 15 // 데드리프트 힙 무릎 동작 중 각도
    private val DEADLIFT_HIP_KNEE_ANGLE_PASS_THRESHOLD = 25 // 데드리프트 힙 무릎 통과 기준 각도
    private val DEADLIFT_HIP_KNEE_ANGLE_END_THRESHOLD = 30 // 데드리프트 힙 무릎 종료 기준 각도
    private val DEADLIFT_HIP_KNEE_ANGLE_DEEP_THRESHOLD = 60 // 데드리프트 힙 무릎 너무 깊은 각도

    private val DEADLIFT_HIP_SHOULDER_ANGLE_NORMAL_THRESHOLD = 22 // 데드리프트 어깨 힙 시작 기준 각도
    private val DEADLIFT_HIP_SHOULDER_ANGLE_TRANSITION_THRESHOLD = 25 // 데드리프트 어깨 힙 동작 중 각도
    private val DEADLIFT_HIP_SHOULDER_ANGLE_PASS_THRESHOLD = 35 // 데드리프트 어깨 힙 통과 기준 각도
    private val DEADLIFT_HIP_SHOULDER_ANGLE_END_THRESHOLD = 40 // 데드리프트 어깨 힙 종료 기준 각도
    private val DEADLIFT_HIP_SHOULDER_ANGLE_DEEP_THRESHOLD = 80 // 데드리프트 어깨 힙 너무 깊은 각도

    // 컨벤션 데드리프트 감지를 위한 각도 기준치 설정
    private var deadLiftCorrectCount = 0
    private var deadLiftInCorrectCount = 0
    private var incorrectText = ""

    // 초기 상태는 컨벤션 데드리프트 준비 상태로 설정
    private var deadLiftState = DeadLiftPhase.S1
    private var S1_CHECK = false
    private var S2_CHECK = false
    private var S2_LOWER_CHECK = false
    private var S3_CHECK = false
    private var S3_DEEP_CHECK = false
    private var isRight = false
    fun getDeadLiftCount(): Int {
        return deadLiftCorrectCount
    }
    fun getDeadLiftInCorrectCount(): Int {
        return deadLiftInCorrectCount
    }
    fun getIncorrectText(): String {
        return incorrectText
    }
    fun clearDeadLiftCount():Int{
        deadLiftCorrectCount = 0
        return deadLiftCorrectCount
    }
    fun clearDeadLiftInCorrectCount():Int{
        deadLiftInCorrectCount = 0
        return deadLiftInCorrectCount
    }
    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        arcShoulderHipPaint.reset()
        arcHipKneePaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color = ContextCompat.getColor(context!!, R.color.icActive)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        arcShoulderHipPaint.color = ContextCompat.getColor(context!!, R.color.pose_message)
        arcShoulderHipPaint.strokeWidth = LANDMARK_ARC_STROKE_WIDTH
        arcShoulderHipPaint.style = Paint.Style.STROKE

        arcHipKneePaint.color = ContextCompat.getColor(context!!, R.color.pose_message)
        arcHipKneePaint.strokeWidth = LANDMARK_ARC_STROKE_WIDTH
        arcHipKneePaint.style = Paint.Style.STROKE

        pointPaint.color = resources.getColor(R.color.primary)
        pointPaint.strokeWidth = LANDMARK_POINT_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val hipIndex = POSE_LANDMARK_LEFT_HIP
        val kneeIndex = POSE_LANDMARK_LEFT_KNEE
        val ankleIndex = POSE_LANDMARK_LEFT_ANKLE
        val shoulderIndex = POSE_LANDMARK_LEFT_SHOULDER

        results?.let { poseLandmarkerResult ->
            for (landmark in poseLandmarkerResult.landmarks()) {

                val hip = landmark.get(hipIndex)
                val knee = landmark.get(kneeIndex)
                val ankle = landmark.get(ankleIndex)
                val shoulder = landmark.get(shoulderIndex)

                // 힙과 무릎 사이의 각도 계산
                val hipKneeAngle = calculateHipAndKneeDeadLift(hip, knee)
                // 힙과 어깨 사이의 각도 계산
                val hipShoulderAngle = calculateHipAndShoulderDeadLift(hip, shoulder)

                detectDeadLift(hipShoulderAngle.toInt(),hipKneeAngle.toInt())

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

                // 힙,무릎 반원형 그리기
                val hipKneeCx = (knee.x() * imageWidth * scaleFactor)
                val hipKneeCy = (knee.y() * imageHeight * scaleFactor)
                val radius = 50f // 반지름 설정
//                val startHipKneeAngle = if (isRight) 90f else 90f // 시작 각도 (위치 상단부터 시작하기 때문에 270도)
//                val sweepHipKneeAngle = if (isRight) -hipKneeAngle.toFloat() else hipKneeAngle.toFloat() // 그릴 각도

                val hipKneeLineAngle = Math.toDegrees(Math.atan2((knee.y() - hip.y()).toDouble(), (knee.x() - hip.x()).toDouble()))
                val hipAnkleLineAngle = Math.toDegrees(Math.atan2((ankle.y() - hip.y()).toDouble(), (ankle.x() - hip.x()).toDouble()))

                val kneeAnkleLineAngle = Math.toDegrees(Math.atan2((ankle.y() - knee.y()).toDouble(), (ankle.x() - knee.x()).toDouble()))
                // 엉덩이, 무릎, 발목 간의 각도 계산
                val hipKneeAnkleAngle = hipAnkleLineAngle - hipKneeLineAngle

                val startHipKneeAngle = if (isRight) kneeAnkleLineAngle.toFloat() + 360 else kneeAnkleLineAngle.toFloat()
                val sweepHipKneeAngle = if (isRight) hipKneeAnkleAngle.toFloat() else hipKneeAnkleAngle.toFloat() // 엉덩이와 무릎 사이의 각도에서 발목의 각도를 뺀 값

                // 선의 크기와 색상 설정
                canvas.drawArc(
                    hipKneeCx - radius,
                    hipKneeCy - radius,
                    hipKneeCx + radius,
                    hipKneeCy + radius,
                    startHipKneeAngle,
                    sweepHipKneeAngle,
                    true, // 반원 형태로 그리기 위해 true로 설정
                    arcHipKneePaint)

                // 힙과무릎 각도 텍스트 그리기
                val hipKneeCxText = (hip.x() * imageWidth * scaleFactor + if(isRight) 80 else -80)
                val hipKneeCyText = (hip.y() * imageHeight * scaleFactor + 100)
                val hipKneeAngleText = "${hipKneeAngle.toInt()}°"
                val hipKneeTextPaint = Paint()
                hipKneeTextPaint.color = ContextCompat.getColor(context!!, R.color.white)
                hipKneeTextPaint.textSize = 42f
                hipKneeTextPaint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                val textHipKneeWidth = hipKneeTextPaint.measureText(hipKneeAngleText)
                val textHipKneeX = hipKneeCxText - textHipKneeWidth / 2
                val textHipKneeY = hipKneeCyText + 60 // 텍스트를 원 위에 놓기 위해 조정
                canvas.drawText(hipKneeAngleText, textHipKneeX, textHipKneeY, hipKneeTextPaint)

                // 힙어깨
                // 힙,어깨 반원형 그리기
                val hipShoulderCx = (hip.x() * imageWidth * scaleFactor)
                val hipShoulderCy = (hip.y() * imageHeight * scaleFactor)
                val hipShoulderRadius = 50f // 반지름 설정
//                val startHipShoulderAngle = if (isRight) 90f else 90f // 시작 각도 (위치 상단부터 시작하기 때문에 270도)
//                val sweepHipShoulderAngle = if (isRight) -hipKneeAngle.toFloat() else hipKneeAngle.toFloat() // 그릴 각도

                val hipShoulderLineAngle = Math.toDegrees(Math.atan2((hip.y() - shoulder.y()).toDouble(), (hip.x() - shoulder.x()).toDouble()))
                val hipKnee2LineAngle = Math.toDegrees(Math.atan2((knee.y() - shoulder.y()).toDouble(), (knee.x() - shoulder.x()).toDouble()))

                val kneeKneeLineAngle = Math.toDegrees(Math.atan2((knee.y() - hip.y()).toDouble(), (knee.x() - hip.x()).toDouble()))

                // 엉덩이, 무릎, 발목 간의 각도 계산
                val hipKneeShoulderAngle = hipKnee2LineAngle - hipShoulderLineAngle

                val startHipShoulderAngle = if (isRight) kneeKneeLineAngle.toFloat() + 360 else kneeKneeLineAngle.toFloat()
                val sweepHipShoulderAngle = if (isRight) hipKneeShoulderAngle.toFloat() else hipKneeShoulderAngle.toFloat() // 엉덩이와 무릎 사이의 각도에서 발목의 각도를 뺀 값

                // 선의 크기와 색상 설정
                canvas.drawArc(
                    hipShoulderCx - hipShoulderRadius,
                    hipShoulderCy - hipShoulderRadius,
                    hipShoulderCx + hipShoulderRadius,
                    hipShoulderCy + hipShoulderRadius,
                    startHipShoulderAngle,
                    sweepHipShoulderAngle,
                    true, // 반원 형태로 그리기 위해 true로 설정
                    arcShoulderHipPaint)

                // 힙과어깨 각도 성공 시 arcPaint 색상을 및 스타일변경
                if(hipShoulderAngle.toInt() >= DEADLIFT_HIP_SHOULDER_ANGLE_PASS_THRESHOLD){
                    arcShoulderHipPaint.color = ContextCompat.getColor(context!!, R.color.light_green) // GREEN 색상으로 설정
//                    arcShoulderHipPaint.style = Paint.Style.FILL // FILL로 스타일 변경
                }
                else {
                    arcShoulderHipPaint.color = ContextCompat.getColor(context!!, R.color.pose_message) // GREEN 색상으로 설정
//                    arcShoulderHipPaint.style = Paint.Style.STROKE // FILL로 스타일 변경
                }
                if(hipKneeAngle.toInt() >= DEADLIFT_HIP_KNEE_ANGLE_PASS_THRESHOLD) {
                    arcHipKneePaint.color = ContextCompat.getColor(context!!, R.color.light_green) // GREEN 색상으로 설정
//                    arcHipKneePaint.style = Paint.Style.FILL // FILL로 스타일 변경
                }
                else {
                    arcHipKneePaint.color = ContextCompat.getColor(context!!, R.color.pose_message) // GREEN 색상으로 설정
//                    arcHipKneePaint.style = Paint.Style.STROKE // FILL로 스타일 변경
                }

                // 힙과어깨 각도 텍스트 그리기
                val hipShoulderCxText = (hip.x() * imageWidth * scaleFactor + if(isRight) 80 else -80)
                val hipShoulderCyText = (hip.y() * imageHeight * scaleFactor - 40)
                val hipShoulderAngleText = "${hipShoulderAngle.toInt()}°"
                val hipShoulderTextPaint = Paint()
                hipShoulderTextPaint.color = ContextCompat.getColor(context!!, R.color.white)
                hipShoulderTextPaint.textSize = 42f
                hipShoulderTextPaint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                val textHipShoulderWidth = hipKneeTextPaint.measureText(hipShoulderAngleText)
                val textHipShoulderX = hipShoulderCxText - textHipShoulderWidth / 2
                val textHipShoulderY = hipShoulderCyText + 60 // 텍스트를 원 위에 놓기 위해 조정
                canvas.drawText(hipShoulderAngleText, textHipShoulderX, textHipShoulderY, hipKneeTextPaint)
            }
        }
    }

    private fun calculateHipAndKneeDeadLift(hip: NormalizedLandmark, knee: NormalizedLandmark): Double {
        // 힙-무릎 선과 수직선 사이의 각도 계산
        val hipKneeLineAngle = Math.toDegrees(Math.atan2((knee.y() - hip.y()).toDouble(), (knee.x() - hip.x()).toDouble()))
        val verticalLineAngle = 90.0 // 수직선의 각도는 90도

        var kneeVerticalAngle =   hipKneeLineAngle - verticalLineAngle

        if (convertToPositiveAngle(kneeVerticalAngle) > 180.0 ) {
            kneeVerticalAngle = 360 - convertToPositiveAngle(kneeVerticalAngle)
        }
        isRightAngle(kneeVerticalAngle)
        return convertToPositiveAngle(kneeVerticalAngle)
    }

    private fun calculateHipAndShoulderDeadLift(hip: NormalizedLandmark, shoulder: NormalizedLandmark): Double {
        // 힙-어깨 선과 수직선 사이의 각도 계산
        val hipShoulderLineAngle = Math.toDegrees(Math.atan2((hip.y() - shoulder.y()).toDouble(), (hip.x() - shoulder.x()).toDouble()))
        val verticalLineAngle = 90.0 // 수직선의 각도는 90도

        var shoulderVerticalAngle =   hipShoulderLineAngle - verticalLineAngle

        if (convertToPositiveAngle(shoulderVerticalAngle) > 180.0 ) {
            shoulderVerticalAngle = 360 - convertToPositiveAngle(shoulderVerticalAngle)
        }
        isRightAngle(shoulderVerticalAngle)
        return convertToPositiveAngle(shoulderVerticalAngle)

        return shoulderVerticalAngle
    }


    private var stateSequence: MutableList<DeadLiftPhase> = mutableListOf()
    private fun handleS1State(shoulderAngleValue: Int, kneeAngleValue: Int) {
        if (shoulderAngleValue < DEADLIFT_HIP_SHOULDER_ANGLE_NORMAL_THRESHOLD && kneeAngleValue < DEADLIFT_HIP_KNEE_ANGLE_NORMAL_THRESHOLD) {
            when(stateSequence.size){
                1 -> {}
                2 -> {
                    if(S1_CHECK && S2_CHECK && S2_LOWER_CHECK) {
                        deadLiftInCorrectCount++
                        incorrectText = " Lower Your Hips ! "
                    }
                }
                3 -> {
                    if(S1_CHECK && S2_CHECK && S3_CHECK && !S3_DEEP_CHECK) {
                        deadLiftCorrectCount++
                        incorrectText = " Good ! "
                    }
                    else if(S1_CHECK && S2_CHECK && S3_CHECK && S3_DEEP_CHECK) {
                        deadLiftInCorrectCount++
                        incorrectText = " Too Deep DeadLift ! "
                    }
                }
            }

            S1_CHECK = false
            S2_CHECK = false
            S2_LOWER_CHECK = false
            S3_CHECK = false
            S3_DEEP_CHECK = false
            stateSequence.clear()

        } else if( shoulderAngleValue > DEADLIFT_HIP_SHOULDER_ANGLE_NORMAL_THRESHOLD && kneeAngleValue > DEADLIFT_HIP_KNEE_ANGLE_NORMAL_THRESHOLD && !S1_CHECK && !S2_CHECK && !S3_CHECK) {
            // 각도가 32 이상일때 스쿼트 자세 진입
            S1_CHECK = true
            incorrectText = ""
            stateSequence.clear()
            stateSequence.add(DeadLiftPhase.S1)
            deadLiftState = DeadLiftPhase.S2
        }
    }

    private fun handleS2State(shoulderAngleValue: Int, kneeAngleValue: Int) {

        if(shoulderAngleValue < DEADLIFT_HIP_SHOULDER_ANGLE_PASS_THRESHOLD && shoulderAngleValue > DEADLIFT_HIP_SHOULDER_ANGLE_TRANSITION_THRESHOLD && kneeAngleValue < DEADLIFT_HIP_KNEE_ANGLE_PASS_THRESHOLD && kneeAngleValue > DEADLIFT_HIP_KNEE_ANGLE_TRANSITION_THRESHOLD) {
            S2_CHECK = true
            S2_LOWER_CHECK = true
        }
        if(shoulderAngleValue > DEADLIFT_HIP_SHOULDER_ANGLE_PASS_THRESHOLD && kneeAngleValue > DEADLIFT_HIP_KNEE_ANGLE_PASS_THRESHOLD) {
            S2_CHECK = true
            S2_LOWER_CHECK = false
        }

        if(shoulderAngleValue > DEADLIFT_HIP_SHOULDER_ANGLE_PASS_THRESHOLD && kneeAngleValue > DEADLIFT_HIP_KNEE_ANGLE_PASS_THRESHOLD && S2_CHECK && !S2_LOWER_CHECK) {
            stateSequence.add(DeadLiftPhase.S2)
            deadLiftState = DeadLiftPhase.S3
        }
        else if(shoulderAngleValue < DEADLIFT_HIP_SHOULDER_ANGLE_NORMAL_THRESHOLD && kneeAngleValue < DEADLIFT_HIP_KNEE_ANGLE_NORMAL_THRESHOLD && S2_CHECK && S2_LOWER_CHECK) {
            // 적절한 스쿼트 각도를 통과하지 못하고 처음 준비자세 이전으로 돌아왔을때 S1 으로 이동 (Lower Your Hips)
            stateSequence.add(DeadLiftPhase.S2)
            deadLiftState = DeadLiftPhase.S1
        }
    }
    private fun handleS3State(shoulderAngleValue: Int, kneeAngleValue: Int) {

        if ( shoulderAngleValue > DEADLIFT_HIP_SHOULDER_ANGLE_END_THRESHOLD && shoulderAngleValue < DEADLIFT_HIP_SHOULDER_ANGLE_DEEP_THRESHOLD && kneeAngleValue > DEADLIFT_HIP_KNEE_ANGLE_END_THRESHOLD && kneeAngleValue < DEADLIFT_HIP_KNEE_ANGLE_DEEP_THRESHOLD) {
            S3_CHECK = true
        }
        if( shoulderAngleValue > DEADLIFT_HIP_SHOULDER_ANGLE_DEEP_THRESHOLD && kneeAngleValue > DEADLIFT_HIP_KNEE_ANGLE_DEEP_THRESHOLD) {
            S3_DEEP_CHECK = true
        }

        if( shoulderAngleValue < DEADLIFT_HIP_SHOULDER_ANGLE_PASS_THRESHOLD && kneeAngleValue < DEADLIFT_HIP_KNEE_ANGLE_PASS_THRESHOLD && S3_CHECK && !S3_DEEP_CHECK) {
            stateSequence.add(DeadLiftPhase.S3)
            deadLiftState = DeadLiftPhase.S1
        }
        else if( shoulderAngleValue < DEADLIFT_HIP_SHOULDER_ANGLE_PASS_THRESHOLD && kneeAngleValue < DEADLIFT_HIP_KNEE_ANGLE_PASS_THRESHOLD && S3_CHECK && S3_DEEP_CHECK) {
            stateSequence.add(DeadLiftPhase.S3)
            deadLiftState = DeadLiftPhase.S1
        }
    }

    private fun detectDeadLift(shouldAngleValue: Int, kneeAngleValue: Int) {
        when (deadLiftState) {
            DeadLiftPhase.S1 -> handleS1State(shouldAngleValue, kneeAngleValue)
            DeadLiftPhase.S2 -> handleS2State(shouldAngleValue, kneeAngleValue)
            DeadLiftPhase.S3 -> handleS3State(shouldAngleValue, kneeAngleValue)
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
        private const val LANDMARK_POINT_WIDTH = 20F
        private const val LANDMARK_ARC_STROKE_WIDTH = 4F
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
    private enum class DeadLiftPhase {
        S1,     // Angle < 32°
        S2, // 35° <= Angle <= 65°
        S3        // 75° <= Angle <= 95°
    }
}
