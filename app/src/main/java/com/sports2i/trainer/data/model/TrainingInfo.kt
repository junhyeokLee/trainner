package com.sports2i.trainer.data.model
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */

@Parcelize
data class TrainingInfo(
    val organizationId:String = "",
    val groupId:String = "",
    var userId: String = "",
    var userName: String = "",
    val trainingDate: String = "",
    val trainingStartDate: String = "",
    val trainingEndDate: String = "",
    val trainingTime: String = "",
    val exerciseList: MutableList<ExerciseList> = mutableListOf(),
) : Parcelable {
    override fun toString(): String {
        return "TrainingInfo(organizationId=$organizationId,groupId=$groupId,userId=$userId,userName=$userName,trainingDate=$trainingDate,trainingStartDate=$trainingStartDate,trainingEndDate=$trainingEndDate,trainingTime=$trainingTime ,exerciseList=$exerciseList)"
    }

    @Parcelize
    data class ExerciseList(
        val exerciseId: String = "",
        val exerciseName: String = "",
        val exerciseUnitId: String = "",
        val exerciseUnit: String = "",
        val exerciseUnitName: String = "",
        val goalValue: Double = 0.0,
        val measuredValue: Double = 0.0,
        val achieveRate: Double = 0.0,
        val totalAchieveRate: Double = 0.0
    ) : Parcelable {
        override fun toString(): String {
            return "ExcerciseList(exerciseId=$exerciseId,exerciseName=$exerciseName,exerciseUnitId=$exerciseUnitId,exerciseUnit=$exerciseUnit,exerciseUnitName=$exerciseUnitName,goalValue=$goalValue,measuredValue=$measuredValue,achieveRate=$achieveRate,totalAchieveRate=$totalAchieveRate)"
        }
    }
}

@Parcelize
data class TrainingInfoResponse(
    val organizationId:String = "",
    val groupId:String = "",
    val trainingId:String? = "",
    val userId:String? = "",
    var trainingDate: String? = "",
    var trainingTime: String? = "",
    val exerciseId: String? = "",
    val exerciseName: String? = "",
    val exerciseUnitId: String? = "",
    val exerciseUnitName: String? = "",
    val exerciseUnit: String? = "",
    val goalValue: Double? = 0.0,
    var measuredValue: Double? = 0.0,
    var achieveRate: Double? = 0.0,
    val exerciseAchieveRate: Double? = 0.0,
    val totalAchieveRate: Double? = 0.0
) : Parcelable {
    override fun toString(): String {
        return "TrainingInfoResponse(organizationId=$organizationId,groupId=$groupId,trainingId=$trainingId,userId=$userId,trainingDate=$trainingDate,trainingTime=$trainingTime,exerciseId=$exerciseId," +
                "exerciseName=$exerciseName,exerciseUnitId=$exerciseUnitId,exerciseUnit=$exerciseUnit,exerciseUnitName=$exerciseUnitName,goalValue=$goalValue,measuredValue=$measuredValue,achieveRate=$achieveRate,totalAchieveRate=$totalAchieveRate)"
    }

    @Parcelize
    data class TrainingInfoResponses(
        val data: MutableList<TrainingInfoResponse> = mutableListOf(),
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingInfoResponse(data=$data)"
        }
    }

    @Parcelize
    data class TrainingInfoUpdateResponse(
        val data: TrainingInfoResponse ?= null,
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingInfoUpdateResponse(data=$data)"
        }
    }
}

