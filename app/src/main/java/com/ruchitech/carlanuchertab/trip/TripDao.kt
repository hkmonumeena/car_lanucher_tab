package com.ruchitech.carlanuchertab.trip

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips WHERE status = 'ONGOING' ORDER BY startedAt DESC LIMIT 1")
    fun observeActiveTrip(): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE status = 'COMPLETED' ORDER BY endedAt DESC, startedAt DESC")
    fun observeCompletedTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun observeTrip(tripId: Long): Flow<TripEntity?>

    @Query("SELECT * FROM trip_persons WHERE tripId = :tripId ORDER BY id ASC")
    fun observePersons(tripId: Long): Flow<List<TripPersonEntity>>

    @Transaction
    @Query("SELECT * FROM trip_expenses WHERE tripId = :tripId ORDER BY createdAt DESC, id DESC")
    fun observeExpensesWithShares(tripId: Long): Flow<List<TripExpenseWithShares>>

    @Insert
    suspend fun insertTrip(trip: TripEntity): Long

    @Insert
    suspend fun insertPerson(person: TripPersonEntity): Long

    @Insert
    suspend fun insertPersons(persons: List<TripPersonEntity>)

    @Insert
    suspend fun insertExpense(expense: TripExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShares(shares: List<TripExpenseShareEntity>)

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Update
    suspend fun updatePerson(person: TripPersonEntity)

    @Update
    suspend fun updateExpense(expense: TripExpenseEntity)

    @Query("DELETE FROM trip_expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: Long)

    @Query("DELETE FROM trip_expense_shares WHERE expenseId = :expenseId")
    suspend fun deleteSharesForExpense(expenseId: Long)

    @Transaction
    suspend fun createTrip(name: String, startOdo: Int, personNames: List<String>, notes: String?) : Long {
        val tripId = insertTrip(TripEntity(name = name, startOdo = startOdo, notes = notes))
        insertPersons(personNames.map { TripPersonEntity(tripId = tripId, name = it) })
        return tripId
    }

    @Transaction
    suspend fun addExpenseWithShares(expense: TripExpenseEntity, selectedPersonIds: Set<Long>): Long {
        val expenseId = insertExpense(expense)
        if (expense.splitEnabled) {
            insertShares(selectedPersonIds.map { TripExpenseShareEntity(expenseId = expenseId, personId = it) })
        }
        return expenseId
    }

    @Transaction
    suspend fun replaceExpenseWithShares(expense: TripExpenseEntity, selectedPersonIds: Set<Long>) {
        updateExpense(expense)
        deleteSharesForExpense(expense.id)
        if (expense.splitEnabled) {
            insertShares(selectedPersonIds.map { TripExpenseShareEntity(expenseId = expense.id, personId = it) })
        }
    }
}

data class TripExpenseWithShares(
    @Embedded val expense: TripExpenseEntity,
    @Relation(parentColumn = "id", entityColumn = "expenseId")
    val shares: List<TripExpenseShareEntity>,
)
