package com.cgr.codrinterraerp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cgr.codrinterraerp.db.entities.ExpenseData;

import java.util.List;

@Dao
public interface ExpenseDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExpenseData(List<ExpenseData> expenseDataList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertExpenseData(ExpenseData expenseData);

    @Query("SELECT * FROM expense_data WHERE tempTransactionId = :tempTransactionId")
    ExpenseData getExpenseByTempTransactionId(String tempTransactionId);

    @Query("UPDATE expense_data SET isSynced = 0, isDeleted = 1 WHERE capturedTimeStamp = :capturedTimeStamp")
    int deleteExpenseDataManual(long capturedTimeStamp);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM expense_data WHERE isDeleted = 0")
    LiveData<Double> getTotalDebit();

    @Query("SELECT IFNULL(SUM(amount), 0) FROM expense_data A WHERE A.isDeleted = 0 " +
            "AND (:accountHeadId = 0 OR A.accountHeadId = :accountHeadId) AND (:transactionId = 0 OR A.creditTransactionId = :transactionId) " +
            "AND ( :startDate IS NULL OR :startDate = '' OR :endDate IS NULL OR :endDate = '' OR " +
            "date(substr(A.expenseDate, 7, 4) || '-' || substr(A.expenseDate, 4, 2) || '-' || substr(A.expenseDate, 1, 2)) BETWEEN :startDate AND :endDate)")
    Double getFilteredTotalDebit(int transactionId, int accountHeadId, String startDate, String endDate);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM expense_data WHERE isDeleted = 0")
    Double getUnfilteredTotalDebit();
}