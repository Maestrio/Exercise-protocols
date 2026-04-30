package com.example.exerciseprotocols.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ProtocolEntity::class, ExerciseEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun protocolDao(): ProtocolDao
}
