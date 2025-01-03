package com.sports2i.trainer.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.sports2i.trainer.R
import com.sports2i.trainer.utils.Preferences

abstract class BaseActivity<B: ViewBinding>(val bindingFactory: (LayoutInflater) -> B): AppCompatActivity() {
    protected var _binding: B? = null
    protected val binding: B
        get() {
            return _binding ?: throw IllegalStateException("Binding is not initialized")
        }
    protected fun next() {
        overridePendingTransition(R.anim.page_right_in, R.anim.page_left_out)
    }
    protected fun back() {
        overridePendingTransition(R.anim.page_left_in, R.anim.page_right_out)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingFactory(layoutInflater)
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun checkAdminAuthority(): Boolean {
        val authority = Preferences.string(Preferences.KEY_AUTHORITY)
        return authority.equals("ADMIN")
    }
    fun checkAdminOrTrainerAuthority(): Boolean {
        val authority = Preferences.string(Preferences.KEY_AUTHORITY)
        return authority.equals("ADMIN") || authority.equals("TRAINER")
    }
    fun checkTraineeAuthority(): Boolean {
        val authority = Preferences.string(Preferences.KEY_AUTHORITY)
        return authority.equals("TRAINEE")
    }

    open fun setLoadingState(isLoading: Boolean) {
        // 하위 클래스에서 오버라이드하여 로딩 상태를 처리
    }

//    protected fun nextWithCustomDuration(durationMillis: Long) {
//        overridePendingTransitionWithCustomDuration(R.anim.page_right_in, R.anim.page_left_out, durationMillis)
//    }
//
//    protected fun backWithCustomDuration(durationMillis: Long) {
//        overridePendingTransitionWithCustomDuration(R.anim.page_left_in, R.anim.page_right_out, durationMillis)
//    }
//    private fun overridePendingTransitionWithCustomDuration(enterAnim: Int, exitAnim: Int, durationMillis: Long) {
//        val enterAnimation = AnimationUtils.loadAnimation(this, enterAnim)
//        val exitAnimation = AnimationUtils.loadAnimation(this, exitAnim)
//        enterAnimation.duration = durationMillis
//        exitAnimation.duration = durationMillis
//        findViewById<View>(android.R.id.content).startAnimation(exitAnimation)
//    }
}
