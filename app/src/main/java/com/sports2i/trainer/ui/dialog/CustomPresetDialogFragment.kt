package com.sports2i.trainer.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.ExercisePreset
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomPresetDialogFragment : DialogFragment() {
    private var message: String? = null
    private var exercisePreset: MutableList<ExercisePreset> = mutableListOf()
    private var positiveButtonText: String? = null
    private var negativeButtonText: String? = null
    private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
    private var negativeButtonClickListener: DialogInterface.OnClickListener? = null
    private val trainingViewModel: TrainingViewModel by viewModels()

    companion object {
        const val TAG = "CustomPresetDialogFragment"

        fun newInstance(
            message: String,
            exercisePreset: MutableList<ExercisePreset>,
            positiveButtonText: String,
            negativeButtonText: String,
            positiveButtonClickListener: DialogInterface.OnClickListener,
            negativeButtonClickListener: DialogInterface.OnClickListener
        ): CustomPresetDialogFragment {
            val fragment = CustomPresetDialogFragment()
            fragment.message = message
            fragment.exercisePreset = exercisePreset
            fragment.positiveButtonText = positiveButtonText
            fragment.negativeButtonText = negativeButtonText
            fragment.positiveButtonClickListener = positiveButtonClickListener
            fragment.negativeButtonClickListener = negativeButtonClickListener
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
        val view = inflater.inflate(R.layout.custom_preset_dailog, null)

        val messageTextView: TextView = view.findViewById(R.id.messageTextView)
        val changePresetName: AppCompatEditText = view.findViewById(R.id.et_change_preset_name)
        val currentPresetName: TextView = view.findViewById(R.id.currentPresetName)
        val positiveButton: Button = view.findViewById(R.id.positiveButton)
        val negativeButton: Button = view.findViewById(R.id.negativeButton)


        messageTextView.text = message
        currentPresetName.text = exercisePreset.first().exercisePresetName
        positiveButton.text = positiveButtonText
        negativeButton.text = negativeButtonText

        positiveButton.setOnClickListener {
            if(changePresetName.text.toString().isEmpty() || changePresetName.text.toString().isNullOrBlank() ) {
                Global.showBottomSnackBar(view, "이름을 입력 해주세요.")
                return@setOnClickListener
            }

            positiveButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            exercisePreset.first().exercisePresetName = changePresetName.text.toString()
            trainingViewModel.requestExercisePreset(exercisePreset)
            dismiss()
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

}
