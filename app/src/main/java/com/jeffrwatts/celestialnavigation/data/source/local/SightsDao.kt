package com.jeffrwatts.celestialnavigation.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jeffrwatts.celestialnavigation.data.Sight
import kotlinx.coroutines.flow.Flow

@Dao
interface SightsDao {

    /**
     * Observes list of sights.
     *
     * @return all sights.
     */
    @Query("SELECT * FROM Sights")
    fun observeSights(): Flow<List<Sight>>

    /**
     * Observes a single sight.
     *
     * @param sightId the sight id.
     * @return the sight with sightId.
     */
    @Query("SELECT * FROM Sights WHERE entryid = :sightId")
    fun observeSightById(sightId: String): Flow<Sight>

    /**
     * Select all sights from the sights table.
     *
     * @return all sights.
     */
    @Query("SELECT * FROM Sights")
    suspend fun getSights(): List<Sight>

    /**
     * Select a sight by id.
     *
     * @param sightId the sight id.
     * @return the sight with sightId.
     */
    @Query("SELECT * FROM Sights WHERE entryid = :sightId")
    suspend fun getSightById(sightId: String): Sight?

    /**
     * Insert a sight in the database. If the sight already exists, replace it.
     *
     * @param sight the sight to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSight(sight: Sight)

    /**
     * Update a sight.
     *
     * @param sight sight to be updated
     * @return the number of sights updated. This should always be 1.
     */
    @Update
    suspend fun updateSight(sight: Sight): Int

    /**
     * Update the selected status of a sight
     *
     * @param sightId id of the sight
     * @param selected status to be updated
     */
    @Query("UPDATE sights SET selected = :selected WHERE entryid = :sightId")
    suspend fun updateSelected(sightId: String, selected: Boolean)

    /**
     * Delete a sight by id.
     *
     * @return the number of sights deleted. This should always be 1.
     */
    @Query("DELETE FROM Sights WHERE entryid = :sightId")
    suspend fun deleteSightById(sightId: String): Int

    /**
     * Delete all sights.
     */
    @Query("DELETE FROM Sights")
    suspend fun deleteSights()

}
