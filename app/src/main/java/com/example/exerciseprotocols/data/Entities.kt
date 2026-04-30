package com.example.exerciseprotocols.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "protocols")
data class ProtocolEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val restBetweenExercisesSec: Int = 0
)

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = ProtocolEntity::class,
            parentColumns = ["id"],
            childColumns = ["protocolId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("protocolId")]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val protocolId: Long,
    val name: String,
    val workDurationSec: Int,
    val sets: Int,
    val restBetweenSetsSec: Int,
    val orderIndex: Int
)

data class ProtocolWithExercises(
    val protocol: ProtocolEntity,
    val exercises: List<ExerciseEntity>
)
