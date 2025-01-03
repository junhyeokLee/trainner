package com.sports2i.trainer.data.model
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Parcelize
data class Nutrition(
    val userId: String = "",
    val userName: String = "",
    val nutritionList: MutableList<NutritionPicture> = mutableListOf()
) : Parcelable {
    override fun toString(): String {
        return "Nutrition(userId='$userId', userName='$userName', nutritionList=$nutritionList)"
    }

    @Parcelize
    data class NutritionPicture(
        val nutritionId: Int = 0,
        val pictureUrl: String = "",
        val reportingDate: String = "",
        val evaluation: Int = 0
    ) : Parcelable {
        override fun toString(): String {
            return "NutritionPicture(nutritionId='$nutritionId', pictureUrl='$pictureUrl', pictureUrl='$pictureUrl', reportingDate='$reportingDate', evaluation='$evaluation')"
        }
    }
}

@Parcelize
data class NutritionResponse(
    val data: MutableList<Nutrition> = mutableListOf()
    ) : Parcelable {
    override fun toString(): String {
        return "NutritionResponse(data=$data)"
    }
}


@Parcelize
data class NutritionPictureUser(
    val nutritionId: Int = 0,
    val userId: String = "",
    val userName: String = "",
    val pictureUrl: String = "",
    val reportingDate: String = "",
    var evaluation: Int = 0
) : Parcelable {
    override fun toString(): String {
        return "NutritionPictureUser(nutritionId='$nutritionId', userId='$userId', userName='$userName', pictureUrl='$pictureUrl', reportingDate='$reportingDate', evaluation='$evaluation')"
    }
}

@Parcelize
data class NutritionPictureUserResponse(
    val data: MutableList<NutritionPictureUser> = mutableListOf()
) : Parcelable {
    override fun toString(): String {
        return "NutritionPictureUserResponse(data=$data)"
    }
}

@Parcelize
data class NutritionDirection(
    val userIdList : MutableList<String> = mutableListOf(),
    val keywordList : MutableList<NutritionDirectionKeyword> = mutableListOf(),
    val content : String = "",
    val startDate : String = "",
    val endDate : String = ""
) : Parcelable {
    override fun toString(): String {
        return "NutritionDirection(userIdList=$userIdList, keywordList=$keywordList, content='$content', startDate='$startDate', endDate='$endDate')"
    }
}


@Parcelize
data class NutritionDirectionKeyword(
    val item: String = "",
    val status: String = ""
) : Parcelable {
    override fun toString(): String {
        return "NutritionDirectionKeyword(item='$item', status='$status')"
    }
}

@Parcelize
data class NutritionDirectionResponse(
    val data: MutableList<NutritionDirection> = mutableListOf()
) : Parcelable {
    override fun toString(): String {
        return "NutritionDirectionResponse(data=$data)"
    }
}



@Parcelize
data class NutritionDirectionSearch(
    val directionId: DirectionId = DirectionId(),
    val writer: String = "",
    val keywordList: MutableList<NutritionDirectionKeyword> = mutableListOf(),
    val content: String = ""
) : Parcelable {
    override fun toString(): String {
        return "NutritionDirectionSearch(directionId=$directionId, writer='$writer', keywordList=$keywordList, content='$content')"
    }

    @Parcelize
    data class DirectionId(
        val userId: String = "",
        val directionDate: String = ""
    ) : Parcelable {
        override fun toString(): String {
            return "DirectionId(userId='$userId', directionDate='$directionDate')"
        }
    }
}

@Parcelize
data class NutritionDirectionSearchResponse(
    val data: DirectionData = DirectionData()
) : Parcelable {
    override fun toString(): String {
        return "NutritionDirectionSearchResponse(data=$data)"
    }

    @Parcelize
    data class DirectionData(
        val directionId: NutritionDirectionSearch.DirectionId = NutritionDirectionSearch.DirectionId(),
        val writer: String = "",
        val keywordList: MutableList<NutritionDirectionKeyword> = mutableListOf(),
        val content: String = ""
    ) : Parcelable {
        override fun toString(): String {
            return "DirectionData(directionId=$directionId, writer='$writer', keywordList=$keywordList, content='$content')"
        }
    }
}