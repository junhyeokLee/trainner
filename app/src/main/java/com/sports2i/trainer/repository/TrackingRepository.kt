package com.sports2i.trainer.repository

import com.sports2i.trainer.data.model.TrackingData
import com.sports2i.trainer.db.TrackingDao
import javax.inject.Inject

class TrackingRepository @Inject constructor(val trackingDao: TrackingDao) {

    suspend fun insertTracking(trackingData: TrackingData) = trackingDao.insertTrackingData(trackingData)
    fun getAllTrackingData() = trackingDao.getAllTrackingData()
    fun getTrackingData(exerciseId:String,userId:String,trainingTime:String,trainingDate:String) = trackingDao.getTrackingData(exerciseId,userId,trainingTime,trainingDate)

}
