package com.sports2i.trainer.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.Graph
import com.sports2i.trainer.data.model.TrainingOverallGraphItem
import com.sports2i.trainer.utils.ColorUtil
import com.sports2i.trainer.utils.DateTimeUtil.extractDayAndMonth
import com.sports2i.trainer.utils.DateTimeUtil.extractDayOfMonth
import com.tbuonomo.viewpagerdotsindicator.setBackgroundCompat

@SuppressLint("ViewConstructor", "SetTextI18n")
class GraphPain constructor(context: Context, type: Int, bottomValue:String, list: List<Graph.PainGraph>,dateTime:String): RelativeLayout(context) {

    private var painGraph: Graph.PainGraph = Graph.PainGraph()

    private fun measures(type: Int, num: Int): Int {
        return when {
            type != 0 -> when {
                num < 60 -> 0
                num <= 69 -> -1
                num <= 75 -> -2
                num <= 81 -> -3
                num <= 87 -> -4
                num <= 91 -> -5
                num <= 99 -> -6
                else -> -7
            }
            else -> when {
                num == 0 -> 0
                num == 100 -> 11
                num <= 20 -> if (num % 2 != 0) (num / 4) + (num % 2) else num / 4
                num <= 44 -> if (num % 2 != 0) num / 10 + 6 else num / 10 + 5
                num >= 80 -> num / 4 - ((num / 4 - (6 * 3)) * 2)
                else -> 8
            }
        }
    }


    private fun graphPain(list: ArrayList<Double>, type: Int): LinearLayoutCompat {
        val layout = LinearLayoutCompat(context)
        layout.layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layout.orientation = LinearLayoutCompat.HORIZONTAL
        layout.gravity = Gravity.BOTTOM
        layout.setPadding((40 * resources.displayMetrics.density).toInt(), 0, (20 * resources.displayMetrics.density).toInt(), (20 * resources.displayMetrics.density).toInt())
        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))

        var view = View(context)
        var params = LinearLayoutCompat.LayoutParams((10 * resources.displayMetrics.density).toInt(), (10 * resources.displayMetrics.density).toInt())

        params.bottomMargin = (list[0] * resources.displayMetrics.density).toInt()
        view.layoutParams = params
