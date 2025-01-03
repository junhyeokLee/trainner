package com.sports2i.trainer.ui.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.TrainingComment
import com.sports2i.trainer.data.model.TrainingCommentRequest
import com.sports2i.trainer.ui.adapter.groupmember.TrainingCommentAdapter
import com.sports2i.trainer.utils.Global.getSystemService
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommentBottomSheet(context: Context,userId:String,dateTime:String,trainingViewModel: TrainingViewModel) : BottomSheetDialogFragment() {

    private val context = context
    private val trainingViewModel = trainingViewModel
    private var trainingCommentList = mutableListOf<TrainingComment>()
    private val userId = userId
    private val dateTime = dateTime
    private lateinit var trainingCommentAdapter: TrainingCommentAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var editText: EditText
    private lateinit var btnComment: LinearLayout
    private lateinit var ibComment: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return View.inflate(context, R.layout.layout_bottom_sheet, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        bindList()
        networkStatus()
    }

    private fun init() {

        view?.let {
             recyclerView = it.findViewById<RecyclerView>(R.id.recyclerView)
             editText = it.findViewById<EditText>(R.id.et_comment)
             btnComment = it.findViewById<LinearLayout>(R.id.layout_comment)
             ibComment = it.findViewById<ImageButton>(R.id.ib_comment)


            recyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                setHasFixedSize(true)
                trainingCommentAdapter = TrainingCommentAdapter(context, trainingCommentList)
                adapter = trainingCommentAdapter
            }
        }
    }

    private fun networkStatus(){

        Log.e("userId",userId)
        trainingViewModel.getTrainingComment(userId,dateTime)


            trainingViewModel.trainingCommentState.observe(this) {
                when (it) {
                    is NetworkState.Success ->  handlerGetTrainingCommentSuccess(it.data.data)
                    is NetworkState.Error -> handlerError(it.message)
                    is NetworkState.Loading -> handlerLoading()
                }
            }
        trainingViewModel.trainingCommentAddState.observe(this) {
            when (it) {
                is NetworkState.Success -> {
                    handlerAddTrainingCommentSuccess(it.data.data)
                    editText.setText("") // EditText 초기화
                    val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0) // 키보드 숨기기
                }
                is NetworkState.Error -> {
                    handlerError(it.message)
                }
                is NetworkState.Loading -> handlerLoading()
            }
        }
    }

    private fun bindList() {

            recyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                setHasFixedSize(true)
                trainingCommentAdapter = TrainingCommentAdapter(context, trainingCommentList)
                adapter = trainingCommentAdapter
            }

            editText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    // 포커스를 잃으면 키보드 숨기기
                    val inputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
                }
            }
            btnComment.setOnClickListener {
                val comment = editText.text.toString()
                if (comment.isNotEmpty()) {
                    trainingViewModel.requestTrainingComment(TrainingCommentRequest(userId, comment,dateTime))
                }
            }
            ibComment.setOnClickListener {
                val comment = editText.text.toString()
                if (comment.isNotEmpty()) {
                    trainingViewModel.requestTrainingComment(TrainingCommentRequest(userId, comment,dateTime))
                }
            }
    }




    private fun handlerGetTrainingCommentSuccess(trainingComment: MutableList<TrainingComment>) {
        trainingCommentList = trainingComment

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
            trainingCommentAdapter = TrainingCommentAdapter(context, trainingCommentList)
            adapter = trainingCommentAdapter
            adapter?.notifyDataSetChanged()
        }

        trainingCommentAdapter.mListener = object : TrainingCommentAdapter.OnItemClickListener {
            override fun onDeleteComment(position: Int, deletedComment: TrainingComment) {
                trainingViewModel.deleteTrainingComment(deletedComment.id)
                trainingCommentAdapter.deleteComment(position)
            }
        }
    }

    private fun handlerAddTrainingCommentSuccess(trainingComment: MutableList<TrainingComment>) {
        trainingCommentList = trainingComment
        trainingCommentAdapter.insertComment(trainingCommentList)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
            trainingCommentAdapter = TrainingCommentAdapter(context, trainingCommentList)
            adapter = trainingCommentAdapter
            adapter?.notifyDataSetChanged()

        }
        trainingCommentAdapter.mListener = object : TrainingCommentAdapter.OnItemClickListener {
            override fun onDeleteComment(position: Int, deletedComment: TrainingComment) {
                trainingViewModel.deleteTrainingComment(deletedComment.id)
                trainingCommentAdapter.deleteComment(position)
            }
        }
    }


    private fun handlerError(errorMessage: String?) {
        errorMessage?.let {}
    }


    private fun handlerLoading() {
//        Global.progressON(requireContext())
    }



    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

}