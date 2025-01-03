package com.sports2i.trainer.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.Graph

@SuppressLint("ViewConstructor", "SetTextI18n")
class GraphTrainingStatistics constructor(context: Context, type: Int, bottomValue:String, list: List<Graph.StatisticsGraph>): RelativeLayout(context) {


    private fun measures(type: Int, num: Int): Int {
        return when {
            num == 0 -> 0
            num <= 20 -> if (num % 2 != 0) (num / 2) + (num % 2) else num / 2
            num <= 44 -> num / 2
            num <= 60 -> num / 2 - 4
            num <= 69 -> num / 2 - 7
            num <= 80 -> num / 4 - ((num / 4 - (6 * 3)) * 2) + 4
            num <= 99 -> num / 4 - ((num / 4 - (6 * 3)) * 2) + 4
            else -> num / 2 - ((num / 4 - (6 * 2)) * 2)+ 170
        }
    }

    private fun color(num: Int): Int {
        when (num) {
            10 -> { return R.color.border_color }
            1, 11 -> { return R.color.graph_type1 }
            2, 12 -> { return R.color.purple }
            3, 13 -> { return R.color.blue }
        }
        return R.color.primary
    }

    private fun graph(list: ArrayList<Int>, type: Int): LinearLayoutCompat {
        val layout = LinearLayoutCompat(context)
        layout.layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layout.orientation = LinearLayoutCompat.HORIZONTAL
        layout.gravity = Gravity.BOTTOM
        layout.elevation = 10f
        layout.setPadding((14 * resources.displayMetrics.density).toInt(), 0,0, (20 * resources.displayMetrics.density).toInt())
        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))

        var view = View(context)
        var params = LinearLayoutCompat.LayoutParams((10 * resources.displayMetrics.density).toInt(), (10 * resources.displayMetrics.density).toInt())
        params.bottomMargin = (list[0] * resources.displayMetrics.density).toInt()
        view.layoutParams = params
        attributeView(type, view)
        layout.addView(view)

        val layoutParams = LinearLayoutCompat.LayoutParams((42 * resources.displayMetrics.density).toInt(), LinearLayoutCompat.LayoutParams.MATCH_PARENT)
        for (i in 1 until list.size) {
            val value = (list[i - 1] - list[i])
//            val bottomMarginValue = if( ((list[i - 1] + list[i]) / 2 + 4) > 100 ) 100 else ((list[i - 1] + list[i]) / 2 + 4)

            val lineContainer = LinearLayoutCompat(context)
            lineContainer.layoutParams = layoutParams
            lineContainer.orientation = LinearLayoutCompat.VERTICAL
            lineContainer.gravity = Gravity.BOTTOM
            lineContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))

            val lineView = View(context)
            val lineParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, (2 * resources.displayMetrics.density).toInt())
            lineParams.leftMargin = (-60 * resources.displayMetrics.density).toInt()
            lineParams.rightMargin = (-60 * resources.displayMetrics.density).toInt()
            lineParams.bottomMargin = (((list[i - 1] + list[i]) / 2 + 4) * resources.displayMetrics.density).toInt()
            lineParams.gravity = Gravity.BOTTOM
            lineView.layoutParams = lineParams
            lineView.rotation = (value.toInt()) / 2 + when { value > 0 -> measures(type, (value + 1)) else -> measures(type, ((value + 1) * -1).toInt()) * -1}.toFloat()
            lineView.setBackgroundColor(ContextCompat.getColor(context, color(type + 10)))
            lineContainer.addView(lineView)
            layout.addView(lineContainer)

            val circleView = View(context)
            val circleParams = LinearLayoutCompat.LayoutParams((10 * resources.displayMetrics.density).toInt(), (10 * resources.displayMetrics.density).toInt())
            circleParams.bottomMargin = ((list[i]) * resources.displayMetrics.density).toInt()
            circleView.layoutParams = circleParams
            attributeView(type, circleView)
            layout.addView(circleView)
        }
        return layout
    }

    private fun attributeView(type: Int, view: View) {
        if (type == 0) view.setBackgroundResource(R.drawable.circle_dot)
        else {
            view.setBackgroundResource(R.drawable.circle_type)
            (view.background as GradientDrawable).setStroke(9, ContextCompat.getColor(context, color(type)))
        }
    }

    init {
        this.layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, ((when {type == 4 -> 150 else -> 100 }) * resources.displayMetrics.density).toInt())
        val scrollView = HorizontalScrollView(context)
        val scrollViewLayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        scrollViewLayoutParams.marginStart = (16 * resources.displayMetrics.density).toInt()
        scrollViewLayoutParams.marginEnd = (16 * resources.displayMetrics.density).toInt()
        scrollView.layoutParams = scrollViewLayoutParams
        scrollView.setBackgroundResource(R.drawable.bg_round_gray)
        scrollView.isHorizontalScrollBarEnabled = false

        var tss : MutableList<Int> = mutableListOf()
        var tss7Avg : MutableList<Int> = mutableListOf()
        var tss28Avg : MutableList<Int> = mutableListOf()
        var injury : MutableList<Int> = mutableListOf()

        val layout = RelativeLayout(context)
        layout.layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layout.setPadding(0, 0, 0, 0)

            when (type) {
                4 -> {
                    layout.setPadding((16 * resources.displayMetrics.density).toInt(), 0, (16 * resources.displayMetrics.density).toInt(), (12 * resources.displayMetrics.density).toInt())

                    var view = View(context)
                    var params = LayoutParams((30 * resources.displayMetrics.density).toInt(), ((list[0].value + 10) * resources.displayMetrics.density).toInt())
                    params.leftMargin = (2 * resources.displayMetrics.density).toInt()
                    params.bottomMargin = (20 * resources.displayMetrics.density).toInt()
                    params.addRule(ALIGN_PARENT_BOTTOM)
                    view.layoutParams = params
                    view.setBackgroundResource(R.drawable.round_dot)
                    (view.background as GradientDrawable).setColor(ContextCompat.getColor(context, R.color.white))
                    view.elevation = 4F
                    layout.addView(view)
                    var textView = AppCompatTextView(context)
                    params = LayoutParams((30 * resources.displayMetrics.density).toInt(), LayoutParams.WRAP_CONTENT)
                    params.leftMargin = (resources.displayMetrics.density).toInt()
                    params.addRule(ALIGN_PARENT_BOTTOM)
                    textView.layoutParams = params
                    textView.gravity = Gravity.CENTER
                    textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                    textView.textSize = 11F
                    textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                    var trainingDate = list[0].trainingDate
                    textView.text = "${trainingDate.substring(trainingDate.length - 2, trainingDate.length)}일"
                    layout.addView(textView)
                    for (i in 1 until list.size) {
                        view = View(context)
                        params = LayoutParams((30 * resources.displayMetrics.density).toInt(), ((list[i].value + 10) * resources.displayMetrics.density).toInt())
                        params.leftMargin = ((4 + (i * 52)) * resources.displayMetrics.density).toInt()
                        params.bottomMargin = (20 * resources.displayMetrics.density).toInt()
                        params.addRule(ALIGN_PARENT_BOTTOM)
                        view.layoutParams = params
                        view.setBackgroundResource(R.drawable.round_dot)
                        view.elevation = 4F
                        layout.addView(view)
                        textView = AppCompatTextView(context)
                        params = LayoutParams((30 * resources.displayMetrics.density).toInt(), LayoutParams.WRAP_CONTENT)
                        params.leftMargin = ((4 + (i * 52)) * resources.displayMetrics.density).toInt()
                        params.addRule(ALIGN_PARENT_BOTTOM)
                        textView.layoutParams = params
                        textView.gravity = Gravity.CENTER
                        textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                        textView.textSize = 11F
                        textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                        trainingDate = list[i].trainingDate
                        textView.text = "${trainingDate.substring(trainingDate.length - 2, trainingDate.length)}일"
                        layout.addView(textView)
                    }

                    for(i in 0 until list.size){
                        tss7Avg.add(list[i].value2)
                        tss28Avg.add(list[i].value3)
                    }

                    layout.addView(graph(tss28Avg as ArrayList<Int>, 2))
                    layout.addView(graph(tss7Avg as ArrayList<Int>, 3))
//                    layout.addView(graph(random3(), 2))
//                    layout.addView(graph(random(), 3))

                }

                5 -> {
                    layout.setPadding((16 * resources.displayMetrics.density).toInt(), 0, (16 * resources.displayMetrics.density).toInt(), (12 * resources.displayMetrics.density).toInt())
//                    var params = LayoutParams((30 * resources.displayMetrics.density).toInt(), ((list[0].value4) * resources.displayMetrics.density).toInt())
                    var params = LayoutParams((42 * resources.displayMetrics.density).toInt(), ((100 + 10) * resources.displayMetrics.density).toInt())
                    params.leftMargin = (4 * resources.displayMetrics.density).toInt()
                    params.bottomMargin = (20 * resources.displayMetrics.density).toInt()
                    params.addRule(ALIGN_PARENT_BOTTOM)
                    var textView = AppCompatTextView(context)
                    params = LayoutParams((30 * resources.displayMetrics.density).toInt(), LayoutParams.WRAP_CONTENT)
                    params.leftMargin = (resources.displayMetrics.density).toInt()
                    params.addRule(ALIGN_PARENT_BOTTOM)
                    textView.layoutParams = params
                    textView.gravity = Gravity.CENTER
                    textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                    textView.textSize = 11F
                    textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                    var trainingDate = list[0].trainingDate
                    textView.text = "${trainingDate.substring(trainingDate.length - 2, trainingDate.length)}일"
                    layout.addView(textView)
                    for (i in 1 until list.size) {
                        params = LayoutParams((30 * resources.displayMetrics.density).toInt(), ((list[i].value4) * resources.displayMetrics.density).toInt())
                        params.leftMargin = ((4 + (i * 52)) * resources.displayMetrics.density).toInt()
                        params.bottomMargin = (20 * resources.displayMetrics.density).toInt()
                        params.addRule(ALIGN_PARENT_BOTTOM)
                        textView = AppCompatTextView(context)
                        params = LayoutParams((30 * resources.displayMetrics.density).toInt(), LayoutParams.WRAP_CONTENT)
                        params.leftMargin = ((4 + (i * 52)) * resources.displayMetrics.density).toInt()
                        params.addRule(ALIGN_PARENT_BOTTOM)
                        textView.layoutParams = params
                        textView.gravity = Gravity.CENTER
                        textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                        textView.textSize = 11F
                        textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                        trainingDate = list[i].trainingDate
                        textView.text = "${trainingDate.substring(trainingDate.length - 2, trainingDate.length)}일"
                        layout.addView(textView)
                    }

                    for(i in 0 until list.size){
                        injury.add((list[i].value4 / 2))
//                        injury.add(list[i].value4 )
                    }
                    layout.addView(graph(injury as ArrayList<Int>, 1))
//                    layout.addView(graph(random2(), 1))
                }
            }
            scrollView.addView(layout)

        this.addView(scrollView)
    }

    private fun random3(): ArrayList<Int> {
        val list = ArrayList<Int>()
        for (i in 0 until 31) {
            list.add((0..100).random())
        }
        return list
    }

    private fun random(): ArrayList<Int> {
        val list = ArrayList<Int>()
        list.add(69)
        list.add(0)
        list.add(60)
        list.add(0)
        list.add(100)
        list.add(90)
        list.add(10)
        list.add(30)
        list.add(81)
        list.add(10)
        list.add(0)
        list.add(87)
        list.add(0)
        list.add(91)
        list.add(89)
        list.add(20)
        list.add(99)
        list.add(100)
        list.add(0)
        list.add(93)
        list.add(0)
        list.add(100)
        list.add(12)

//        for (i in 0 until 31) {
//            list.add((0..100).random())
//        }
        return list
    }

    private fun random2(): ArrayList<Int> {
        val list = ArrayList<Int>()
        for (i in 0 until 31) {
            list.add((0..50).random())
        }
        return list
    }
}