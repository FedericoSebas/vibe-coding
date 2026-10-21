package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_account")
data class UserAccountEntity(
  @PrimaryKey val id: Int = 1,
  val cashBalance: Double = 100000.0,
  val startingCapital: Double = 100000.0,
  val resetCount: Int = 0
)

@Entity(tableName = "positions")
data class PositionEntity(
  @PrimaryKey val ticker: String,
  val companyName: String,
  val shares: Double,
  val averageCost: Double,
  val totalInvested: Double,
  val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val ticker: String,
  val companyName: String,
  val orderType: String, // "MARKET_BUY", "MARKET_SELL", "LIMIT_BUY", "LIMIT_SELL"
  val shares: Double,
  val limitPrice: Double? = null,
  val executionPrice: Double? = null,
  val totalAmount: Double,
  val status: String, // "FILLED", "PENDING", "CANCELLED"
  val timestamp: Long = System.currentTimeMillis(),
  val realizedPnL: Double = 0.0
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
  @PrimaryKey val ticker: String,
  val addedAt: Long = System.currentTimeMillis()
)
