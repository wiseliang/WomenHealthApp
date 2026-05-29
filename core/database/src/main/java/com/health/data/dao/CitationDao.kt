package com.health.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.health.data.entity.CitationEntity

@Dao
interface CitationDao {
    @Query("SELECT * FROM citations WHERE citationKey = :key")
    suspend fun getCitation(key: String): CitationEntity?

    @Query("SELECT * FROM citations WHERE citationKey IN (:keys)")
    suspend fun getCitations(keys: List<String>): List<CitationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(citations: List<CitationEntity>)

    @Query("DELETE FROM citations")
    suspend fun deleteAll()
}
