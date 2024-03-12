package com.example.cybproject.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface FoodRecordDao {
    @Query("SELECT * FROM FoodRecord")
    fun getAll(): List<FoodRecord>

    @Insert
    fun insert(foodRecord: FoodRecord)

    @Update
    fun update(foodRecord: FoodRecord)

    @Delete
    fun delete(foodRecord: FoodRecord)

    @Query("DELETE FROM FoodRecord")
    fun deleteAll()
}