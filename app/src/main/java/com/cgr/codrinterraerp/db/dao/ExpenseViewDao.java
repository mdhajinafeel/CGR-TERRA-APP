package com.cgr.codrinterraerp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.cgr.codrinterraerp.db.views.ExpenseView;

import java.util.List;

@Dao
public interface ExpenseViewDao {

    @Query("SELECT * FROM expense_view ORDER BY tempTransactionId DESC LIMIT 10")
    LiveData<List<ExpenseView>> getRecentExpenseList();

    @Query("SELECT * FROM expense_view WHERE isDeleted = 0 ORDER BY capturedTimeStamp DESC")
    List<ExpenseView> getAllExpenseData();

    @Query("SELECT * FROM expense_view WHERE isDeleted = 0 " +
            "AND (:accountHeadId = 0 OR accountHeadId = :accountHeadId) AND (:transactionId = 0 OR creditTransactionId = :transactionId) AND " +
            "( :startDate IS NULL OR :startDate = '' OR :endDate IS NULL OR :endDate = '' OR " +
            "date(substr(expenseDate, 7, 4) || '-' || substr(expenseDate, 4, 2) || '-' || substr(expenseDate, 1, 2)) BETWEEN :startDate AND :endDate) " +
            "ORDER BY capturedTimeStamp DESC")
    List<ExpenseView> getFilteredExpenseTransactions(int transactionId, int accountHeadId, String startDate, String endDate);
}