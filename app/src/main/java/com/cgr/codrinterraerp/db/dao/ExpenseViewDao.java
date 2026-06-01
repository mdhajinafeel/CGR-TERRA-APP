package com.cgr.codrinterraerp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.cgr.codrinterraerp.db.views.ExpenseView;

import java.util.List;

@Dao
public interface ExpenseViewDao {

    @Query("SELECT * FROM expense_view ORDER BY tempTransactionId DESC")
    LiveData<List<ExpenseView>> getExpenseList();

    @Query("SELECT * FROM expense_view ORDER BY tempTransactionId DESC LIMIT 10")
    LiveData<List<ExpenseView>> getRecentExpenseList();
}