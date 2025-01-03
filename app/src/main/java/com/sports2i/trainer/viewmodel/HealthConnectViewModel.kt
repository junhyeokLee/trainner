package com.sports2i.trainer.viewmodel

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseRoute
import androidx.health.connect.client.records.ExerciseRouteResult
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class HealthConnectViewModel @Inject constructor() : ViewModel() {

     lateinit var  startTimeDate: Instant
     lateinit var endTimeDate: Instant

//   dataOriginFilter = setOf(DataOrigin("com.sec.android.app.shealth")) // 삼성 헬스
//   dataOriginFilter = setOf(DataOrigin("com.google.android.apps.fitness")) // 구글 피트니스

     val flow: MutableStateFlow<Long?> = MutableStateFlow(0)
     val flowStep: MutableStateFlow<Long?> = MutableStateFlow(0)
     val flowCalories: MutableStateFlow<Int?> = MutableStateFlow(0)
     val flowSpeedAvg: MutableStateFlow<Double?> = MutableStateFlow(0.0)
     val flowSpeedMin: MutableStateFlow<Double?> = MutableStateFlow(0.0)
     val flowSpeedMax: MutableStateFlow<Double?> = MutableStateFlow(0.0)
     val flowDistance: MutableStateFlow<Double?> = MutableStateFlow(0.0)
     val flowHeartRateAvg: MutableStateFlow<Long?> = MutableStateFlow(0)
     val flowHeartRateMax: MutableStateFlow<Long?> = MutableStateFlow(0)
     val flowHeartRateMin: MutableStateFlow<Long?> = MutableStateFlow(0)
     val flowSleepTime: MutableStateFlow<String?> = MutableStateFlow("")
     val flowExerciseRoute: MutableStateFlow<ExerciseSessionRecord?> = MutableStateFlow(null)

    @Composable
    fun fetchData(startTime: String, endTime: String) {
        // HealthConnectClient 및 타임존 초기화
        val healthConnectClient = HealthConnectClient.getOrCreate(LocalContext.current)
        val koreaTimeZone = ZoneId.of("Asia/Seoul")
        // UTC 타임존 초기화
//        val utcTimeZone = ZoneId.of("UTC")

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val startInstant = LocalDateTime.parse(startTime, formatter).atZone(koreaTimeZone).toInstant()
        val endInstant = LocalDateTime.parse(endTime, formatter).atZone(koreaTimeZone).toInstant()

        startTimeDate = startInstant
        endTimeDate = endInstant

        val startZoneOffset = koreaTimeZone.rules.getOffset(Instant.parse(startTimeDate.toString()))
        val endZoneOffset = koreaTimeZone.rules.getOffset(Instant.parse(endTimeDate.toString()))

        suspend fun aggregateCalories(): Int {
            var totalCalories: Int = 0

            try {
                val response = healthConnectClient.aggregate(
                    AggregateRequest(metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startTimeDate, endTimeDate),
                        dataOriginFilter = setOf(DataOrigin("com.sec.android.app.shealth"))
                    )
                )
                totalCalories = response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.toInt() ?: 0

            } catch (e: Exception) {}

            return totalCalories
        }


        suspend fun aggregateDistance(): Double {
            var totalDistance: Double = 0.0

            try {
                val response = healthConnectClient.aggregate(
                    AggregateRequest(metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startTimeDate, endTimeDate),
                        dataOriginFilter = setOf(DataOrigin("com.sec.android.app.shealth"))
                    )
                )
              totalDistance = response[DistanceRecord.DISTANCE_TOTAL]?.inKilometers ?: 0.0
            } catch (e: Exception) {}
            return totalDistance
        }


        suspend fun aggregateHeartRateAvg(): Long {
            var heartRageAvg: Long = 0

            try {
                val response = healthConnectClient.aggregate(
                    AggregateRequest(metrics = setOf(HeartRateRecord.BPM_AVG),
                        timeRangeFilter = TimeRangeFilter.between(startTimeDate, endTimeDate),
                        dataOriginFilter = setOf(DataOrigin("com.sec.android.app.shealth"))
                    )
                )
                heartRageAvg = response[HeartRateRecord.BPM_AVG] ?: 0

            } catch (e: Exception) {}
            return heartRageAvg
        }

        suspend fun aggregateHeartRateMax(): Long {
            var heartRageMax: Long = 0

            try {
                val response = healthConnectClient.aggregate(
                    AggregateRequest(
                        metrics = setOf(HeartRateRecord.BPM_MAX),
                        timeRangeFilter = TimeRangeFilter.between(startTimeDate, endTimeDate),
                        dataOriginFilter = setOf(DataOrigin("com.sec.android.app.shealth"))
                    )
                )
                heartRageMax = response[HeartRateRecord.BPM_MAX] ?: 0

            } catch (e: Exception) {}

            return heartRageMax
        }

        suspend fun aggregateHeartRateMin(): Long {
            var heartRageMin: Long = 0

            try {
                val response = healthConnectClient.aggregate(
                    AggregateRequest(metrics = setOf(HeartRateRecord.BPM_MIN),
                        timeRangeFilter = TimeRangeFilter.between(startTimeDate, endTimeDate),
                        dataOriginFilter = setOf(DataOrigin("com.sec.android.app.shealth"))
                    )
                )
                heartRageMin = response[HeartRateRecord.BPM_MIN] ?: 0

            } catch (e: Exception) {}

            return heartRageMin
        }

        // 세션운동을 통한 속도 가져오기
        suspend fun readExerciseSessions() {

            var speedRecords: List<SpeedRecord>? = null


            val response =
                healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        ExerciseSessionRecord::class,
                        TimeRangeFilter.between(startTimeDate, endTimeDate)
                    )
                )

            for (exerciseRecord in response.records) {

                speedRecords =
                    healthConnectClient
                        .readRecords(
                            ReadRecordsRequest(
                                SpeedRecord::class,
                                TimeRangeFilter.between(
                                    exerciseRecord.startTime,
                                    exerciseRecord.endTime
                                )
                            )
                        ).records


                speedRecords?.let { records ->
                    val speeds = mutableListOf<Double>()
                    records.forEach { record ->
                        record.samples.forEach { sample ->
                            val speed = sample.speed.inKilometersPerHour
                            if (speed != 0.0) {
                                speeds.add(speed)
                            }
                        }
                    }

                    if (speeds.isNotEmpty()) {
                        val minSpeed = speeds.minOrNull() ?: 0.0 // 최소값이 없을 경우를 대비해 기본값 설정
                        val maxSpeed = speeds.maxOrNull() ?: 0.0 // 최대값이 없을 경우를 대비해 기본값 설정
                        val averageSpeed = speeds.average()

                        flowSpeedMin.value = minSpeed
                        flowSpeedMax.value = maxSpeed
                        flowSpeedAvg.value = averageSpeed

                    } else {
                        Log.e("속도 데이터", "속도 데이터가 없습니다.")
                    }
                }
            }
        }

        suspend fun aggregateSleepTime(): String {
            var sleepTime: Duration = Duration.ZERO
            var totalSleep = ""

            try {
                val response = healthConnectClient.aggregate(
                    AggregateRequest(metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startTimeDate, endTimeDate),
                        dataOriginFilter = setOf(DataOrigin("com.sec.android.app.shealth"))))
                sleepTime = response[SleepSessionRecord.SLEEP_DURATION_TOTAL] ?: Duration.ZERO

                val hours = sleepTime.toHours()
                val minutes = (sleepTime.toMinutes() % 60)
                val seconds = (sleepTime.seconds % 60)

//                totalSleep = String.format("%02d 시간 %02d 분 %02d 초 수면", hours, minutes, seconds)
                totalSleep = hours.toString()

            } catch (e: Exception) { }

            return totalSleep
        }

        suspend fun readExerciseRoute(): ExerciseSessionRecord {
            var exerciseRoute:ExerciseSessionRecord? = null
            try {
                val response = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        ExerciseSessionRecord::class,
                        TimeRangeFilter.between(startTimeDate, endTimeDate)
                    )
                )
                exerciseRoute = response.records.first()

                flowExerciseRoute.value = exerciseRoute
            } catch (e: Exception) {
                // 예외 처리
                flowExerciseRoute.value = null
            }
            return exerciseRoute!!
        }


