package com.sports2i.trainer.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.NutritionPictureUser
import com.sports2i.trainer.viewmodel.NutritionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomNutritionEvaluationDialogFragment : DialogFragment() {
    private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
    private var selectedNutritionPictureList: MutableList<NutritionPictureUser> = mutableListOf()
    private val nutritionViewModel: NutritionViewModel by viewModels()
    private var selectedCheckBoxId: Int = -1
    private var selectedNutritionEvaluation: Int = 0

    companion object {
        const val TAG = "CustomNutritionEvaluationDialogFragment"

        fun newInstance(
            positiveButtonClickListener: DialogInterface.OnClickListener,
            selectedNutritionPictureList: MutableList<NutritionPictureUser>
        ): CustomNutritionEvaluationDialogFragment {
            val fragment = CustomNutritionEvaluationDialogFragment()
            fragment.positiveButtonClickListener = positiveButtonClickListener
            fragment.selectedNutritionPictureList = selectedNutritionPictureList
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.custom_nutrition_evaluation_dailog, null)

        val positiveButton: Button = view.findViewById(R.id.positiveButton)

        val veryBadCheckBox: CheckBox = view.findViewById(R.id.very_bad)
        val badCheckBox: CheckBox = view.findViewById(R.id.bad)
        val normalCheckBox: CheckBox = view.findViewById(R.id.normal)
        val goodCheckBox: CheckBox = view.findViewById(R.id.good)
        val veryGoodCheckBox: CheckBox = view.findViewById(R.id.very_good)


        // 최초 설정값
        normalCheckBox.isChecked = true
        selectedCheckBoxId = R.id.normal

        val checkBoxes = listOf(
            veryBadCheckBox, badCheckBox, normalCheckBox, goodCheckBox, veryGoodCheckBox
        )

        // 체크박스 클릭 리스너 설정
        checkBoxes.forEach { checkBox ->
            checkBox.setOnClickListener {
                if (checkBox.id != selectedCheckBoxId) {
                    // 다른 체크박스 선택 시 이전 선택을 취소
                    selectedCheckBoxId = checkBox.id
                }

                when (selectedCheckBoxId) {
                    R.id.very_bad -> {
                        // "매우 부적절" 선택
                        veryBadCheckBox.isChecked = true
                        badCheckBox.isChecked = false
                        normalCheckBox.isChecked = false
                        goodCheckBox.isChecked = false
                        veryGoodCheckBox.isChecked = false

                    }
                    R.id.bad -> {
                        // "부적절" 선택
                        veryBadCheckBox.isChecked = false
                        badCheckBox.isChecked = true
                        normalCheckBox.isChecked = false
                        goodCheckBox.isChecked = false
                        veryGoodCheckBox.isChecked = false
                    }
                    R.id.normal -> {
                        // "보통" 선택
                        veryBadCheckBox.isChecked = false
                        badCheckBox.isChecked = false
                        normalCheckBox.isChecked = true
                        goodCheckBox.isChecked = false
                        veryGoodCheckBox.isChecked = false
                    }
                    R.id.good -> {
                        // "적절" 선택
                        veryBadCheckBox.isChecked = false
                        badCheckBox.isChecked = false
                        normalCheckBox.isChecked = false
                        goodCheckBox.isChecked = true
                        veryGoodCheckBox.isChecked = false
                    }
                    R.id.very_good -> {
                        // "매우 적절" 선택
                        veryBadCheckBox.isChecked = false
                        badCheckBox.isChecked = false
                        normalCheckBox.isChecked = false
                        goodCheckBox.isChecked = false
                        veryGoodCheckBox.isChecked = true
                    }
                    else -> {
                        // 아무 체크박스도 선택되지 않았을 때 처리
                    }
                }

            }
        }


        positiveButton.setOnClickListener {
            // 선택된 체크박스 확인
            when (selectedCheckBoxId) {
                R.id.very_bad -> {
                    // "매우 부적절" 선택
                    selectedNutritionEvaluation = 1

                }
                R.id.bad -> {
                    // "부적절" 선택
                    selectedNutritionEvaluation = 2
                }
                R.id.normal -> {
                    // "보통" 선택
                    selectedNutritionEvaluation = 3
                }
                R.id.good -> {
                    // "적절" 선택
                    selectedNutritionEvaluation = 4
                }
                R.id.very_good -> {
                    // "매우 적절" 선택
                    selectedNutritionEvaluation = 5
                }

                else -> {
                    // 아무 체크박스도 선택되지 않았을 때 처리
                }
            }
            // 선택된 `selectedNutritionPictureList`의 각 항목에 `selectedNutritionEvaluation` 값을 설정

            selectedNutritionPictureList.forEach {
                it.evaluation = selectedNutritionEvaluation
            }

            Log.e("selectedNutritionPictureList", selectedNutritionPictureList.toString())

            nutritionViewModel.updateNutritionEvaluation(selectedNutritionPictureList)

            positiveButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            dismiss()
        }

        builder.setView(view)
        val dialog = builder.create()

        // 영역 바깥을 터치해도 다이얼로그가 닫히지 않도록 설정
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

}
