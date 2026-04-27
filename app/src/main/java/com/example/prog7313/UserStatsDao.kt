package com.example.prog7313

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStats(stats: UserStats)

    @Query("SELECT * FROM UserStats WHERE id = 1")
    suspend fun getStats(): UserStats?
}