package com.sports2i.trainer.data.model
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Parcelize
data class User(
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
data class LoginResponse(
    val data: User
) : Parcelable {
    override fun toString(): String {
        return "LoginResponse(data=$data)"
    }
}

@Parcelize
data class LoginRequest(
    val email: String = "",
    val password: String = "",
    val accessToken: String = "",
    val refreshToken: String = ""
) : Parcelable {
    override fun toString(): String {
        return "LoginRequest(email='$email',password='$password',accessToken='$accessToken',refreshToken='$refreshToken')"
    }
}
@Parcelize
data class EmailCheckResponse(
    val data: Boolean
) : Parcelable {
    override fun toString(): String {
        return "EmailCheckResponse(data=$data)"
    }
}

@Parcelize
data class VerifyRequest(
    val email: String = "",
    val verificationCode: String = "",
) : Parcelable {
    override fun toString(): String {
        return "verifyRequest(email='$email',verificationCode='$verificationCode')"
    }
}
@Parcelize
data class VerifyResponse(
    val data: Boolean = false
) : Parcelable {
    override fun toString(): String {
        return "VerifyResponse(data='$data')"
    }
}

@Parcelize
data class RequestUserResetPassword(
    val email: String = "",
    val password: String = "",
) : Parcelable {
    override fun toString(): String {
        return "RequestUserResetPassword(email='$email',password='$password')"
    }
}


@Parcelize
data class RequestProfileUpdate(
    val email: String = "",
    val password: String = "",
    val userName: String = "",
    val dateOfBirth: String = "",
    val authority: String = "",
    val gender: String = "",
    val groupId: String = "",
    val profileUrl: String = ""
) : Parcelable {
    override fun toString(): String {
        return "RequestProfileUpdate(email='$email',password='$password',userName='$userName',dateOfBirth='$dateOfBirth',authority='$authority',gender='$gender',groupId='$groupId',profileUrl='$profileUrl')"
    }
}

@Parcelize
data class UserResponse(
    val userId: String = "",
    val email: String = "",
    val userName: String = "",
    val dateOfBirth: String = "",
    val authority: String = "",
    val gender: String = "",
    val groupId: String = "",
    val profileUrl: String? = null,
    val bioActivated: Boolean = false,
): Parcelable {
    override fun toString(): String {
        return "UserResponse(userId='$userId',email='$email',userName='$userName',dateOfBirth='$dateOfBirth',authority='$authority',gender='$gender,groupId='$groupId',profileUrl='$profileUrl',bioActivated='$bioActivated')"
    }
}

@Parcelize
data class UserResponseData(
    val data: UserResponse
) : Parcelable {
    override fun toString(): String {
        return "UserResponse(data=$data)"
    }
}

