package com.sports2i.trainer.utils

import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup

abstract class OnSingleClickListener :  View.OnClickListener {
    private val TAG = "SINGLE_CLICK_LISTENER"

    private val MIN_CLICK_INTERVAL = 400
//    private val MIN_CLICK_INTERVAL = 1
    private var lastClickTime: Long = 0

    override fun onClick(v: View) {
        try {
            val currentClickTime = SystemClock.uptimeMillis()
            val elapsedTime = currentClickTime - lastClickTime
            lastClickTime = currentClickTime

            if (elapsedTime < MIN_CLICK_INTERVAL) return

//            when ( v ) {
//                is ViewGroup -> keyboardHide(v.context, v)
//            }

            onSingleClick(v)
        } catch (e: Exception) {
            Log.d(TAG, "error : $e")
        }
    }

    abstract fun onSingleClick(v: View)
}