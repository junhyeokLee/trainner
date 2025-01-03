package com.sports2i.trainer.utils

import android.app.DatePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.provider.Settings.System.DATE_FORMAT
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.GroupInfo
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateTimeUtil {
    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

    private const val INPUT_DEFAULT_DATE_FORMAT = "yyyy.MM.dd HH:mm:ss" // 입력 문자열 날짜 형식
    private const val OUTPUT_DATE_MONTH_DAY_FORMAT = "MM.dd" // 출력 문자열 날짜 형식

    fun getCurrentFormattedDate(): String {
        return dateFormat.format(calendar.time)
    }

    fun getMonthDayFormattedDate(date: String): String {
        try {
            val inputDateFormat = SimpleDateFormat(INPUT_DEFAULT_DATE_FORMAT, Locale.getDefault())
            val outputDateFormat = SimpleDateFormat(OUTPUT_DATE_MONTH_DAY_FORMAT, Locale.getDefault())
            val parsedDate = inputDateFormat.parse(date)
            return outputDateFormat.format(parsedDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
    fun navigateToPreviousDay() {
        calendar.add(Calendar.DAY_OF_MONTH, -1)
    }

    fun navigateToNextDay() {
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    fun showDatePickerDialog(context: Context, initialDate: String, onDateSetListener: (String) -> Unit) {
        val dateParts = initialDate.split(".")
        var initialYear = calendar.get(Calendar.YEAR)
        var initialMonth = calendar.get(Calendar.MONTH)
        var initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        if (dateParts.size == 3) {
            initialYear = dateParts[0].toInt()
            initialMonth = dateParts[1].toInt() - 1
            initialDay = dateParts[2].toInt()
        }

        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                // 날짜를 선택한 경우, onDateSetListener를 호출하여 선택한 날짜를 전달
                val formattedMonth = String.format("%02d", month + 1)
                val formattedDay = String.format("%02d", dayOfMonth)
                val newSelectedDate = "$year.$formattedMonth.$formattedDay"
                onDateSetListener.invoke(newSelectedDate)

                // 사용자가 날짜를 선택한 후에도 calendar를 업데이트
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            },
            initialYear, initialMonth, initialDay
        )
        datePickerDialog.window?.setBackgroundDrawableResource(R.drawable.round_date_picker)
        datePickerDialog.show()
    }


    fun formatTime(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = Date(timeInMillis)
        return dateFormat.format(date)
    }

    fun formatDateTime(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = Date(timeInMillis)
        return dateFormat.format(date)
    }

    fun formatDateSelection(selectedDateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = inputFormat.parse(selectedDateTime)
        return outputFormat.format(parsedDate)
    }

    fun formatTimeTrainingSubDetail(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val date = Date(timeInMillis)
        return dateFormat.format(date)
    }

    fun check5MinuteTimeDifference(startTimeFormattedInsert: Long, endTimeFormattedInsert: Long): Boolean {
        val startDate = Date(startTimeFormattedInsert)
        val endDate = Date(endTimeFormattedInsert)
        val timeDifference = Duration.between(startDate.toInstant(), endDate.toInstant()).toMinutes()

        // 시간 차이가 5분 이하인 경우를 처리
        if (timeDifference <= 5) return true
        else return false
    }

    fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        return currentDate
    }

    fun extractDayOfMonth(dateString: String): String? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return try {
            val date = LocalDate.parse(dateString, formatter)
            val dayOfMonth = date.dayOfMonth.toString()
             dayOfMonth+"일"
        } catch (e: Exception) {
            // 날짜 파싱이 실패하면 null 반환 또는 기본값 설정 가능
            null
        }
    }

    fun extractDayAndMonth(dateString: String): String? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return try {
            val date = LocalDate.parse(dateString, formatter)
            val dayOfMonth = date.dayOfMonth.toString()
            val month = date.monthValue.toString()
            month+"월"+dayOfMonth+"일"
        } catch (e: Exception) {
            // 날짜 파싱이 실패하면 null 반환 또는 기본값 설정 가능
            null
        }
    }

    fun subtractOneMonth(it: String): String {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-M-d") // 입력된 날짜 형식 지정
        val date = LocalDate.parse(it, dateFormatter)
        val oneMonthBefore = date.minusMonths(1)
        return oneMonthBefore.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) // 원하는 형식으로 포맷팅
    }
    fun subtractDate(it: String): String {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.parse(it, dateFormatter)
        return date.format(dateFormatter)
    }

}