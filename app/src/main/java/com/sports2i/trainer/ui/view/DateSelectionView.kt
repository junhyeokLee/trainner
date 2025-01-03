package com.sports2i.trainer.ui.view

import DaysAdapter
import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
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

class DateSelectionView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val yearSpinner: CustomSpinner
    private val monthSpinner: CustomSpinner
    private val recyclerView: RecyclerView
    private lateinit var daysAdapter: DaysAdapter

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> get() = _selectedDate

    private val years: List<Int> by lazy {
        // 현재 년도 가져오기
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        // 년도 목록 (필요에 따라 조정)
        (currentYear downTo 2011).toList()
    }

    private val months: List<String> by lazy {
        // 월 목록
        arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12").toList()
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.date_selection_view, this, true)

        yearSpinner = findViewById(R.id.yearSpinner)
        monthSpinner = findViewById(R.id.monthSpinner)
        recyclerView = findViewById(R.id.recyclerView)

        daysAdapter = DaysAdapter(emptyList(), recyclerView)

        setupYearSpinner()
        setupMonthSpinner()

        // 비동기로 초기화 작업 수행
//        post {
//            setupYearSpinner()
//            setupMonthSpinner()
//        }

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
        // 현재 년도의 인덱스 찾기
        val currentYearIndex = years.indexOf(Calendar.getInstance().get(Calendar.YEAR))

        val itemBinder = object : ItemBinder<Int> {
            override fun bindItem(view: View, item: Int, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.toString()
                textView.setTextColor(this@DateSelectionView.resources.getColor(android.R.color.black))
            }
        }

        yearSpinner.setAdapterData(years, itemBinder)
        yearSpinner.setSelection(currentYearIndex)
    }

    private fun setupMonthSpinner() {
        // 현재 월의 인덱스 찾기
        val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)

        val itemBinder = object : ItemBinder<String> {
            override fun bindItem(view: View, item: String, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item
                textView.setTextColor(this@DateSelectionView.resources.getColor(android.R.color.black))
            }
        }
        monthSpinner.setAdapterData(months, itemBinder)
        monthSpinner.setSelection(currentMonthIndex)
    }

    // 해당 월의 마지막 날짜를 반환하는 함수
    private fun getLastDayOfMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1) // 월은 0부터 시작하므로 1을 빼줍니다.
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
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


    // setSelectedDate 함수에서 날짜 처리 로직을 수정
    fun setSelectedDate(selectedDateTime: String) {
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

        // RecyclerView의 레이아웃 매니저와 어댑터 설정
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        // 아이템 간격 설정
        // 기존의 데코레이터 제거 후 새로운 데코레이터 추가
        recyclerView.removeItemDecorationIfAlreadyAdded()
        val space = calculateItemSpace(context, days.size)
        recyclerView.addItemDecoration(DaysItemDecoration(space))

        daysAdapter.updateDays(days)
        recyclerView.adapter = daysAdapter

        // 선택된 날짜의 위치로 설정
        // 범위를 벗어나면 마지막 날짜를 선택하도록 수정
        if (selectedDay > lastDayOfMonth) {
            selectedDay = lastDayOfMonth
        }

        // 선택된 날짜의 위치로 설정
        daysAdapter.setSelectedPosition(selectedDay)

        // 아이템 클릭 리스너 설정
        daysAdapter.setOnItemClickListener { selectedDate ->
            _selectedDate.value = getSelectedDate()
        }
    }


    fun setSelectedDateExternal(selectedDateTime: String) {
        // 선택된 날짜 설정
        setSelectedDate(selectedDateTime)
        // 선택된 날짜를 LiveData로 업데이트
//        _selectedDate.value = getSelectedDate()
    }

    // 아이템 간격을 계산하는 함수
    private fun calculateItemSpace(context: Context, itemCount: Int): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        // 아이템 간격을 dp 단위로 계산
        val itemWidthDp = 12.7 // 원하는 dp 값을 지정
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

//    private fun calculateItemSpace(itemCount: Int): Int {
//        // RecyclerView의 너비를 가져옴
//        val recyclerViewWidth = recyclerView.width
//
//        // 아이템 간격을 dp 단위로 계산
//        val itemWidthDp = 7.64 // 원하는 dp 값을 지정
//        val itemWidthPx = TypedValue.applyDimension(
//            TypedValue.COMPLEX_UNIT_DIP,
//            itemWidthDp.toFloat(),
//            resources.displayMetrics
//        ).toInt()
//
//        // 아이템 간격 계산
//        return (recyclerViewWidth - itemWidthPx * itemCount) / (itemCount - 1)
//    }

}