//        해당 속도 관련 함수들이 적용이 안됨
//        suspend fun aggregateSpeedAvg(): Double {
//            var avgSpeed: Double = 0.0
//            try {
//                val response = healthConnectClient.aggregate(
//                    AggregateRequest(metrics = setOf(SpeedRecord.SPEED_AVG),
//                        timeRangeFilter = TimeRangeFilter.between(startTimeDate, endTimeDate),
//                        dataOriginFilter = setOf(DataOrigin("com.sec.android.app.shealth"))
//                    )
//                )
//                avgSpeed = response[SpeedRecord.SPEED_AVG]?.inKilometersPerHour ?: 0.0
//
//            } catch (e: Exception) {}
//
//            Log.e("Debug", "aggregateSpeedAvg: $avgSpeed")
//            if(avgSpeed.isNaN()) avgSpeed = 0.0
//            return avgSpeed
//        }
//
//
//        suspend fun aggregateSpeedMax(): Double {
//            var maxSpeed: Double = 0.0
//            try {
//                val response = healthConnectClient.aggregate(
//                    AggregateRequest(metrics = setOf(SpeedRecord.SPEED_MAX),
//                        timeRangeFilter = TimeRangeFilter.between(startTimeDate, endTimeDate),
//                        dataOriginFilter = setOf(DataOrigin("com.sec.android.app.shealth"))
//                    )
//                )
//                maxSpeed = response[SpeedRecord.SPEED_MAX]?.inKilometersPerHour ?: 0.0
//
//            } catch (e: Exception) {}
//
//            Log.e("Debug", "aggregateSpeedMax: $maxSpeed")
//            return maxSpeed
//        }
//        suspend fun aggregateSpeedMin(): Double {
//            var minSpeed: Double = 0.0
//            try {
//                val response = healthConnectClient.aggregate(
//                    AggregateRequest(
//                        setOf(SpeedRecord.SPEED_MIN),
//                        timeRangeFilter = TimeRangeFilter.between(startTimeDate, endTimeDate),
//                        dataOriginFilter = setOf(DataOrigin("com.sec.android.app.shealth"))
//                    )
//                )
//                minSpeed = response[SpeedRecord.SPEED_MIN]?.inKilometersPerHour ?:  0.0
//
//            } catch (e: Exception) {
//                Log.e("Error", "Error in aggregateSpeedMin: $e")
//            }
//            return minSpeed
//        }



