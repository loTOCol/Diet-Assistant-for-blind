package com.example.cybproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FoodRecord::class], version = 1)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun foodRecordDao(): FoodRecordDao

    companion object {
        private const val DATABASE_NAME = "food_database" //db 이름

        @Volatile
        private var INSTANCE: FoodDatabase? = null

        private fun buildDataBase(context: Context): FoodDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                FoodDatabase::class.java,
                "Food"
            ).build()

        fun getInstance(context: Context): FoodDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDataBase(context).also { INSTANCE = it }
            }
    }
}