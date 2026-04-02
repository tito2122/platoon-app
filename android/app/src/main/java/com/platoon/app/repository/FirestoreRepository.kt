package com.platoon.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.platoon.app.model.MissingPerson
import com.platoon.app.model.Mission
import com.platoon.app.model.PlatoonList
import com.platoon.app.model.Reminder
import com.platoon.app.model.Soldier
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(private val platoonId: String) {

    private val db = FirebaseFirestore.getInstance()

    private fun soldiersCol() = db.collection("platoons").document(platoonId).collection("soldiers")
    private fun missionsCol() = db.collection("platoons").document(platoonId).collection("missions")
    private fun listsCol() = db.collection("platoons").document(platoonId).collection("lists")
    private fun missingCol() = db.collection("platoons").document(platoonId).collection("missing")
    private fun remindersCol() = db.collection("platoons").document(platoonId).collection("reminders")

    // --- Soldiers ---

    fun soldiersFlow(): Flow<List<Soldier>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = soldiersCol().addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val soldiers = snapshot?.documents?.mapNotNull { doc ->
                @Suppress("UNCHECKED_CAST")
                (doc.data as? Map<String, Any>)?.let { Soldier.fromMap(it) }
            } ?: emptyList()
            trySend(soldiers.sortedBy { it.id })
        }
        awaitClose { registration?.remove() }
    }

    suspend fun saveSoldier(soldier: Soldier) {
        soldiersCol().document(soldier.id.toString()).set(soldier.toMap()).await()
    }

    suspend fun deleteSoldier(soldierId: Long) {
        soldiersCol().document(soldierId.toString()).delete().await()
    }

    suspend fun getSoldiers(): List<Soldier> {
        val snapshot = soldiersCol().get().await()
        return snapshot.documents.mapNotNull { doc ->
            @Suppress("UNCHECKED_CAST")
            (doc.data as? Map<String, Any>)?.let { Soldier.fromMap(it) }
        }.sortedBy { it.id }
    }

    // --- Missing ---

    fun missingFlow(): Flow<List<MissingPerson>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = missingCol().addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val missing = snapshot?.documents?.mapNotNull { doc ->
                @Suppress("UNCHECKED_CAST")
                (doc.data as? Map<String, Any>)?.let { MissingPerson.fromMap(it) }
            } ?: emptyList()
            trySend(missing)
        }
        awaitClose { registration?.remove() }
    }

    suspend fun saveMissing(missing: MissingPerson) {
        missingCol().document(missing.id.toString()).set(missing.toMap()).await()
    }

    suspend fun deleteMissing(id: Long) {
        missingCol().document(id.toString()).delete().await()
    }

    // --- Missions ---

    fun missionsFlow(): Flow<List<Mission>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = missionsCol().addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val missions = snapshot?.documents?.mapNotNull { doc ->
                @Suppress("UNCHECKED_CAST")
                (doc.data as? Map<String, Any>)?.let { Mission.fromMap(it) }
            } ?: emptyList()
            trySend(missions.sortedByDescending { it.createdAt })
        }
        awaitClose { registration?.remove() }
    }

    suspend fun saveMission(mission: Mission) {
        missionsCol().document(mission.id.toString()).set(mission.toMap()).await()
    }

    suspend fun deleteMission(id: Long) {
        missionsCol().document(id.toString()).delete().await()
    }

    // --- Lists ---

    fun listsFlow(): Flow<List<PlatoonList>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = listsCol().addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val lists = snapshot?.documents?.mapNotNull { doc ->
                @Suppress("UNCHECKED_CAST")
                (doc.data as? Map<String, Any>)?.let { PlatoonList.fromMap(it) }
            } ?: emptyList()
            trySend(lists.sortedByDescending { it.createdAt })
        }
        awaitClose { registration?.remove() }
    }

    suspend fun saveList(list: PlatoonList) {
        listsCol().document(list.id).set(list.toMap()).await()
    }

    suspend fun deleteList(id: String) {
        listsCol().document(id).delete().await()
    }

    // --- Reminders ---

    fun remindersFlow(): Flow<List<Reminder>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = remindersCol().addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val reminders = snapshot?.documents?.mapNotNull { doc ->
                @Suppress("UNCHECKED_CAST")
                (doc.data as? Map<String, Any>)?.let { Reminder.fromMap(it) }
            } ?: emptyList()
            trySend(reminders.sortedBy { it.id })
        }
        awaitClose { registration?.remove() }
    }

    suspend fun saveReminder(reminder: Reminder) {
        remindersCol().document(reminder.id.toString()).set(reminder.toMap()).await()
    }

    suspend fun deleteReminder(id: Long) {
        remindersCol().document(id.toString()).delete().await()
    }

    // --- Platoon auth ---

    suspend fun getPlatoon(platoonId: String): Map<String, Any>? {
        val doc = db.collection("platoons").document(platoonId).get().await()
        @Suppress("UNCHECKED_CAST")
        return doc.data as? Map<String, Any>
    }

    suspend fun createPlatoon(platoonId: String, name: String, passwordHash: String, logo: String) {
        val data = mapOf(
            "name" to name,
            "password" to passwordHash,
            "logo" to logo,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("platoons").document(platoonId).set(data).await()
    }
}
