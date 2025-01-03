package com.sports2i.trainer.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ExerciseTimeItem(
    val timeItemId: String = "",
    val timeItemName: String = "",
) : Parcelable {
    override fun toString(): String {
        return "ExerciseTimeItem(timeItemId=$timeItemId,timeItemName=$timeItemName)"
    }
}

@Parcelize
data class ExerciseTimeItemResponse(
    val data: MutableList<ExerciseTimeItem>
) : Parcelable {
    override fun toString(): String {
        return "ExerciseTimeItemResponse(data=$data)"
    }
}
