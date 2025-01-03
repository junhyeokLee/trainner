package com.sports2i.trainer.ui.dialog
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*
import androidx.fragment.app.FragmentManager
import androidx.core.util.Pair
import com.sports2i.trainer.R

class DateRangePickerHelper() {
    fun showDateRangePicker(
        fragmentManager: FragmentManager,
        title: String,
        onDateSelected: (startDate: String, endDate: String) -> Unit
    ) {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(title)
            .setTheme(R.style.ThemeOverlay_App_DatePicker)
            .setSelection(Pair(
                MaterialDatePicker.todayInUtcMilliseconds(),
                MaterialDatePicker.todayInUtcMilliseconds()
            ))
            .build()

        dateRangePicker.show(fragmentManager, "date_picker")
        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

            calendar.timeInMillis = selection.first ?: 0
            val startDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(calendar.time)

            calendar.timeInMillis = selection.second ?: 0
            val endDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(calendar.time)

            onDateSelected(startDate, endDate)
        }
    }
}