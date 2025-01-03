package com.sports2i.trainer.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.sports2i.trainer.data.model.User
import com.sports2i.trainer.ui.dialog.CustomDialog
import com.sports2i.trainer.ui.dialog.CustomDialogClickListener
import com.sports2i.trainer.ui.dialog.CustomRecordDialogFragment
import com.sports2i.trainer.ui.dialog.ProgressDialog
import com.sports2i.trainer.ui.dialog.VideoProgressDialog


@SuppressLint("Registered")
object Global : Application() {
    private val TAG = "TRAINER_GLOBAL"
    private val handler = Handler()

    var DEBUG = true
    var authToken = ""
    var apiBase = ""
//    var apiBase = "http://175.106.99.110:8080" // dev
//    var apiBase = "http://175.45.204.233:8080" // stg

    // ncp
    var endPoint = "https://kr.object.ncloudstorage.com"
     val regionName = "kr-standard"
     val accessKey = "LW4fRcA2NJanlqXuUBc8"
     val secretKey = "iNZs4u15WXHno0oCq95V1k29cqbegtFrjfWDiiL1"
     var bucketName = "trainer/dev"

    var myInfo = User()
    var progress: ProgressDialog? = null
    var videoProgress: VideoProgressDialog? = null

    override fun onCreate() {
        super.onCreate()
        setMode()
    }

    fun setMode() {
        if (DEBUG) {
//            dev server
            apiBase = "http://175.106.99.110:8080"
            bucketName = "trainer/dev"
        } else {
            // stg server
            apiBase = "http://175.45.204.233:8080"
            bucketName = "trainer/stg"
        }
    }

    fun progressON(context: Context?) {
        try {
            Log.d(TAG, "progress on")
//            handler.postDelayed({
                if (progress == null) {
                    if (context != null) {
                        progress = ProgressDialog(context)
                        progress?.show()
                    } else {
                        Log.d(TAG, "context is null")
                    }
                } else if (progress?.isShowing == true) {
                    return
                }
//            }, 200)
        } catch (e: Exception) {
            Log.d(TAG, "progress on error : $e")
        }
    }

    fun videoProgressON(context: Context?, isValue: Boolean = false) {
        try {
            if (videoProgress == null) videoProgress = VideoProgressDialog(context, isValue)
            else if (videoProgress?.isShowing == true) return
            videoProgress?.show()
        } catch (e: Exception) {}
    }

    fun getVideoProgress(): Float {
        return videoProgress?.getProgress()?: 0f
    }

    fun setVideoProgress(value: Float) {
        try {
            if ( videoProgress != null && videoProgress?.isShowing == true ) {
                videoProgress?.setVideoProgress(value)
            }
        } catch (e: Exception) { }
    }

    fun videoProgressOff(){
        try {
            if (videoProgress == null || videoProgress?.isShowing == false) return
            videoProgress?.dismiss()
            videoProgress = null
        } catch (e: Exception) { }
    }

    fun progressOFF() {
        try {
            Log.d(TAG, "progress off")
                if (progress == null || progress?.isShowing == false) return
                progress?.dismiss()
                progress = null
        } catch (e: Exception) {
            Log.d(TAG, "progress off error : $e")
        }
    }

    fun customDialog(
        title: String,
        showNegative: Boolean,
        fragmentManager: FragmentManager,
        listener: CustomDialogClickListener
    ) {
        val positiveClickListener = DialogInterface.OnClickListener { dialog, _ ->
            listener.onPositiveButtonClick()
        }
        val negativeClickListener = DialogInterface.OnClickListener { dialog, _ ->
            listener.onNegativeButtonClick()
        }
        val dialogFragment = CustomDialog.newInstance(positiveClickListener, negativeClickListener, showNegative,title)
        dialogFragment.isCancelable = false
        dialogFragment.show(fragmentManager, CustomRecordDialogFragment.TAG)
    }

    fun showBottomSnackBar(view: View, message: String) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        val snackbarView = snackbar.view
        val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.maxLines = 5 // 텍스트 줄 수 조정
        snackbar.show()
    }
}