@Parcelize
data class ExercisePreset(
    val organizationId:String = "",
    val exercisePresetId:String? = null,
    var exercisePresetName:String? = null,
    val trainingTime:String = "",
    var exerciseList: MutableList<TrainingInfo.ExerciseList> = mutableListOf()
) : Parcelable {
    override fun toString(): String {
        return "ExercisePreset(organizationId=$organizationId,exercisePresetId=$exercisePresetId,exercisePresetName=$exercisePresetName,trainingTime=$trainingTime,exerciseList=$exerciseList)"
    }

 @Parcelize
    data class ExercisePresetResponse(
        val data: MutableList<ExercisePreset> = mutableListOf(),
        val createdTime: String = "",
    ) : Parcelable {
        override fun toString(): String {
            return "ExercisePresetResponse(data=$data)"
        }
    }
}

     @Parcelize
    data class TrainingOverallSearchResponse(
        val data: MutableList<TrainingOverallSearch> = mutableListOf(),
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingOverallSearchResponse(data=$data)"
        }
    }

    @Parcelize
    data class TrainingOverallSearch(
        val userId:String = "",
        val userName:String = "",
        val trainingOverallData: TrainingOverallData? = null,
        val exerciseList: MutableList<TrainingGroupStatus.TrainingGroupStatusExercise> = mutableListOf(),
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingOverallSerach(userId=$userId,userName=$userName,trainingOverallData=$trainingOverallData,exerciseList=$exerciseList)"
        }

    @Parcelize
    data class TrainingOverallData(
        val id:Int = 0,
        val trainingId:String = "",
        val organizationId:String = "",
        val groupId:String = "",
        val userId:String = "",
        val userName:String = "",
        val trainingDate:String = "",
        val performanceIndex:Double? = 0.0,
        val injuryIndex:Double? = 0.0,
        val sleepIndex:Double? = 0.0,
        val sleepDuration:Double? = 0.0,
        val nutritionIndex:Double? = 0.0,
        val performanceWeekAvg:Double? = 0.0,
        val performanceMonthAvg:Double? = 0.0,
        val injuryWeekAvg:Double? = 0.0,
        val injuryMonthAvg:Double? = 0.0,
        val sleepWeekAvg:Double? = 0.0,
        val sleepMonthAvg:Double? = 0.0,
        val sleepDurationWeekAvg:Double? = 0.0,
        val sleepDurationMonthAvg:Double? = 0.0,
        val nutritionWeekAvg:Double? = 0.0,
        val nutritionMonthAvg:Double? = 0.0
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingOverall(trainingId=$trainingId,organizationId=$organizationId,groupId=$groupId,userId=$userId,userName=$userName,trainingDate=$trainingDate,performanceIndex=$performanceIndex,injuryIndex=$injuryIndex,sleepIndex=$sleepIndex,sleepDuration=$sleepDuration,nutritionIndex=$nutritionIndex,performanceWeekAvg=$performanceWeekAvg,performanceMonthAvg=$performanceMonthAvg,injuryWeekAvg=$injuryWeekAvg,injuryMonthAvg=$injuryMonthAvg,sleepWeekAvg=$sleepWeekAvg,sleepMonthAvg=$sleepMonthAvg,sleepDurationWeekAvg=$sleepDurationWeekAvg,sleepDurationMonthAvg=$sleepDurationMonthAvg,nutritionWeekAvg=$nutritionWeekAvg,nutritionMonthAvg=$nutritionMonthAvg)"
        }
    }
}
@Parcelize
data class TrainingOverall(
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
        return "TrainingOverall(trainingId=$trainingId,organizationId=$organizationId,groupId=$groupId,userId=$userId,userName=$userName,trainingDate=$trainingDate,performanceIndex=$performanceIndex,injuryIndex=$injuryIndex,sleepIndex=$sleepIndex,sleepDuration=$sleepDuration,nutritionIndex=$nutritionIndex,performanceWeekAvg=$performanceWeekAvg,performanceMonthAvg=$performanceMonthAvg,injuryWeekAvg=$injuryWeekAvg,injuryMonthAvg=$injuryMonthAvg,sleepWeekAvg=$sleepWeekAvg,sleepMonthAvg=$sleepMonthAvg,sleepDurationWeekAvg=$sleepDurationWeekAvg,sleepDurationMonthAvg=$sleepDurationMonthAvg,nutritionWeekAvg=$nutritionWeekAvg,nutritionMonthAvg=$nutritionMonthAvg)"
    }

    @Parcelize
    data class TrainingOverallResponse(
        val data: MutableList<TrainingOverall> = mutableListOf(),
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingOverallResponse(data=$data)"
        }
    }
}

