package com.cgr.codrinterraerp.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Upsert;

import com.cgr.codrinterraerp.db.entities.FarmSummary;

import java.util.List;

@Dao
public interface FarmSummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(FarmSummary summary);

    @Upsert
    void upsert(List<FarmSummary> summaries);
}