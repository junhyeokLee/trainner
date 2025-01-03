package com.sports2i.trainer.data.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng
import com.sports2i.trainer.db.Converters

@Entity(tableName = "tracking_data")
data class TrackingData(
    val pathPoints: MutableList<MutableList<LatLng>>,
    val exerciseId:String,
    val userId:String,
    val trainingTime:String,
    val trainingDate:String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
