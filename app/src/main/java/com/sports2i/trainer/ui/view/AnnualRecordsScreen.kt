package com.sports2i.trainer.ui.view

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val flow: MutableStateFlow<Long> = MutableStateFlow(0)
private val flowStep: MutableStateFlow<Long> = MutableStateFlow(0)
private val flowCalories: MutableStateFlow<Int> = MutableStateFlow(0)
private val flowSpeedAvg: MutableStateFlow<Double> = MutableStateFlow(0.0)
private val flowSpeedMin: MutableStateFlow<Double> = MutableStateFlow(0.0)
private val flowSpeedMax: MutableStateFlow<Double> = MutableStateFlow(0.0)
private val flowDistance: MutableStateFlow<Double> = MutableStateFlow(0.0)
private val flowHeartRateAvg: MutableStateFlow<Long> = MutableStateFlow(0)
private val flowHeartRateMax: MutableStateFlow<Long> = MutableStateFlow(0)
private val flowHeartRateMin: MutableStateFlow<Long> = MutableStateFlow(0)
private val flowSleepTime: MutableStateFlow<String> = MutableStateFlow("")

//private val dataOrigin = DataOrigin("com.sec.android.app.shealth")

@Composable
fun AnnualRecordsScreen() {
    val healthConnectClient = HealthConnectClient.getOrCreate(LocalContext.current)
    val koreaTimeZone = ZoneId.of("Asia/Seoul")


    suspend fun readStepsByTimeRange(
        healthConnectClient: HealthConnectClient, startTime: Instant, endTime: Instant): Long {
        var totalStep:Long = 0
        try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            for (stepRecord in response.records) {
                Log.e("Debug", "stepRecord: ${stepRecord.count.toInt()}")
                totalStep += stepRecord.count
            }
        } catch (e: Exception) {
            // Run error handling here
        }
        return totalStep
    }


    suspend fun aggregateStep(healthConnectClient: HealthConnectClient, startTime: Instant, endTime: Instant): Long {
        var totalStep : Long = 0

        try {
            val response = healthConnectClient.aggregate(
                AggregateRequest(setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)))

            totalStep = response[StepsRecord.COUNT_TOTAL]!!

            Log.e("Debug", "totalStep: $totalStep")
        } catch (e: Exception) { }

        return totalStep

    }

