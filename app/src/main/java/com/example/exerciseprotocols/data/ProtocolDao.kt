package com.example.exerciseprotocols.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtocolDao {
    @Query("SELECT * FROM protocols ORDER BY id DESC")
    fun getProtocols(): Flow<List<ProtocolEntity>>

    @Query("SELECT * FROM protocols WHERE id = :id")
    suspend fun getProtocol(id: Long): ProtocolEntity?

    @Query("SELECT * FROM exercises WHERE protocolId = :protocolId ORDER BY orderIndex")
    suspend fun getExercises(protocolId: Long): List<ExerciseEntity>

    @Transaction
    suspend fun getProtocolWithExercises(id: Long): ProtocolWithExercises? {
        val p = getProtocol(id) ?: return null
        return ProtocolWithExercises(p, getExercises(id))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtocol(protocol: ProtocolEntity): Long

    @Update
    suspend fun updateProtocol(protocol: ProtocolEntity)

    @Delete
    suspend fun deleteProtocol(protocol: ProtocolEntity)

    @Query("DELETE FROM exercises WHERE protocolId = :protocolId")
    suspend fun deleteExercisesForProtocol(protocolId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)
}
