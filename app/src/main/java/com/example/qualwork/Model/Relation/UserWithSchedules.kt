package com.example.qualwork.Model.Relation


data class PatientCourseGroup(
    val patientName: String,
    val patientId: String,
    val courses: List<MedicationWithSchedules>,
    val nextDoseTimes: Map<Long, String> = emptyMap()
)