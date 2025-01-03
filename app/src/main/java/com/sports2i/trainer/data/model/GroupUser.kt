package com.sports2i.trainer.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GroupUser(
    var userId: String = "",
    var email: String = "",
    var userName: String = "",
    var dateOfBirth: String = "",
    var authority: String = "",
    var gender: String = "",
    var groupId: String = "",
    var profileUrl: String = "",
    var bioActivated: Boolean = false,
): Parcelable {
    override fun toString(): String {
        return "GroupUser(userId=$userId, email=$email, userName='$userName', dateOfBirth='$dateOfBirth', authority='$authority', gender='$gender', groupId='$groupId', bioActivated='$bioActivated)'"
    }
}

@Parcelize
data class GroupUserResponse(
    val data: MutableList<GroupUser>
) : Parcelable {
    override fun toString(): String {
        return "GroupUserResponse(data=$data)"
    }
}