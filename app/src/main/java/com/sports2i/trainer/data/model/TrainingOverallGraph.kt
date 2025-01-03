package com.sports2i.trainer.data.model

import android.os.Parcelable
import android.view.View
import com.sports2i.trainer.ui.widget.GraphTrainingOverall
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TrainingOverallGraphItem(
    val trainingId:String = "",
    val organizationId:String = "",
    val groupId:String = "",
    val userId:String = "",
    val userName:String = "",
    val trainingDate:String = "",
    val performanceIndex:Double? = null,
    val injuryIndex:Double? = null,
    val sleepIndex:Double? = null,
    val sleepDuration:Double? = null,
    val nutritionIndex:Double? = null,
    val performanceWeekAvg:Double? = null,
    val performanceMonthAvg:Double? = null,
    val injuryWeekAvg:Double? = null,
    val injuryMonthAvg:Double? = null,
    val sleepWeekAvg:Double? = null,
    val sleepMonthAvg:Double? = null,
    val sleepDurationWeekAvg:Double? = null,
    val sleepDurationMonthAvg:Double? = null,
    val nutritionWeekAvg:Double? = null,
    val nutritionMonthAvg:Double? = null
) : Parcelable {
    override fun toString(): String {
        return "TrainingOverallGraphItem(trainingId=$trainingId,organizationId=$organizationId,groupId=$groupId,userId=$userId,userName=$userName,trainingDate=$trainingDate,performanceIndex=$performanceIndex,injuryIndex=$injuryIndex,sleepIndex=$sleepIndex,sleepDuration=$sleepDuration,nutritionIndex=$nutritionIndex," +
                "performanceWeekAvg=$performanceWeekAvg,performanceMonthAvg=$performanceMonthAvg,injuryWeekAvg=$injuryWeekAvg,injuryMonthAvg=$injuryMonthAvg,sleepWeekAvg=$sleepWeekAvg,sleepMonthAvg=$sleepMonthAvg,sleepDurationWeekAvg=$sleepDurationWeekAvg,sleepDurationMonthAvg=$sleepDurationMonthAvg,nutritionWeekAvg=$nutritionWeekAvg,nutritionMonthAvg=$nutritionMonthAvg)"
    }

    @Parcelize
    data class TrainingOverallGraphDate(
        val day: MutableList<TrainingOverallGraphItem>,
        val week: MutableList<TrainingOverallGraphItem>,
        val month: MutableList<TrainingOverallGraphItem>
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingOverallGraphDate(day=$day,week=$week,month=$month)"
        }
    }


    @Parcelize
    data class TrainingOverallGraph(
        val trainingOverallItemId: Int? = null,
        val trainingOverallItemName: String = "",
        val trainingOverallData: TrainingOverallGraphDate
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingOverallGraph(trainingOverallData=$trainingOverallData)"
        }
    }


    @Parcelize
    data class TrainingOverallGraphResponse(
        val data: MutableList<TrainingOverallGraph>
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingOverallGraphResponse(data=$data)"
        }
    }
}


@Parcelize
data class SurveyOverallGraphItem(
    val userId:String = "",
    val userName:String = "",
    val surveyDate:String = "",
    val surveyItemId:String = "",
    val surveyItemName:String = "",
    val surveyValueAvg:Double? = null
) : Parcelable {
    override fun toString(): String {
        return "SurveyOverallGraphItem(userId=$userId,userName=$userName,surveyItemId=$surveyItemId,surveyItemName=$surveyItemName,surveyValueAvg=$surveyValueAvg)"
    }

    @Parcelize
    data class SurveyOverallGraphDate(
        val day: MutableList<SurveyOverallGraphItem> = mutableListOf(),
        val week: MutableList<SurveyOverallGraphItem> = mutableListOf(),
        val month: MutableList<SurveyOverallGraphItem> = mutableListOf()
    ) : Parcelable {
        override fun toString(): String {
            return "SurveyOverallGraphDate(day=$day,week=$week,month=$month)"
        }
    }


    @Parcelize
    data class SurveyOverallGraph(
        val surveyItemId: String = "",
        val surveyItemName: String = "",
        val surveyData: SurveyOverallGraphDate
    ) : Parcelable {
        override fun toString(): String {
            return "SurveyOverallGraph(surveyData=$surveyData)"
        }
    }


    @Parcelize
    data class SurveyOverallGraphResponse(
        val data: MutableList<SurveyOverallGraph>
    ) : Parcelable {
        override fun toString(): String {
            return "SurveyOverallGraphResponse(data=$data)"
        }
    }
}


@Parcelize
data class TrainingOverallSample(
    val userName: String = "",
    val trainingDate: String = "",
    val injuryIndex: Float = 0F,
    val sleepIndex: Float = 0F,
    val sleepDuration: Float = 0F): Parcelable {
    override fun toString(): String {
        return "TrainingOverall(userName=$userName, trainingDate=$trainingDate, injuryIndex=$injuryIndex, sleepIndex=$sleepIndex, sleepDuration=$sleepDuration)"
    }
}

@Parcelize
data class TrainingSubStatisticsGraphItem(
    val trainingDate:String = "",
    val tss:Int = 0,
    val tss7Avg:Int = 0,
    val tss28Avg:Int = 0
) : Parcelable {
    override fun toString(): String {
        return "TrainingSubStatisticsGraph(trainingDate=$trainingDate, tss=$tss, tss7Avg=$tss7Avg, tss28Avg=$tss28Avg)"
    }

    @Parcelize
    data class TrainingSubStatisticsGraph(
        val week: MutableList<TrainingSubStatisticsGraphItem> = mutableListOf(),
        val month: MutableList<TrainingSubStatisticsGraphItem> = mutableListOf()
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingSubStatisticsGraph(week=$week,month=$month)"
        }
    }

    @Parcelize
    data class TrainingSubStatisticsGraphResponse(
        val data: TrainingSubStatisticsGraph
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingSubStatisticsGraphResponse(data=$data)"
        }
    }
}




