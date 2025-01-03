package com.sports2i.trainer.utils

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import java.io.ByteArrayOutputStream
//import com.amazonaws.SdkClientException
//import com.amazonaws.auth.AWSStaticCredentialsProvider
//import com.amazonaws.auth.BasicAWSCredentials
//import com.amazonaws.client.builder.AwsClientBuilder
//import com.amazonaws.services.s3.AmazonS3
//import com.amazonaws.services.s3.AmazonS3ClientBuilder
//import com.amazonaws.services.s3.model.AmazonS3Exception
//import com.amazonaws.services.s3.model.ListObjectsRequest
//import com.amazonaws.services.s3.model.ObjectListing
//import com.amazonaws.services.s3.model.ObjectMetadata
//import com.amazonaws.services.s3.model.PutObjectRequest
//import com.sports2i.trainer.utils.Global.bucketName
//import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object FileUtil {

    private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap, maxWidth: Int = 1200, maxHeight: Int = 1200): Uri? {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir("images", AppCompatActivity.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val scaledBitmap = scaleBitmap(bitmap, maxWidth, maxHeight)
            val stream: OutputStream = FileOutputStream(file)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

//    fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap, maxWidth: Int = 800, maxHeight: Int = 800): Uri? {
//        val wrapper = ContextWrapper(context)
//        var file = wrapper.getDir("images", AppCompatActivity.MODE_PRIVATE)
//        file = File(file, "${UUID.randomUUID()}.jpg")
//
//        try {
//            val uri = Uri.fromFile(file)
//            val scaledBitmap = scaleBitmap(bitmap, maxWidth, maxHeight)
//            val orientation = getOrientation(context, uri)
//            val rotatedBitmap = rotateBitmap(orientation, scaledBitmap)
//
//            val stream: OutputStream = FileOutputStream(file)
//            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
//            stream.flush()
//            stream.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//        return Uri.fromFile(file)
//    }


    fun compressAndSaveImage(context: Context, uri: Uri, maxWidth: Int = 1200, maxHeight: Int = 1200): Uri {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        val orientation = getOrientation(context, uri)
        val rotatedBitmap = rotateBitmap(orientation, bitmap)
        val compressedBitmap = scaleBitmap(rotatedBitmap, maxWidth, maxHeight)
        return saveBitmapAndGetUri(context, compressedBitmap)!!
    }

    private fun getOrientation(context: Context, uri: Uri): Int {
        val inputStream = context.contentResolver.openInputStream(uri)
        inputStream?.use { stream ->
            val exif = ExifInterface(stream)
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }
        return ExifInterface.ORIENTATION_NORMAL
    }



    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        var newWidth = originalWidth
        var newHeight = originalHeight

        // 이미지 크기가 제한값을 초과하면 조정
        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()

            if (originalWidth > originalHeight) {
                newWidth = maxWidth
                newHeight = (newWidth / aspectRatio).toInt()
            } else {
                newHeight = maxHeight
                newWidth = (newHeight * aspectRatio).toInt()
            }
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }


    fun rotateBitmap(orientation: Int, bitmap: Bitmap): Bitmap {
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }



    private fun compressBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        val ratio = originalWidth.toFloat() / originalHeight.toFloat()

        val width = if (originalWidth > originalHeight) maxWidth
        else (maxHeight * ratio).toInt()

        val height = if (originalHeight > originalWidth) maxHeight
        else (maxWidth / ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }


    fun extractFileName(url: String): String {
        // URL에서 마지막 "/" 이후의 부분을 추출
        val startIndex = url.lastIndexOf("/") + 1
        return url.substring(startIndex)
    }



    // 비디오 압축
    fun compressVideo(inputPath: String, outputPath: String): String? {
        return try {
            val videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(inputPath)

            // 선택된 비디오 트랙의 형식을 가져옴
            val format = selectVideoTrack(videoExtractor)

            val decoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
            decoder.configure(format, null, null, 0)
            decoder.start()

            val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val videoBufferInfo = MediaCodec.BufferInfo()

            // 비디오 트랙 추가
            val videoTrackIndex = muxer.addTrack(format)

            muxer.start()

            // 비디오 프레임 압축
            val duration = format.getLong(MediaFormat.KEY_DURATION)
            val compressBitRate = 1024 * 1024 // Set your desired bitrate here
            compressVideoFrames(videoExtractor, decoder, videoBufferInfo, muxer, videoTrackIndex, duration, compressBitRate)

            muxer.stop()
            muxer.release()

            videoExtractor.release()
            decoder.stop()
            decoder.release()

            outputPath // 압축된 파일의 경로를 리턴
        } catch (e: Exception) {
            e.printStackTrace()
            null // 압축 실패 시 null 리턴
        }
    }

    private fun compressVideoFrames(
        extractor: MediaExtractor,
        decoder: MediaCodec,
        bufferInfo: MediaCodec.BufferInfo,
        muxer: MediaMuxer,
        trackIndex: Int,
        duration: Long?,
        compressBitRate: Int
    ) {
        val inputBuffer = ByteBuffer.allocate(1024 * 1024)
        var inputBufferIndex = -1 // Initialize inputBufferIndex

        while (true) {
            val sampleSize = extractor.readSampleData(inputBuffer, 0)
            if (sampleSize < 0) {
                // End of stream, queue input buffer with EOS flag
                inputBufferIndex = decoder.dequeueInputBuffer(-1)
                if (inputBufferIndex >= 0) {
                    decoder.queueInputBuffer(
                        inputBufferIndex,
                        0,
                        0,
                        duration ?: extractor.sampleTime,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                }
                break
            } else {
                val sampleTime = extractor.sampleTime
                val flags = extractor.sampleFlags
                inputBufferIndex = decoder.dequeueInputBuffer(-1)
                if (inputBufferIndex >= 0) {
                    val inputBufferArray = decoder.getInputBuffer(inputBufferIndex)!!
                    inputBufferArray.clear()
                    inputBufferArray.put(inputBuffer.array(), 0, sampleSize)
                    decoder.queueInputBuffer(
                        inputBufferIndex,
                        0,
                        sampleSize,
                        sampleTime,
                        flags
                    )
                    extractor.advance()
                }

                var outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0)
                while (outputBufferIndex >= 0) {
                    val outputBufferArray = decoder.getOutputBuffer(outputBufferIndex)!!
                    muxer.writeSampleData(trackIndex, outputBufferArray, bufferInfo)
                    decoder.releaseOutputBuffer(outputBufferIndex, false)
                    outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0)
                }
            }
        }
    }


    private fun selectVideoTrack(extractor: MediaExtractor): MediaFormat {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith("video/")) {
                extractor.selectTrack(i)
                return format
            }
        }
        throw RuntimeException("No video track found in the provided media file.")
    }


    fun encodeBitmapToString(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val byteArray: ByteArray = baos.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    fun decodeBitmapFromString(encodedBitmap: String): Bitmap? {
        val decodedBytes = Base64.decode(encodedBitmap, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }


    fun getBitmapFromVector(context: Context, vectorResId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, vectorResId)
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    // 경로 정보를 문자열로 인코딩하는 함수
     fun encodePathToString(pathPoints: PolyLines): String {
        val encodedPath = StringBuilder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                encodedPath.append("${pos.latitude},${pos.longitude}|")
            }
        }
        // 마지막 '|' 문자 제거
        if (encodedPath.isNotEmpty()) {
            encodedPath.deleteCharAt(encodedPath.length - 1)
        }
        return encodedPath.toString()
    }

    private fun decodePathFromString(pathString: String): MutableList<LatLng> {
        val pathPoints = mutableListOf<LatLng>()
        // 경로 문자열을 파싱하여 LatLng로 변환
        val points = pathString.split(";")
        for (point in points) {
            val coordinates = point.split(",")
            if (coordinates.size == 2) {
                val latitude = coordinates[0].toDouble()
                val longitude = coordinates[1].toDouble()
                pathPoints.add(LatLng(latitude, longitude))
            }
        }

        return pathPoints
    }

     fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

}