//        suspend fun readExerciseSessionAndRoute() {
//            // 운동 세션 레코드를 읽기 위한 권한을 확인합니다.
//            val grantedPermissions =
//                healthConnectClient.permissionController.getGrantedPermissions()
//            if (!grantedPermissions.contains(
//                    HealthPermission.getReadPermission(ExerciseSessionRecord::class))) {
//                // 사용자가 앱에 대한 운동 세션 데이터의 읽기 권한을 허용하지 않았습니다.
//                return
//            }
//
//            // 지정된 시간 범위 내의 운동 세션 레코드를 읽어옵니다.
//            val readResponse =
//                healthConnectClient.readRecords(
//                    ReadRecordsRequest(
//                        ExerciseSessionRecord::class,
//                        TimeRangeFilter.between(startTimeDate, endTimeDate)
//                    )
//                )
//
//            // 첫 번째 운동 세션 레코드를 가져옵니다.
//            val exerciseRecord = readResponse.records.firstOrNull()
//
//            // 운동 세션 레코드가 존재하는지 확인합니다.
//            if (exerciseRecord != null) {
//                // 운동 세션 레코드에서 운동 경로 결과를 가져옵니다.
//                val exerciseRouteResult = exerciseRecord.exerciseRouteResult
//
//                // 운동 경로 결과에 따라 처리합니다.
//                when (exerciseRouteResult) {
//                    is ExerciseRouteResult.Data -> {
//                        // 운동 경로가 있는 경우 해당 운동 경로를 표시합니다.
//                        Log.d("운동 경로가 있는 경우", "Exercise Route: ${exerciseRouteResult.exerciseRoute}")
//                    }
//                    is ExerciseRouteResult.ConsentRequired -> {
//                        // 운동 경로가 없고 사용자 동의가 필요한 경우, 사용자에게 동의를 요청합니다.
//                        Log.d("운동 경로가 없고 사용자 동의가 필요한 경우", "Consent Required")
//                    }
//                    is ExerciseRouteResult.NoData -> {
//                        // 운동 경로가 없는 경우 아무 작업도 수행하지 않습니다.
//                        Log.d("운동 경로가 없는 경우", "No Exercise Route")
//                    }
//                    else -> {
//                        // 기타 경우에는 아무 작업도 수행하지 않습니다.
//                        Log.d("기타 경우", "Other Case")
//                    }
//                }
//            } else {
//                // 운동 세션 레코드가 없는 경우 아무 작업도 수행하지 않습니다.
//                Log.d("운동 세션 레코드가 없는 경우", "No Exercise Session Record")
//            }
//        }


        suspend fun readSleepSessions() {
            val response =
                healthConnectClient.readRecords(
                    ReadRecordsRequest(SleepSessionRecord::class, timeRangeFilter = TimeRangeFilter.between(startTimeDate, endTimeDate)))
            for (sleepRecord in response.records) {
                // Retrieve relevant sleep stages from each sleep record
                val sleepStages = sleepRecord
            }
        }

        LaunchedEffect(Unit) {
        try{
            val caloriesBurnedRecord = aggregateCalories()
//            val speedAvg = aggregateSpeedAvg()
//            val speedMax = aggregateSpeedMax()
//            val speedMin = aggregateSpeedMin()
            val distance = aggregateDistance()
            val heartRateAvg = aggregateHeartRateAvg()
            val heartRateMax = aggregateHeartRateMax()
            val heartRateMin = aggregateHeartRateMin()
            val sleepTime = aggregateSleepTime()
            val readExerciseRoute = readExerciseRoute()

//            readExerciseSessionAndRoute()
            readExerciseSessions() // 세션을 통한 속도 가져오기

            flowCalories.value = caloriesBurnedRecord.toInt()
            flowDistance.value = distance
            flowHeartRateAvg.value = heartRateAvg
            flowHeartRateMax.value = heartRateMax
            flowHeartRateMin.value = heartRateMin
            flowSleepTime.value = sleepTime
            flowExerciseRoute.value = readExerciseRoute
        } catch (e: Exception) {
            Log.e("Debug", "Error: $e")
            // 예외 발생 시 기본값을 null로 설정
            flowCalories.value = 0
            flowSpeedAvg.value = 0.0
            flowSpeedMax.value = 0.0
            flowSpeedMin.value = 0.0
            flowDistance.value = 0.0
            flowHeartRateAvg.value = 0
            flowHeartRateMax.value = 0
            flowHeartRateMin.value = 0
            flowSleepTime.value = ""
        }
    }

        val dataStep by flowStep.collectAsState()
        val dataCalories by flowCalories.collectAsState()
        val dataSpeedAvg by flowSpeedAvg.collectAsState()
        val dataSpeedMax by flowSpeedMax.collectAsState()
        val dataSpeedMin by flowSpeedMin.collectAsState()
        val dataDistance by flowDistance.collectAsState()
        val dataHeartRateAvg by flowHeartRateAvg.collectAsState()
        val dataHeartRateMax by flowHeartRateMax.collectAsState()
        val dataHeartRateMin by flowHeartRateMin.collectAsState()
        val dataSleepTime by flowSleepTime.collectAsState()

    }
}