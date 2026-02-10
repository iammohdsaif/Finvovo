package com.example.finvovo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import com.example.finvovo.data.model.PlanningType
import com.example.finvovo.data.model.UpcomingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface UpcomingDao {
    @Query("SELECT * FROM upcoming_items ORDER BY dueDate ASC")
    fun getAllUpcomingItems(): Flow<List<UpcomingItem>>

    @Query("SELECT * FROM upcoming_items WHERE type = :type ORDER BY dueDate ASC")
    fun getUpcomingItemsByType(type: PlanningType): Flow<List<UpcomingItem>>

    @Query("SELECT * FROM upcoming_items WHERE status = 'PENDING' AND dueDate <= :timestamp")
    fun getDuePendingItems(timestamp: Long): Flow<List<UpcomingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUpcomingItem(item: UpcomingItem)

    @Update
    fun updateUpcomingItem(item: UpcomingItem)

    @Delete
    fun deleteUpcomingItem(item: UpcomingItem)

    // Backup & Restore
    @Query("SELECT * FROM upcoming_items")
    fun getAllItemsSync(): List<UpcomingItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<UpcomingItem>)

    @Query("DELETE FROM upcoming_items")
    fun deleteAll()
}
