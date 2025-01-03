/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sports2i.trainer.ui.view

import android.content.Context
import android.graphics.Canvas
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

class OverlayPushUpView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()
    private var arcPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    // 푸쉬업 감지를 위한 각도 기준치 설정
    // 사용자의 푸쉬업 스타일에 따라 조정 가능
    private val PUSH_UP_ANGLE_NORMAL_THRESHOLD = 60 // 푸쉬업 시작 기준 각도
    private val PUSH_UP_ANGLE_TRANSITION_THRESHOLD = 50 // 푸쉬업 동작 중 각도
    private val PUSH_UP_ANGLE_PASS_THRESHOLD = 15 // 푸쉬업 통과 기준 각도

    // 푸쉬업 수 카운터
    private var pushUpCorrectCount = 0
    private var pushUpInCorrectCount = 0
    private var incorrectText = ""

    // 초기 상태는 푸쉬업 준비 상태로 설정
    private var pushUpState = PushUpState.S1
    private var S1_CHECK = false
    private var S2_CHECK = false
    private var S2_LOWER_CHECK = false
    private var S3_CHECK = false
    private var isRight = false

    fun getPushUpCount(): Int {
        return pushUpCorrectCount
    }
    fun getPushUpInCorrectCount(): Int {
        return pushUpInCorrectCount
    }
    fun getIncorrectText(): String {
        return incorrectText
    }
    fun clearPushUpCount():Int{
        pushUpCorrectCount = 0
        return pushUpCorrectCount
    }
    fun clearPushUpInCorrectCount():Int{
        pushUpInCorrectCount = 0
        return pushUpInCorrectCount
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

        val leftWristIndex = POSE_LANDMARK_LEFT_WRIST
        val leftShoulderIndex = POSE_LANDMARK_LEFT_SHOULDER
        val leftElbowIndex = POSE_LANDMARK_LEFT_ELBOW

        results?.let { poseLandmarkerResult ->
            for (landmark in poseLandmarkerResult.landmarks()) {

                val leftShoulder = landmark.get(leftShoulderIndex)
                val leftElbow = landmark.get(leftElbowIndex)
//                val leftWrist = landmark.get(leftWristIndex)
//                val pushUpAngle = calculateElbowShoulderAngle(leftShoulder,leftElbow,leftWrist)

                val pushUpAngle = calculateElbowShoulderAngle(leftShoulder,leftElbow)
                detectPushUp(pushUpAngle.toInt())

                // 제외할 랜드마크 인덱스 (눈, 코, 입 랜드마크)
                val excludedLandmarkIndices = setOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

                // 푸쉬업 감지를 위해 팔꿈치와 어깨 사이의 각도를 계산하여 그림
                PoseLandmarker.POSE_LANDMARKS.forEach {
                    if (it != null) {
                        val shoulder = poseLandmarkerResult.landmarks().get(0).get(it.start())
                        val elbow = poseLandmarkerResult.landmarks().get(0).get(it.end())

                        if (it.start() !in excludedLandmarkIndices && it.end() !in excludedLandmarkIndices) {
                            canvas.drawLine(
                                shoulder.x() * imageWidth * scaleFactor,
                                shoulder.y() * imageHeight * scaleFactor,
                                elbow.x() * imageWidth * scaleFactor,
                                elbow.y() * imageHeight * scaleFactor,
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
                val cx = (leftShoulder.x() * imageWidth * scaleFactor + if(isRight) 20 else -20)
                val cy = (leftShoulder.y() * imageHeight * scaleFactor - 100)

                // 반원형 그리기
                val arcCx = (leftShoulder.x() * imageWidth * scaleFactor)
                val arcCy = (leftShoulder.y() * imageHeight * scaleFactor)
                val radius = 60f // 반지름 설정

//                val startAngle = 270f // 시작 각도 (위치 상단부터 시작하기 때문에 270도)
//                val startAngle = 1f // 시작 각도 (위치 상단부터 시작하기 때문에 270도)
//                val sweepAngle = pushUpAngle.toFloat() // 그릴 각도
                val ShoulderElbowLineAngle = Math.toDegrees(Math.atan2((leftElbow.y() - leftShoulder.y()).toDouble(), (leftElbow.x() - leftShoulder.x()).toDouble()))

                val startAngle = if (isRight) ShoulderElbowLineAngle.toFloat() + 360 else ShoulderElbowLineAngle.toFloat()
                val sweepAngle = if (isRight) -pushUpAngle.toFloat() else pushUpAngle.toFloat() // 엉덩이와 무릎 사이의 각도에서 발목의 각도를 뺀 값

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
                val angleText = "${pushUpAngle.toInt()}°"
                val textPaint = Paint()
                textPaint.color = ContextCompat.getColor(context!!, R.color.white)
                textPaint.textSize = 42f
                textPaint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                val textWidth = textPaint.measureText(angleText)
                val textX = cx - textWidth / 2
                val textY = cy + 60 // 텍스트를 원 위에 놓기 위해 조정

                canvas.drawText(angleText, textX, textY, textPaint)

//                val angleText = "${pushUpAngle.toInt()}°"
//                val textPaint = Paint()
//                textPaint.color = ContextCompat.getColor(context!!, R.color.white)
//                textPaint.textSize = 42f
//                textPaint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
//                val leftShoulderX = (leftShoulder.x() * imageWidth * scaleFactor + 40)
//                val leftShoulderY = (leftShoulder.y() * imageHeight * scaleFactor - 60)
//                canvas.drawText(angleText, leftShoulderX, leftShoulderY, textPaint)
            }
        }
    }

    private fun calculateElbowShoulderAngle(shoulder: NormalizedLandmark, elbow: NormalizedLandmark): Double {
        val deltaY = shoulder.y() - elbow.y()
        val deltaX = shoulder.x() - elbow.x()

        // 어깨와 팔꿈치의 x 좌표 비교하여 어떤 팔이 사용되었는지 확인
        val isRightArm = elbow.x() > shoulder.x()

        // 손목까지의 거리를 고려하지 않음
        var angle = Math.toDegrees(Math.atan2(deltaY.toDouble(), deltaX.toDouble()))

        if (isRightArm) {
            // 오른쪽 팔 사용 시
                angle = 180 - convertToPositiveAngle(angle)
                isRight = true
        } else {
            // 왼쪽 팔 사용 시
            if (convertToPositiveAngle(angle) < 180.0) {
                angle = convertToPositiveAngle(angle)
                isRight = false
            }
        }

//        isRightAngle(angle)
        return convertToPositiveAngle(angle)
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

    private var stateSequence: MutableList<PushUpState> = mutableListOf()

    // 푸쉬업 감지 함수
    private fun detectPushUp(angleValue: Int) {
        when (pushUpState) {
            PushUpState.S1 -> handleS1State(angleValue)
            PushUpState.S2 -> handleS2State(angleValue)
            PushUpState.S3 -> handleS3State(angleValue)
        }
    }

    private fun handleS1State(angleValue: Int) {
        if (angleValue > PUSH_UP_ANGLE_NORMAL_THRESHOLD) {
            when(stateSequence.size){
                1 -> {}
                2 -> {
                    if(S1_CHECK && S2_CHECK && S2_LOWER_CHECK) {
                        pushUpInCorrectCount++
                        incorrectText = " Further Down ! "
                    }
                }
                3 -> {
                    if(S1_CHECK && S2_CHECK && S3_CHECK) {
                        pushUpCorrectCount++
                        incorrectText = " Good ! "
                    }
                }
            }

            S1_CHECK = false
            S2_CHECK = false
            S2_LOWER_CHECK = false
            S3_CHECK = false
            stateSequence.clear()

        } else if(angleValue < PUSH_UP_ANGLE_NORMAL_THRESHOLD && !S1_CHECK && !S2_CHECK && !S3_CHECK) {
            // 각도가 60 이하일때 푸쉬업 자세 진입
            S1_CHECK = true
            incorrectText = ""
            stateSequence.clear()
            stateSequence.add(PushUpState.S1)
            pushUpState = PushUpState.S2
        }
    }

    private fun handleS2State(angleValue: Int) {

        if(angleValue > PUSH_UP_ANGLE_PASS_THRESHOLD && angleValue < PUSH_UP_ANGLE_TRANSITION_THRESHOLD) {
            // 각도 15이상 50이하
            S2_CHECK = true
            S2_LOWER_CHECK = true
        }
        if(angleValue < PUSH_UP_ANGLE_PASS_THRESHOLD) {
            // 각도 15이하
            S2_CHECK = true
            S2_LOWER_CHECK = false
        }

        if(angleValue < PUSH_UP_ANGLE_PASS_THRESHOLD && S2_CHECK && !S2_LOWER_CHECK) {
            // 적절한 푸쉬업 15각도 이하로 통과 S3로 이동
            stateSequence.add(PushUpState.S2)
            pushUpState = PushUpState.S3
        }

        else if(angleValue > PUSH_UP_ANGLE_TRANSITION_THRESHOLD && S2_CHECK && S2_LOWER_CHECK) {
            // 적절한 푸쉬업 각도를 통과하지 못하고 처음 준비자세 이전으로 돌아왔을때 S1 으로 이동
            stateSequence.add(PushUpState.S2)
            pushUpState = PushUpState.S1
        }
    }
    private fun handleS3State(angleValue: Int) {

        S3_CHECK = true
        stateSequence.add(PushUpState.S3)
        pushUpState = PushUpState.S1
    }
    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE,
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
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 14F
        private const val LANDMARK_ARC_STROKE_WIDTH = 6F
        private const val LANDMARK_POINT_WIDTH = 20F
        // 상바디 왼쪽 어깨 랜드마크 인덱스
        private const val POSE_LANDMARK_LEFT_SHOULDER = 11
        // 상바디 오른쪽 어깨 랜드마크 인덱스
        private const val POSE_LANDMARK_RIGHT_SHOULDER = 12
        // 왼쪽 팔꿈치 랜드마크 인덱스
        private const val POSE_LANDMARK_LEFT_ELBOW = 13
        // 오른쪽 팔꿈치 랜드마크 인덱스
        private const val POSE_LANDMARK_RIGHT_ELBOW = 14
        // 상바디 왼쪽 엉덩이 랜드마크 인덱스
        private const val POSE_LANDMARK_HIP = 23
        // 왼쪽 무릎 랜드마크 인덱스
        private const val POSE_LANDMARK_KNEE = 24

        // 왼쪽 손목
        private const val POSE_LANDMARK_LEFT_WRIST = 15
    }

    // 푸쉬업 동작 상태를 나타내는 열거형(enum) 클래스
    enum class PushUpState {
        S1,      // 푸쉬업 준비 상태
        S2,// 푸쉬업 동작 진행 중 상태
        S3    // 푸쉬업 동작 완료 상태
    }
}
