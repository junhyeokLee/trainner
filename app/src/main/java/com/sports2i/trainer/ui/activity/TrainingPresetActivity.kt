package com.sports2i.trainer.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.ExercisePreset
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.TrainingInfo
import com.sports2i.trainer.databinding.ActivityTrainingPresetBinding
import com.sports2i.trainer.ui.adapter.group.GroupExercisePresetAdapter
import com.sports2i.trainer.ui.dialog.CustomPresetDialogFragment
import com.sports2i.trainer.ui.view.CustomSpinner
import com.sports2i.trainer.ui.view.ItemBinder
import com.sports2i.trainer.utils.DateTimeUtil
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.OnSingleClickListener
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrainingPresetActivity : BaseActivity<ActivityTrainingPresetBinding>({ActivityTrainingPresetBinding.inflate(it)}){

    private lateinit var presetCustomSpinner: CustomSpinner // CustomSpinner 정의

    private var exercisePresetList: MutableList<ExercisePreset> = mutableListOf()
    private var exerciseList: MutableList<TrainingInfo.ExerciseList> = mutableListOf()
    private lateinit var dawnAdapter : com.sports2i.trainer.ui.adapter.group.GroupExercisePresetAdapter
    private lateinit var morningAdapter : com.sports2i.trainer.ui.adapter.group.GroupExercisePresetAdapter
    private lateinit var afternoonAdapter : com.sports2i.trainer.ui.adapter.group.GroupExercisePresetAdapter
    private lateinit var dinnerAdapter : com.sports2i.trainer.ui.adapter.group.GroupExercisePresetAdapter
    private lateinit var nightAdapter : com.sports2i.trainer.ui.adapter.group.GroupExercisePresetAdapter
    private var exercisePresetId = ""
    private var selectedGroup : GroupInfo? = null
    private var selectedDateTime: String? = DateTimeUtil.getCurrentDate()

    private val trainingViewModel: TrainingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        setContent()
        setFunction()
        networkStatus()
        clickBack()

        this@TrainingPresetActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

    }
    private fun setContent() {
        refreshing()

        selectedGroup = intent.getParcelableExtra("selectedGroup")
        selectedDateTime = intent.getStringExtra("selectedDateTime")

        presetCustomSpinner = binding.spinnerPreset

        // RecyclerView 및 어댑터 초기화
        val dawnRecyclerView = binding.rvPriviewDawnExercise
        val morningRecyclerView = binding.rvPriviewMorningExercise
        val afternoonRecyclerView = binding.rvPriviewAfternoonExercise
        val dinnerRecyclerView = binding.rvPriviewDinnerExercise
        val nightRecyclerView = binding.rvPriviewNightExercise

        dawnAdapter = com.sports2i.trainer.ui.adapter.group.GroupExercisePresetAdapter(
            this@TrainingPresetActivity,
            mutableListOf()
        )
        morningAdapter = com.sports2i.trainer.ui.adapter.group.GroupExercisePresetAdapter(
            this@TrainingPresetActivity,
            mutableListOf()
        )
        afternoonAdapter = com.sports2i.trainer.ui.adapter.group.GroupExercisePresetAdapter(
            this@TrainingPresetActivity,
            mutableListOf()
        )
        dinnerAdapter = com.sports2i.trainer.ui.adapter.group.GroupExercisePresetAdapter(
            this@TrainingPresetActivity,
            mutableListOf()
        )
        nightAdapter = com.sports2i.trainer.ui.adapter.group.GroupExercisePresetAdapter(
            this@TrainingPresetActivity,
            mutableListOf()
        )

        dawnRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        morningRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        afternoonRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        dinnerRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        nightRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

        dawnRecyclerView.adapter = dawnAdapter
        morningRecyclerView.adapter = morningAdapter
        afternoonRecyclerView.adapter = afternoonAdapter
        dinnerRecyclerView.adapter = dinnerAdapter
        nightRecyclerView.adapter = nightAdapter
    }


    private fun setFunction() {
        val selectedPresets: MutableList<ExercisePreset> = mutableListOf()

        presetCustomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectPreset = parent?.getItemAtPosition(position) as ExercisePreset
                exercisePresetId = selectPreset.exercisePresetId.toString()

                // 최종적으로 선택된 userList를 기반으로 trainingInfoList를 갱신
                val updatePresetList = mutableListOf<ExercisePreset>()
                for (preset in exercisePresetList) {
                    // 각 trainingInfo 객체를 복제하여 사용자 정보 업데이트
                    val updatedPreset = preset.copy( organizationId = Global.myInfo.organizationId)
                    updatePresetList.add(updatedPreset.copy())
                }

                // 선택된 exercisePresetId에 해당하는 리스트를 가져옴
                val selectedList = exercisePresetList.filter { it.exercisePresetId.toString() == exercisePresetId }

                // 선택된 리스트를 사용하여 UI 업데이트 등을 수행
                updateUIWithSelectedList(selectedList)

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle Nothing Selected
            }
        }

        binding.btnPresetLoad.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                if (exercisePresetId.isNotEmpty()) {
                    val selectedExercisePresets = exercisePresetList.filter { it.exercisePresetId.toString() == exercisePresetId }

//                    val intent = Intent(this@TrainingPresetActivity, TrainingEnrollActivity::class.java)
//                    intent.putExtra("selectedGroup", selectedGroup)
//                    intent.putExtra("selectedDateTime", selectedDateTime)
//                    intent.putParcelableArrayListExtra("selectedExercisePresets", ArrayList(selectedExercisePresets))
//                    intent.putExtra("preset", true)
//                    startActivity(intent)
//                    finish()
//                    back()

                    val intent = Intent()
                    intent.putExtra("preset", true)
                    intent.putExtra("selectedDateTime", selectedDateTime)
                    intent.putExtra("selectedGroup", selectedGroup)
                    intent.putParcelableArrayListExtra("selectedExercisePresets", ArrayList(selectedExercisePresets))
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                    back()

                }
            }
        })


        binding.btnPresetDelete.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View) {

                if (exercisePresetId != null) {
                    trainingViewModel.deleteExercisePreset(exercisePresetId)
                    showTrainingEnrollDialog(resources.getString(R.string.delete_preset_message))
                }
            }
        })

        binding.btnChangeName.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View) {

                selectedPresets.clear()

                val selectPreset = binding.spinnerPreset.selectedItem as ExercisePreset
                selectedPresets.add(selectPreset)

                val positiveButtonClickListener = DialogInterface.OnClickListener { dialog, _ ->
                    // Positive 버튼 클릭 시 처리할 로직 작성
                    Global.showBottomSnackBar(binding.root, "프리셋 이름이 변경되었습니다.")
                    presetCustomSpinner.notifyData()
                    dialog.dismiss()
                }
                val negativeButtonClickListener = DialogInterface.OnClickListener { dialog, _ ->
                    // Negative 버튼 클릭 시 처리할 로직 작성
                    dialog.dismiss()
                }
                val dialogFragment = CustomPresetDialogFragment.newInstance(
                    "프리셋 이름 변경",
                    selectedPresets,
                    "확인",
                    "취소",
                    positiveButtonClickListener,
                    negativeButtonClickListener
                )
                dialogFragment.show(supportFragmentManager, CustomPresetDialogFragment.TAG)

            }
        })

    }

    private fun updateUIWithSelectedList(selectedList: List<ExercisePreset>) {
        // Clear all adapters
        clearAdapters()

        for (exercisePreset in selectedList) {
            val trainingTime = exercisePreset.trainingTime

            // 현재 exercisePreset에서 exerciseId로 그룹화된 exerciseList를 가져옵니다.
            val exerciseIdToExerciseListMap = exercisePreset.exerciseList.groupBy { it.exerciseId }

            when (trainingTime) {

                "T1" -> addDataToAdapter(exercisePreset, dawnAdapter, exerciseIdToExerciseListMap)

                "T2" -> addDataToAdapter(exercisePreset, morningAdapter, exerciseIdToExerciseListMap)

                "T3" -> addDataToAdapter(exercisePreset, afternoonAdapter, exerciseIdToExerciseListMap)

                "T4" -> addDataToAdapter(exercisePreset, dinnerAdapter, exerciseIdToExerciseListMap)

                "T5" -> addDataToAdapter(exercisePreset, nightAdapter, exerciseIdToExerciseListMap)

                else -> {}
            }
        }

        // Update visibility based on item count
        updateVisibility()
    }

    private fun addDataToAdapter(
        exercisePreset: ExercisePreset,
        adapter: com.sports2i.trainer.ui.adapter.group.GroupExercisePresetAdapter,
        exerciseIdToExerciseListMap: Map<String, List<TrainingInfo.ExerciseList>>
    ) {
        for ((_, groupedExerciseList) in exerciseIdToExerciseListMap) {
            val exercisePresetWithGroupedExerciseList = exercisePreset.copy(exerciseList = groupedExerciseList.toMutableList())
            adapter.addData(exercisePresetWithGroupedExerciseList)
        }
    }

    private fun clearAdapters() {
        dawnAdapter.clearData()
        morningAdapter.clearData()
        afternoonAdapter.clearData()
        dinnerAdapter.clearData()
        nightAdapter.clearData()
    }

    private fun updateVisibility() {
        binding.layoutPriviewDawnExercise.visibility = if (dawnAdapter.itemCount > 0) View.VISIBLE else View.GONE
        binding.layoutPriviewMorningExercise.visibility = if (morningAdapter.itemCount > 0) View.VISIBLE else View.GONE
        binding.layoutPriviewAfternoonExercise.visibility = if (afternoonAdapter.itemCount > 0) View.VISIBLE else View.GONE
        binding.layoutPriviewDinnerExercise.visibility = if (dinnerAdapter.itemCount > 0) View.VISIBLE else View.GONE
        binding.layoutPriviewNightExercise.visibility = if (nightAdapter.itemCount > 0) View.VISIBLE else View.GONE
    }



    private fun refreshing() {
        binding.refreshLayout?.setOnRefreshListener {
//            refresh()
            binding.refreshLayout?.isRefreshing = false
        }
    }

    private fun networkStatus() {
        trainingViewModel.getExercisePreset(Global.myInfo.organizationId)
        trainingViewModel.exercisePresetResponseState.observe(this@TrainingPresetActivity) { presetState ->
            when (presetState) {
                is NetworkState.Success -> { handlerPreSetItemSuccess(presetState.data.data) }
                is NetworkState.Error -> handlerError(presetState.message)
                is NetworkState.Loading -> handlerLoading()
            }
        }
    }


    private fun handlerPreSetItemSuccess(exercisePreSet: MutableList<ExercisePreset>) {
        exercisePresetList.clear()

        exercisePreSet.forEach {
            exercisePresetList.add(it)
        }

        // ItemBinder 인터페이스를 구현하여 데이터를 바인딩합니다.
        val itemBinder = object : ItemBinder<ExercisePreset> {
            override fun bindItem(view: View, item: ExercisePreset, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.exercisePresetName
                textView.setTextColor(this@TrainingPresetActivity.resources.getColor(android.R.color.black))
            }
        }
        val uniqueExercisePresets = exercisePresetList.distinctBy { it.exercisePresetId }

        presetCustomSpinner.setAdapterData(uniqueExercisePresets, itemBinder)
    }

    private fun handlerError(errorMessage: String?) {
        errorMessage?.let {
            Log.e("TAG", "Error: $it")
        }
    }

    private fun handlerLoading() {
//        Global.progressON(this@TrainingPresetActivity)
    }

    private fun showTrainingEnrollDialog(message: String){
        val alertDialogBuilder = AlertDialog.Builder(this@TrainingPresetActivity)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()

            val intent = Intent(this@TrainingPresetActivity, TrainingRegistrationActivity::class.java)
            intent.putExtra("selectedGroup", selectedGroup)
            intent.putExtra("selectedDateTime", selectedDateTime)
            intent.putExtra("preset", true)
            startActivity(intent)
            finish()
            back()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }



    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(this@TrainingPresetActivity, TrainingRegistrationActivity::class.java)
            intent.putExtra("selectedGroup", selectedGroup)
            intent.putExtra("selectedDateTime", selectedDateTime)
            intent.putExtra("preset", true)
            startActivity(intent)
            finish()
            back()
        }
    }
    private fun clickBack() {
        binding.layoutBack.setOnClickListener {
            val intent = Intent(this@TrainingPresetActivity, TrainingRegistrationActivity::class.java)
            intent.putExtra("selectedGroup", selectedGroup)
            intent.putExtra("selectedDateTime", selectedDateTime)
            intent.putExtra("preset", true)
            startActivity(intent)
            finish()
            back()
        }
        }
    }
