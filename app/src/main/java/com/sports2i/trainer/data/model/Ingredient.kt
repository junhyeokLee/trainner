package com.sports2i.trainer.data.model
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Parcelize
data class Ingredient(
    val drugId: DrugId,
    val itemName:String = "",
    var companyName: String = "",
    var permitDate: String = "",
    val mainIngredient: String = "",
    val additive: String = "",
    val itemClassification: String = "",
    val effect: String = "",
    val direction: String = "",
    val caution: String = "",
    val createdDate: String = ""
    ) : Parcelable {
    override fun toString(): String {
        return "Ingredient(drugId=$drugId,itemName=$itemName,companyName=$companyName,permitDate=$permitDate,mainIngredient=$mainIngredient,additive=$additive,itemClassification=$itemClassification,effect=$effect,direction=$direction,caution=$caution,createdDate=$createdDate)"
    }

    @Parcelize
    data class DrugId(
        val category: String = "",
        val itemSeq: Int = 0,
        val version: String = ""
    ) : Parcelable {
        override fun toString(): String {
            return "DrugId(category=$category,itemSeq=$itemSeq,version=$version)"
        }
    }
}

@Parcelize
data class IngredientListResponse(
    val data: MutableList<Ingredient> = mutableListOf(),
) : Parcelable {
    override fun toString(): String {
        return "IngredientListResponse(data=$data)"
    }
}

@Parcelize
data class IngredientResponse(
    val data: Ingredient?= null,
) : Parcelable {
    override fun toString(): String {
        return "IngredientListResponse(data=$data)"
    }
}








