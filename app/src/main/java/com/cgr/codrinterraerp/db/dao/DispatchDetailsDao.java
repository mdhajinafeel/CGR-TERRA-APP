package com.cgr.codrinterraerp.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Upsert;

import com.cgr.codrinterraerp.db.entities.DispatchDetails;

import java.util.List;

@Dao
public interface DispatchDetailsDao {

    @Upsert
    void upsert(List<DispatchDetails> list);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrUpdateDispatchDetails(DispatchDetails dispatchDetails);

    @Query("SELECT * FROM dispatch_details WHERE tempDispatchId = :tempDispatchId AND isDeleted = 0")
    DispatchDetails fetchDispatchDetailById(String tempDispatchId);

    @Query("SELECT COUNT(*) FROM dispatch_details " +
            "WHERE containerNumber = :containerNumber " +
            "AND shippingLineId = :shippingLineId " +
            "AND tempDispatchId != :tempDispatchId " +
            "AND isDeleted = 0")
    int getDispatchContainersCountForEdit(String containerNumber, int shippingLineId, String tempDispatchId);

    @Query("UPDATE dispatch_details SET isDeleted = 1, updatedAt = :updatedAt WHERE tempDispatchId = :tempDispatchId")
    int deleteDispatch(String tempDispatchId, long updatedAt);

    @Query("UPDATE dispatch_details SET isSynced = 0, isClosed = :isClose, closedBy = :closedBy, closedDate = :closedDate WHERE tempDispatchId = :tempDispatchId")
    int closeDispatchDetails(String tempDispatchId, long closedDate, int closedBy, boolean isClose);

    @Query("DELETE FROM dispatch_details WHERE createdAt < :threeMonthsAgo AND isSynced = 1 AND isClosed = 1")
    void deleteOldData(long threeMonthsAgo);
}