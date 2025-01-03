package com.sports2i.trainer.ui.dialog


import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.sports2i.trainer.R
import com.sports2i.trainer.databinding.ActivityLoginBinding
import com.sports2i.trainer.databinding.DialogLoadingBinding

class ProgressDialog(context: Context?) : AppCompatDialog(context!!,R.style.ProgressDialogStyle) {

    private lateinit var binding: DialogLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)
        window!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
    }

    override fun show() {
        super.show()
        binding.normalLayout.visibility = View.VISIBLE
    }

    override fun isShowing(): Boolean {
        return super.isShowing()
        binding.normalLayout.visibility = View.VISIBLE
    }

    override fun dismiss() {
        super.dismiss()
        binding.normalLayout.visibility = View.GONE
    }
}
