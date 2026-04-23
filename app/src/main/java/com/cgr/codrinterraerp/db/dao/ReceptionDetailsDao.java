package com.cgr.codrinterraerp.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.cgr.codrinterraerp.db.entities.ReceptionDetails;

@Dao
public interface ReceptionDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    int insertOrUpdateReceptionDetails(ReceptionDetails receptionDetails);
}