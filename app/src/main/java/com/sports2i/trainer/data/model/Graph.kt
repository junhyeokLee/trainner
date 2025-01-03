package com.sports2i.trainer.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Graph(
    val userName: String = "",
    val trainingDate: String = "",
    val value: Int = 0,
    val value2: Int = 0
    )
    : Parcelable {
    override fun toString(): String {
        return "TrainingOverall(userName=$userName, trainingDate=$trainingDate, value=$value,value2=$value2)"
    }

    @Parcelize
    data class StatisticsGraph(
        val trainingDate: String = "",
        val value: Int = 0,
        val value2: Int = 0,
        val value3: Int = 0,
        val value4: Int = 0,
    )
        : Parcelable {
        override fun toString(): String {
            return "StatisticsGraph( trainingDate=$trainingDate, value=$value,value2=$value2,value3=$value3)"
        }
    }

@Parcelize
    data class PainGraph(
        val userName: String = "",
        val trainingDate: String = "",
        val value: Double = 0.0,
    )
        : Parcelable {
        override fun toString(): String {
            return "TrainingOverall(userName=$userName, trainingDate=$trainingDate, value=$value)"
        }
    }
}

data class ExerciseGraph(
    val exerciseName: String= "",
    val trainingDates: List<String> = mutableListOf(),
    val exerciseAchieveRate: List<Int>
)

data class ExerciseRecentAchievementGraph(
    val trainingUnitName: String = "",
    val trainingUnit: String = "",
    val trainingDates: List<String> = mutableListOf(),
    val achieveRates: List<Int>
)
