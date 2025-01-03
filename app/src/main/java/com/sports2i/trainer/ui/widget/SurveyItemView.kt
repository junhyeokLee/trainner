package com.sports2i.trainer.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.sports2i.trainer.R
import com.sports2i.trainer.interfaces.EditListener
import com.sports2i.trainer.interfaces.NoticeListener

@SuppressLint("ViewConstructor", "ResourceType")
class SurveyItemView constructor(context: Context, tag: String, title: String, editListener: EditListener, noticeListener: NoticeListener) : CardView(context) {

    private fun icon(editListener: EditListener): LinearLayoutCompat {
        val layout = LinearLayoutCompat(context)
        val params = RelativeLayout.LayoutParams((60 * resources.displayMetrics.density).toInt(), RelativeLayout.LayoutParams.MATCH_PARENT)
        params.addRule(RelativeLayout.ALIGN_PARENT_END)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        layout.layoutParams = params
        layout.orientation = LinearLayoutCompat.VERTICAL
        layout.gravity = Gravity.END or Gravity.CENTER
        layout.setPadding(0, 0, (16 * resources.displayMetrics.density).toInt(), 0)
        layout.setOnClickListener { editListener.onEdit(this) }
        val imageView = AppCompatImageView(context)
        imageView.layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
        imageView.setImageBitmap(BitmapFactory.decodeResource(resources, R.mipmap.ic_remove))
        layout.addView(imageView)
        return layout
    }

    private fun icon(): AppCompatImageView {
        val imageView = AppCompatImageView(context)
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.rightMargin = (16 * resources.displayMetrics.density).toInt()
        params.addRule(RelativeLayout.ALIGN_PARENT_END)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        imageView.layoutParams = params
        imageView.setImageBitmap(BitmapFactory.decodeResource(resources, R.mipmap.ic_check))
        imageView.visibility = View.GONE
        return imageView
    }

    private fun title(text: String): AppCompatTextView {
        val textView = AppCompatTextView(context)
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.ALIGN_PARENT_START)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        textView.layoutParams = params
        textView.setTextColor(ContextCompat.getColor(context, R.color.black))
        textView.text = text
        return textView
    }

    init {
        val params = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, (48 * resources.displayMetrics.density).toInt())
        params.leftMargin = (16 * resources.displayMetrics.density).toInt()
        params.rightMargin = (16 * resources.displayMetrics.density).toInt()
        params.bottomMargin = (12 * resources.displayMetrics.density).toInt()
        this.tag = tag
        this.layoutParams = params
        this.radius = 8 * resources.displayMetrics.density
        val layout = RelativeLayout(context)
        layout.id = -1
        layout.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        layout.setPadding((16 * resources.displayMetrics.density).toInt(), 0, 0, 0)
        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        layout.setOnClickListener {
            if (layout.getChildAt(2).visibility == View.VISIBLE) return@setOnClickListener
            val textView = layout.getChildAt(0) as AppCompatTextView
            if (layout.id != -1) {
                textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                layout.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }else {
                textView.setTextColor(ContextCompat.getColor(context, R.color.white))
                layout.setBackgroundResource(R.drawable.gradient_survey_item)
            }
            layout.id = when { layout.id != -1 -> -1 else -> 1 }
            noticeListener.onNotice(layout.id)
        }
        layout.addView(title(title))
        layout.addView(icon())
        layout.addView(icon(editListener))
        this.addView(layout)
    }
}