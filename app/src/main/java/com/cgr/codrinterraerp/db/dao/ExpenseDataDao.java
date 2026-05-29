package com.cgr.codrinterraerp.db.dao;

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
}