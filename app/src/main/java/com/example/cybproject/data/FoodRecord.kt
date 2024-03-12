package com.example.cybproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FoodRecord")
data class FoodRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val foodName: String,

)
