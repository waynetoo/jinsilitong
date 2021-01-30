package com.waynetoo.videotv.room.dao

import androidx.room.*
import com.waynetoo.videotv.room.entity.AdInfo


/**
 *
 */
@Dao
interface AdDao {

    @Query("SELECT * FROM ad_info")
    suspend fun getAdList(): List<AdInfo>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertList(adList: List<AdInfo>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(ad: AdInfo)

    @Update
    suspend fun update(ad: AdInfo): Int

    @Delete
    suspend fun delete(ad: AdInfo): Int

    @Delete
    suspend fun deleteList(adList: List<AdInfo>): Int

    @Query("DELETE FROM ad_info")
    suspend fun deleteAll()

    @Query("DELETE FROM ad_info where filePath ='' or filePath  is null")
    suspend fun deletePathEmpty()
}
