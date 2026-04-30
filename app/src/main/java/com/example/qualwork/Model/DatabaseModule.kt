package com.example.qualwork.Model
import android.content.Context
import com.example.qualwork.Model.DAO.ConnectionDao
import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.Model.DAO.IntakeTimeDao
import com.example.qualwork.Model.DAO.MedicationDao
import com.example.qualwork.Model.DAO.ScheduleDao
import com.example.qualwork.Model.DAO.UserDao
import com.example.qualwork.Model.Notification.NotificationScheduler
import com.example.qualwork.Model.Repository.FirestoreRepository
import com.example.qualwork.Model.Repository.IntakeLogRepository
import com.example.qualwork.Model.Repository.MedicationRepository
import com.example.qualwork.Model.Repository.UserRepository
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
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides
    fun provideConnectionDao(db: AppDatabase): ConnectionDao = db.connectionDao()
    @Provides
    fun provideIntakeLogDao(db: AppDatabase): IntakeLogDao = db.intakeLogDao()
    @Provides
    fun provideMedicationDao(db: AppDatabase): MedicationDao = db.medicationDao()
    @Provides
    fun provideScheduleDao(db: AppDatabase): ScheduleDao = db.scheduleDao()
    @Provides
    fun provideIntakeTimeDao(db: AppDatabase): IntakeTimeDao = db.intakeTimeDao()
    @Provides
    @Singleton
    fun provideUserPreferences(
        @ApplicationContext context: Context
    ): UserPreferences = UserPreferences(context)
    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        firestoreRepository: FirestoreRepository
    ): UserRepository = UserRepository(userDao,firestoreRepository)
    @Provides
    @Singleton
    fun provideMedicationRepository(
        medicationDao: MedicationDao,
        intakeTimeDao: IntakeTimeDao,
        scheduleDao: ScheduleDao,
        firestoreRepository: FirestoreRepository
    ): MedicationRepository = MedicationRepository(medicationDao, intakeTimeDao, scheduleDao,firestoreRepository)
    @Provides
    @Singleton
    fun provideNotificationScheduler(
        @ApplicationContext context: Context
    ): NotificationScheduler = NotificationScheduler(context)
    @Provides
    @Singleton
    fun provideIntakeLogRepository(
        intakeLogDao: IntakeLogDao,
        scheduleDao: ScheduleDao,
        firestoreRepository: FirestoreRepository
    ): IntakeLogRepository = IntakeLogRepository(intakeLogDao,scheduleDao,firestoreRepository)
}