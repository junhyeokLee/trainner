package com.sports2i.trainer.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.sports2i.trainer.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomRecordDialogFragment : DialogFragment() {
    private var videoDataRecordButtonClickListener: DialogInterface.OnClickListener? = null
    private var dataRecordButtonClickListener: DialogInterface.OnClickListener? = null

    companion object {
        const val TAG = "CustomPresetDialogFragment"

        fun newInstance(
            videoDataRecordButtonClickListener: DialogInterface.OnClickListener,
            dataRecordButtonClickListener: DialogInterface.OnClickListener
        ): CustomRecordDialogFragment {
            val fragment = CustomRecordDialogFragment()
            fragment.videoDataRecordButtonClickListener = videoDataRecordButtonClickListener
            fragment.dataRecordButtonClickListener = dataRecordButtonClickListener
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
        val view = inflater.inflate(R.layout.custom_record_dailog, null)

        val videoDataRecordButton: Button = view.findViewById(R.id.btn_video_data_record)
        val dataRecordButton: Button = view.findViewById(R.id.btn_data_record)


        videoDataRecordButton.setOnClickListener {
            videoDataRecordButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            dismiss()
        }

        dataRecordButton.setOnClickListener {
            dataRecordButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            dismiss()
        }

        builder.setView(view)

        val dialog = builder.create()

        // 영역 바깥을 터치해도 다이얼로그가 닫히지 않도록 설정
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

}
