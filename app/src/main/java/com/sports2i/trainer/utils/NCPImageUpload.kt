package com.sports2i.trainer.utils

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import java.io.File

class NCPImageUpload(private val context: Context, private val imagePath: String,private val storagePath:String) : AsyncTask<Void, Void, Void>() {
    private val TAG = "UploadImageTask"

    override fun doInBackground(vararg voids: Void): Void? {
        // AWS 계정 정보
        val endPoint = Global.endPoint
        val regionName = Global.regionName
        val accessKey = Global.accessKey
        val secretKey = Global.secretKey

        // S3 클라이언트 생성
        val s3 = AmazonS3Client(BasicAWSCredentials(accessKey, secretKey))
        s3.setEndpoint(endPoint)

        val bucketName = Global.bucketName+"/"+storagePath

        val transferUtility = TransferUtility.builder()
            .s3Client(s3)
            .context(context)
            .build()

        TransferNetworkLossHandler.getInstance(context)

        val file = File(imagePath)
        // 파일 업로드
        val uploadObserver: TransferObserver = transferUtility.upload(
            bucketName,
            file.name,  // 업로드될 파일 이름
            file,
            CannedAccessControlList.PublicRead  // 업로드된 파일의 ACL(Access Control List)
        )

        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
            }

            override fun onError(id: Int, ex: Exception?) {
            }
        })

        return null
    }


}
