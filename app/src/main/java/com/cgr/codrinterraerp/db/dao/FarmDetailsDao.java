package com.cgr.codrinterraerp.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Upsert;

import com.cgr.codrinterraerp.db.entities.FarmDetails;

import java.util.List;

@Dao
public interface FarmDetailsDao {

    @Upsert
    void upsert(List<FarmDetails> list);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrUpdateFarmDetails(FarmDetails farmDetails);

    @Query("SELECT * FROM farm_details WHERE tempFarmId = :tempFarmId AND isDeleted = 0")
    FarmDetails fetchFarmDetailById(String tempFarmId);

    @Query("SELECT COUNT(*) FROM farm_details " +
            "WHERE ica = :inventoryOrder " +
            "AND supplierId = :supplierId " +
            "AND tempFarmId != :tempFarmId " +
            "AND isDeleted = 0")
    int getFarmInventoryOrdersCountForEdit(String inventoryOrder, int supplierId, String tempFarmId);

    @Query("UPDATE farm_details SET isEdited = 1, isSynced = 0, updatedAt = :updatedAt, isDeleted = 1 WHERE tempFarmId = :tempFarmId")
    int deleteFarmDetails(String tempFarmId, long updatedAt);

    @Query("UPDATE farm_details SET isSynced = 0, isClosed = :isClose, closedBy = :closedBy, closedDate = :closedDate WHERE tempFarmId = :tempFarmId")
    int closeFarmDetails(String tempFarmId, long closedDate, int closedBy, boolean isClose);

    @Query("DELETE FROM farm_details WHERE createdAt < :threeMonthsAgo AND isSynced = 1 AND isClosed = 1")
    void deleteOldData(long threeMonthsAgo);
}