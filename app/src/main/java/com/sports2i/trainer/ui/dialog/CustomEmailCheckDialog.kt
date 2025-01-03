package com.sports2i.trainer.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
class CustomEmailCheckDialog : DialogFragment() {

    private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
    private var title:String = ""

    companion object {
        const val TAG = "CustomPasswordChangeDialog"

        fun newInstance(
            positiveButtonClickListener: DialogInterface.OnClickListener,title:String): CustomEmailCheckDialog {
            val fragment = CustomEmailCheckDialog()
            fragment.positiveButtonClickListener = positiveButtonClickListener
            fragment.title = title
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        Preferences.init(requireContext(), Preferences.DB_USER_INFO)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.custom_email_check_dailog, null)

        val title: TextView = view.findViewById(R.id.tv_title_name)
        val positiveButton: Button = view.findViewById(R.id.positiveButton)

        title.text = this.title

        positiveButton.setOnClickListener {
            positiveButtonClickListener?.onClick(dialog,DialogInterface.BUTTON_POSITIVE)
            dismiss()
        }

        builder.setView(view)
        val dialog = builder.create()
        // 영역 바깥을 터치해도 다이얼로그가 닫히지 않도록 설정
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

}
