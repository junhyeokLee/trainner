package com.sports2i.trainer.ui.view

import DaysAdapter
import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.utils.DateTimeUtil.getCurrentDate
import com.sports2i.trainer.utils.DaysItemDecoration
import java.text.SimpleDateFormat
import java.util.*

class DateSelectionView2 @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val yearSpinner: CustomSpinner
    private val monthSpinner: CustomSpinner
    private val recyclerView: RecyclerView
    private lateinit var daysAdapter: DaysAdapter

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> get() = _selectedDate

    private val years: List<Int> by lazy {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        (currentYear downTo 2011).toList()
    }

    private val months: List<String> by lazy {
        arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12").toList()
    }

    init {
        orientation = VERTICAL

        val spinnerLayout = LinearLayout(context)
        spinnerLayout.orientation = HORIZONTAL // 스피너를 가로로 정렬하기 위해 수평 방향으로 설정


        yearSpinner = CustomSpinner(context,attrs)
        monthSpinner = CustomSpinner(context,attrs)


        setupYearSpinner()
        setupMonthSpinner()

        yearSpinner.id = View.generateViewId()
        monthSpinner.id = View.generateViewId()

        val spinnerLayoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        val margin = dpToPx(16)
        spinnerLayoutParams.setMargins(margin, 0, margin, 0)

        spinnerLayout.addView(yearSpinner, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        spinnerLayout.addView(monthSpinner, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(spinnerLayout, spinnerLayoutParams)

        recyclerView = RecyclerView(context)
        val recyclerViewLayoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        recyclerViewLayoutParams.setMargins(margin, 0, margin, 0)
        addView(recyclerView, recyclerViewLayoutParams)
        recyclerView.id = View.generateViewId()

        daysAdapter = DaysAdapter(emptyList(), recyclerView)


        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        recyclerView.layoutParams = layoutParams

        // 년도 스피너 변경 이벤트 리스너 추가
        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                _selectedDate.value = yearSpinner.selectedItem.toString()+"-"+getSelectedMonths()+"-"+getSelectedDays()
                setSelectedDateExternal(selectedDate.value ?: getCurrentDate())
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        // 월 스피너 변경 이벤트 리스너 추가
        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                _selectedDate.value = getSelectedYears()+"-"+monthSpinner.selectedItem.toString()+"-"+getSelectedDays()
                setSelectedDateExternal(selectedDate.value ?: getCurrentDate())
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

    }

    private fun setupYearSpinner() {
        val currentYearIndex = years.indexOf(Calendar.getInstance().get(Calendar.YEAR))

        val itemBinder = object : ItemBinder<Int> {
            override fun bindItem(view: View, item: Int, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.toString()
                textView.setTextColor(resources.getColor(android.R.color.black))
            }
        }

        yearSpinner.setAdapterData(years, itemBinder)
        yearSpinner.setSelection(currentYearIndex)
    }

    private fun setupMonthSpinner() {
        val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)

        val itemBinder = object : ItemBinder<String> {
            override fun bindItem(view: View, item: String, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item
                textView.setTextColor(resources.getColor(android.R.color.black))
            }
        }
        monthSpinner.setAdapterData(months, itemBinder)
        monthSpinner.setSelection(currentMonthIndex)
    }

    fun getSelectedDate(): String {
        val selectedYear = yearSpinner.selectedItem.toString()
        val selectedDay = daysAdapter.getSelectedDay()
        // 날짜 형식 지정
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear.toInt())
            set(Calendar.MONTH, monthSpinner.selectedItemPosition)
            set(Calendar.DAY_OF_MONTH, selectedDay)
        }

        return dateFormat.format(calendar.time)
    }

    fun getSelectedYears(): String {
        return yearSpinner.selectedItem.toString()
    }

    fun getSelectedMonths(): String {
        return monthSpinner.selectedItem.toString()
    }

    fun getSelectedDays(): String {
        return daysAdapter.getSelectedDay().toString()
    }

    private fun setSelectedDate(selectedDateTime: String) {
        val selectedDateParts = selectedDateTime.split("-")
        val selectedYear = selectedDateParts[0]
        val selectedMonth = selectedDateParts[1].toInt()
        var selectedDay = selectedDateParts[2].toInt()

        val yearIndex = years.indexOf(selectedYear.toInt())
        yearSpinner.setSelection(yearIndex)

        val monthIndex = months.indexOf(selectedMonth.toString())
        monthSpinner.setSelection(monthIndex)

        val lastDayOfMonth = getLastDayOfMonth(selectedYear.toInt(), monthIndex + 1)
        val days = (1..lastDayOfMonth).toList()

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        recyclerView.removeItemDecorationIfAlreadyAdded()
        val space = calculateItemSpace(context, days.size)
        recyclerView.addItemDecoration(DaysItemDecoration(space))

        daysAdapter.updateDays(days)
        recyclerView.adapter = daysAdapter

        if (selectedDay > lastDayOfMonth) {
            selectedDay = lastDayOfMonth
        }

        daysAdapter.setSelectedPosition(selectedDay)

        daysAdapter.setOnItemClickListener { selectedDate ->
            _selectedDate.value = getSelectedDate()
        }
    }

    fun setSelectedDateExternal(selectedDateTime: String) {
        setSelectedDate(selectedDateTime)
    }

    private fun getLastDayOfMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            displayMetrics
        ).toInt()
    }

    private fun calculateItemSpace(context: Context, itemCount: Int): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val itemWidthDp = 12.7
        val itemWidthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            itemWidthDp.toFloat(),
            displayMetrics
        ).toInt()

        return (displayMetrics.widthPixels - itemWidthPx * itemCount) / (itemCount - 1)
    }

    fun RecyclerView.removeItemDecorationIfAlreadyAdded() {
        val existingDecorations = (0 until itemDecorationCount)
            .map { getItemDecorationAt(it) }
            .filterIsInstance<DaysItemDecoration>()

        existingDecorations.forEach { removeItemDecoration(it) }
    }
}
