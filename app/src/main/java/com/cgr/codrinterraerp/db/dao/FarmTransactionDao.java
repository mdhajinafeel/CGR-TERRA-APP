package com.cgr.codrinterraerp.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Transaction;

import com.cgr.codrinterraerp.db.entities.FarmData;
import com.cgr.codrinterraerp.db.entities.ReceptionData;
import com.cgr.codrinterraerp.utils.AppLogger;

@Dao
public abstract class FarmTransactionDao {

    @Insert
    public abstract long insertFarmData(FarmData data);

    @Transaction
    public boolean saveMeasurementData(FarmData farmData) {
        try {
            long receptionId = insertFarmData(farmData);

            return receptionId > 0;

        } catch (Exception e) {
            AppLogger.e(getClass(), "saveMeasurementData", e);
            return false;
        }
    }
}