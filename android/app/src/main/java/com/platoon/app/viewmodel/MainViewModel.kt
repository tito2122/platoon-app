package com.platoon.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platoon.app.model.MissingPerson
import com.platoon.app.model.Mission
import com.platoon.app.model.PlatoonList
import com.platoon.app.model.Reminder
import com.platoon.app.model.Soldier
import com.platoon.app.repository.FirestoreRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repo: FirestoreRepository) : ViewModel() {

    private val _soldiers = MutableLiveData<List<Soldier>>(emptyList())
    val soldiers: LiveData<List<Soldier>> = _soldiers

    private val _missing = MutableLiveData<List<MissingPerson>>(emptyList())
    val missing: LiveData<List<MissingPerson>> = _missing

    private val _missions = MutableLiveData<List<Mission>>(emptyList())
    val missions: LiveData<List<Mission>> = _missions

    private val _lists = MutableLiveData<List<PlatoonList>>(emptyList())
    val lists: LiveData<List<PlatoonList>> = _lists

    private val _reminders = MutableLiveData<List<Reminder>>(emptyList())
    val reminders: LiveData<List<Reminder>> = _reminders

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    init {
        startListening()
    }

    private fun startListening() {
        viewModelScope.launch {
            repo.soldiersFlow().collect { _soldiers.postValue(it) }
        }
        viewModelScope.launch {
            repo.missingFlow().collect { _missing.postValue(it) }
        }
        viewModelScope.launch {
            repo.missionsFlow().collect { _missions.postValue(it) }
        }
        viewModelScope.launch {
            repo.listsFlow().collect { _lists.postValue(it) }
        }
        viewModelScope.launch {
            repo.remindersFlow().collect { _reminders.postValue(it) }
        }
    }

    fun saveSoldier(soldier: Soldier) = viewModelScope.launch {
        runCatching { repo.saveSoldier(soldier) }
            .onFailure { _error.postValue(it.message) }
    }

    fun deleteSoldier(id: Long) = viewModelScope.launch {
        runCatching { repo.deleteSoldier(id) }
            .onFailure { _error.postValue(it.message) }
    }

    fun saveMission(mission: Mission) = viewModelScope.launch {
        runCatching { repo.saveMission(mission) }
            .onFailure { _error.postValue(it.message) }
    }

    fun deleteMission(id: Long) = viewModelScope.launch {
        runCatching { repo.deleteMission(id) }
            .onFailure { _error.postValue(it.message) }
    }

    fun saveList(list: PlatoonList) = viewModelScope.launch {
        runCatching { repo.saveList(list) }
            .onFailure { _error.postValue(it.message) }
    }

    fun deleteList(id: String) = viewModelScope.launch {
        runCatching { repo.deleteList(id) }
            .onFailure { _error.postValue(it.message) }
    }

    fun saveMissing(missing: MissingPerson) = viewModelScope.launch {
        runCatching { repo.saveMissing(missing) }
            .onFailure { _error.postValue(it.message) }
    }

    fun deleteMissing(id: Long) = viewModelScope.launch {
        runCatching { repo.deleteMissing(id) }
            .onFailure { _error.postValue(it.message) }
    }

    fun saveReminder(reminder: Reminder) = viewModelScope.launch {
        runCatching { repo.saveReminder(reminder) }
            .onFailure { _error.postValue(it.message) }
    }

    fun deleteReminder(id: Long) = viewModelScope.launch {
        runCatching { repo.deleteReminder(id) }
            .onFailure { _error.postValue(it.message) }
    }

    fun clearError() {
        _error.value = null
    }

    fun getSoldierById(id: Long): Soldier? = _soldiers.value?.find { it.id == id }

    fun getNextSoldierId(): Long {
        val existing = _soldiers.value?.maxOfOrNull { it.id } ?: 0L
        return existing + 1
    }

    fun getNextMissionId(): Long {
        val existing = _missions.value?.maxOfOrNull { it.id } ?: 0L
        return existing + 1
    }

    fun getNextMissingId(): Long {
        val existing = _missing.value?.maxOfOrNull { it.id } ?: 0L
        return existing + 1
    }

    fun getNextReminderId(): Long {
        val existing = _reminders.value?.maxOfOrNull { it.id } ?: 0L
        return existing + 1
    }
}
