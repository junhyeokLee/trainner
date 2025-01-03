package com.sports2i.trainer.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.sports2i.trainer.R
import kotlin.math.roundToInt

class RatioLayout : RelativeLayout {
    private var widthRatio = 1
    private var heightRatio = 1

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        getAttrs(attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var width = measuredWidth
        var height = measuredHeight

        if ( widthRatio > heightRatio ) {
            height = (measuredWidth * (heightRatio / widthRatio.toFloat())).roundToInt()
        } else if ( heightRatio > widthRatio ) {
            width = (measuredHeight * (widthRatio / heightRatio.toFloat())).roundToInt()
        } else {
            height = measuredWidth
        }

        setMeasuredDimension(width, height)
    }

    private fun getAttrs(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RatioLayout)

        widthRatio = typedArray.getInt(R.styleable.RatioLayout_layoutWidthRatio, 1)
        heightRatio = typedArray.getInt(R.styleable.RatioLayout_layoutHeightRatio, 1)

        typedArray.recycle()
    }
}