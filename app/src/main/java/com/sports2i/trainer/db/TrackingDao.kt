package com.sports2i.trainer.db
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sports2i.trainer.data.model.TrackingData

@Dao
interface TrackingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackingData(trackingData: TrackingData)

    @Query("SELECT * FROM tracking_data WHERE exerciseId = :exerciseId AND userId = :userId AND trainingTime = :trainingTime AND trainingDate = :trainingDate")
    fun getTrackingData(exerciseId: String, userId: String, trainingTime: String, trainingDate: String): LiveData<TrackingData>

    @Query("SELECT * FROM tracking_data")
    fun getAllTrackingData(): LiveData<List<TrackingData>>
}