//    suspend fun aggregateCalories(healthConnectClient: HealthConnectClient, startTime: Instant, endTime: Instant): Int {
//        var totalCalories : Int = 0
//
//        try {
//            val response = healthConnectClient.aggregate(
//                AggregateRequest(
//                    metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
//                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
//                )
//            )
//            totalCalories = response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.toInt() ?: 0
//        }
//        catch (e: Exception) { }
//
//        return totalCalories
//    }
    suspend fun aggregateCalories(healthConnectClient: HealthConnectClient, startTime: LocalDateTime, endTime: LocalDateTime): Int {
        var totalCalories : Int = 0

        try {
            val response = healthConnectClient.aggregateGroupByDuration(
                AggregateGroupByDurationRequest(
                    metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    timeRangeSlicer = Duration.ofMinutes(1L)
                )
            )
            for (durationResult in response) {
                totalCalories = durationResult.result[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inCalories?.toInt() ?: 0
            }
        }
        catch (e: Exception) { }

        return totalCalories
    }

    suspend fun aggregateSpeedAvg(healthConnectClient: HealthConnectClient, startTime: Instant, endTime: Instant): Double {
        var totalSpeed : Double = 0.0

        try {
            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(SpeedRecord.SPEED_AVG),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            totalSpeed = response[SpeedRecord.SPEED_AVG]?.inKilometersPerHour ?: 0.0

            Log.e("Debug", "totalSpeedAvg: $totalSpeed")
        }
        catch (e: Exception) { }

        return totalSpeed
    }

    suspend fun aggregateSpeedMax(healthConnectClient: HealthConnectClient, startTime: Instant, endTime: Instant): Double {
        var totalSpeed : Double = 0.0

        try {
            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(SpeedRecord.SPEED_MAX),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            totalSpeed = response[SpeedRecord.SPEED_MAX]?.inKilometersPerHour ?: 0.0

            Log.e("Debug", "totalSpeedMax: $totalSpeed")
        }
        catch (e: Exception) { }

        return totalSpeed
    }

    suspend fun aggregateSpeedMin(healthConnectClient: HealthConnectClient, startTime: Instant, endTime: Instant): Double {
        var totalSpeed : Double = 0.0

        try {
            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(SpeedRecord.SPEED_MIN),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            totalSpeed = response[SpeedRecord.SPEED_MIN]?.inKilometersPerHour ?: 0.0

            Log.e("Debug", "totalSpeedMin: $totalSpeed")
        }
        catch (e: Exception) { }

        return totalSpeed
    }

//    suspend fun aggregateDistance(healthConnectClient: HealthConnectClient, startTime: Instant, endTime: Instant): Double {
//        var totalDistance : Double = 0.0
//
//        try {
//            val response = healthConnectClient.aggregate(
//                AggregateRequest(
//                    metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
//                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
//                    timeRangeSlicer
//                )
//            )
//            totalDistance = response[DistanceRecord.DISTANCE_TOTAL]?.inKilometers ?: 0.0
//
//            Log.e("Debug", "totalDistance: $totalDistance")
//        }
//        catch (e: Exception) { }
//
//        return totalDistance
//    }
    suspend fun aggregateDistance(healthConnectClient: HealthConnectClient, startTime: LocalDateTime, endTime: LocalDateTime): Double {
        var totalDistance : Double = 0.0

        try {
            val response = healthConnectClient.aggregateGroupByDuration(
                AggregateGroupByDurationRequest(
                    metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    timeRangeSlicer = Duration.ofMinutes(1L)
                )
            )
            for (durationResult in response) {
                // The result may be null if no data is available in the time range
                totalDistance = durationResult.result[DistanceRecord.DISTANCE_TOTAL]?.inKilometers!!
            }


            Log.e("Debug", "totalDistance: $totalDistance")
        }
        catch (e: Exception) { }

        return totalDistance
    }


    suspend fun aggregateHeartRateAvg(healthConnectClient: HealthConnectClient, startTime: Instant, endTime: Instant): Long {
        var totalHeartRageAvg : Long = 0

        try {
            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(HeartRateRecord.BPM_AVG),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            totalHeartRageAvg = response[HeartRateRecord.BPM_AVG] ?: 0

            Log.e("Debug", "totalHeartRageAvg: $totalHeartRageAvg")
        }
        catch (e: Exception) { }

        return totalHeartRageAvg
    }

    suspend fun aggregateHeartRateMax(healthConnectClient: HealthConnectClient, startTime: Instant, endTime: Instant): Long {
        var totalHeartRageMax : Long = 0

        try {
            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(HeartRateRecord.BPM_MAX),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            totalHeartRageMax = response[HeartRateRecord.BPM_MAX] ?: 0

            Log.e("Debug", "totalHeartRageMax: $totalHeartRageMax")
        }
        catch (e: Exception) { }

        return totalHeartRageMax
    }


    suspend fun aggregateHeartRateMin(healthConnectClient: HealthConnectClient, startTime: Instant, endTime: Instant): Long {
        var totalHeartRageMin : Long = 0

        try {
            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(HeartRateRecord.BPM_MIN),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            totalHeartRageMin = response[HeartRateRecord.BPM_MIN] ?: 0

            Log.e("Debug", "totalHeartRageMin: $totalHeartRageMin")
        }
        catch (e: Exception) { }

        return totalHeartRageMin
    }

    suspend fun aggregateSleepTime(healthConnectClient: HealthConnectClient, startTime: Instant, endTime: Instant): String {
        var sleepTime : Duration = Duration.ZERO
        var totalSleep = ""

        try {
            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            sleepTime = response[SleepSessionRecord.SLEEP_DURATION_TOTAL] ?: Duration.ZERO

            val hours = sleepTime.toHours()
            val minutes = (sleepTime.toMinutes() % 60)
            val seconds = (sleepTime.seconds % 60)


             totalSleep = String.format("%02d 시간 %02d 분 %02d 초 수면", hours, minutes, seconds)

            Log.e("Debug", "sleepTime: $sleepTime")
        }
        catch (e: Exception) { }

        return totalSleep
    }

    suspend fun readSleepSessions(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ) {
        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
        for (sleepRecord in response.records) {
            // Retrieve relevant sleep stages from each sleep record
            val sleepStages = sleepRecord
        }
    }




    LaunchedEffect(Unit) {

        val startTime = "2023-11-08 10:00:00"
        val endTime = "2023-11-08 12:15:00"

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val startDateTime = LocalDateTime.parse(startTime, formatter)
        val endDateTime = LocalDateTime.parse(endTime, formatter)

//        val totalStep = readStepsByTimeRange(healthConnectClient, startDateTime.atZone(koreaTimeZone).toInstant(), endDateTime.atZone(koreaTimeZone).toInstant())
        val totalStep = aggregateStep(healthConnectClient, startDateTime.atZone(koreaTimeZone).toInstant(), endDateTime.atZone(koreaTimeZone).toInstant())
        val caloriesBurnedRecord = aggregateCalories(healthConnectClient, startDateTime, endDateTime)
        val speedAvg = aggregateSpeedAvg(healthConnectClient, startDateTime.atZone(koreaTimeZone).toInstant(), endDateTime.atZone(koreaTimeZone).toInstant())
        val speedMax = aggregateSpeedMax(healthConnectClient, startDateTime.atZone(koreaTimeZone).toInstant(), endDateTime.atZone(koreaTimeZone).toInstant())
        val speedMin = aggregateSpeedMin(healthConnectClient, startDateTime.atZone(koreaTimeZone).toInstant(), endDateTime.atZone(koreaTimeZone).toInstant())
        val distance = aggregateDistance(healthConnectClient, startDateTime, endDateTime)
        val heartRateAvg = aggregateHeartRateAvg(healthConnectClient, startDateTime.atZone(koreaTimeZone).toInstant(), endDateTime.atZone(koreaTimeZone).toInstant())
        val heartRateMax = aggregateHeartRateMax(healthConnectClient, startDateTime.atZone(koreaTimeZone).toInstant(), endDateTime.atZone(koreaTimeZone).toInstant())
        val heartRateMin = aggregateHeartRateMin(healthConnectClient, startDateTime.atZone(koreaTimeZone).toInstant(), endDateTime.atZone(koreaTimeZone).toInstant())
        val sleepTime = aggregateSleepTime(healthConnectClient, startDateTime.atZone(koreaTimeZone).toInstant(), endDateTime.atZone(koreaTimeZone).toInstant())


        flowStep.value = totalStep
        flowCalories.value = caloriesBurnedRecord
        flowSpeedAvg.value = speedAvg
        flowSpeedMax.value = speedMax
        flowSpeedMin.value = speedMin
        flowDistance.value = distance
        flowHeartRateAvg.value = heartRateAvg
        flowHeartRateMax.value = heartRateMax
        flowHeartRateMin.value = heartRateMin
        flowSleepTime.value = sleepTime

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


    Column {

            val dataType = "걸음 수 "
            Text(text = "$dataType: $dataStep"+"걸음")

            val dataType2 = "총 칼로리 소비량 "
            Text(text = "$dataType2: $dataCalories" + " kcal")

            val dataType3 = "평균 속도 "
            Text(text = "$dataType3: ${String.format("%.2f", dataSpeedAvg)} km/h")

            val dataType4 = "최고 속도 "
            Text(text = "$dataType4: ${String.format("%.2f", dataSpeedMax)} km/h")

            val dataType5 = "최저 속도 "
            Text(text = "$dataType5: ${String.format("%.2f", dataSpeedMin)} km/h")

            val dataType6 = "총 거리 "
            Text(text = "$dataType6: ${String.format("%.2f", dataDistance)} km")

            val dataType7 = "평균 심박수 "
            Text(text = "$dataType7: $dataHeartRateAvg" + " bpm")

            val dataType8 = "최고 심박수 "
            Text(text = "$dataType8: $dataHeartRateMax" + " bpm")

            val dataType9 = "최저 심박수 "
            Text(text = "$dataType9: $dataHeartRateMin" + " bpm")

            val dataType10 = "총 수면 시간 "
            Text(text = "$dataType10: $dataSleepTime")

    }

}
