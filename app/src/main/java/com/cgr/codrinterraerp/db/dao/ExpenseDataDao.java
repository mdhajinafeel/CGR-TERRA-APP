package com.cgr.codrinterraerp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cgr.codrinterraerp.db.entities.ExpenseData;
import com.cgr.codrinterraerp.model.AccountHeadReportData;

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
    @Query("SELECT IFNULL(SUM(amount), 0) AS totalAmount, B.accountHeadId, B.accountHeadName, B.colorCodePrimary, B.colorCodeSecondary, " +
            "(SUM(amount)*100)/(SELECT sum(amount) FROM expense_data WHERE isDeleted = 0 AND (:transactionId = 0 OR A.creditTransactionId = :transactionId)  " +
            "AND ( :startDate IS NULL OR :startDate = '' OR :endDate IS NULL OR :endDate = '' OR " +
            "date(substr(A.expenseDate, 7, 4) || '-' || substr(A.expenseDate, 4, 2) || '-' || substr(A.expenseDate, 1, 2)) BETWEEN :startDate AND :endDate))  AS percentage, B.icon " +
            "FROM expense_data A INNER JOIN account_heads B ON B.accountHeadId = A.accountHeadId " +
            "WHERE isDeleted = 0 AND (:transactionId = 0 OR A.creditTransactionId = :transactionId)  " +
            "AND ( :startDate IS NULL OR :startDate = '' OR :endDate IS NULL OR :endDate = '' OR " +
            "date(substr(A.expenseDate, 7, 4) || '-' || substr(A.expenseDate, 4, 2) || '-' || substr(A.expenseDate, 1, 2)) BETWEEN :startDate AND :endDate)" +
            " GROUP BY A.accountHeadId")
    List<AccountHeadReportData> getExpenseByAccountHead(int transactionId, String startDate, String endDate);

    @Query("SELECT IFNULL(SUM(amount), 0) AS totalAmount " +
            "FROM expense_data A " +
            "WHERE isDeleted = 0 AND (:transactionId = 0 OR A.creditTransactionId = :transactionId)  " +
            "AND ( :startDate IS NULL OR :startDate = '' OR :endDate IS NULL OR :endDate = '' OR " +
            "date(substr(A.expenseDate, 7, 4) || '-' || substr(A.expenseDate, 4, 2) || '-' || substr(A.expenseDate, 1, 2)) BETWEEN :startDate AND :endDate)")
    double getExpenseTotal(int transactionId, String startDate, String endDate);
}