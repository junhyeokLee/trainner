package com.sports2i.trainer.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.RequestUserResetPassword
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.utils.SignUtil
import com.sports2i.trainer.viewmodel.TrainingViewModel
import com.sports2i.trainer.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomDialog : DialogFragment() {

    private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
    private var negativeButtonClickListener: DialogInterface.OnClickListener? = null
    private var title:String = ""
    private var showNegative: Boolean = false

    companion object {
        const val TAG = "CustomDialog"
        fun newInstance(
            positiveButtonClickListener: DialogInterface.OnClickListener,negativeButtonClickListener:DialogInterface.OnClickListener,showNegative:Boolean,title:String): CustomDialog {
            val fragment = CustomDialog()
            fragment.positiveButtonClickListener = positiveButtonClickListener
            fragment.negativeButtonClickListener = negativeButtonClickListener
            fragment.showNegative = showNegative
            fragment.title = title
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
        val view = inflater.inflate(R.layout.custom_dailog, null)

        val title: TextView = view.findViewById(R.id.tv_title_name)
        val positiveButton: Button = view.findViewById(R.id.positiveButton)
        val negativeButton: Button = view.findViewById(R.id.negativeButton)

        title.text = this.title

        when(showNegative){
            true ->{
                negativeButton.visibility = View.VISIBLE
            }
            else -> {
                negativeButton.visibility = View.GONE
            }
        }

        positiveButton.setOnClickListener {
            positiveButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            dismiss()
        }

        negativeButton.setOnClickListener {
            negativeButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_NEGATIVE)
            dismiss()
        }

        builder.setView(view)
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

}
