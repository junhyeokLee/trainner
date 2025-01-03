package com.sports2i.trainer.db
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sports2i.trainer.data.model.TrackingData

@Database(entities = [TrackingData::class], version = 1)
@TypeConverters(Converters::class)
abstract class TrackingDatabase : RoomDatabase() {
    abstract fun trackingDao(): TrackingDao
    // Define your migrations here
    companion object {
        const val DATABASE_NAME = "tracking-database"
    }
}