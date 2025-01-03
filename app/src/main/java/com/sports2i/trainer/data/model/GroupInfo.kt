package com.sports2i.trainer.data.model
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Parcelize
data class GroupInfo(
    var groupId: String = "",
    var groupNameShort: String?,
    var groupNameLong: String?,
    var capacity: Int = 0,
) : Parcelable {
    override fun toString(): String {
        return "GroupInfo(groupId=$groupId,groupNameShort=$groupNameShort, groupNameLong=$groupNameLong, capacity='$capacity')"
    }
}

@Parcelize
data class GroupSearchResponse(
    val data: MutableList<GroupInfo>
) : Parcelable {
    override fun toString(): String {
        return "GroupInfo(data=$data)"
    }
}

@Parcelize
data class GroupSelectedSearchResponse(
    val data: GroupInfo
) : Parcelable {
    override fun toString(): String {
        return "GroupInfo(data=$data)"
    }
}

