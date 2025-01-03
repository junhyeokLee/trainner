package com.sports2i.trainer.data.model
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Parcelize
data class ExerciseItem(
    val exerciseId: String = "",
    val exerciseName: String = "",
) : Parcelable {
    override fun toString(): String {
        return "ExcerciseItem(exerciseId=$exerciseId,exerciseName=$exerciseName)"
    }
}

@Parcelize
data class ExerciseItemResponse(
    val data: MutableList<ExerciseItem>
) : Parcelable {
    override fun toString(): String {
        return "ExerciseItemResponse(data=$data)"
    }
}
