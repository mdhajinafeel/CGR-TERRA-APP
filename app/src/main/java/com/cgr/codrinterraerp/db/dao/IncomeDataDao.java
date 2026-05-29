package com.cgr.codrinterraerp.db.dao;

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

    @Query("SELECT * FROM income_data")
    List<IncomeData> getIncomeData();

    @Query("DELETE FROM income_data")
    void clearAll();
}