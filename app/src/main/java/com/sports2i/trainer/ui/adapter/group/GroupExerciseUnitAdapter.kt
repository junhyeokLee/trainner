package com.sports2i.trainer.ui.adapter.group

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.ExerciseUnit
import com.sports2i.trainer.ui.view.CustomSpinner
import com.sports2i.trainer.ui.view.ItemBinder
import kotlin.collections.*


class GroupExerciseUnitAdapter(
    private val context: Context,
    private var size: Int,
    private val dataList: MutableList<ExerciseUnit>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ADD = 1

    private var selectedEditTextValues: MutableMap<Int, String> = mutableMapOf()
    private var selectedValues: MutableMap<Int, ExerciseUnit> = mutableMapOf()
    private lateinit var groupExerciseUnitCustomSpinner: CustomSpinner // CustomSpinner 정의

    // 스피너의 이전 선택 상태를 저장하는 맵
    private val spinnerSelections: MutableMap<Int, Int> = mutableMapOf()

    // 뷰 홀더 생성 및 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ADD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_group_exercise_edit, parent, false)
                AddViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is com.sports2i.trainer.ui.adapter.group.GroupExerciseUnitAdapter.AddViewHolder -> holder.bind(position)
        }
    }
    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_ADD
    }
    // 아이템 개수 반환
    override fun getItemCount(): Int {
        return size
    }

    // 뷰 홀더 클래스 정의
    inner class AddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val spinnerExerciseGoal: CustomSpinner = itemView.findViewById(R.id.spinner_exercise_goal)
        private val etDirectGoal: AppCompatEditText = itemView.findViewById(R.id.et_exercise_direct)
        private val etExerciseGoal: AppCompatEditText = itemView.findViewById(R.id.et_exercise_value)
        private val tvExerciseGoal: TextView = itemView.findViewById(R.id.tv_exercise_goal)
        private val ibDelete : ImageButton = itemView.findViewById(R.id.ib_delete)

        init {
            // ViewHolder가 생성될 때 스피너의 선택 상태를 저장
            spinnerExerciseGoal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    spinnerSelections[adapterPosition] = pos
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // 선택이 해제되었을 때의 처리 (optional)
                }
            }
        }

        fun bind(position: Int) {

            groupExerciseUnitCustomSpinner = spinnerExerciseGoal
            val itemBinder = object : ItemBinder<ExerciseUnit> {
                override fun bindItem(view: View, item: ExerciseUnit, isDropDown: Boolean) {
                    val textView = view.findViewById<TextView>(R.id.tv_spinner)
                    textView.text = item.exerciseUnitName
                    textView.setTextColor(itemView.resources.getColor(android.R.color.black))
                }
            }

            groupExerciseUnitCustomSpinner.setAdapterData(dataList,itemBinder) // CustomSpinner에 Adapter 연결
            groupExerciseUnitCustomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    val selectedExerciseUnit = dataList[pos]
                    val exerciseUnitId = selectedExerciseUnit.exerciseUnitId
                    val exerciseUnitName = selectedExerciseUnit.exerciseUnitName
                    val exerciseUnit = selectedExerciseUnit.exerciseUnit

                    if(exerciseUnitId.equals("U99")){
                        etDirectGoal.visibility = View.VISIBLE
                    }else{
                        etDirectGoal.visibility = View.GONE
                    }
                    selectedValues[position] = ExerciseUnit(exerciseUnitId, exerciseUnitName, exerciseUnit)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // 선택이 해제되었을 때의 처리 (optional)
                }
            }

            ibDelete.setOnClickListener {
                deleteItems(position)
            }


            etExerciseGoal.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
//                    selectedEditTextValues[position] = s.toString()
                    val text = s.toString().takeIf { it.isNotEmpty() } ?: "0"
                    selectedEditTextValues[position] = text
                }
            })

            etDirectGoal.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
//                    selectedEditTextValues[position] = s.toString()
                    val text = s.toString().takeIf { it.isNotEmpty() } ?: "0"
                    selectedValues[position]?.exerciseUnitName = text
                }
            })
            if (position == 0) {
                tvExerciseGoal.visibility = View.VISIBLE
                ibDelete.visibility = View.GONE
            } else {
                tvExerciseGoal.visibility = View.GONE
                ibDelete.visibility = View.VISIBLE
            }
            // 이전에 저장된 스피너 선택 상태를 복원
            val previousSelection = spinnerSelections[position]
            if (previousSelection != null) {
                groupExerciseUnitCustomSpinner.setSelection(previousSelection)
            }
        }
    }


    fun getSelectedValues(): List<ExerciseUnit> {
        val selectedExerciseUnits = mutableListOf<ExerciseUnit>()
        for (position in 0 until size) {
            val exerciseUnit = selectedValues[position]
            exerciseUnit?.let {
                selectedExerciseUnits.add(it)
            }
        }
        return selectedExerciseUnits
    }
    fun getEditTextValues(): List<String> {
        val editTextValues = mutableListOf<String>()
        for (position in 0 until size) {
            val editTextValue = selectedEditTextValues[position] ?: "0"
            editTextValues.add(editTextValue)
        }
        return editTextValues
    }
    fun resetToInitialState() {
        // 크기를 초기화
        this.size = 1
        notifyDataSetChanged()
    }

    fun setUnitSize(newSize: Int) {
        // 크기를 설정
        this.size = newSize
        notifyDataSetChanged()
    }

    fun deleteItems(position: Int) {
    // 삭제할 아이템을 제거.
    selectedValues.remove(position)
    // 삭제된 아이템 이후의 인덱스를 조정.
    for (i in position + 1 until size) {
        val item = selectedValues.remove(i)
        if (item != null) {
            selectedValues[i - 1] = item // 이전 인덱스에 아이템 삽입
        }
    }
    // 아이템이 삭제되었으므로 크기를 줄이기
    size--
    // RecyclerView에 삭제된 것을 알림
    notifyItemRemoved(position)
}

    fun addNewItems(newSize: Int) {
        // 크기를 1 늘려서 설정
        this.size = newSize
        // 아이템 추가를 어댑터에 알림
        notifyItemInserted(this.size)
    }

}
