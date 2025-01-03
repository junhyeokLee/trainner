package com.sports2i.trainer.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.Ingredient
import com.sports2i.trainer.utils.Global
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomIngredientDialogFragment : DialogFragment() {
    private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
    private var ingredient: Ingredient? = null

    companion object {
        const val TAG = "CustomIngredientDialogFragment"

        fun newInstance(
            positiveButtonClickListener: DialogInterface.OnClickListener,
            ingredient: Ingredient
        ): CustomIngredientDialogFragment {
            val fragment = CustomIngredientDialogFragment()
            Log.e("CustomIngredientDialogFragment", ingredient.toString())
            fragment.positiveButtonClickListener = positiveButtonClickListener
            fragment.ingredient = ingredient
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
        val view = inflater.inflate(R.layout.custom_ingredient_dailog, null)

        val title: TextView = view.findViewById(R.id.tv_title)
        val company: TextView = view.findViewById(R.id.tv_company_name)
        val permitDate: TextView = view.findViewById(R.id.tv_permit_date)
        val mainIngredient: TextView = view.findViewById(R.id.tv_ingredient)
        val classification: TextView = view.findViewById(R.id.tv_classification)
        val additive: TextView = view.findViewById(R.id.tv_additive)

        val positiveButton: TextView = view.findViewById(R.id.positiveButton)

        title.text = ingredient?.itemName
        company.text = ingredient?.companyName
        permitDate.text = ingredient?.permitDate
        mainIngredient.text = ingredient?.mainIngredient
        classification.text = ingredient?.itemClassification
        additive.text = ingredient?.additive

        positiveButton.setOnClickListener {
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
