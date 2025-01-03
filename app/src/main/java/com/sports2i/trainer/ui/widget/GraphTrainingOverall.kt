package com.sports2i.trainer.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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
import com.sports2i.trainer.utils.DateTimeUtil.extractDayOfMonth

@SuppressLint("ViewConstructor", "SetTextI18n")
class GraphTrainingOverall constructor(context: Context, type: Int, bottomValue:String, list: List<Graph>): RelativeLayout(context) {

    private fun measures(type: Int, num: Int): Int {
        if (type != 0) {
            return if (num < 60) 0
            else if (num <= 69) -1
            else if (num <= 75) -2
            else if (num <= 81) -3
            else if (num <= 87) -4
            else if (num <= 91) -5
            else if (num <= 99) -6
            else -7
        }else {
            return if (num == 0) 0
            else if (num == 100) 11
            else if (num <= 20) when { num % 2 != 0 -> (num / 2) + (num % 2) else -> num / 2 }
            else if (num <= 44) when { num % 2 != 0 -> num / 4 + 6 else -> num / 4 + 5 }
            else if (num >= 80) num / 4 - ((num / 4 - (6 * 3)) * 2)
            else 17
        }
    }


//    private fun random(): ArrayList<Int> {
//        val list = ArrayList<Int>()
//        for (i in 0 until 31) {
//            list.add((50..100).random())
//        }
//        return list
//    }
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

    return list
}

