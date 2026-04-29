package com.example.exerciseprotocols.data

import kotlinx.coroutines.flow.Flow

class ProtocolRepository(private val dao: ProtocolDao) {
    fun protocols(): Flow<List<ProtocolEntity>> = dao.getProtocols()
    suspend fun protocolDetails(id: Long): ProtocolWithExercises? = dao.getProtocolWithExercises(id)

    suspend fun saveProtocol(
        id: Long?,
        name: String,
        restBetweenExercisesSec: Int,
        exercises: List<ExerciseEntity>
    ): Long {
        val protocolId = if (id == null) {
            dao.insertProtocol(ProtocolEntity(name = name, restBetweenExercisesSec = restBetweenExercisesSec))
        } else {
            dao.updateProtocol(ProtocolEntity(id = id, name = name, restBetweenExercisesSec = restBetweenExercisesSec))
            id
        }
        dao.deleteExercisesForProtocol(protocolId)
        dao.insertExercises(exercises.mapIndexed { idx, e -> e.copy(protocolId = protocolId, orderIndex = idx) })
        return protocolId
    }

    suspend fun deleteProtocol(protocol: ProtocolEntity) = dao.deleteProtocol(protocol)
}
