package com.sports2i.trainer.ui.dialog

import android.animation.Animator
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.LottieAnimationView
import com.sports2i.trainer.R
import com.sports2i.trainer.utils.TrainingTimerService
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
@AndroidEntryPoint
class CustomRecordStartDialogFragment : DialogFragment() {
    private var pauseButtonClickListener: DialogInterface.OnClickListener? = null
    private var finishButtonClickListener: DialogInterface.OnClickListener? = null
    private lateinit var recordingTime: TextView // recordingTime TextView 선언
    private var isPaused = false // 일시 중지 여부를 나타내는 변수
    private var seconds :Long = 0

    // 아직 사용할 필요없음
    private var dialogListener: CustomDialogListener? = null

    private val timerBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == TrainingTimerService.TIMER_BROADCAST_ACTION) {
                 seconds = intent.getLongExtra(TrainingTimerService.TIMER_BROADCAST_EXTRA_SECONDS, 0)
                updateTimerUI(seconds)
            }
        }
    }
    companion object {
        const val TAG = "CustomRecordStartDialogFragment"
        fun newInstance(
            pauseButtonClickListener: DialogInterface.OnClickListener,
            finishButtonClickListener: DialogInterface.OnClickListener
        ): CustomRecordStartDialogFragment {
            val fragment = CustomRecordStartDialogFragment()
            fragment.pauseButtonClickListener = pauseButtonClickListener
            fragment.finishButtonClickListener = finishButtonClickListener
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        // LocalBroadcastManager를 사용하여 브로드캐스트 리시버 등록
        startTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }

    override fun onStop() {
        super.onStop()
        // LocalBroadcastManager를 사용하여 브로드캐스트 리시버 해제
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(timerBroadcastReceiver)
//        stopTimer()
    }

    private fun startTimer() {
        if (!isPaused) {
//            countDownTimer.start()
            val intentFilter = IntentFilter(TrainingTimerService.TIMER_BROADCAST_ACTION)
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(timerBroadcastReceiver, intentFilter)
            // TrainingTimerService 시작
            val serviceIntent = Intent(requireContext(), TrainingTimerService::class.java)
            requireContext().startService(serviceIntent)
        }
    }

    private fun stopTimer() {
        val serviceIntent = Intent(requireContext(), TrainingTimerService::class.java)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(timerBroadcastReceiver)
        // 서비스 종료
        requireContext().stopService(serviceIntent)
    }

    private fun pauseTimer(){
        val serviceIntent = Intent(requireContext(), TrainingTimerService::class.java)
        val intentFilter = IntentFilter(TrainingTimerService.TIMER_BROADCAST_ACTION)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(timerBroadcastReceiver, intentFilter)
        requireContext().startService(serviceIntent.apply { action = "pauseTimer" })
    }
    private fun resumeTimer(){
        val serviceIntent = Intent(requireContext(), TrainingTimerService::class.java)
        val intentFilter = IntentFilter(TrainingTimerService.TIMER_BROADCAST_ACTION)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(timerBroadcastReceiver, intentFilter)
        requireContext().startService(serviceIntent.apply { action = "resumeTimer" })

    }

    private fun updateTimerUI(seconds: Long) {
        val hours = TimeUnit.SECONDS.toHours(seconds)
        val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
        val seconds = seconds % 60
        recordingTime?.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.custom_record_start_dailog, null)

        val pauseButton: Button = view.findViewById(R.id.btn_pause)
        val finishButton: Button = view.findViewById(R.id.btn_finish)
        val lottieTrainingView : LottieAnimationView = view.findViewById(R.id.lottie_training_view)
        recordingTime = view.findViewById(R.id.tv_recording_time) // TextView 초기화

        lottieTrainingView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        pauseButton.setOnClickListener {
//            pauseButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            // 일시 중지 및 재시작 토글
            isPaused = !isPaused
            if (isPaused) {
                pauseButton.text = getString(R.string.resume)
                pauseTimer()
            } else {
                pauseButton.text = getString(R.string.pause)
                resumeTimer()
            }
        }

        finishButton.setOnClickListener {
            finishButtonClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            stopTimer()
            dismiss()
        }

        builder.setView(view)
        val dialog = builder.create()
        // 영역 바깥을 터치해도 다이얼로그가 닫히지 않도록 설정
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    // 아직 사용할 필요없음
    fun setDialogListener(listener: CustomDialogListener) {
        this.dialogListener = listener
    }

    // 아직 사용할 필요없음
    interface CustomDialogListener {
        fun onFinishButtonClicked(Time: Long)
    }
}
