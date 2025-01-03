package com.sports2i.trainer.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun toBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes,0,bytes.size)
    }

    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun fromLatLngList(pathPoints: MutableList<MutableList<LatLng>>): String {
        return Gson().toJson(pathPoints)
    }

    @TypeConverter
    fun toLatLngList(pathPointsString: String): MutableList<MutableList<LatLng>> {
        val type = object : TypeToken<MutableList<MutableList<LatLng>>>() {}.type
        return Gson().fromJson(pathPointsString, type)
    }
}