@Parcelize
data class TrainingGroupStatus(
    val userId: String = "",
    val userName: String = "",
    val exerciseList: MutableList<TrainingGroupStatusExercise> = mutableListOf(),
    ) : Parcelable {
    override fun toString(): String {
        return "TrainingGroupStatus(userId=$userId,userName=$userName,exerciseList=$exerciseList)"
    }


    @Parcelize
    data class TrainingGroupStatusExercise(
        val id: Int = 0,
        val trainingId: String? = null,
        val userId: String? = null,
        val trainingDate: String? = null,
        val trainingTime: String? = null,
        val exerciseId: String? = null,
        val exerciseName: String? = null,
        val exerciseUnitId: String? = null,
        val exerciseUnitName: String? = null,
        val exerciseUnit:String? = null,
        val goalValue: Double = 0.0,
        val measuredValue: Double = 0.0,
        val achieveRate: Double = 0.0,
        val totalAchieveRate: Double = 0.0,

    ) : Parcelable {
        override fun toString(): String {
            return "TrainingGroupStatusExercise(id=$id,trainingId=$trainingId,userId=$userId,trainingDate=$trainingDate,trainingTime=$trainingTime,exerciseId=$exerciseId,exerciseName=$exerciseName,exerciseUnitId=$exerciseUnitId,exerciseUnitName=$exerciseUnitName,exerciseUnit=$exerciseUnit,goalValue=$goalValue,measuredValue=$measuredValue,achieveRate=$achieveRate,totalAchieveRate=$totalAchieveRate)"
        }
    }
    @Parcelize
    data class TrainingGroupStatusResponse(
        val data: MutableList<TrainingGroupStatus> = mutableListOf(),
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingGroupStatusResponse(data=$data)"
        }
    }
}

@Parcelize
data class DeleteTrainingGroupStatus(
    val userId: String? = null,
    val trainingDate: String? = null,
    val trainingTime: String? = null,
    val exerciseList: MutableList<DeleteTrainingGroupStatusExercise> = mutableListOf(),
): Parcelable {
    override fun toString(): String {
        return "DeleteTrainingGroupStatus(userId=$userId,trainingDate=$trainingDate,trainingTime=$trainingTime,exerciseList=$exerciseList)"
    }
    @Parcelize
    data class DeleteTrainingGroupStatusExercise(
        val exerciseId: String? = null,
        val exerciseUnitId: String? = null,
    ) : Parcelable {
        override fun toString(): String {
            return "DeleteTrainingGroupStatusExercise(exerciseId=$exerciseId,exerciseUnitId=$exerciseUnitId)"
        }
    }
}

@Parcelize
data class TrainingExercise(
    val userId:String = "",
    val trainingTime:String = "",
    var exerciseList: MutableList<TrainingInfo.ExerciseList> = mutableListOf()
) : Parcelable {
    override fun toString(): String {
        return "TrainingExercise(userId=$userId,trainingTime=$trainingTime,exerciseList=$exerciseList)"
    }

    @Parcelize
    data class TrainingExerciseResponse(
        val data: MutableList<TrainingExercise> = mutableListOf(),
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingExerciseResponse(data=$data)"
        }
    }
}

@Parcelize
data class TrainingCommentRequest(
    val userId:String = "",
    val contents:String = "",
    val targetDate:String = "",
) : Parcelable {
    override fun toString(): String {
        return "TrainingCommentRequest(userId=$userId,contents=$contents,targetDate=$targetDate)"
    }
}

    @Parcelize
    data class TrainingComment(
        val id:String = "",
        val contents:String = "",
        val writer:String = "",
        val writerName:String = "",
        val targetDate:String = "",
        val createdDate:String = ""
    ) : Parcelable {
        override fun toString(): String {
                return "TrainingComment(id=$id,contents=$contents,writer=$writer,writerName=$writerName,targetDate=$targetDate,createdDate=$createdDate)"
        }


    @Parcelize
    data class TrainingCommentResponse(
        val data: MutableList<TrainingComment> = mutableListOf(),
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingCommentResponse(data=$data)"
        }
    }
}

    @Parcelize
    data class TrainingConfirm(
        val goal: MutableList<TrainingExercise> = mutableListOf(),
        val trainingoverall : MutableList<TrainingOverall> = mutableListOf(),
        val training : MutableList<TrainingInfoResponse> = mutableListOf()) : Parcelable{
        override fun toString(): String {
            return "TrainingConfirm(goal=$goal,trainingoverall=$trainingoverall,training=$training)"
        }
    }

    @Parcelize
    data class TrainingConfirmResponse(
        val data: MutableList<TrainingConfirm> = mutableListOf(),
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingConfirmRequest(data=$data)"
        }
    }
