@file:Suppress("DEPRECATED_ANNOTATION")

package com.sports2i.trainer.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PainInfo(
    val id: Int = 0,
    val userId: String = "",
    val painLocation: String = "",
    val comment: String = "",
    val reportingDate: String = "",
    val locationX: Float = 0F,
    val locationY: Float = 0F,
    val painLevel: Int = 0) : Parcelable {
    override fun toString(): String {
        return "Pain(id=$id, userId=$userId, painLocation=$painLocation, comment=$comment, reportingDate=$reportingDate, locationX=$locationX, locationY=$locationY, painLevel=$painLevel)"
    }
}

@Parcelize
data class PainInfo2(
    val id: Int = 0,
    val userId: String = "",
    val painLocation: String = "",
    val locationX: Float = 0F,
    val locationY: Float = 0F,
    val painLevel: Int = 0,
    val comment: String = "",
    val reportingDate: String = "",
    val painLevelAvg: String = "",
) : Parcelable {
    override fun toString(): String {
        return "PainInfo2(id=$id, userId=$userId, painLocation=$painLocation, comment=$comment, reportingDate=$reportingDate, locationX=$locationX, locationY=$locationY, painLevel=$painLevel,painLevelAvg=$painLevelAvg)"
    }
}

@Parcelize
data class PainInfoResponse(val data: MutableList<PainInfo>): Parcelable {
    override fun toString(): String {
        return "PainInfoResponse(data=$data)"
    }
}

@Parcelize
data class PainInfoResponse2(val data: MutableList<PainInfo2>): Parcelable {
    override fun toString(): String {
        return "PainInfoResponse2(data=$data)"
    }
}
