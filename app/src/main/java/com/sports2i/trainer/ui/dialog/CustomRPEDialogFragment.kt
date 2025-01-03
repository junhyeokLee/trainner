package com.sports2i.trainer.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.sports2i.trainer.R
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomRPEDialogFragment : DialogFragment() {

    private var negativeButtonClickListener: DialogInterface.OnClickListener? = null
    private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
    private val trainingViewModel: TrainingViewModel by viewModels()
     var mProgressValueDialogListener: progressValueDialogListener? = null

    companion object {
        const val TAG = "CustomRPEDialogFragment"

        fun newInstance(
            negativeButtonClickListener: DialogInterface.OnClickListener,
            positiveButtonClickListener: DialogInterface.OnClickListener): CustomRPEDialogFragment {
            val fragment = CustomRPEDialogFragment()
            fragment.negativeButtonClickListener = negativeButtonClickListener
            fragment.positiveButtonClickListener = positiveButtonClickListener
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
        val view = inflater.inflate(R.layout.custom_rpe_dailog, null)

        val linearLayout: LinearLayout = view.findViewById(R.id.linearProgress)
        val negativeButton: Button = view.findViewById(R.id.negativeButton)
        val positiveButton: Button = view.findViewById(R.id.positiveButton)
        val progressBar: SeekBar = view.findViewById(R.id.progressSeekBar)
//        val progressValueTextView = view.findViewById<TextView>(R.id.progressValueTextView)


        val textViewList = mutableListOf<TextView>()

        for (i in 1..10) {
            val textView = TextView(view.context)
            textView.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            textView.text = i.toString()
            textView.setTextColor(ContextCompat.getColor(view.context, R.color.gray2))
            textView.gravity = Gravity.CENTER
            textView.textSize = 12f
            textViewList.add(textView)
            linearLayout.addView(textView)
        }

        progressBar.progress = 5 // 초기 값 설정

        // Update text color based on initial progress
        for (i in 0 until textViewList.size) {
            textViewList[i].setTextColor(
                if (i + 1 <= progressBar.progress) {
                    ContextCompat.getColor(requireContext(), R.color.primary)
                } else {
                    ContextCompat.getColor(requireContext(), R.color.gray2)
                }
            )
        }


        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                // Update text color based on progress
                for (i in 0 until textViewList.size) {
                    textViewList[i].setTextColor(
                        if (i + 1 <= progress) {
                            // Set the desired color for values less than or equal to progress
                            ContextCompat.getColor(view.context, R.color.primary)
                        } else {
                            // Set the color for values greater than progress
                            ContextCompat.getColor(view.context, R.color.gray2)
                        }
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        positiveButton.setOnClickListener {

            positiveButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            Log.e(TAG, "progressBar.progress: ${progressBar.progress}")
            mProgressValueDialogListener?.setProgress(progressBar.progress)
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



    interface progressValueDialogListener {
        fun setProgress(progress: Int)
    }

}
