package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CachedSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedSnapshotDao {
    @Query("SELECT * FROM cached_snapshots ORDER BY cachedTimestamp DESC")
    fun getAllSnapshots(): Flow<List<CachedSnapshotEntity>>

    @Query("SELECT * FROM cached_snapshots WHERE url = :url LIMIT 1")
    suspend fun getSnapshotByUrl(url: String): CachedSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: CachedSnapshotEntity): Long

    @Delete
    suspend fun deleteSnapshot(snapshot: CachedSnapshotEntity)

    @Query("DELETE FROM cached_snapshots")
    suspend fun clearAllSnapshots()

    @Query("SELECT COUNT(*) FROM cached_snapshots")
    fun getSnapshotCount(): Flow<Int>
}
