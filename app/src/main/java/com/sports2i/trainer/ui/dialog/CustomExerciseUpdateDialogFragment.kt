package com.sports2i.trainer.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.TrainingInfoResponse
import com.sports2i.trainer.ui.adapter.myexercise.ExerciseUpdateAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomExerciseUpdateDialogFragment : DialogFragment() {
    private var trainingInfoResponseList: MutableList<TrainingInfoResponse>? = null
    private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
    private var negativeButtonClickListener: DialogInterface.OnClickListener? = null
    private lateinit var exerciseUpdateAdapter: ExerciseUpdateAdapter

    var mListener: CustomExerciseUpdateDialogFragment.OnSaveClickListener? = null

    companion object {
        const val TAG = "CustomPresetDialogFragment"

        fun newInstance(
            trainingInfoResponseList: MutableList<TrainingInfoResponse>,
            positiveButtonClickListener: DialogInterface.OnClickListener,
            negativeButtonClickListener: DialogInterface.OnClickListener
        ): CustomExerciseUpdateDialogFragment {
            val fragment = CustomExerciseUpdateDialogFragment()
            fragment.trainingInfoResponseList = trainingInfoResponseList
            fragment.positiveButtonClickListener = positiveButtonClickListener
            fragment.negativeButtonClickListener = negativeButtonClickListener
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.custom_exercise_update_dailog, null)

        val rvExerciseUnit = view.findViewById<RecyclerView>(R.id.rv_exercise_unit)

        val positiveButton: Button = view.findViewById(R.id.positiveButton)
        val negativeButton: Button = view.findViewById(R.id.negativeButton)

        exerciseUpdateAdapter = ExerciseUpdateAdapter(requireContext(),trainingInfoResponseList!!)

        rvExerciseUnit.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            adapter = exerciseUpdateAdapter
        }

        positiveButton.setOnClickListener {
            // ExerciseUpdateAdapter에서 dataList에 있는 TrainingInfoResponse 객체를 가져온다
            val updatedList = exerciseUpdateAdapter.getDataList()
            positiveButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            mListener?.onSaveClick(updatedList)

            // null 또는 빈 값이 있는지 확인
//            val hasNullOrEmptyValue = updatedList.any { it.measureValue == null || it.measureValue!!.isNaN() }
//
//            if (hasNullOrEmptyValue) {
//                it.isEnabled = false
//                Global.showCustomToast(requireContext(), "빈 값이 있습니다.")
//            } else {
//                it.isEnabled = true
//                positiveButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
//                dismiss()
//            }


        }

        negativeButton.setOnClickListener {
            negativeButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_NEGATIVE)
            dismiss()
        }

        builder.setView(view)
        val dialog = builder.create()
        // 영역 바깥을 터치해도 다이얼로그가 닫히지 않도록 설정
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    interface OnSaveClickListener {
        fun onSaveClick(trainingExerciseList: List<TrainingInfoResponse>)
    }

}
