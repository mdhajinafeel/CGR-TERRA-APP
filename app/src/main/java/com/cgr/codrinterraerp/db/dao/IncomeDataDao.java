package com.cgr.codrinterraerp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cgr.codrinterraerp.db.entities.IncomeData;

import java.util.List;

@Dao
public interface IncomeDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIncomeData(List<IncomeData> incomeDataList);

    @Query("SELECT * FROM income_data ORDER BY transactionTimestamp DESC")
    LiveData<List<IncomeData>> getIncomeData();

    @Query("SELECT * FROM income_data ORDER BY transactionTimestamp DESC LIMIT 10")
    LiveData<List<IncomeData>> getRecentIncomeData();

    @Query("SELECT * FROM income_data ORDER BY transactionTimestamp DESC")
    List<IncomeData> getAllIncomeData();

    @Query("DELETE FROM income_data")
    void clearAll();

    @Query("SELECT IFNULL(SUM(amount), 0) FROM income_data")
    LiveData<Double> getTotalCredit();

    @Query("SELECT IFNULL(SUM(amount), 0) FROM income_data " +
            "WHERE (:transactionId = 0 OR creditTransactionId = :transactionId)")
    Double getFilteredTotalCredit(int transactionId);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM income_data")
    Double getUnfilteredTotalCredit();
}