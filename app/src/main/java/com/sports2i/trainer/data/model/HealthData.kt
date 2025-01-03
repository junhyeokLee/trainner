package com.sports2i.trainer.data.model
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Parcelize
data class HealthData(
    val id: Int,
    val type: String,
    val value: String
) : Parcelable {
    override fun toString(): String {
        return "HealthData(id='$id', type='$type', value='$value')"
    }
}
