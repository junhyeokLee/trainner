package com.sports2i.trainer.data.model
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Parcelize
data class Notice(
    var id: Int = 0,
    var groupId: String? = "",
    var title: String? = "",
    var contents: String? = "",
    var writer: String? = "",
    var writerName: String? = "",
    var createdDate: String? = "",
    var modifiedDate: String? = "",
    var numOfComments: Int? = 0,
) : Parcelable {
    override fun toString(): String {
        return "Notice(id=$id,groupId=$groupId, title=$title, contents='$contents', writer='$writer', writerName='$writerName'," +
                " createdDate='$createdDate', modifiedDate='$modifiedDate', numOfComments=$numOfComments)"
    }
}

@Parcelize
data class NoticeListResponse(
    var data: MutableList<Notice> = mutableListOf()
) : Parcelable {
    override fun toString(): String {
        return "NoticeList(data=$data)"
    }
}

@Parcelize
data class NoticeResponse(
    val data: NoticeListResponse = NoticeListResponse()
    ) : Parcelable {
    override fun toString(): String {
        return "NoticeResponse(data=$data)"
    }

    @Parcelize
    data class NoticeListResponse(
        var totalPages: Int = 0,
        var noticeList: MutableList<Notice> = mutableListOf()
    ) : Parcelable {
        override fun toString(): String {
            return "NoticeList(totalPages=$totalPages,noticeList=$noticeList)"
        }
    }
}


@Parcelize
data class NoticeInsert(
    val groupId: String = "",
    val title: String = "",
    val contents: String = ""
) : Parcelable {
    override fun toString(): String {
        return "NoticeInsert(groupId=$groupId, title=$title, contents='$contents')"
    }
}

@Parcelize
data class NoticeInertResponse(
    var data: Notice = Notice()
) : Parcelable {
    override fun toString(): String {
        return "NoticeList(data=$data)"
    }
}
