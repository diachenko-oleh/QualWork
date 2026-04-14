package com.example.qualwork.Model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.qualwork.Model.DAO.MedicationDao
import com.example.qualwork.Model.DAO.ScheduleDao
import com.example.qualwork.Model.Entity.Medication
import com.example.qualwork.Model.Entity.Schedule
import com.example.qualwork.Model.Relation.Converters

@TypeConverters(Converters::class)
@Database(
    entities = [Medication::class, Schedule::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medapp_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}