package com.sports2i.trainer.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.SurveyInsert
import com.sports2i.trainer.data.model.SurveyPreset
import com.sports2i.trainer.ui.adapter.myexercise.SurveyInsertAdapter
import com.sports2i.trainer.ui.fragment.myexercise.MyExerciseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomSurveyInsertDialog : DialogFragment() {
    private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
    private var surveyInsertListener: MyExerciseFragment.SurveyInsertListener? = null
    private var surveyPresetList: MutableList<SurveyPreset>? = null
    private lateinit var surveyInsertAdapter: SurveyInsertAdapter
    private var surveyInsertList: MutableList<SurveyInsert> = mutableListOf()
    private var userId: String = ""
    private var date: String= ""

    companion object {
        const val TAG = "CustomSurveyInsertDialog"

        fun newInstance(
            surveyInsertListener: MyExerciseFragment.SurveyInsertListener,
            positiveButtonClickListener: DialogInterface.OnClickListener,
            surveyPresetList: MutableList<SurveyPreset>,
            userId:String,
            date: String
            ): CustomSurveyInsertDialog {
            val fragment = CustomSurveyInsertDialog()
            fragment.surveyInsertListener = surveyInsertListener
            fragment.positiveButtonClickListener = positiveButtonClickListener
            fragment.surveyPresetList = surveyPresetList
            fragment.userId = userId
            fragment.date = date
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
        val view = inflater.inflate(R.layout.custom_survey_input_dialog, null)

        val rvSurvey: RecyclerView = view.findViewById(R.id.rv_survey)
        val cancel: TextView = view.findViewById(R.id.txt_cancel)
        val positiveButton: TextView = view.findViewById(R.id.positiveButton)

        var surveyInsertList: MutableList<SurveyInsert> = mutableListOf()
        surveyPresetList.let {
            for (i in 0 until it!!.size) {
                surveyInsertList.add(SurveyInsert(userId, date, it[i].surveyItemId, it[i].surveyItemName, 0))
            }
        }

        surveyInsertAdapter = SurveyInsertAdapter(requireContext(),surveyPresetList!!,surveyInsertList!!)

        rvSurvey.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            adapter = surveyInsertAdapter
        }

        positiveButton.setOnClickListener {
            val surveyInsertList = surveyInsertAdapter.getSurveyInsert()
            positiveButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)

            surveyInsertListener?.onSurveyInsertSelected(surveyInsertList)

        }

        cancel.setOnClickListener {
            dismiss()
        }

        builder.setView(view)
        val dialog = builder.create()

        // 영역 바깥을 터치해도 다이얼로그가 닫히지 않도록 설정
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

}
