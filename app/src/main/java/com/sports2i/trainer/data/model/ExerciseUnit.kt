package com.sports2i.trainer.data.model
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Parcelize
data class ExerciseUnit(
    var exerciseUnitId: String = "",
    var exerciseUnitName: String = "",
    var exerciseUnit: String = "",
    var organizationId : String = "",
    var numberOver: Boolean = false,
) : Parcelable {
    override fun toString(): String {
        return "ExcerciseUnit(exerciseUnitId=$exerciseUnitId,exerciseUnitName=$exerciseUnitName,exerciseUnit=$exerciseUnit,organizationId=$organizationId,numberOver=$numberOver)"
    }
}

@Parcelize
data class ExerciseUnitResponse(
    val data: MutableList<ExerciseUnit>
) : Parcelable {
    override fun toString(): String {
        return "ExerciseUnithResponse(data=$data)"
    }
}
