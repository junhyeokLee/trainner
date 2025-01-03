package com.sports2i.trainer.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.sports2i.trainer.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomNutiritionImageDialogFragment : DialogFragment() {
    private var image: String? = null
    private var score: String? = null

    companion object {
        const val TAG = "CustomNutritionImageDialogFragment"
        fun newInstance(image: String, score: String): CustomNutiritionImageDialogFragment {
            val fragment = CustomNutiritionImageDialogFragment()
            fragment.image = image
            fragment.score = score
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
        val view = inflater.inflate(R.layout.custom_nutrition_image_dailog, null)

        val nutritionImage: AppCompatImageView = view.findViewById(R.id.iv_nutrition_image)
        val nutritionScore: TextView = view.findViewById(R.id.tv_nutrition_score)
        val cancelButton: TextView = view.findViewById(R.id.positiveButton)

        Glide.with(nutritionImage)
            .load(image)
            .transform(CenterCrop(), RoundedCorners(24)) // 라운드 처리
            .error(R.drawable.ic_empty_food) // 에러 시 디폴트 이미지 표시
            .into(nutritionImage)

        if(score == null || score == "0") nutritionScore.text = "-점"
        else nutritionScore.text = score+"점"

        cancelButton.setOnClickListener {
            dismiss()
        }

        builder.setView(view)

        val dialog = builder.create()

        // 영역 바깥을 터치해도 다이얼로그가 닫히지 않도록 설정
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

}
