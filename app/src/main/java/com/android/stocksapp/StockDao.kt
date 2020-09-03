package com.android.stocksapp

import androidx.room.*

@Dao
interface StockDao {
    @Query("SELECT * FROM stock")
    suspend fun getAll(): List<Stock>

    @Insert
    suspend fun insertAll(vararg stocks: Stock)

    @Delete
    suspend fun delete(stock: Stock)

    @Update
    suspend fun updateStock(vararg stocks: Stock)
}