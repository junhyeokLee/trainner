package com.sports2i.trainer.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.Ingredient
import com.sports2i.trainer.data.model.PainInfo
import com.sports2i.trainer.utils.ColorUtil
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.Preferences
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomPainInputDialog : DialogFragment() {
    private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
    private var userId: String? = null
    private var painId: Int? = 0
    private var painLocation: String? = null
    private var level: Int = 5
    private var x: Float? = null
    private var y: Float? = null
    private var date: String? = null
    private var comment: String = ""
    private var onSaveClickListener: OnSaveClickListener? = null

    companion object {
        const val TAG = "CustomIngredientDialogFragment"

        fun newInstance(
            userId:String,
            painId:Int,
            painLocation:String,
            level:Int,
            x:Float,
            y:Float,
            date:String,
            comment:String
        ): CustomPainInputDialog {
            val fragment = CustomPainInputDialog()
            fragment.userId = userId
            fragment.painId = painId
            fragment.painLocation = painLocation
            fragment.level = level
            fragment.x = x
            fragment.y = y
            fragment.date = date
            fragment.comment = comment
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
        val view = inflater.inflate(R.layout.custom_pain_input_dailog, null)

        val seekBar: SeekBar = view.findViewById(R.id.seek_bar)
        val layoutRange : LinearLayoutCompat = view.findViewById(R.id.layout_range)
        val txtSave: TextView = view.findViewById(R.id.positiveButton)
        val txtCancel: TextView = view.findViewById(R.id.txt_cancel)
        val layoutPopupFront: RelativeLayout = view.findViewById(R.id.layout_popup_front)
        val layoutPopupBack: RelativeLayout = view.findViewById(R.id.layout_popup_back)
        val editWrite: TextView = view.findViewById(R.id.edit_write)

        editWrite.text = comment
        seekBar.progress = level

        editWrite.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) txtSave.isEnabled = false
                else txtSave.isEnabled  = true
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        txtSave.setOnClickListener {
            positiveButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            dismiss()
        }

        if(painLocation.equals("F")) layoutPopupFront.addView(icon(level, x!!, y!!))
        else layoutPopupBack.addView(icon(level, x!!, y!!))

        seekBar.min = 1
        seekBar.max = 10
        for (i in 1 until 11) {
            val textView = AppCompatTextView(requireContext())
            val params = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
            params.weight = 1.0F
            textView.layoutParams = params
            textView.gravity = Gravity.CENTER
            textView.text = i.toString()
            layoutRange.addView(textView)
        }

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBarValue: SeekBar?, progress: Int, fromUser: Boolean) {
                range(progress,seekBar,layoutPopupFront,layoutPopupBack)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        txtCancel.setOnClickListener { dismiss() }

        txtSave.setOnClickListener {
            when(editWrite.text.toString()){
                "" -> return@setOnClickListener
                else -> {
                    it.isEnabled = true
                    val painInfo = PainInfo(
                        painId!!,
                        userId!!,
                        painLocation!!,
                        editWrite.text.toString(),
                        date!!,
                        x!!,
                        y!!,
                        seekBar.progress
                    )
                    onSaveClickListener?.onSaveClick(painInfo)
                    dismiss()
                }
            }
        }

        builder.setView(view)
        val dialog = builder.create()

        // 영역 바깥을 터치해도 다이얼로그가 닫히지 않도록 설정
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    private fun range(num: Int,seekBar: SeekBar,layoutPopupFront: RelativeLayout,layoutPopupBack: RelativeLayout) {
        seekBar.thumb.setColorFilter(ContextCompat.getColor(requireContext(), ColorUtil.PainColor(num)), PorterDuff.Mode.SRC_IN)
        if (layoutPopupFront.childCount > 1) {
            (layoutPopupFront.getChildAt(1) as AppCompatImageView).setColorFilter(
                ContextCompat.getColor(requireContext(), ColorUtil.PainColor(num)))
        }else if (layoutPopupBack.childCount > 1) {
            (layoutPopupBack.getChildAt(1) as AppCompatImageView).setColorFilter(
                ContextCompat.getColor(requireContext(), ColorUtil.PainColor(num)))
        }
    }

    private fun icon(level: Int, x: Float, y: Float): AppCompatImageView {
        this.x = x
        this.y = y
        val imageView = AppCompatImageView(requireContext())
        imageView.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        imageView.setImageBitmap(BitmapFactory.decodeResource(resources, R.mipmap.touch_point))
        imageView.setColorFilter(ContextCompat.getColor(requireContext(), ColorUtil.PainColor(level)))
        imageView.x = x
        imageView.y = y
        return imageView
    }

    fun setOnSaveClickListener(listener: OnSaveClickListener) {
        onSaveClickListener = listener
    }

    interface OnSaveClickListener {
        fun onSaveClick(painInfo: PainInfo)
    }
}
