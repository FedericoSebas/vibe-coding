package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
  @Query("SELECT * FROM user_account WHERE id = 1 LIMIT 1")
  fun getUserAccount(): Flow<UserAccountEntity?>

  @Query("SELECT * FROM user_account WHERE id = 1 LIMIT 1")
  suspend fun getUserAccountDirect(): UserAccountEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateAccount(account: UserAccountEntity)

  @Query("SELECT * FROM positions ORDER BY totalInvested DESC")
  fun getAllPositions(): Flow<List<PositionEntity>>

  @Query("SELECT * FROM positions WHERE ticker = :ticker LIMIT 1")
  suspend fun getPositionByTicker(ticker: String): PositionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdatePosition(position: PositionEntity)

  @Query("DELETE FROM positions WHERE ticker = :ticker")
  suspend fun deletePosition(ticker: String)

  @Query("DELETE FROM positions")
  suspend fun clearAllPositions()
}

@Dao
interface OrderDao {
  @Query("SELECT * FROM orders ORDER BY timestamp DESC")
  fun getAllOrders(): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE status = 'PENDING' ORDER BY timestamp DESC")
  fun getPendingOrders(): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE status = 'PENDING'")
  suspend fun getPendingOrdersList(): List<OrderEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrder(order: OrderEntity): Long

  @Update
  suspend fun updateOrder(order: OrderEntity)

  @Query("UPDATE orders SET status = 'CANCELLED' WHERE id = :orderId")
  suspend fun cancelOrder(orderId: Long)

  @Query("DELETE FROM orders")
  suspend fun clearAllOrders()
}

@Dao
interface WatchlistDao {
  @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
  fun getWatchlist(): Flow<List<WatchlistEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun addToWatchlist(item: WatchlistEntity)

  @Query("DELETE FROM watchlist WHERE ticker = :ticker")
  suspend fun removeFromWatchlist(ticker: String)

  @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE ticker = :ticker)")
  fun isWatchlisted(ticker: String): Flow<Boolean>
}
