package com.sports2i.trainer.ui.dialog


import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.sports2i.trainer.databinding.VideoDialogLoadingBinding

class VideoProgressDialog(context: Context?, private val isValue: Boolean = false) : AppCompatDialog(context!!) {

    private lateinit var binding: VideoDialogLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = VideoDialogLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)
        window!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        if ( isValue ) binding.valueLayout.visibility = View.VISIBLE
        else binding.normalLayout.visibility = View.VISIBLE

    }

    fun setVideoProgress(value: Float) {
        if ( isValue ) {
            binding.valueProgressWheel.text = "${value.toInt()}%"
            binding.valueProgressWheel.progress = value
        }
    }

    fun getProgress(): Float {
        return binding.valueProgressWheel.progress
    }
}
