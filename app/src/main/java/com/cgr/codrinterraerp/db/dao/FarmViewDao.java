package com.cgr.codrinterraerp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.cgr.codrinterraerp.db.views.FarmView;

import java.util.List;

@Dao
public interface FarmViewDao {

    // Optional (recommended)
    @Query("SELECT * FROM farm_view WHERE isClosed = :isClosed ORDER BY tempFarmId DESC")
    LiveData<List<FarmView>> getFarmList(boolean isClosed);
}