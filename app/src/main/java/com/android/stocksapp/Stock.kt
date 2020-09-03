package com.android.stocksapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Stock(
    @PrimaryKey @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "price") var price: Double,
    @ColumnInfo(name = "lastUpdate") var lastUpdate: Long,
    @ColumnInfo(name = "increase") var increase: Int
)