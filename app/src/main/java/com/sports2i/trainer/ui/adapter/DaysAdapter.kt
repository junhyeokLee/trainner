import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.utils.CenterSmoothScroller

class DaysAdapter(private var days: List<Int>,
                  private val recyclerView: RecyclerView) : RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {
    // ViewHolder 클래스
    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayCheckBox: CheckBox = itemView.findViewById(R.id.dayCheckBox)
    }

    // SparseBooleanArray를 사용하여 체크 상태를 추적
    private val selectedPositions = SparseBooleanArray()

    private var onItemClickListener: ((String) -> Unit)? = null

    private var itemWidth: Int = 0

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        // 레이아웃을 인플레이트하고 ViewHolder를 생성
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.day_item, parent, false)

        val holder = DayViewHolder(itemView)

        // 체크박스가 클릭될 때 호출되는 리스너 설정
        holder.dayCheckBox.setOnClickListener {
            // 현재 체크박스가 위치한 아이템의 위치를 얻어와서 클릭 이벤트 처리
            val position = holder.adapterPosition
            onCheckBoxClicked(position)
        }

        return holder
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        // 아이템의 데이터를 뷰홀더에 바인딩
//        val day = days[position % days.size] // 일자를 리스트의 크기로 나눈 나머지로 반복
        val day = days[position] // 일자를 리스트의 크기로 나눈 나머지로 반복
        holder.dayCheckBox.text = day.toString()
        // SparseBooleanArray를 사용하여 체크 상태 설정
//        holder.dayCheckBox.isChecked = selectedPositions.get(position % days.size, false)
        holder.dayCheckBox.isChecked = selectedPositions.get(position, false)

    }

    override fun getItemCount(): Int {
        // 무한 스크롤을 지원하기 위해 Int.MAX_VALUE 대신 아주 큰 수를 사용
//        return Int.MAX_VALUE
        return days.size
    }

    override fun getItemId(position: Int): Long {
        // 아이템의 ID를 위치로 설정
        return position.toLong()
    }

    // 체크박스가 클릭될 때 호출되는 함수
    private fun onCheckBoxClicked(position: Int) {
        // 다른 항목의 체크를 해제하고 현재 항목만 체크 상태로 설정
        selectedPositions.clear()
        selectedPositions.put(position, true)
        notifyDataSetChanged()

//        RecyclerView를 선택된 항목으로 부드럽게 스크롤
//        val centerPosition = days.size * 1000 + position % days.size
        val centerPosition = position
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val smoothScroller = CenterSmoothScroller(recyclerView.context)
        smoothScroller.targetPosition = centerPosition
        layoutManager.startSmoothScroll(smoothScroller)

        // 클릭 이벤트를 외부로 전달
//        val selectedDate = days[position % days.size].toString()
        val selectedDate = days[position].toString()
        onItemClickListener?.invoke(selectedDate)
        Log.e("DaysAdapter", "Checkbox clicked at position: $position")
    }

    // 외부에서 선택된 항목의 위치를 설정하는 함수 추가
    fun setSelectedPosition(position: Int) {
        // 체크된 항목이 있다면 첫 번째 체크된 항목의 위치 반환, 그렇지 않으면 NO_POSITION 반환
        selectedPositions.clear()
        selectedPositions.put(position - 1, true)
//        selectedPositions.put(position % days.size, true)
        notifyDataSetChanged()

        val centerPosition = position - 1
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val smoothScroller = CenterSmoothScroller(recyclerView.context)
        smoothScroller.targetPosition = centerPosition
        layoutManager.startSmoothScroll(smoothScroller)

        val selectedDate = days[position - 1].toString()
        onItemClickListener?.invoke(selectedDate)

    }

    fun getSelectedDay(): Int {
        return selectedPositions.keyAt(0) + 1
    }

    fun updateDays(newDays: List<Int>) {
        days = newDays
        selectedPositions.clear()
        notifyDataSetChanged()
    }

}
