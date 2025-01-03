package com.sports2i.trainer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class TrainerVideoMaket(val context: Context) {
    private val TAG = "TRAINER_IMAGE_MAKER"

    fun getBitmap(path: String) : Bitmap {
        return BitmapFactory.decodeFile(path)
    }
    fun getBitmapByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, stream)

        return stream.toByteArray()
    }
    fun getBitmapByteArray(path: String): ByteArray {
        val stream = ByteArrayOutputStream()
        var bitmap = getBitmap(path)

        val ei = ExifInterface(path)
        when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> bitmap = rotateBitmap(90f, bitmap)
            ExifInterface.ORIENTATION_ROTATE_180 -> bitmap = rotateBitmap(180f, bitmap)
            ExifInterface.ORIENTATION_ROTATE_270 -> bitmap = rotateBitmap(270f, bitmap)
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, stream)

        return stream.toByteArray()
    }

    private fun rotateBitmap(angle: Int, x: Int, y: Int, width: Int, height: Int, bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(bitmap, x, y, width, height, matrix, true)
    }

    private fun rotateBitmap(angle: Float, bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun saveBitmapCache(bitmap: Bitmap, exe: String = JPEG_EXE, f: File? = null): String {

        val file = f?: File.createTempFile(
            System.currentTimeMillis().toString(),
            exe,
            context.cacheDir
        )

        file.deleteOnExit()

        val outStream = FileOutputStream(file)
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
//        bitmap.compress(CompressFormat.PNG, 100, outStream)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.close()

        return file.absolutePath
    }

    fun makeSquareBitmap(bitmap: Bitmap, rotation: Int): Bitmap {
        var changeBitmap = bitmap

        var x = 0
        var y = 0
        var width = bitmap.width
        var height = bitmap.height


        var isChange = false
        if ( width != height ) {
            if ( width < height ) {
//                y = (height - width) / 2
                height = width
            } else if ( width > height ) {
//                x = (width - height) / 2
                width = height
            }

            isChange = true

        }

        if (isChange) {
//            changeBitmap = rotateBitmap(x, y, width, height, bitmap)
            changeBitmap = rotateBitmap(rotation, x, y, width, height, bitmap)
        }

        return changeBitmap
    }

    fun makeCacheFile(exe: String = MP4_EXE): File {
        return File.createTempFile(
            System.currentTimeMillis().toString(),
            exe,
            context.cacheDir
        )
    }

    fun videoCmd(videoPathList: ArrayList<Uri>, file: File): String {
        file.deleteOnExit()

        val cmdList = ArrayList<String>()
        cmdList.add("-y")
        for (i in 0 until videoPathList.size) {
            cmdList.add("-i")
            cmdList.add("'${PathUtils.getPath(context, videoPathList[i])}'")
        }
            cmdList.add("-c:v")
            cmdList.add("libx264")
            cmdList.add("-preset")
            cmdList.add("ultrafast")

                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, videoPathList[0])

                val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()?: 1440
                val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()?: 1440
                val ratio = width / height.toFloat()

        val scale = if (ratio > 1) {
            "-vf 'scale=1280:${(height / (width.toFloat() / 1280)).toInt()}'"
        } else {
            "-vf 'scale=${(width / (height.toFloat() / 1280)).toInt()}:1280'"
        }

        cmdList.add(scale)
        cmdList.add("-b:v")
//        cmdList.add("800k")   // libx264 약 20초에 2.5MB
        cmdList.add("1400k")  // libx264 약 60초에 12MB
//        cmdList.add("1600k")  // libx264 약 20초에 5MB
//        cmdList.add("3200k")    // libx264 약 20초에 10MB
        cmdList.add(file.absolutePath)

        return cmdList.joinToString(" ")
    }



}
