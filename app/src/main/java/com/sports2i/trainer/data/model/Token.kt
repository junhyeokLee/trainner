package com.sports2i.trainer.data.model
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Parcelize
data class TokenResponse(
    var accessToken: String = "",
    var refreshToken: String = "",
    var userId: String = "",
    var userName: String = "",
    var email: String = "",
    var organizationId: String = "",
    var groupId: String = "",
    var authority: String = "",
    var profileUrl: String = "",
    var bioActivated: Boolean = false,
) : Parcelable {
    override fun toString(): String {
        return "User(accessToken=$accessToken,refreshToken=$refreshToken,userId=$userId, email=$email, userName='$userName',groupId='$groupId', organizationId='$organizationId', authority='$authority', bioActivated='$bioActivated'"
    }
}

@Parcelize
data class TokenResponseData(
    val data: TokenResponse
) : Parcelable {
    override fun toString(): String {
        return "TokenResponse(data=$data)"
    }
}

@Parcelize
data class TokenRequest(
    val email: String = "",
    val password: String = "",
    val accessToken: String = "",
    val refreshToken: String = ""
) : Parcelable {
    override fun toString(): String {
        return "TorkenRequest(email='$email',password='$password',accessToken='$accessToken',refreshToken='$refreshToken')"
    }
}

