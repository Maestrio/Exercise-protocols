package com.example.exerciseprotocols.viewmodel

import android.media.ToneGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.exerciseprotocols.data.ExerciseEntity
import com.example.exerciseprotocols.data.ProtocolEntity
import com.example.exerciseprotocols.data.ProtocolRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExerciseInput(var name: String = "", var work: String = "30", var sets: String = "3", var rest: String = "15")

class ListViewModel(private val repo: ProtocolRepository) : ViewModel() {
    val protocols = repo.protocols().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun delete(protocol: ProtocolEntity) = viewModelScope.launch { repo.deleteProtocol(protocol) }
}
class ListViewModelFactory(private val repo: ProtocolRepository) : ViewModelProvider.Factory { override fun <T : ViewModel> create(c: Class<T>): T = ListViewModel(repo) as T }

class BuilderViewModel(private val repo: ProtocolRepository, private val id: Long?) : ViewModel() {
    var name = MutableStateFlow("")
    var restBetweenExercises = MutableStateFlow("0")
    var exercises = MutableStateFlow(listOf(ExerciseInput()))

    init { if (id != null) viewModelScope.launch {
        repo.protocolDetails(id)?.let {
            name.value = it.protocol.name
            restBetweenExercises.value = it.protocol.restBetweenExercisesSec.toString()
            exercises.value = it.exercises.map { e -> ExerciseInput(e.name, e.workDurationSec.toString(), e.sets.toString(), e.restBetweenSetsSec.toString()) }
        }
    } }

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        val ex = exercises.value.filter { it.name.isNotBlank() }.mapIndexed { idx, e ->
            ExerciseEntity(protocolId = 0, name = e.name, workDurationSec = e.work.toIntOrNull() ?: 30, sets = e.sets.toIntOrNull() ?: 1, restBetweenSetsSec = e.rest.toIntOrNull() ?: 0, orderIndex = idx)
        }
        repo.saveProtocol(id, name.value.ifBlank { "Untitled" }, restBetweenExercises.value.toIntOrNull() ?: 0, ex)
        onDone()
    }
}
class BuilderViewModelFactory(private val repo: ProtocolRepository, private val id: Long?) : ViewModelProvider.Factory { override fun <T : ViewModel> create(c: Class<T>): T = BuilderViewModel(repo, id) as T }

enum class Phase { WORK, REST, DONE }
data class PlayerUi(val exercise: String = "", val setLabel: String = "", val phase: Phase = Phase.WORK, val remaining: Int = 0, val next: String = "", val running: Boolean = false)
class PlayerViewModel(private val repo: ProtocolRepository, private val id: Long, private val tone: ToneGenerator) : ViewModel() {
    private val _ui = MutableStateFlow(PlayerUi())
    val ui: StateFlow<PlayerUi> = _ui.asStateFlow()
    private var timeline: List<PlayerUi> = emptyList()
    private var idx = 0

    init { viewModelScope.launch {
        val p = repo.protocolDetails(id) ?: return@launch
        val steps = mutableListOf<PlayerUi>()
        p.exercises.forEachIndexed { eidx, e ->
            repeat(e.sets) { s ->
                steps += PlayerUi(e.name, "Set ${s + 1} / ${e.sets}", Phase.WORK, e.workDurationSec, p.exercises.getOrNull(eidx + 1)?.name ?: "Complete")
                if (s < e.sets - 1) steps += PlayerUi(e.name, "Set ${s + 1} / ${e.sets}", Phase.REST, e.restBetweenSetsSec, e.name)
            }
            if (eidx < p.exercises.lastIndex && p.protocol.restBetweenExercisesSec > 0) {
                steps += PlayerUi("Transition", "", Phase.REST, p.protocol.restBetweenExercisesSec, p.exercises[eidx + 1].name)
            }
        }
        timeline = steps
        _ui.value = steps.firstOrNull() ?: PlayerUi(phase = Phase.DONE)
    } }

    fun start() = viewModelScope.launch {
        _ui.value = _ui.value.copy(running = true)
        while (_ui.value.running && idx < timeline.size) {
            val cur = _ui.value
            if (cur.remaining <= 0) { advance(); continue }
            delay(1000)
            _ui.value = _ui.value.copy(remaining = (_ui.value.remaining - 1).coerceAtLeast(0))
        }
    }
    fun pause() { _ui.value = _ui.value.copy(running = false) }
    fun reset() { idx = 0; _ui.value = timeline.firstOrNull() ?: PlayerUi(phase = Phase.DONE) }
    fun skip() { advance() }
    private fun advance() {
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        idx++
        if (idx >= timeline.size) {
            tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 600)
            _ui.value = PlayerUi(phase = Phase.DONE)
        } else _ui.value = timeline[idx]
    }
}
class PlayerViewModelFactory(private val repo: ProtocolRepository, private val id: Long, private val tone: ToneGenerator) : ViewModelProvider.Factory { override fun <T : ViewModel> create(c: Class<T>): T = PlayerViewModel(repo, id, tone) as T }
