package com.sports2i.trainer.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.ExercisePreset
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomQualityDialogFragment : DialogFragment() {

    private var qualityClickListener: QualityClickListener? = null
    companion object {
        const val TAG = "CustomPresetDialogFragment"

        fun newInstance(): CustomQualityDialogFragment {
            val fragment = CustomQualityDialogFragment()
            return fragment
        }
    }

    fun setQualityClickListener(listener: QualityClickListener) {
        this.qualityClickListener = listener
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.custom_quality_dailog, null)

        val tvSD: CardView = view.findViewById(R.id.cv_sd)
        val tvHD: CardView = view.findViewById(R.id.cv_hd)
        val tvFHD: CardView = view.findViewById(R.id.cv_fhd)
        val tvFHD2: CardView = view.findViewById(R.id.cv_fhd2)

        tvSD.setOnClickListener {
            qualityClickListener?.onQualityClicked("SD")
            dismiss()
        }

        tvHD.setOnClickListener {
            qualityClickListener?.onQualityClicked("HD")
            dismiss()
        }

        tvFHD.setOnClickListener {
            qualityClickListener?.onQualityClicked("FHD")
            dismiss()
        }

        tvFHD2.setOnClickListener {
            qualityClickListener?.onQualityClicked("FHD2")
            dismiss()
        }

        builder.setView(view)
        val dialog = builder.create()

        // 영역 바깥을 터치해도 다이얼로그가 닫히지 않도록 설정
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    interface QualityClickListener {
        fun onQualityClicked(quality: String)
    }
}
