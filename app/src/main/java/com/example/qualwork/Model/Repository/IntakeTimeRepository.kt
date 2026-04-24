package com.example.qualwork.Model.Repository

import com.example.qualwork.Model.DAO.IntakeTimeDao
import com.example.qualwork.Model.Entity.IntakeTimeEntity
import java.time.LocalTime
import javax.inject.Inject

class IntakeTimeRepository @Inject constructor(
    private val dao: IntakeTimeDao
) {

    suspend fun getTimes(scheduleId: Long) =
        dao.getTimesForSchedule(scheduleId)

    suspend fun insertTimes(scheduleId: Long, times: List<LocalTime>) {
        dao.deleteBySchedule(scheduleId)

        dao.insertAll(
            times.map {
                IntakeTimeEntity(
                    scheduleId = scheduleId,
                    time = it.toString()
                )
            }
        )
    }
}