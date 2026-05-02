package com.example.qualwork.Model.Repository

import android.util.Log
import com.example.qualwork.Model.Entity.IntakeLog
import com.example.qualwork.Model.Entity.IntakeTimeEntity
import com.example.qualwork.Model.Entity.Medication
import com.example.qualwork.Model.Entity.MedicationForm
import com.example.qualwork.Model.Entity.Schedule
import com.example.qualwork.Model.Entity.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor() {
    private val firestore = Firebase.firestore

    //user
    fun syncUser(user: User) {
        firestore.collection("users")
            .document(user.id)
            .set(mapOf(
                "id" to user.id,
                "name" to user.name,
                "code" to user.code
            ))
            .addOnFailureListener { e ->
                Log.e("Firestore", "Помилка синхронізації user: $e")
            }
    }
    suspend fun getUserById(userId: String): User? {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            val data = doc.data ?: return null
            User(
                id = data["id"] as String,
                name = data["name"] as String,
                code = data["code"] as String
            )
        } catch (e: Exception) {
            Log.e("Firestore", "Помилка отримання user: $e")
            null
        }
    }
    suspend fun findUserByCode(code: String): User? {
        return try {
            val result = firestore.collection("users")
                .whereEqualTo("code", code)
                .get()
                .await()

            if (result.isEmpty) return null

            val doc = result.documents[0]
            User(
                id = doc.getString("id") ?: return null,
                name = doc.getString("name") ?: return null,
                code = doc.getString("code") ?: return null
            )
        } catch (e: Exception) {
            Log.e("Firestore", "Помилка пошуку user: $e")
            null
        }
    }

    //connection

    suspend fun getSupervisors(patientId: String): List<User> {
        return try {
            val links = firestore.collection("connections")
                .whereEqualTo("patientId", patientId)
                .get()
                .await()

            links.documents.mapNotNull { link ->
                val supervisorId = link.getString("supervisorId") ?: return@mapNotNull null
                val userDoc = firestore.collection("users")
                    .document(supervisorId)
                    .get()
                    .await()
                val data = userDoc.data ?: return@mapNotNull null
                User(
                    id = data["id"] as String,
                    name = data["name"] as String,
                    code = data["code"] as String
                )
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Помилка отримання опікунів: $e")
            emptyList()
        }
    }

    suspend fun getPatients(supervisorId: String): List<User> {
        return try {
            val links = firestore.collection("connections")
                .whereEqualTo("supervisorId", supervisorId)
                .get()
                .await()

            links.documents.mapNotNull { link ->
                val patientId = link.getString("patientId") ?: return@mapNotNull null
                val userDoc = firestore.collection("users")
                    .document(patientId)
                    .get()
                    .await()
                val data = userDoc.data ?: return@mapNotNull null
                User(
                    id = data["id"] as String,
                    name = data["name"] as String,
                    code = data["code"] as String
                )
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Помилка отримання пацієнтів: $e")
            emptyList()
        }
    }

    suspend fun connectToPatient(supervisorId: String, patientCode: String): Boolean {
        val patient = findUserByCode(patientCode) ?: return false

        return try {
            firestore.collection("connections")
                .add(mapOf(
                    "supervisorId" to supervisorId,
                    "patientId" to patient.id
                ))
                .await()
            true
        } catch (e: Exception) {
            Log.e("Firestore", "Помилка створення зв'язку: $e")
            false
        }
    }

    suspend fun getPatientIds(supervisorId: String): List<String> {
        return try {
            val result = firestore.collection("connections")
                .whereEqualTo("supervisorId", supervisorId)
                .get()
                .await()

            result.documents.mapNotNull { it.getString("patientId") }
        } catch (e: Exception) {
            Log.e("Firestore", "Помилка отримання пацієнтів: $e")
            emptyList()
        }
    }

    suspend fun removeLink(userId1: String, userId2: String): Boolean {
        return try {
            val asSupervisor = firestore.collection("connections")
                .whereEqualTo("supervisorId", userId1)
                .whereEqualTo("patientId", userId2)
                .get()
                .await()

            val asPatient = firestore.collection("connections")
                .whereEqualTo("supervisorId", userId2)
                .whereEqualTo("patientId", userId1)
                .get()
                .await()

            (asSupervisor.documents + asPatient.documents).forEach { it.reference.delete().await() }
            true
        } catch (e: Exception) {
            Log.e("Firestore", "Помилка відключення: $e")
            false
        }
    }

    //medication

    fun syncMedication(medication: Medication, userId: String) {
        val docId = "${userId}_${medication.id}"

        firestore.collection("medications")
            .document(docId)
            .set(mapOf(
                "localId" to medication.id,
                "userId" to userId,
                "name" to medication.name,
                "form" to medication.form.name
            ))
            .addOnFailureListener { e ->
                Log.e("Firestore", "Помилка синхронізації medication: $e")
            }
    }

    suspend fun getPatientMedications(patientId: String): List<Medication> {
        return try {
            val result = firestore.collection("medications")
                .whereEqualTo("userId", patientId)
                .get()
                .await()

            result.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                Medication(
                    id = (data["localId"] as Long),
                    name = data["name"] as String,
                    form = MedicationForm.valueOf(data["form"] as String)
                )
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Помилка отримання medications: $e")
            emptyList()
        }
    }
    fun syncSchedule(schedule: Schedule) {
        val docId = "${schedule.userId}_${schedule.id}"

        firestore.collection("schedules")
            .document(docId)
            .set(mapOf(
                "localId" to schedule.id,
                "medicationId" to schedule.medicationId,
                "userId" to schedule.userId,
                "startDate" to schedule.startDate,
                "endDate" to schedule.endDate,
                "dosage" to schedule.dosage,
                "medAmount" to schedule.medAmount
            ))
            .addOnFailureListener { e ->
                Log.e("Firestore", "Помилка синхронізації schedule: $e")
            }
    }

    suspend fun deleteCourse(scheduleId: Long, medicationId: Long, userId: String) {
        try {
            // Видалити Schedule
            firestore.collection("schedules")
                .document("${userId}_${scheduleId}")
                .delete()
                .await()

            // Видалити Medication
            firestore.collection("medications")
                .document("${userId}_${medicationId}")
                .delete()
                .await()

            // Видалити IntakeTimes
            val intakeTimes = firestore.collection("intake_times")
                .whereEqualTo("userId", userId)
                .whereEqualTo("scheduleId", scheduleId)
                .get()
                .await()
            intakeTimes.documents.forEach { it.reference.delete() }

            // Видалити IntakeLogs
            val intakeLogs = firestore.collection("intake_logs")
                .whereEqualTo("userId", userId)
                .whereEqualTo("scheduleId", scheduleId)
                .get()
                .await()
            intakeLogs.documents.forEach { it.reference.delete() }

        } catch (e: Exception) {
            Log.e("Firestore", "Помилка видалення курсу: $e")
        }
    }

    //intake time

    fun syncIntakeTimes(scheduleId: Long, userId: String, intakeTimes: List<IntakeTimeEntity>) {
        intakeTimes.forEach { intakeTime ->
            val docId = "${userId}_${intakeTime.id}"
            firestore.collection("intake_times")
                .document(docId)
                .set(mapOf(
                    "localId" to intakeTime.id,
                    "scheduleId" to scheduleId,
                    "userId" to userId,
                    "time" to intakeTime.time
                ))
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Помилка синхронізації intake_time: $e")
                }
        }
    }

    suspend fun getPatientIntakeTimes(patientId: String): List<IntakeTimeEntity> {
        return try {
            val result = firestore.collection("intake_times")
                .whereEqualTo("userId", patientId)
                .get()
                .await()

            result.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                IntakeTimeEntity(
                    id = data["localId"] as Long,
                    scheduleId = data["scheduleId"] as Long,
                    time = data["time"] as String
                )
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Помилка отримання intake_times: $e")
            emptyList()
        }
    }

    //intake log

    fun syncIntakeLog(intakeLog: IntakeLog, userId: String) {
        val docId = "${userId}_${intakeLog.id}"
        firestore.collection("intake_logs")
            .document(docId)
            .set(mapOf(
                "localId" to intakeLog.id,
                "scheduleId" to intakeLog.scheduleId,
                "userId" to userId,
                "plannedDoseTime" to intakeLog.plannedDoseTime.toString(),
                "actualDoseTime" to intakeLog.actualDoseTime?.toString(),
                "taken" to intakeLog.taken
            ))
            .addOnFailureListener { e ->
                Log.e("Firestore", "Помилка синхронізації intake_log: $e")
            }
    }

    suspend fun getPatientIntakeLogs(patientId: String): List<IntakeLog> {
        return try {
            val result = firestore.collection("intake_logs")
                .whereEqualTo("userId", patientId)
                .get()
                .await()

            result.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                IntakeLog(
                    id = data["localId"] as Long,
                    scheduleId = data["scheduleId"] as Long,
                    plannedDoseTime = LocalDateTime.parse(data["plannedDoseTime"] as String),
                    actualDoseTime = (data["actualDoseTime"] as? String)?.let { LocalDateTime.parse(it) },
                    taken = data["taken"] as Boolean
                )
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Помилка отримання intake_logs: $e")
            emptyList()
        }
    }

    // extra

    data class PatientFullData(
        val medications: List<Medication>,
        val schedules: List<Schedule>,
        val intakeTimes: List<IntakeTimeEntity>,
        val intakeLogs: List<IntakeLog>
    )

    suspend fun getPatientFullData(patientId: String): PatientFullData {
        return PatientFullData(
            medications = getPatientMedications(patientId),
            schedules = getPatientSchedules(patientId),
            intakeTimes = getPatientIntakeTimes(patientId),
            intakeLogs = getPatientIntakeLogs(patientId)
        )
    }
    suspend fun getPatientSchedules(patientId: String): List<Schedule> {
        return try {
            val result = firestore.collection("schedules")
                .whereEqualTo("userId", patientId)
                .get()
                .await()

            result.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                Schedule(
                    id = (data["localId"] as Long),
                    medicationId = (data["medicationId"] as Long),
                    userId = data["userId"] as String,
                    startDate = (data["startDate"] as Long),
                    endDate = data["endDate"] as? Long,
                    dosage = (data["dosage"] as Long).toInt(),
                    medAmount = (data["medAmount"] as? Long)?.toInt()
                )
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Помилка отримання schedules: $e")
            emptyList()
        }
    }

    //notification

    suspend fun notifySupervisors(
        patientId: String,
        patientName: String,
        medicationName: String,
        time: String
    ): Boolean {
        return try {
            firestore.collection("missed_notifications")
                .add(mapOf(
                    "patientId" to patientId,
                    "patientName" to patientName,
                    "medicationName" to medicationName,
                    "time" to time,
                    "timestamp" to System.currentTimeMillis(),
                    "seen" to false
                ))
                .await()
            true
        } catch (e: Exception) {
            Log.e("Firestore", "Помилка сповіщення наглядача: $e")
            false
        }
    }

    fun markNotificationSeen(docId: String) {
        firestore.collection("missed_notifications")
            .document(docId)
            .update("seen", true)
    }







}