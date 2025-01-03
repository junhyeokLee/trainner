package com.sports2i.trainer.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.sports2i.trainer.R
import com.sports2i.trainer.ui.activity.BottomNavHandler
import com.sports2i.trainer.ui.activity.TopBarHandler
import com.sports2i.trainer.utils.Preferences

abstract class BaseFragment<B: ViewBinding> : Fragment() {

    private var _binding: B? = null
    val binding get() = _binding!!

    private var topBarHandler: TopBarHandler? = null
    private var bottomNavHandler: BottomNavHandler? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getFragmentBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): B

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TopBarHandler) {
            topBarHandler = context
        }
        if(context is BottomNavHandler){
            bottomNavHandler = context
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    open fun navigateBackStack(popUpTo: Int, inclusive: Boolean = true) {
        val navOptionsBuilder = NavOptions.Builder()
            .setPopUpTo(popUpTo, inclusive)
            .setEnterAnim(R.anim.enter_animation) // 원하는 애니메이션 설정
            .setExitAnim(R.anim.exit_animation) // 원하는 애니메이션 설정
            .setPopEnterAnim(R.anim.pop_enter_animation) // 원하는 애니메이션 설정
            .setPopExitAnim(R.anim.pop_exit_animation) // 원하는 애니메이션 설정
            .build()

        // true 일때 백스택 초기화
        if(inclusive) findNavController().navigate(popUpTo, null, navOptionsBuilder)
        else findNavController().navigateUp()
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


    protected fun showTopBar() {
        topBarHandler?.showTopBar()
    }
    protected fun hideTopBar() {
        topBarHandler?.hideTopBar()
    }
    protected fun setTopBarTitleHide(){
        topBarHandler?.hideTopBarTitle()
    }
    protected fun setTopBarTitleShow(){
        topBarHandler?.showTopBarTitle()
    }
    protected fun setTopBarTitle(title: String) {
        topBarHandler?.setTopBarTitle(title)
    }
    protected fun showTopBarBack(){
        topBarHandler?.showTopBarBack()
    }
    protected fun hideTopBarBack(){
        topBarHandler?.hideTopBarBack()
    }

    protected fun showBottomNavigation(){
        bottomNavHandler?.showBottomNav()
    }
    protected fun hideBottomNavigation(){
        bottomNavHandler?.hideBottomNav()
    }

    protected fun topbarBackTrainingEnroll(previewLayout: View,enrollLayout: View){
        topBarHandler?.topbarBackTrainingEnroll(previewLayout,enrollLayout)
    }

    protected fun setTopBarBackClickListener(listener: View.OnClickListener){
        topBarHandler?.setTopBarBackClickListener(listener)
    }

    // Additional abstract functions
    open fun setFunction() {}
    open fun setContent() {}
    open fun refreshing() {}
    open fun networkStatus() {}


}