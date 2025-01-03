package com.sports2i.trainer.utils

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client

class NCPImageDelete(private val context: Context, private val fileName: String) : AsyncTask<Void, Void, Void>() {
    private val TAG = "DeleteImageTask"

    override fun doInBackground(vararg voids: Void): Void? {
        // AWS 계정 정보
        val endPoint = Global.endPoint
        val regionName = Global.regionName
        val accessKey = Global.accessKey
        val secretKey = Global.secretKey

        // S3 클라이언트 생성
        val s3 = AmazonS3Client(BasicAWSCredentials(accessKey, secretKey))
        s3.setEndpoint(endPoint)

        val bucketName = Global.bucketName + "/foods"

        // 파일 삭제
        try {
            s3.deleteObject(bucketName, fileName)
            Log.e(TAG, "파일 $fileName 삭제 성공.")
        } catch (e: AmazonServiceException) {
            Log.e(TAG, "AmazonServiceException: ${e.message}")
            e.printStackTrace()
        } catch (e: AmazonClientException) {
            Log.e(TAG, "AmazonClientException: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "파일 $fileName 삭제 오류: ${e.message}")
            e.printStackTrace()
        }


        return null
    }
}