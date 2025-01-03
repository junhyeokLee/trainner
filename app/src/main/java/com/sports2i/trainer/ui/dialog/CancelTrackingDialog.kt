package com.sports2i.trainer.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sports2i.trainer.R

class CancelTrackingDialog : DialogFragment() {

    private var yesListener: (() -> Unit)? = null
    private var noListener: (() -> Unit)? = null

    fun setYesListener(listener: () -> Unit) {
        yesListener = listener
    }
    fun setNoListener(listener: () -> Unit) {
        noListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Trainer Tracker")
            .setMessage("운동을 종료 하시겠습니까?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                yesListener?.let {yes ->
                    yes()
                }
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
                noListener?.let { no ->
                    no()
                }
            }
            .create()
    }
}