//    private fun sample(num: Int): Int {
//        when (num) {
//            1 -> { return R.color.graph_type1 }
//            2 -> { return R.color.graph_type2 }
//            3 -> { return R.color.graph_type3 }
//            4 -> { return R.color.graph_type4 }
//            5 -> { return R.color.graph_type5 }
//        }
//        return R.color.graph_base
//    }



    @SuppressLint("ResourceType")
    private fun graph(num1: Int, num2: Int, text: String, layoutParams: LinearLayoutCompat.LayoutParams): RelativeLayout {
        val layout = RelativeLayout(context)
        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        layout.layoutParams = layoutParams
        val container = LinearLayoutCompat(context)
        var params = LayoutParams(LayoutParams.WRAP_CONTENT, (100 * resources.displayMetrics.density).toInt())
        params.bottomMargin = (6 * resources.displayMetrics.density).toInt()
        params.addRule(ABOVE, 1)
        params.addRule(CENTER_IN_PARENT)
        container.layoutParams = params
        container.orientation = LinearLayoutCompat.HORIZONTAL
        container.gravity = Gravity.BOTTOM
        container.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        var view = View(context)
        var viewParams = LinearLayoutCompat.LayoutParams((10 * resources.displayMetrics.density).toInt(), (num1 * resources.displayMetrics.density).toInt())
        viewParams.leftMargin = (3 * resources.displayMetrics.density).toInt()
        viewParams.rightMargin = (3 * resources.displayMetrics.density.toInt())
        view.layoutParams = viewParams
        view.setBackgroundResource(R.drawable.round_dot)
        (view.background as GradientDrawable).setColor(ContextCompat.getColor(context, R.color.purple))
        container.addView(view)
        view = View(context)
        viewParams = LinearLayoutCompat.LayoutParams((10 * resources.displayMetrics.density).toInt(), (num2 * resources.displayMetrics.density).toInt())
        viewParams.leftMargin = (3 * resources.displayMetrics.density).toInt()
        viewParams.rightMargin = (3 * resources.displayMetrics.density.toInt())
        view.layoutParams = viewParams
        view.setBackgroundResource(R.drawable.round_dot)
        (view.background as GradientDrawable).setColor(ContextCompat.getColor(context, R.color.blue))
        container.addView(view)
        layout.addView(container)
        val textView = AppCompatTextView(context)
        params = LayoutParams(LayoutParams.MATCH_PARENT, (18 * resources.displayMetrics.density).toInt())
        params.addRule(ALIGN_PARENT_BOTTOM)
        textView.id = 1
        textView.layoutParams = params
        textView.gravity = Gravity.CENTER
        textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        textView.textSize = 11F
        textView.setTextColor(ContextCompat.getColor(context, R.color.black))
        textView.text = text
        layout.addView(textView)
        return layout
    }

    @SuppressLint("SetTextI18n")
    private fun line(num: Int, num2: Int) {
        val view = View(context)
        var params = LayoutParams(LayoutParams.MATCH_PARENT, (2 * resources.displayMetrics.density).toInt())

        val value = if(num > 100) 100 else num

        params.bottomMargin = ((value + 22 + num2) * resources.displayMetrics.density).toInt()
        params.addRule(ALIGN_PARENT_BOTTOM)
        view.layoutParams = params
        view.setBackgroundResource(R.drawable.line_dotted)
        val layout = LinearLayoutCompat(context)
        params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params.rightMargin = (82 * resources.displayMetrics.density).toInt()

        params.bottomMargin = ((value + 28 + num2) * resources.displayMetrics.density).toInt()
        params.addRule(ALIGN_PARENT_END)
        params.addRule(ALIGN_PARENT_BOTTOM)
        layout.layoutParams = params
        layout.orientation = LinearLayoutCompat.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        val textview = AppCompatTextView(context)
        val layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
        layoutParams.bottomMargin = (-1 * resources.displayMetrics.density).toInt()
        textview.layoutParams = layoutParams
        textview.setPadding((12 * resources.displayMetrics.density).toInt(), (4 * resources.displayMetrics.density).toInt(), (12 * resources.displayMetrics.density).toInt(), (4 * resources.displayMetrics.density).toInt())
        textview.setBackgroundResource(R.drawable.round_type_on)
        textview.textSize = 12F
        textview.setTextColor(ContextCompat.getColor(context, R.color.white))
        textview.text = context.getString(R.string.average) + " $num%"
        layout.addView(textview)
        val imageView = AppCompatImageView(context)
        imageView.layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
        imageView.setImageResource(R.mipmap.text_point)
        imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        layout.addView(imageView)
        this.addView(layout)
        this.addView(view)
    }

    private fun graph(list: ArrayList<Int>, type: Int): LinearLayoutCompat {
        val layout = LinearLayoutCompat(context)
        layout.layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layout.orientation = LinearLayoutCompat.HORIZONTAL
        layout.gravity = Gravity.BOTTOM
        layout.setPadding((20 * resources.displayMetrics.density).toInt(), 0, (20 * resources.displayMetrics.density).toInt(), (20 * resources.displayMetrics.density).toInt())
        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))

        var view = View(context)
        var params = LinearLayoutCompat.LayoutParams((10 * resources.displayMetrics.density).toInt(), (10 * resources.displayMetrics.density).toInt())

        params.bottomMargin = (list[0] * resources.displayMetrics.density).toInt()
        view.layoutParams = params

        attributeView(type, view)
        layout.addView(view)
        val layoutParams = LinearLayoutCompat.LayoutParams(((when { type != 0 -> 100 else -> 50 }) * resources.displayMetrics.density).toInt(), LinearLayoutCompat.LayoutParams.MATCH_PARENT)
        for (i in 1 until list.size) {
            val value = list[i - 1] - list[i]
            val container = LinearLayoutCompat(context)
            container.layoutParams = layoutParams
            container.orientation = LinearLayoutCompat.VERTICAL
            container.gravity = Gravity.BOTTOM
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
            view = View(context)
            params = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, (2 * resources.displayMetrics.density).toInt())
            params.leftMargin = (-60 * resources.displayMetrics.density).toInt()
            params.rightMargin = (-60 * resources.displayMetrics.density).toInt()
            params.bottomMargin = (((list[i - 1] + list[i]) / 2 + 4) * resources.displayMetrics.density).toInt()
            view.layoutParams = params
            view.rotation = (value) / 2 + when { value > 0 -> measures(type, value) else -> measures(type, value * -1) * -1}.toFloat()
            view.setBackgroundColor(ContextCompat.getColor(context, ColorUtil.GraphColor(type + 10)
            ))
            container.addView(view)
            layout.addView(container)
            view = View(context)
            params = LinearLayoutCompat.LayoutParams((10 * resources.displayMetrics.density).toInt(), (10 * resources.displayMetrics.density).toInt())
            params.bottomMargin = ((list[i]) * resources.displayMetrics.density).toInt()
            view.layoutParams = params
            attributeView(type, view)
            layout.addView(view)
        }
        return layout
    }

    @SuppressLint("ResourceType", "SetTextI18n")
    private fun graph(num: Int, data: String, type: Boolean, itemName:Boolean, layoutParams: LinearLayoutCompat.LayoutParams): RelativeLayout {
        val layout = RelativeLayout(context)
        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        layout.layoutParams = layoutParams
        var textView = AppCompatTextView(context)
        var params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val dateData = extractDayOfMonth(data)

        params.bottomMargin = (2 * resources.displayMetrics.density).toInt()
        params.addRule(ABOVE, 2)
        textView.layoutParams = params
        textView.gravity = Gravity.CENTER
        textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        textView.typeface = Typeface.DEFAULT_BOLD
        textView.textSize = 11F
        textView.setTextColor(ContextCompat.getColor(context, R.color.black))
        textView.text = "$num%"
        layout.addView(textView)
        val view = View(context)
        val value = if (num > 100) 100 else num
        params = LayoutParams((10 * resources.displayMetrics.density).toInt(), (value * resources.displayMetrics.density).toInt())
        params.bottomMargin = (6 * resources.displayMetrics.density).toInt()
        params.addRule(ABOVE, 1)
        params.addRule(CENTER_IN_PARENT)
        view.id = 2
        view.layoutParams = params
        view.setBackgroundResource(R.drawable.round_dot)
        (view.background as GradientDrawable).setColor(ContextCompat.getColor(context, R.color.graph_base))
        layout.addView(view)
        if (type) {
            textView = AppCompatTextView(context)
            params = LayoutParams(LayoutParams.MATCH_PARENT, (18 * resources.displayMetrics.density).toInt())
            params.addRule(ALIGN_PARENT_BOTTOM)
            textView.id = 1
            textView.layoutParams = params
            textView.gravity = Gravity.CENTER
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
            textView.textSize = 11F
            textView.setTextColor(ContextCompat.getColor(context, R.color.black))
            if(itemName) textView.text = data
            else textView.text = dateData
            layout.addView(textView)
        }else {
            val container = LinearLayoutCompat(context)
            params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            params.addRule(ALIGN_PARENT_BOTTOM)
            container.id = 1
            container.layoutParams = params
            container.orientation = LinearLayoutCompat.VERTICAL
            container.gravity = Gravity.CENTER
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
            textView = AppCompatTextView(context)
            textView.layoutParams = LinearLayoutCompat.LayoutParams((30 * resources.displayMetrics.density).toInt(), (30 * resources.displayMetrics.density).toInt())
            textView.gravity = Gravity.CENTER
            textView.setBackgroundResource(R.drawable.circle_type)
            textView.background.colorFilter = BlendModeColorFilter(ContextCompat.getColor(context, R.color.green_light), BlendMode.SRC_ATOP)
            textView.textSize = 13F
            textView.setTextColor(ContextCompat.getColor(context, R.color.white))
            textView.text = data.subSequence(0, 1)
            container.addView(textView)
            textView = AppCompatTextView(context)
            textView.layoutParams = LinearLayoutCompat.LayoutParams(LayoutParams.WRAP_CONTENT, (18 * resources.displayMetrics.density).toInt())
            textView.gravity = Gravity.BOTTOM
            textView.maxLines = 1
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
            textView.textSize = 11F
            textView.setTextColor(ContextCompat.getColor(context, R.color.black))
            textView.text = data
            container.addView(textView)
            layout.addView(container)
        }
        return layout
    }

    private fun attributeView(type: Int, view: View) {
        if (type == 0) view.setBackgroundResource(R.drawable.circle_dot)
        else {
            view.setBackgroundResource(R.drawable.circle_type)
            (view.background as GradientDrawable).setStroke(9, ContextCompat.getColor(context, ColorUtil.GraphColor(type)))
        }
    }

    init {
        this.layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, ((when {type != 1 -> 150 else -> 180 }) * resources.displayMetrics.density).toInt())
        val scrollView = HorizontalScrollView(context)
        scrollView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        scrollView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        scrollView.isHorizontalScrollBarEnabled = false

        if (type < 3) {
            val layout = LinearLayoutCompat(context)
            layout.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
            layout.layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layout.orientation = LinearLayoutCompat.HORIZONTAL
            val params = LinearLayoutCompat.LayoutParams((78 * resources.displayMetrics.density.toInt()), LinearLayoutCompat.LayoutParams.MATCH_PARENT)
            when (type) {
                1 -> {
                    var sum = 0
                    for (i in 0 until list.size) {
                        sum += list[i].value
                    }
                    var isTrainingDate :Boolean = false
                    var isItemName :Boolean = false
                    var margin :Int = 30

                    /** 선수
                     * isTrainingDate = false
                     * margin = 30
                     * */
                    /** 날짜
                     * isTrainingDate = true
                     * margin = 0
                     * */

                    if(bottomValue.equals("name")){
                         isTrainingDate = false
                         isItemName = false
                         margin = 30

                        if(list.size > 0) {
                            line(sum / list.size, margin)
                            for (i in 0 until list.size) {
                                layout.addView(
                                    graph(
                                        list[i].value,
                                        list[i].userName,
                                        isTrainingDate,
                                        isItemName,
                                        params
                                    )
                                )
                            }
                        }

                    } else if(bottomValue.equals("date")){
                         isTrainingDate = true
                         isItemName = false
                         margin = 0

                        if(list.size > 0) {
                            line(sum / list.size, margin)
                            for (i in 0 until list.size) {
                                layout.addView(
                                    graph(
                                        list[i].value,
                                        list[i].trainingDate,
                                        isTrainingDate,
                                        isItemName,
                                        params
                                    )
                                )
                            }
                        }

                    } else if(bottomValue.equals("itemName")){
                        isTrainingDate = true
                        isItemName= true
                        margin = 0
                        if(list.size > 0) {
                            for (i in 0 until list.size) {
                                layout.addView(
                                    graph(
                                        list[i].value,
                                        list[i].userName,
                                        isTrainingDate,
                                        isItemName,
                                        params
                                    )
                                )
                            }
                        }

                    }else{
                         isTrainingDate = false
                         margin = 30
                    }


                }
                2 -> {
                    for (i in 0 until list.size) {
                        val trainingDate = list[i].trainingDate
                        layout.addView(graph(list[i].value.toInt(), list[i].value2.toInt(), "${trainingDate.substring(trainingDate.length - 2, trainingDate.length)}일", params))
                    }
                }
            }
            scrollView.addView(layout)
        } else {
            val layout = RelativeLayout(context)
            layout.layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layout.setPadding(0, 0, (14 * resources.displayMetrics.density).toInt(), 0)
            when (type) {
                3 -> {
                    layout.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                    val sampleList = ArrayList<Int>()
                    for (i in 0 until list.size) {
                        // 값이 100을 초과하는 경우 100으로 제한
                        val value = if (list[i].value > 100) 100 else list[i].value
                        sampleList.add(value)
                    }
                    layout.addView(graph(sampleList, 0))
                    var textView = AppCompatTextView(context)
                    var params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    params.leftMargin = (14 * resources.displayMetrics.density).toInt()
                    params.bottomMargin = ((sampleList[0] + 30) * resources.displayMetrics.density).toInt()
                    params.addRule(ALIGN_PARENT_BOTTOM)
                    textView.layoutParams = params
                    textView.gravity = Gravity.CENTER
                    textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                    textView.typeface = Typeface.DEFAULT_BOLD
                    textView.textSize = 11F
                    textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                    textView.text = "${list[0].value}%"
                    layout.addView(textView)
                    textView = AppCompatTextView(context)
                    params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    params.leftMargin = (14 * resources.displayMetrics.density).toInt()
                    params.bottomMargin = (6 * resources.displayMetrics.density).toInt()
                    params.addRule(ALIGN_PARENT_BOTTOM)
                    textView.layoutParams = params
                    textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                    textView.textSize = 11F
                    textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                    var trainingDate = list[0].trainingDate
                    textView.text = "${trainingDate.substring(trainingDate.length - 2, trainingDate.length)}일"
                    layout.addView(textView)
                    for (i in 1 until list.size) {
                        textView = AppCompatTextView(context)
                        params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                        params.leftMargin = ((14 + (i * 60)) * resources.displayMetrics.density).toInt()
                        params.bottomMargin = ((sampleList[i] + 30) * resources.displayMetrics.density).toInt()
                        params.addRule(ALIGN_PARENT_BOTTOM)
                        textView.layoutParams = params
                        textView.gravity = Gravity.CENTER
                        textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                        textView.typeface = Typeface.DEFAULT_BOLD
                        textView.textSize = 11F
                        textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                        textView.text = "${list[i].value}%"
                        layout.addView(textView)
                        textView = AppCompatTextView(context)
                        params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                        params.leftMargin = ((14 + (i * 60)) * resources.displayMetrics.density).toInt()
                        params.addRule(ALIGN_PARENT_BOTTOM)
                        params.bottomMargin = (6 * resources.displayMetrics.density).toInt()
                        textView.layoutParams = params
                        textView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                        textView.textSize = 11F
                        textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                        trainingDate = list[i].trainingDate
                        textView.text = "${trainingDate.substring(trainingDate.length - 2, trainingDate.length)}일"
                        layout.addView(textView)
                    }
                }
                4 -> {
                    layout.setPadding((10 * resources.displayMetrics.density).toInt(), 0, (10 * resources.displayMetrics.density).toInt(), 0)
                    var view = View(context)
                    var params = LayoutParams((42 * resources.displayMetrics.density).toInt(), ((100 + 10) * resources.displayMetrics.density).toInt())
                    params.leftMargin = (4 * resources.displayMetrics.density).toInt()
                    params.bottomMargin = (20 * resources.displayMetrics.density).toInt()
                    params.addRule(ALIGN_PARENT_BOTTOM)
                    view.layoutParams = params
                    view.setBackgroundResource(R.drawable.round_dot)
                    (view.background as GradientDrawable).setColor(ContextCompat.getColor(context, R.color.white))
                    layout.addView(view)
                    var textView = AppCompatTextView(context)
                    params = LayoutParams((42 * resources.displayMetrics.density).toInt(), LayoutParams.WRAP_CONTENT)
                    params.leftMargin = (4 * resources.displayMetrics.density).toInt()
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
                        params = LayoutParams((42 * resources.displayMetrics.density).toInt(), ((80 + 10) * resources.displayMetrics.density).toInt())
                        params.leftMargin = ((2 + (i * 110)) * resources.displayMetrics.density).toInt()
                        params.bottomMargin = (20 * resources.displayMetrics.density).toInt()
                        params.addRule(ALIGN_PARENT_BOTTOM)
                        view.layoutParams = params
                        view.setBackgroundResource(R.drawable.round_dot)
                        layout.addView(view)
                        textView = AppCompatTextView(context)
                        params = LayoutParams((42 * resources.displayMetrics.density).toInt(), LayoutParams.WRAP_CONTENT)
                        params.leftMargin = ((2 + (i * 110)) * resources.displayMetrics.density).toInt()
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
                    layout.addView(graph(random(), 1))
                    layout.addView(graph(random(), 2))
                    layout.addView(graph(random(), 3))
                }
            }
            scrollView.addView(layout)
        }
        this.addView(scrollView)
    }
}