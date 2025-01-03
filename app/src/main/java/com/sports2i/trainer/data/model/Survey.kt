@file:Suppress("DEPRECATED_ANNOTATION")

package com.sports2i.trainer.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Survey(
    val userId: String = "",
    val surveyDate: String = "",
    val surveyItemId: String = "",
    val surveyItemName: String = "",
    val surveyValue: Int = 0,
    val surveyItemList: MutableList<SurveyItemList> = mutableListOf()): Parcelable {
    override fun toString(): String {
        return "Survey(userId=$userId, surveyDate=$surveyDate, surveyItemId=$surveyItemId, surveyItemName=$surveyItemName, surveyValue=$surveyValue, surveyItemList=$surveyItemList)"
    }
}

@Parcelize
data class SurveyItemInsertRequest(val organizationId: String, val surveyItemName: String): Parcelable {
    override fun toString(): String {
        return "SurveyItemInsertRequest(organizationId=$organizationId,surveyItemName=$surveyItemName)"
    }
}

@Parcelize
data class SurveyItemResponse(val data: MutableList<Survey>): Parcelable {
    override fun toString(): String {
        return "SurveyItemResponse(data=$data)"
    }
}

@Parcelize
data class SurveyItemList(val surveyItemId: String, val surveyItemName: String): Parcelable {
    override fun toString(): String {
        return "SurveyItemList(surveyItemId=$surveyItemId,surveyItemName=$surveyItemName)"
    }
}


@Parcelize
data class SurveyInsert(
    var userId: String,
    var surveyDate: String,
    var surveyItemId: String,
    var surveyItemName: String,
    var surveyValue: Int
    ): Parcelable {
    override fun toString(): String {
        return "SurveyInsert(userId=$userId,surveyDate=$surveyDate,surveyItemId=$surveyItemId,surveyItemName=$surveyItemName,surveyValue=$surveyValue)"
    }
}

@Parcelize
data class SurveyInsertResponse(val data: MutableList<SurveyInsert>): Parcelable {
    override fun toString(): String {
        return "SurveyInsertResponse(data=$data)"
    }
}

@Parcelize
data class SurveySearch(
    val id: Int,
    val userId: String,
    val surveyDate: String,
    val surveyItemId: String,
    val surveyItemName: String,
    val surveyValue: Int,
    val surveyValueAvg: String
): Parcelable {
    override fun toString(): String {
        return "SurveySearch(id=$id,userId=$userId,surveyDate=$surveyDate,surveyItemId=$surveyItemId,surveyItemName=$surveyItemName,surveyValue=$surveyValue,surveyValueAvg=$surveyValueAvg)"
    }
}

@Parcelize
data class SurveySearchResponse(val data: MutableList<SurveySearch>): Parcelable {
    override fun toString(): String {
        return "SurveySearchResponse(data=$data)"
    }
}

@Parcelize
data class SurveyPreset(
    val id: Int,
    val surveyItemId: String,
    val surveyItemName: String
): Parcelable {
    override fun toString(): String {
        return "SurveyPreset(id=$id,surveyItemId=$surveyItemId,surveyItemName=$surveyItemName)"
    }
}

@Parcelize
data class SurveyPresetResponse(val data: MutableList<SurveyPreset>): Parcelable {
    override fun toString(): String {
        return "SurveyPresetResponse(data=$data)"
    }
}