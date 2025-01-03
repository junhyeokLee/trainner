package com.sports2i.trainer.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.AdapterView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.Graph
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.PainInfo
import com.sports2i.trainer.data.model.PainInfo2
import com.sports2i.trainer.databinding.ActivityPainBinding
import com.sports2i.trainer.ui.dialog.CustomPainInputDialog
import com.sports2i.trainer.ui.view.CustomSpinner
import com.sports2i.trainer.ui.view.CustomSpinnerAdapter
import com.sports2i.trainer.ui.view.DateSelectionView
import com.sports2i.trainer.ui.view.ItemBinder
import com.sports2i.trainer.ui.widget.GraphPain
import com.sports2i.trainer.utils.ColorUtil.PainColor
import com.sports2i.trainer.utils.DateTimeUtil
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.GroupViewModel
import com.sports2i.trainer.viewmodel.PainInfoViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PainActivity: BaseActivity<ActivityPainBinding>({ActivityPainBinding.inflate(it)}) {

    private var AUTHORITY = "" // 권한

    private val groupViewModel: GroupViewModel by viewModels()
    private val painInfoViewModel: PainInfoViewModel by viewModels()

    private var painInfoList = mutableListOf<PainInfo>()
    private var painInfoList2 = mutableListOf<PainInfo2>()

    private var userList: MutableList<GroupUser> = mutableListOf()
    private var groupInfoList: MutableList<GroupInfo> = mutableListOf()

    private var type = ""
    private var x = 0.0F
    private var y = 0.0F
    private var groupId = ""

    var selectedUserId : String? = null
    var selectedGroup : GroupInfo? = null
    var selectedDateTime: String? = ""
    var selectedPainDateTime: String? = ""

    private lateinit var customGroupSpinner: CustomSpinner
    private lateinit var customUserSpinner: CustomSpinner

    // 통증 그래프 클릭시 날짜 리스트에서 해당 날짜로 이동되야함

    private var dateSelectionView : DateSelectionView? = null

    private var painSuccess = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        init()
        setFunction()
        observe()
        clickEvent()
        binding.root.post { setLiveData() }
        this@PainActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun init() {
        Preferences.init(this, Preferences.DB_USER_INFO)
        AUTHORITY = Preferences.string(Preferences.KEY_AUTHORITY)

        groupViewModel.requestGroupInfo(Preferences.string(Preferences.KEY_ORGANIZATION_ID))

        selectedUserId = intent.getStringExtra("userId")
        selectedDateTime = intent.getStringExtra("dateTime")
        selectedGroup = intent.getParcelableExtra("selectedGroup")
        selectedPainDateTime = selectedDateTime

        customGroupSpinner = binding.spinnerGroup
        customUserSpinner = binding.spinnerUser

        with(binding) {
            spinnerGroup.visibility = if (AUTHORITY == "TRAINEE") View.GONE else View.VISIBLE
            spinnerUser.visibility = if (AUTHORITY == "TRAINEE") View.GONE else View.VISIBLE
        }
    }

    private fun setLiveData() {
        dateSelectionView = DateSelectionView(this)
        binding.dateSelectionView.apply {
            addView(dateSelectionView)
        }
        dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: DateTimeUtil.getCurrentDate())
        dateSelectionView!!.selectedDate.observe(this, ::handleSelectedDate)
    }
    private fun handleSelectedDate(selectedDate: String) {
            selectedDateTime = DateTimeUtil.formatDateSelection(selectedDate)
            selectedPainDateTime = selectedDateTime

            val oneMonthBefore = DateTimeUtil.subtractOneMonth(selectedDateTime!!)
            painInfoViewModel.requestPainInfoList(selectedUserId!!, selectedDateTime!!)
            painInfoViewModel.requestPainInfoSearch(selectedUserId!!, oneMonthBefore,selectedDateTime!!)
    }

    private fun setFunction() {
        refreshing()
    }

    private fun handlerGroupInfoSuccess(groupInfos: MutableList<GroupInfo>) {
        groupInfoList = groupInfos
        // ItemBinder 인터페이스를 구현하여 데이터를 바인딩합니다.
        val itemBinder = object : ItemBinder<GroupInfo> {
            override fun bindItem(view: View, item: GroupInfo, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.groupNameShort
                if (isDropDown) {
                    textView.setTextColor(this@PainActivity.resources.getColor(android.R.color.black))
                } else {
                    textView.setTextColor(this@PainActivity.resources.getColor(android.R.color.white))
                }
            }
        }
        customGroupSpinner.setAdapterData(groupInfoList, itemBinder)
    }

    private fun handlerGroupUserSuccess(groupUsers: MutableList<GroupUser>) {
        userList = groupUsers

        // ItemBinder 인터페이스를 구현하여 데이터를 바인딩합니다.
        val itemBinder = object : ItemBinder<GroupUser> {
            override fun bindItem(view: View, item: GroupUser, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.userName
                if (isDropDown) {
                    textView.setTextColor(this@PainActivity.resources.getColor(android.R.color.black))
                } else {
                    textView.setTextColor(this@PainActivity.resources.getColor(android.R.color.white))
                }
            }
        }
        customUserSpinner.setAdapterData(userList, itemBinder)

    }

    @SuppressLint("ResourceType")
    private fun handleSuccess(hanlderList: MutableList<PainInfo>) {
        painInfoList.clear()
        painInfoList = hanlderList
        getPainInfoList()
    }


    @SuppressLint("ResourceType")
    private fun handleGraphSuccess(hanlderList: MutableList<PainInfo2>) {
        painInfoList2.clear()
        painInfoList2 = hanlderList

        var graphList: List<Graph.PainGraph> = mutableListOf()

        for (i in 0 until painInfoList2.size) {
            val itemName = painInfoList2[i].reportingDate
            graphList = extractTrainingGraphList()
        }

        val graphLayout = binding.graphLayout
        graphLayout.removeAllViews()

        fun updateGraphList(selectedList: List<Graph.PainGraph>) {
            val graphPain = GraphPain(this, 5, "data", selectedList,selectedDateTime!!)
            graphLayout.removeAllViews()
            graphLayout.addView(graphPain)

            graphPain.setOnDataSelectedListener(object : GraphPain.OnDataSelectedListener {
                override fun onDataSelected(painGraph: Graph.PainGraph) {
                    dateSelectionView!!.setSelectedDateExternal(painGraph.trainingDate!!)
                }
            })
        }

        if(graphList.isNullOrEmpty()){
            binding.tvGraphEmpty.visibility = View.VISIBLE
        } else {
            binding.tvGraphEmpty.visibility = View.GONE
            updateGraphList(graphList)
        }
    }

    private fun extractTrainingGraphList(): List<Graph.PainGraph> {
        val graphList = mutableListOf<Graph.PainGraph>()

        for(trainingOverallGraph in painInfoList2) {
            val painLevelAvg = trainingOverallGraph.painLevelAvg?.toDouble() ?: 0.0
            val graph = Graph.PainGraph("", trainingOverallGraph.reportingDate, painLevelAvg)
            graphList.add(graph)
        }

        return graphList
    }
    private fun handleError(errorMessage: String?) {
        errorMessage?.let {
            Global.showBottomSnackBar(binding.root, it)
        }
    }

    private fun handlerLoading(isLoading: Boolean) {
        if(_binding != null) {
            if (isLoading) {
                binding.llProgressBar.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.llProgressBar.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun handlerError(errorMessage: String?) {
        errorMessage?.let {}
    }

    private fun observeGroupInfo() = observeNetworkState(
        groupViewModel.groupInfoState, {
        handlerGroupInfoSuccess(it.data.data)
        if (selectedGroup != null) {
            val groupAdapter = customGroupSpinner.adapter as CustomSpinnerAdapter<GroupInfo>
            val groupIndex = groupAdapter.getPosition(selectedGroup)
            binding.spinnerGroup.setSelection(groupIndex)
        }
    },{Log.e("error",it.message)})

    private fun observeGroupUser() = observeNetworkState(
        groupViewModel.groupUserState, {
        handlerGroupUserSuccess(it.data.data)
        if(selectedUserId != null){
            val groupUserAdapter = customUserSpinner.adapter as CustomSpinnerAdapter<GroupUser>
            userList.forEach {
                if(it.userId == selectedUserId){
                    val userIndex = groupUserAdapter.getPosition(it)
                    binding.spinnerUser.setSelection(userIndex)
                }
            }
        }
    },{Log.e("error",it.message)})

    private fun observePainInfoList() = observeNetworkState(
        painInfoViewModel.painInfoListState, {
        handleSuccess(it.data.data)
    },{Log.e("error",it.message)})
    private fun observePainInfoInsert() = observeNetworkState(
        painInfoViewModel.painInfoInsertState, {
        handleSuccess(it.data.data)
    }, {Log.e("error",it.message)})
    private fun observePainInfoDelete() = observeNetworkState(
        painInfoViewModel.painInfoDeleteState, {
        handleSuccess(it.data.data)
    }, {Log.e("error",it.message)})
    private fun observePainInfoUpdate() = observeNetworkState(
        painInfoViewModel.painInfoUpdateState, {
        handleSuccess(it.data.data)
    },{Log.e("error",it.message)})
    private fun observePainInfoSearch() = observeNetworkState(
        painInfoViewModel.painInfoSearcheState, {
        handleGraphSuccess(it.data.data)
    },{Log.e("error",it.message)})


    private inline fun <reified T : Any> observeNetworkState(
        stateLiveData: LiveData<NetworkState<T>>,
        crossinline onSuccess: (NetworkState.Success<T>) -> Unit,
        crossinline onError: (NetworkState.Error) -> Unit
    ) {
        stateLiveData.observe(this) { state ->
            when (state) {
                is NetworkState.Success -> {
                    onSuccess(state)
                    Handler(mainLooper).postDelayed({ handlerLoading(false) }, 200)
                }
                is NetworkState.Error -> {
                    handlerError(state.message)
                    onError(state)
                }
                is NetworkState.Loading -> {
                    handlerLoading(state.isLoading)
                }
            }
        }
    }

    private fun observe() {

         val networkStateObservers = listOf(
            ::observeGroupInfo,
            ::observeGroupUser,
            ::observePainInfoList,
            ::observePainInfoInsert,
            ::observePainInfoUpdate,
            ::observePainInfoDelete,
            ::observePainInfoSearch
        )

        networkStateObservers.forEach { observe ->
            observe.invoke()
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if(painSuccess){
                val resultIntent = Intent()
                resultIntent.putExtra("painSuccess",true )
                setResult(Activity.RESULT_OK, resultIntent)
            }
            finish()
            back()
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun clickEvent() {

        with(binding) {
            layoutBack.setOnClickListener {
                if(painSuccess){
                    val resultIntent = Intent()
                    resultIntent.putExtra("painSuccess",true )
                    setResult(Activity.RESULT_OK, resultIntent)
                }
                finish()
                back()
            }

            fun onTouchListener(type: String, event: MotionEvent) {
                if (!selectedPainDateTime.equals(selectedDateTime) || checkAdminAuthority()) return

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val dialogFragment = CustomPainInputDialog.newInstance(
                            selectedUserId!!, 0, type, 5, event.x - 20, event.y - 20, selectedDateTime!!, ""
                        )
                        dialogFragment.setOnSaveClickListener(object : CustomPainInputDialog.OnSaveClickListener {
                            override fun onSaveClick(painInfo: PainInfo) {
                                painInfoList.clear()
                                painInfoList.add(painInfo)
                                painSuccess = true
                                painInfoViewModel.requestPainInfoInsert(painInfoList)
                            }
                        })
                        dialogFragment.show(supportFragmentManager, CustomPainInputDialog.TAG)
                    }
                }
            }

            layoutSampleFront.setOnTouchListener { _, event -> onTouchListener("F", event); true }
            layoutSampleBack.setOnTouchListener { _, event -> onTouchListener("B", event); true }
        }


        setGroupCustomSpinnerListener()
        setUserCustomSpinnerListener()
    }


    private fun setGroupCustomSpinnerListener() {
        customGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedGroup = parent?.getItemAtPosition(position) as GroupInfo
                groupId= selectedGroup!!.groupId
                groupViewModel.requestGroupUser(groupId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setUserCustomSpinnerListener() {
        customUserSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGroupUser = parent?.getItemAtPosition(position) as GroupUser
                selectedUserId = selectedGroupUser.userId

                val oneMonthBefore = DateTimeUtil.subtractOneMonth(selectedPainDateTime!!)
                painInfoViewModel.requestPainInfoList(selectedUserId!!, selectedPainDateTime!!)
                painInfoViewModel.requestPainInfoSearch(selectedUserId!!, oneMonthBefore,selectedPainDateTime!!)
            }
        }
    }

    private fun icon(level: Int, x: Float, y: Float): AppCompatImageView {
        this.x = x
        this.y = y
        val imageView = AppCompatImageView(this)
        imageView.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        imageView.setImageBitmap(BitmapFactory.decodeResource(resources, R.mipmap.touch_point))
        imageView.setColorFilter(ContextCompat.getColor(this, PainColor(level)))
        imageView.x = x
        imageView.y = y
        return imageView
    }


    private fun refreshing() {
        binding.refreshLayout?.setOnRefreshListener {
            binding.refreshLayout?.isRefreshing = false
            dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: DateTimeUtil.getCurrentDate())
        }
    }


    private fun getPainInfoList(){

        binding.layoutContainer.removeAllViews()

        if (binding.layoutSampleFront.childCount > 1) {
            for (i in 1 until binding.layoutSampleFront.childCount) {
                binding.layoutSampleFront.removeAllViews()
                val imageView = AppCompatImageView(this)
                imageView.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                imageView.setImageBitmap(BitmapFactory.decodeResource(resources, R.mipmap.front))
                binding.layoutSampleFront.addView(imageView)
            }
        }
        if (binding.layoutSampleBack.childCount > 1) {
            for (i in 1 until binding.layoutSampleBack.childCount) {
                binding.layoutSampleBack.removeAllViews()
                val imageView = AppCompatImageView(this)
                imageView.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                imageView.setImageBitmap(BitmapFactory.decodeResource(resources, R.mipmap.back))
                binding.layoutSampleBack.addView(imageView)
            }
        }

        for (i in 0 until painInfoList.size) {
            if (painInfoList[i].painLocation == "F") binding.layoutSampleFront.addView(icon(painInfoList[i].painLevel, painInfoList[i].locationX, painInfoList[i].locationY))
            else binding.layoutSampleBack.addView(icon(painInfoList[i].painLevel, painInfoList[i].locationX, painInfoList[i].locationY))
            val cardView = CardView(this@PainActivity)
            var layoutParams = LinearLayoutCompat.LayoutParams((300 * resources.displayMetrics.density).toInt(), (80 * resources.displayMetrics.density).toInt())

            layoutParams.marginStart = (12 * resources.displayMetrics.density).toInt()
            cardView.layoutParams = layoutParams
            cardView.radius = 20.0F
            cardView.useCompatPadding = true
            val container = RelativeLayout(this@PainActivity)
            container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            val layout = LinearLayoutCompat(this@PainActivity)
            layout.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
            layout.orientation = LinearLayoutCompat.HORIZONTAL

            val view = View(this@PainActivity)
            layoutParams = LinearLayoutCompat.LayoutParams((10 * resources.displayMetrics.density).toInt(), (10 * resources.displayMetrics.density).toInt())
            layoutParams.marginStart = (12 * resources.displayMetrics.density).toInt()
            layoutParams.topMargin = (12 * resources.displayMetrics.density).toInt()
            view.layoutParams = layoutParams
            view.setBackgroundResource(R.drawable.round_dot)
            (view.background as GradientDrawable).setColor(ContextCompat.getColor(this@PainActivity, PainColor(painInfoList[i].painLevel)))
            layout.addView(view)

            val textViewPainLevel = AppCompatTextView(this@PainActivity)
            val textParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
            textParams.marginStart = (4 * resources.displayMetrics.density).toInt() // 텍스트와 도트 사이 여백 조정
            textParams.topMargin = (6 * resources.displayMetrics.density).toInt()
            textViewPainLevel.layoutParams = textParams
            textViewPainLevel.setTextAppearance(R.style.text_roboto_R3)
            textViewPainLevel.setTextColor(ContextCompat.getColor(this@PainActivity, R.color.black))
            textViewPainLevel.text = painInfoList[i].painLevel.toString()+"점"
            layout.addView(textViewPainLevel)

            var textView = AppCompatTextView(this@PainActivity)
            layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
            layoutParams.marginStart = (9 * resources.displayMetrics.density).toInt()
            layoutParams.topMargin = (7 * resources.displayMetrics.density).toInt()
            textView.layoutParams = layoutParams
            textView.text = painInfoList[i].comment
            layout.addView(textView)
            container.addView(layout)
            textView = AppCompatTextView(this@PainActivity)

            var params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            params.marginEnd = (12 * resources.displayMetrics.density).toInt()
            params.bottomMargin = (12 * resources.displayMetrics.density).toInt()
            params.addRule(RelativeLayout.ALIGN_PARENT_END)
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            textView.id = 1
            textView.layoutParams = params
            textView.textSize = 13F
            textView.setTextColor(ContextCompat.getColor(this@PainActivity, R.color.primary))
            textView.text = getString(R.string.modify)
            textView.setOnClickListener {
                val dialogFragment = CustomPainInputDialog.newInstance(selectedUserId!!,painInfoList[i].id,painInfoList[i].painLocation,painInfoList[i].painLevel,painInfoList[i].locationX ,painInfoList[i].locationY,selectedDateTime!!,painInfoList[i].comment)
                dialogFragment.setOnSaveClickListener(object : CustomPainInputDialog.OnSaveClickListener {
                    override fun onSaveClick(painInfo: PainInfo) {
                        painInfoList.clear()
                        painInfoList.add(painInfo)
                        painSuccess = true
                        painInfoViewModel.requestPainInfoUpdate(painInfoList)
                    }
                })
                dialogFragment.show(supportFragmentManager, CustomPainInputDialog.TAG)
            }
            container.addView(textView)

            textView = AppCompatTextView(this@PainActivity)
            params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            params.marginEnd = (12 * resources.displayMetrics.density).toInt()
            params.bottomMargin = (12 * resources.displayMetrics.density).toInt()
            params.addRule(RelativeLayout.LEFT_OF, 1)
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            textView.layoutParams = params
            textView.textSize = 13F
            textView.setTextColor(ContextCompat.getColor(this@PainActivity, R.color.primary))
            textView.text = getString(R.string.delete)
            textView.setOnClickListener {
                val list = mutableListOf<PainInfo>()
                list.add(painInfoList[i])
                painSuccess = true
                painInfoViewModel.requestPainInfoDelete(list)
            }
            container.addView(textView)
            cardView.addView(container)
            binding.layoutContainer.addView(cardView)
        }
    }
}