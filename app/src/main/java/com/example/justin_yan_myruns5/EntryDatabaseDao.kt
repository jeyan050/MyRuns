package com.example.justin_yan_myruns5

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDatabaseDao {

    @Insert
    suspend fun insertEntry(entry: Entry)

    @Query("SELECT * FROM entry_table")
    fun getAllEntries(): Flow<List<Entry>>

    @Query("DELETE FROM entry_table")
    suspend fun deleteAll()

    @Query("DELETE FROM entry_table WHERE id = :key")
    suspend fun deleteEntry(key: Long)
}