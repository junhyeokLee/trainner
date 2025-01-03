package com.sports2i.trainer.data.model
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Parcelize
data class TrainingSub(
    val exerciseId:String = "",
    val userId:String = "",
    val trainingTime: String = "",
    val trainingDate: String = "",
    val startDate: String = "",
    val endDate: String = ""
) : Parcelable {
    override fun toString(): String {
        return "TrainingSub(exerciseId=$exerciseId,userId=$userId,trainingTime=$trainingTime,trainingDate=$trainingDate,startDate=$startDate,endDate=$endDate)"
    }
    @Parcelize
    data class TrainingSubObejctResponse(
        val data: TrainingSub = TrainingSub()
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingSubObejctResponse(data=$data)"
        }
    }
}

@Parcelize
data class TrainingSubResponse(
    val exerciseId:String = "",
    val userId:String = "",
    val trainingTime: String = "",
    val trainingDate: String = "",
    var rpe: Int = 0,
    var tss: Int = 0,
    val startDate: String = "",
    val endDate: String = "",
    val url: String? = "",
    val start_longitude: Double = 0.0,
    val start_latitude: Double = 0.0,
    val end_longitude: Double = 0.0,
    val end_latitude: Double = 0.0,
    val weather: String? = "",
    val weatherDetail: String? = "",
    val temp: Double? = 0.0,
    val humidity: Double? = 0.0,
    val wind: Double? = 0.0,
    val healthData: HealthData = HealthData(),
) : Parcelable {
    override fun toString(): String {
        return "TrainingSubDetailResponse(exerciseId=$exerciseId,userId=$userId,trainingTime=$trainingTime,trainingDate=$trainingDate,rpe=$rpe,tss=$tss,startDate=$startDate,endDate=$endDate,url=$url,start_longitude=$start_longitude,start_latitude=$start_latitude,end_longitude=$end_longitude,end_latitude=$end_latitude,weather=$weather,weatherDetail=$weatherDetail,temp=$temp,humidity=$humidity,wind=$wind,healthData=$healthData)"
    }

    @Parcelize
    data class TrainingSubDetailResponse(
        val data: TrainingSubResponse = TrainingSubResponse()
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingSubResponse(data=$data)"
        }
    }

    @Parcelize
    data class HealthData(
        val energy_total: Double? = 0.0,
        val speed_avg: Double? = 0.0,
        val speed_max: Double? = 0.0,
        val speed_min: Double? = 0.0,
        val distance_total: Double? = 0.0,
        val bpm_avg: Double? = 0.0,
        val bpm_max: Double? = 0.0,
        val bpm_min: Double? = 0.0,
    ) : Parcelable {
        override fun toString(): String {
            return "HealthData(energy_total=$energy_total,speed_avg=$speed_avg,speed_max=$speed_max,speed_min=$speed_min,distance_total=$distance_total,bpm_avg=$bpm_avg,bpm_max=$bpm_max,bpm_min=$bpm_min)"
        }

        fun isNull(): Boolean {
            return energy_total == 0.0 &&
                    speed_avg == 0.0 &&
                    speed_max == 0.0 &&
                    speed_min == 0.0 &&
                    distance_total == 0.0 &&
                    bpm_avg == 0.0 &&
                    bpm_max == 0.0 &&
                    bpm_min == 0.0
        }

    }

}


@Parcelize
data class TrainingSubDetailRpeRequest(
    val exerciseId: String? = null,
    val userId: String? = null,
    val trainingTime: String? = null,
    val trainingDate: String? = null,
    val rpe: Int? = null,
) :Parcelable {
    override fun toString(): String {
        return "TrainingSubDetailRequest(exerciseId=$exerciseId,userId=$userId,trainingTime=$trainingTime,trainingDate=$trainingDate,rpe=$rpe)"
    }
}
@Parcelize
data class TrainingSubDetailUrlRequest(
    val exerciseId: String? = null,
    val userId: String? = null,
    val trainingTime: String? = null,
    val trainingDate: String? = null,
    val url: String? = null,

) : Parcelable {
    override fun toString(): String {
        return "TrainingSubDetailUrlRequest(exerciseId=$exerciseId,userId=$userId,trainingTime=$trainingTime,trainingDate=$trainingDate,url=$url)"
    }
}

@Parcelize
data class TrainingSubDetailHealthRequest(
    val exerciseId: String? = null,
    val userId: String? = null,
    val trainingTime: String? = null,
    val trainingDate: String? = null,
    val energy_total: Double? = null,
    val speed_avg: Double? = null,
    val speed_max: Double? = null,
    val speed_min: Double? = null,
    val distance_total: Double? = null,
    val bpm_avg: Double? = null,
    val bpm_max: Double? = null,
    val bpm_min: Double? = null
) : Parcelable {
    override fun toString(): String {
        return "TrainingSubDetailHealthRequest(exerciseId=$exerciseId,userId=$userId,trainingTime=$trainingTime,trainingDate=$trainingDate,energy_total=$energy_total,speed_avg=$speed_avg,speed_max=$speed_max,speed_min=$speed_min,distance_total=$distance_total,bpm_avg=$bpm_avg,bpm_max=$bpm_max,bpm_min=$bpm_min)"
    }
}

@Parcelize
data class TrainingSubDetailInsert(
    val exerciseId:String? = "",
    val userId:String? = "",
    val trainingTime: String? = "",
    val trainingDate: String? = "",
    val startDate: String? = "",
    val endDate: String? = "",
    val url: String? = "",
    val start_longitude: Double = 0.0,
    val start_latitude: Double = 0.0,
    val end_longitude: Double = 0.0,
    val end_latitude: Double = 0.0,
    val weather: String? = "",
    val weatherDetail: String? = "",
    val temp: Double? = 0.0,
    val humidity: Double? = 0.0,
    val wind: Double? = 0.0
) : Parcelable {
    override fun toString(): String {
        return "TrainingSubDetailInsert(exerciseId=$exerciseId,userId=$userId,trainingTime=$trainingTime,trainingDate=$trainingDate,startDate=$startDate,endDate=$endDate,url=$url,start_longitude=$start_longitude,start_latitude=$start_latitude,end_longitude=$end_longitude,end_latitude=$end_latitude,weather=$weather,weatherDetail=$weatherDetail,temp=$temp,humidity=$humidity,wind=$wind)"
    }
}

@Parcelize
data class TrainingTssDataTime(
    val all:Int? = 0,
    val T1:Int? = 0,
    val T2:Int? = 0,
    val T3:Int? = 0,
    val T4:Int? = 0,
    val T5:Int? = 0
) : Parcelable {
    override fun toString(): String {
        return "TrainingTssDataTime(all=$all,T1=$T1,T2=$T2,T3=$T3,T4=$T4,T5=$T5)"
    }

    @Parcelize
    data class TrainingTssDataTimeResponse(
        val data : TrainingTssDataTime? = TrainingTssDataTime()
    ) : Parcelable {
        override fun toString(): String {
            return "TrainingTssDataTimeResponse(data = $data)"
        }
    }
}