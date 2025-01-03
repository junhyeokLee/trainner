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
package com.google.mediapipe.examples.poselandmarker

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    // 푸쉬업 감지를 위한 각도 기준치 설정
// 사용자의 푸쉬업 스타일에 따라 조정 가능
    private val PUSH_UP_ANGLE_THRESHOLD = 300.0

    private val SQUAT_ANGLE_THRESHOLD = 100.0
    private var isSquatInProgress = false
    private var squatCount = 0


    // 상태 추적을 위한 변수들
    private var lastPushUpTimestamp: Long = 0
    private var isPushUpInProgress = false

    // 푸쉬업 수 카운터
    private var pushUpCount = 0

    // 초기 상태는 푸쉬업 준비 상태로 설정
    private var pushUpState = PushUpState.READY

    // 초기 상태는 스쿼트 준비 상태로 설정
    private var squatsState = SquatState.READY

    fun getPushUpCount(): Int {
        return pushUpCount
    }

    fun getSquatCount(): Int {
        return squatCount
    }

    // 이전 각도 값
    private var prevElbowShoulderAngle: Double? = null



    // 카운팅 초기화
    private fun resetPushUpCount() {
        isPushUpInProgress = false
        lastPushUpTimestamp = 0
        prevElbowShoulderAngle = null
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
            ContextCompat.getColor(context!!, R.color.primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }
    // 푸쉬업 상태를 초기화
    private fun resetPushUpState() {
        pushUpState = PushUpState.READY
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->
            for (landmark in poseLandmarkerResult.landmarks()) {
                // 기존 코드: 모든 랜드마크를 노란색 점으로 그림
                for (normalizedLandmark in landmark) {
                    canvas.drawPoint(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        pointPaint
                    )
                }
                // 푸쉬업 동작 감지 함수 호출
                detectPushUp(poseLandmarkerResult.landmarks()[0])
                detectSquat(poseLandmarkerResult.landmarks()[0])

                // 푸쉬업 감지를 위해 팔꿈치와 어깨 사이의 각도를 계산하여 그림
                PoseLandmarker.POSE_LANDMARKS.forEach {
                    if (it != null) {
                        val shoulder = poseLandmarkerResult.landmarks().get(0).get(it.start())
                        val elbow = poseLandmarkerResult.landmarks().get(0).get(it.end())
                        canvas.drawLine(
                            shoulder.x() * imageWidth * scaleFactor,
                            shoulder.y() * imageHeight * scaleFactor,
                            elbow.x() * imageWidth * scaleFactor,
                            elbow.y() * imageHeight * scaleFactor,
                            linePaint
                        )

                        // 팔꿈치와 어깨 사이의 각도 계산
//                        val elbowShoulderAngle = calculateElbowShoulderAngle(shoulder, elbow)
//
//                        // 다른 관절 정보를 활용하여 푸쉬업 동작 판단
//                        analyzeOtherJoints(poseLandmarkerResult.landmarks()[0])
//
//                        prevElbowShoulderAngle = elbowShoulderAngle
                    }
                }

                PoseLandmarker.POSE_LANDMARKS.forEach {
                    if (it != null) {
                        val hip = poseLandmarkerResult.landmarks().get(0).get(it.start())
                        val knee = poseLandmarkerResult.landmarks().get(0).get(it.end())
                        canvas.drawLine(
                            hip.x() * imageWidth * scaleFactor,
                            hip.y() * imageHeight * scaleFactor,
                            knee.x() * imageWidth * scaleFactor,
                            knee.y() * imageHeight * scaleFactor,
                            linePaint
                        )

                        // 팔꿈치와 어깨 사이의 각도 계산
//                        val elbowShoulderAngle = calculateElbowShoulderAngle(shoulder, elbow)
//
//                        // 다른 관절 정보를 활용하여 푸쉬업 동작 판단
//                        analyzeOtherJoints(poseLandmarkerResult.landmarks()[0])
//
//                        prevElbowShoulderAngle = elbowShoulderAngle
                    }
                }
            }
        }
        // 푸쉬업 카운트를 UI에 표시
        val pushUpText = "푸쉬업 카운트: $pushUpCount"
        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 36f
        canvas.drawText(pushUpText, 50f, 100f, textPaint)
    }

    // 두 점 사이의 각도 계산 == 푸쉬업 각도 계산
    private fun calculateElbowShoulderAngle(shoulder: NormalizedLandmark, elbow: NormalizedLandmark): Double {
        val deltaY = elbow.y() - shoulder.y()
        val deltaX = elbow.x() - shoulder.x()
        var angle = Math.toDegrees(Math.atan2(deltaY.toDouble(), deltaX.toDouble()))
        if (angle < 0) {
            angle += 360.0
        }
        return angle
    }

    // 스쿼트 각도 계산
    private fun calculateHipKneeAngle(hip: NormalizedLandmark, knee: NormalizedLandmark): Double {
        val deltaY = knee.y() - hip.y()
        val deltaX = knee.x() - hip.x()
        var angle = Math.toDegrees(Math.atan2(deltaY.toDouble(), deltaX.toDouble()))
        if (angle < 0) {
            angle += 360.0
        }
        return angle
    }

    // 푸쉬업 감지 함수
    private fun detectPushUp(landmarks: List<NormalizedLandmark>) {
        if (landmarks.size >= 24) {
            val shoulderIndex = POSE_LANDMARK_SHOULDER
            val elbowIndex = POSE_LANDMARK_ELBOW

            val shoulder = landmarks[shoulderIndex]
            val elbow = landmarks[elbowIndex]

            val shoulderElbowAngle = calculateElbowShoulderAngle(shoulder, elbow)

            when (pushUpState) {
                PushUpState.READY -> {
                    // 푸쉬업 준비 상태에서는 임계값을 초과하면 푸쉬업 동작을 시작하고 상태를 IN_PROGRESS로 변경
                    if (shoulderElbowAngle > PUSH_UP_ANGLE_THRESHOLD) {
                        pushUpState = PushUpState.IN_PROGRESS
                        Log.e(TAG, "푸쉬업 시작 - 현재 푸쉬업 카운트: $pushUpCount")
                    }
                }
                PushUpState.IN_PROGRESS -> {
                    // 푸쉬업 동작 진행 중 상태에서는 임계값 이하로 내려가면 푸쉬업 동작을 완료하고 상태를 COMPLETE로 변경
                    if (shoulderElbowAngle <= PUSH_UP_ANGLE_THRESHOLD) {
                        pushUpState = PushUpState.COMPLETE
                        pushUpCount++
                        Log.e(TAG, "푸쉬업 완료 - 현재 푸쉬업 카운트: $pushUpCount")
                    }
                }
                PushUpState.COMPLETE -> {
                    // 푸쉬업 동작 완료 상태에서는 임계값 이하로 내려가면 다시 푸쉬업 준비 상태로 변경
                    if (shoulderElbowAngle <= PUSH_UP_ANGLE_THRESHOLD) {
                        pushUpState = PushUpState.READY
                    }
                }
            }
        }
    }

    private fun detectSquat(landmarks: List<NormalizedLandmark>) {
        if (landmarks.size >= 25) {
            val hipIndex = POSE_LANDMARK_HIP
            val kneeIndex = POSE_LANDMARK_KNEE

            val hip = landmarks[hipIndex]
            val knee = landmarks[kneeIndex]

            val hipKneeAngle = calculateHipKneeAngle(hip, knee)

            when (squatsState) {
                SquatState.READY -> {
                    // 스쿼트 준비 상태에서는 임계값을 초과하면 푸쉬업 동작을 시작하고 상태를 IN_PROGRESS로 변경
                    if (hipKneeAngle > SQUAT_ANGLE_THRESHOLD) {
                        squatsState = SquatState.IN_PROGRESS
                        Log.e(TAG, "스쿼트 시작 - 현재 스쿼트 카운트: $squatCount")
                    }
                }
                SquatState.IN_PROGRESS -> {
                    // 스쿼트 동작 진행 중 상태에서는 임계값 이하로 내려가면 푸쉬업 동작을 완료하고 상태를 COMPLETE로 변경
                    if (hipKneeAngle <= SQUAT_ANGLE_THRESHOLD) {
                        squatsState = SquatState.COMPLETE
                        squatCount++
                        Log.e(TAG, "스쿼트 완료 - 현재 푸쉬업 카운트: $squatCount")
                    }
                }
                SquatState.COMPLETE -> {
                    // 스쿼트 동작 완료 상태에서는 임계값 이하로 내려가면 다시 푸쉬업 준비 상태로 변경
                    if (hipKneeAngle <= SQUAT_ANGLE_THRESHOLD) {
                        squatsState = SquatState.READY
                    }
                }
            }
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
//        resetPushUpCount()

        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
        // 상바디 왼쪽 어깨 랜드마크 인덱스
        private const val POSE_LANDMARK_SHOULDER = 11
        // 왼쪽 손목 랜드마크 인덱스
        private const val POSE_LANDMARK_ELBOW = 13
        // 상바디 왼쪽 엉덩이 랜드마크 인덱스
        private const val POSE_LANDMARK_HIP = 23
        // 왼쪽 무릎 랜드마크 인덱스
        private const val POSE_LANDMARK_KNEE = 24
    }

    // 푸쉬업 동작 상태를 나타내는 열거형(enum) 클래스
    private enum class PushUpState {
        READY,      // 푸쉬업 준비 상태
        IN_PROGRESS,// 푸쉬업 동작 진행 중 상태
        COMPLETE    // 푸쉬업 동작 완료 상태
    }
    // 스쿼트 동작 상태를 나타내는 열거형(enum) 클래스
    private enum class SquatState {
        READY,      // 푸쉬업 준비 상태
        IN_PROGRESS,// 푸쉬업 동작 진행 중 상태
        COMPLETE    // 푸쉬업 동작 완료 상태
    }
}
