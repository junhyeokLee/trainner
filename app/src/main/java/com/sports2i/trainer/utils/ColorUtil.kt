package com.sports2i.trainer.utils

import android.util.Log
import com.sports2i.trainer.R

object ColorUtil {

     fun PainColor(num: Int): Int {
        return if (num <= 3) R.color.graph_type5
        else if (num <= 6) R.color.graph_type3
        else R.color.graph_type1
    }

     fun GraphColor(num: Int): Int {
        when (num) {
            10 -> { return R.color.border_color }
            1, 11 -> { return R.color.pink }
            2, 12 -> { return R.color.purple }
            3, 13 -> { return R.color.blue }
        }
        return R.color.primary
    }

}