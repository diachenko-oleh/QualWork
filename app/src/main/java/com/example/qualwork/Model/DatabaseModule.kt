package com.example.qualwork.Model
import android.content.Context
import com.example.qualwork.Model.DAO.MedicationDao
import com.example.qualwork.Model.DAO.ScheduleDao
import com.example.qualwork.Model.Repository.MedicationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun provideMedicationDao(db: AppDatabase): MedicationDao = db.medicationDao()

    @Provides
    fun provideScheduleDao(db: AppDatabase): ScheduleDao = db.scheduleDao()

    @Provides
    @Singleton
    fun provideMedicationRepository(
        medicationDao: MedicationDao,
        scheduleDao: ScheduleDao
    ): MedicationRepository = MedicationRepository(medicationDao, scheduleDao)
}