package com.github.kmachida12345.healthconnectplayground.ui.theme

import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime

sealed class RecordState {
    object Init: RecordState()
    class Fetched(val data: List<StepsRecord>): RecordState()
}
private val resFlow = MutableStateFlow<RecordState>(RecordState.Init)
private var totalCount = 0
private var count = 0

@Composable
fun DailyRecordsScreen() {
    val healthConnectClient = HealthConnectClient.getOrCreate(LocalContext.current)


    // Permissions already granted, proceed with inserting or reading data.
    val now = LocalDateTime.now()

    LaunchedEffect(Unit) {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(now.minusDays(1), now)
            )
        )
        resFlow.value = RecordState.Fetched(response.records)
        for (stepRecord in response.records) {
            // Process each step record
            totalCount += stepRecord.count.toInt()
            Log.d(
                "machida",
                "checkPermissionsAndRun: ${stepRecord.startTime}, ${stepRecord.endTime} ${stepRecord.count}"
            )
        }
    }


    val data by resFlow.collectAsState()
    LazyColumn {
        if (data is RecordState.Fetched) {
            val stepsRecords = (data as RecordState.Fetched).data
            items(stepsRecords.size) { index ->
                val stepRecord = stepsRecords[index]
                Text(
                    text = "Count: ${stepRecord.count}",
                    // 다른 텍스트 스타일, 패딩 또는 다른 설정을 적용할 수 있습니다.
                )
            }
        }
    }


//    val data by resFlow.collectAsState()
//
//    LazyColumn {
//        if (data is RecordState.Fetched) {
//            items((data as RecordState.Fetched).data.size) {
//                Text(totalCount.toString())
//            }
//        }
//    }
}
