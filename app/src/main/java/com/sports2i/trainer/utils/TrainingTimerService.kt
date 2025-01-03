package com.sports2i.trainer.utils

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.sports2i.trainer.R
import com.sports2i.trainer.ui.activity.MainActivity
import java.util.concurrent.TimeUnit
class TrainingTimerService : Service() {

    private lateinit var countDownTimer: CountDownTimer
    private var recordingSeconds = 0L
    private var isPaused = false

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "TimerServiceChannel"
        const val NOTIFICATION_ID = 123456
        const val TIMER_BROADCAST_ACTION = "com.sports2i.trainer.TIMER_BROADCAST_ACTION"
        const val TIMER_BROADCAST_EXTRA_SECONDS = "com.sports2i.trainer.TIMER_BROADCAST_EXTRA_SECONDS"
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
        startTimer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            "pauseTimer" -> pauseTimer()
            "resumeTimer" -> resumeTimer()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // 타이머 일시 정지
    private fun pauseTimer() {
        countDownTimer.cancel()
        isPaused = true
    }

    private fun resumeTimer() {
        if (isPaused) {
            startTimer()
            isPaused = false
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }
    fun stopTimerService() {
        stopForeground(true)
        stopSelf()
    }
    private fun startTimer() {
        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                recordingSeconds++
                updateTimerBroadcast(recordingSeconds)
                updateNotification() // 매 초마다 알림 업데이트
                Log.e("seconds = ",recordingSeconds.toString())
            }
            override fun onFinish() {}
        }
        countDownTimer.start()
    }

    private fun updateTimerBroadcast(seconds: Long) {
        val intent = Intent().apply {
            action = TIMER_BROADCAST_ACTION
            putExtra(TIMER_BROADCAST_EXTRA_SECONDS, seconds)
        }
        // LocalBroadcastManager를 사용하여 로컬 브로드캐스트 전송
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun updateNotification() {
        val currentActivityIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(this, 0, currentActivityIntent, PendingIntent.FLAG_IMMUTABLE)

        val notificationManager = getSystemService(NotificationManager::class.java)

        val hours = TimeUnit.SECONDS.toHours(recordingSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(recordingSeconds) % 60
        val seconds = recordingSeconds % 60
        val timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("운동 기록중..")
            .setContentText(timeText)
            .setSmallIcon(R.mipmap.logo)
            .setContentIntent(pendingIntent)
            .build()

           notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(): Notification {
        createNotificationChannel()
        val currentActivityIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(this, 0, currentActivityIntent,
            PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("트레이너")
            .setContentText("00:00:00") // 초기값 설정
            .setSmallIcon(R.mipmap.logo)
            .setContentIntent(pendingIntent)
            .build()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "트레이닝 운동 타이머",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