//        attributePainView(list, view)

        val color = if (list[0].toInt() <= 30) R.color.graph_type5
        else if (list[0].toInt() <= 60) R.color.graph_type3
        else R.color.graph_type1

        view.setBackgroundResource(R.drawable.circle_type)
        (view.background as GradientDrawable).setStroke(9, ContextCompat.getColor(context, color))
        layout.addView(view)

        val layoutParams = LinearLayoutCompat.LayoutParams(((when { type != 0 -> 100 else -> 70 }) * resources.displayMetrics.density).toInt(), LinearLayoutCompat.LayoutParams.MATCH_PARENT)
        for (i in 1 until list.size) {
            val value = list[i - 1] - list[i]

            val lineContainer = LinearLayoutCompat(context)
            lineContainer.layoutParams = layoutParams
            lineContainer.orientation = LinearLayoutCompat.VERTICAL
            lineContainer.gravity = Gravity.BOTTOM

            // 라인 뷰 생성
            val lineView = View(context)
            val lineParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, (2 * resources.displayMetrics.density).toInt())
            lineParams.leftMargin = (-60 * resources.displayMetrics.density).toInt()
            lineParams.rightMargin = (-60 * resources.displayMetrics.density).toInt()
            lineParams.bottomMargin = (((list[i - 1] + list[i]) / 2 + 4) * resources.displayMetrics.density).toInt()
            lineParams.gravity = Gravity.BOTTOM
            lineView.layoutParams = lineParams
            lineView.rotation = (value.toInt()) / 2 + when { value > 0 -> measures(type, value.toInt()) else -> measures(type, (value * -1).toInt()) * -1}.toFloat()
            lineView.setBackgroundColor(ContextCompat.getColor(context, ColorUtil.GraphColor(10)))
            lineContainer.addView(lineView)
            layout.addView(lineContainer)

            val circleContainer = LinearLayoutCompat(context)
            circleContainer.layoutParams = layoutParams
            circleContainer.orientation = LinearLayoutCompat.VERTICAL
            circleContainer.gravity = Gravity.BOTTOM
            // 원 뷰 생성
            val circleView = View(context)
            val circleParams = LinearLayoutCompat.LayoutParams((10 * resources.displayMetrics.density).toInt(), (10 * resources.displayMetrics.density).toInt())
            circleParams.bottomMargin = ((list[i]) * resources.displayMetrics.density).toInt()

            val color = if (list[i].toInt() <= 30) R.color.graph_type5
            else if (list[i].toInt() <= 60) R.color.graph_type3
            else R.color.graph_type1

            circleView.layoutParams = circleParams
            circleView.setBackgroundResource(R.drawable.circle_type)
            (circleView.background as GradientDrawable).setStroke(9, ContextCompat.getColor(context, color))

            layout.addView(circleView)

        }
        return layout
    }

    private fun random(): ArrayList<Double> {
        val list = ArrayList<Double>()
        for (i in 0 until 31) {
            list.add((50..100).random().toDouble())
        }
        return list
    }

    init {
        this.layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, ((when {type != 1 -> 150 else -> 180 }) * resources.displayMetrics.density).toInt())
        val scrollView = HorizontalScrollView(context)
        scrollView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        scrollView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        scrollView.isHorizontalScrollBarEnabled = false

        val layout = RelativeLayout(context)
        layout.layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layout.setPadding(0, 0, (20 * resources.displayMetrics.density).toInt(), 0)

        val selectLayout = LinearLayoutCompat(context)
        selectLayout.layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        selectLayout.orientation = LinearLayoutCompat.HORIZONTAL
        selectLayout.gravity = Gravity.CENTER
        selectLayout.setPadding((1 * resources.displayMetrics.density).toInt(), 0, 0, 0)
        layout.addView(selectLayout)

                val graphList = ArrayList<Double>()
                    for (i in 0 until list.size) {
                        graphList.add(list[i].value * 10)
                    }
                    layout.addView(graphPain(graphList, 0))

                for (i in 0 until list.size) {
                    val selectView = View(context)
                    val selectParams = LinearLayoutCompat.LayoutParams((70 * resources.displayMetrics.density).toInt(), ViewGroup.LayoutParams.MATCH_PARENT)

                    // circleView의 가운데에 위치하도록 계산
                    val circleWidth = (10 * resources.displayMetrics.density).toInt()
                    val selectWidth = (70 * resources.displayMetrics.density).toInt()
                    val margin = (20 * resources.displayMetrics.density).toInt()

                    selectParams.leftMargin = ((selectWidth - circleWidth) / 2 - margin)

                    selectParams.gravity = Gravity.LEFT
                    selectView.layoutParams = selectParams
                    selectLayout.addView(selectView)

                    if(list[i].trainingDate.equals(dateTime)) selectView.setBackgroundResource(R.drawable.bg_select_graph_rounded)
                    else selectView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))

                    // 클릭 이벤트 추가
                    selectView.setOnClickListener {
                        // 클릭한 selectView의 배경색을 변경
                        selectView.setBackgroundResource(R.drawable.bg_select_graph_rounded)
                        // 다른 selectView들의 배경색을 초기화
                        for (index in 0 until selectLayout.childCount) {
                            if (index != i) {
                                val otherView = selectLayout.getChildAt(index) as View
                                otherView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                            }
                        }
                        painGraph = list[i]
                        notifyDataSelected()
                    }
                }

        var textView = AppCompatTextView(context)
                    var params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    params.leftMargin = (36 * resources.displayMetrics.density).toInt()
                    params.bottomMargin = ((graphList[0] + 30) * resources.displayMetrics.density).toInt()
                    params.addRule(ALIGN_PARENT_BOTTOM)
                    textView.layoutParams = params
                    textView.gravity = Gravity.BOTTOM
                    textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                    textView.typeface = Typeface.DEFAULT_BOLD
                    textView.textSize = 11F
                    textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                    textView.text = "${list[0].value}"
                    layout.addView(textView)

             textView = AppCompatTextView(context)
                    params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    params.leftMargin = (30 * resources.displayMetrics.density).toInt()
                    params.bottomMargin = (10 * resources.displayMetrics.density).toInt()
                    params.addRule(ALIGN_PARENT_BOTTOM)
                    textView.layoutParams = params
                    textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                    textView.textSize = 11F
                    textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                    val dateData = extractDayAndMonth(list[0].trainingDate)
                    var trainingDate = list[0].trainingDate
                    textView.text = "${dateData}"
                    layout.addView(textView)

                    for (i in 1 until list.size) {
                        textView = AppCompatTextView(context)
                        params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
                        params.leftMargin = ((36 + (i * 80)) * resources.displayMetrics.density).toInt()
                        params.bottomMargin = ((graphList[i] + 30) * resources.displayMetrics.density).toInt()
                        params.addRule(ALIGN_PARENT_BOTTOM)
                        textView.layoutParams = params
                        textView.gravity = Gravity.BOTTOM
                        textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                        textView.typeface = Typeface.DEFAULT_BOLD
                        textView.textSize = 11F
                        textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                        textView.text = "${list[i].value}"
                        layout.addView(textView)

                        textView = AppCompatTextView(context)
                        params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                        params.leftMargin = ((30 + (i * 80)) * resources.displayMetrics.density).toInt()
                        params.addRule(ALIGN_PARENT_BOTTOM)
                        params.bottomMargin = (10 * resources.displayMetrics.density).toInt()
                        textView.layoutParams = params
                        textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                        textView.textSize = 11F
                        textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                        trainingDate = list[i].trainingDate

                        val dateData = extractDayAndMonth(trainingDate)
                        textView.text = "${dateData}"
                        layout.addView(textView)
                    }

            scrollView.addView(layout)

        this.addView(scrollView)
    }


    interface OnDataSelectedListener {
        fun onDataSelected(painGraph:  Graph.PainGraph)
    }

    // 3. 리스너 변수 선언
    private var onDataSelectedListener: OnDataSelectedListener? = null

    // 3. 리스너 등록 메서드
    fun setOnDataSelectedListener(listener: OnDataSelectedListener) {
        onDataSelectedListener = listener
    }

    // 3. 데이터 전달 메서드
    private fun notifyDataSelected() {
        onDataSelectedListener?.onDataSelected(painGraph)
    }

}