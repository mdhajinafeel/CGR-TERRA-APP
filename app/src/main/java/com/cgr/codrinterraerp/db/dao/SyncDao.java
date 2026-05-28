package com.cgr.codrinterraerp.db.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.cgr.codrinterraerp.db.entities.ContainerData;
import com.cgr.codrinterraerp.db.entities.ContainerImages;
import com.cgr.codrinterraerp.db.entities.FarmData;
import com.cgr.codrinterraerp.db.entities.ReceptionData;
import com.cgr.codrinterraerp.model.DispatchDetailsWithTotals;
import com.cgr.codrinterraerp.model.FarmDetailsWithTotals;
import com.cgr.codrinterraerp.model.ReceptionDetailsWithTotals;

import java.util.List;

@Dao
public interface SyncDao {

    // =====================
    // CONTAINER PHOTOS
    // =====================
    @Query("SELECT * FROM container_images WHERE isDeleted = 0 AND isSynced = 0 ORDER BY createdAt ASC")
    List<ContainerImages> getUnsyncedImages();

    @Query("UPDATE container_images SET serverImageUrl = :url, isSynced=1 WHERE tempDispatchId = :tempDispatchId AND tempContainerImageId=:tempContainerImageId")
    void updateImageSync(String tempContainerImageId, String tempDispatchId, String url);

    @Query("UPDATE container_images SET isSynced=0 WHERE tempContainerImageId = :tempId")
    void markFailed(String tempId);

    @Query("UPDATE container_images SET imagePath = NULL WHERE tempDispatchId = :tempDispatchId AND tempContainerImageId = :tempContainerImageId")
    void clearLocalFilePath(String tempDispatchId, String tempContainerImageId);

    // =====================
    // RECEPTION
    // =====================
    @Transaction
    @Query(
            "SELECT " +
                    "rd.*, " +
                    "IFNULL(rs.totalPieces, 0) AS totalPieces, " +
                    "IFNULL(rs.totalGrossVolume, 0) AS totalGrossVolume, " +
                    "IFNULL(rs.totalNetVolume, 0) AS totalNetVolume, " +
                    "IFNULL(rs.totalVolumePie, 0) AS totalVolumePie " +
                    "FROM reception_details rd " +
                    "LEFT JOIN reception_summary rs " +
                    "ON rd.tempReceptionId = rs.tempReceptionId WHERE rd.isSynced = 0 AND rd.isDeleted = 0"
    )
    List<ReceptionDetailsWithTotals> getUnsyncedReceptionDetails();

    @Query("UPDATE reception_details SET receptionId=:receptionId, isSynced=1, isEdited=0 WHERE tempReceptionId=:tempReceptionId")
    void updateReceptionMapping(String tempReceptionId, int receptionId);

    // =====================
    // RECEPTION DATA
    // =====================
    @Query("SELECT * FROM reception_data WHERE isDeleted = 0 AND isSynced = 0")
    List<ReceptionData> getUnsyncedReceptionData();

    @Query("UPDATE reception_data SET receptionDataId = :receptionDataId, receptionId = :receptionId, " +
            "containerReceptionMappingId = :receptionContainerMappingId, isSynced=1, isEdited=0 WHERE tempReceptionDataId=:tempReceptionDataId AND tempReceptionId = :tempReceptionId")
    void updateReceptionDataMapping(String tempReceptionDataId, String tempReceptionId, int receptionDataId, int receptionId, int receptionContainerMappingId);

    // =====================
    // DISPATCH
    // =====================
    @Transaction
    @Query(
            "SELECT " +
                    "dd.*, " +
                    "IFNULL(ds.totalPieces, 0) AS totalPieces, " +
                    "IFNULL(ds.totalGrossVolume, 0) AS totalGrossVolume, " +
                    "IFNULL(ds.totalNetVolume, 0) AS totalNetVolume, " +
                    "IFNULL(ds.avgGirth, 0) AS avgGirth, " +
                    "IFNULL(ds.cft, 0) AS cft, " +
                    "IFNULL(ds.totalVolumePie, 0) AS totalVolumePie " +
                    "FROM dispatch_details dd " +
                    "LEFT JOIN dispatch_summary ds " +
                    "ON dd.tempDispatchId = ds.tempDispatchId WHERE dd.isDeleted = 0 AND dd.isSynced = 0"
    )
    List<DispatchDetailsWithTotals> getUnsyncedDispatch();

    @Query("UPDATE dispatch_details SET dispatchId=:dispatchId, isSynced=1, isEdited=0 WHERE tempDispatchId=:tempDispatchId")
    void updateDispatchMapping(String tempDispatchId, int dispatchId);

    // =====================
    // CONTAINER DATA
    // =====================

    @Query("SELECT * FROM container_data WHERE isDeleted = 0 AND isSynced = 0")
    List<ContainerData> getUnsyncedContainerData();

    @Query("UPDATE container_data SET dispatchDataId = :dispatchDataId, receptionDataId = :receptionDataId, receptionId = :receptionId, dispatchId = :dispatchId, " +
            "containerReceptionMappingId = :containerReceptionMappingId, " +
            "isSynced=1, isEdited=0 WHERE tempReceptionId = :tempReceptionId AND tempReceptionId = :tempReceptionId " +
            "AND tempReceptionDataId=:tempReceptionDataId AND tempDispatchId = :tempDispatchId")
    void updateContainerDataMapping(String tempReceptionDataId, String tempDispatchId, int dispatchDataId, String containerReceptionMappingId, int receptionDataId,
                                    String tempReceptionId, int receptionId, int dispatchId);

    // =====================
    // FARM
    // =====================
    @Transaction
    @Query(
            "SELECT " +
                    "fd.*, " +
                    "IFNULL(fs.totalPieces, 0) AS totalPieces, " +
                    "IFNULL(fs.totalGrossVolume, 0) AS totalGrossVolume, " +
                    "IFNULL(fs.totalNetVolume, 0) AS totalNetVolume, " +
                    "IFNULL(fs.totalVolumePie, 0) AS totalVolumePie " +
                    "FROM farm_details fd " +
                    "LEFT JOIN farm_summary fs " +
                    "ON fd.tempFarmId = fs.tempFarmId WHERE fd.isSynced = 0 AND fd.isDeleted = 0"
    )
    List<FarmDetailsWithTotals> getUnsyncedFarmDetails();

    @Query("UPDATE farm_details SET farmId=:farmId, isSynced=1, isEdited=0 WHERE tempFarmId=:tempFarmId")
    void updateFarmMapping(String tempFarmId, int farmId);

    // =====================
    // FARM DATA
    // =====================
    @Query("SELECT * FROM farm_data WHERE isDeleted = 0 AND isSynced = 0")
    List<FarmData> getUnsyncedFarmData();

    @Query("UPDATE farm_data SET farmDataId = :farmDataId, farmId = :farmId, " +
            "isSynced=1, isEdited=0 WHERE tempFarmDataId=:tempFarmDataId AND tempFarmId = :tempFarmId")
    void updateFarmDataMapping(String tempFarmDataId, String tempFarmId, int farmDataId, int farmId);

    // ====================
    // UNSYNCED COUNT
    // ====================
    @Query("SELECT COUNT(*)  FROM reception_details  WHERE isSynced = 0 AND NOT (isDeleted = 1 AND IFNULL(receptionId, 0) = 0)")
    int getUnsyncedReceptionDetailsCount();

    @Query("SELECT COUNT(*) FROM reception_data WHERE isSynced = 0 AND NOT (isDeleted = 1 AND IFNULL(receptionDataId, 0) = 0)")
    int getUnsyncedReceptionDataCount();

    @Query("SELECT COUNT(*) FROM dispatch_details WHERE isSynced = 0 AND NOT (isDeleted = 1 AND IFNULL(dispatchId, 0) = 0)")
    int getUnsyncedDispatchDetailsCount();

    @Query("SELECT COUNT(*) FROM container_data WHERE isSynced = 0 AND NOT (isDeleted = 1 AND IFNULL(dispatchDataId, 0) = 0)")
    int getUnsyncedContainerDataCount();

    @Query("SELECT COUNT(*) FROM container_images WHERE isSynced = 0 AND NOT (isDeleted = 1 AND IFNULL(serverImageUrl, '') = '')")
    int getUnsyncedContainerImagesCount();

    @Query("SELECT COUNT(*)  FROM farm_details  WHERE isSynced = 0 AND NOT (isDeleted = 1 AND IFNULL(farmId, 0) = 0)")
    int getUnsyncedFarmDetailsCount();

    @Query("SELECT COUNT(*) FROM farm_data WHERE isSynced = 0 AND NOT (isDeleted = 1 AND IFNULL(farmDataId, 0) = 0)")
    int getUnsyncedFarmDataCount();

    // ====================
    // DELETE LOCAL COUNT
    // ====================
    @Query("DELETE FROM reception_details WHERE isDeleted = 1 AND isSynced = 0 AND IFNULL(receptionId, 0) = 0")
    void deleteLocalDeletedReceptionDetails();

    @Query("DELETE FROM reception_data WHERE isDeleted = 1 AND isSynced = 0 AND IFNULL(receptionDataId, 0) = 0")
    void deleteLocalDeletedReceptionData();

    @Query("DELETE FROM dispatch_details WHERE isDeleted = 1 AND isSynced = 0 AND IFNULL(dispatchId, 0) = 0")
    void deleteLocalDeletedDispatchDetails();

    @Query("DELETE FROM container_data WHERE isDeleted = 1 AND isSynced = 0 AND IFNULL(dispatchDataId, 0) = 0")
    void deleteLocalDeletedContainerData();

    @Query("DELETE FROM container_images WHERE isDeleted = 1 AND isSynced = 0 AND IFNULL(serverImageUrl, '') = ''")
    void deleteLocalDeletedImages();

    @Query("DELETE FROM farm_details WHERE isDeleted = 1 AND isSynced = 0 AND IFNULL(farmId, 0) = 0")
    void deleteLocalDeletedFarmDetails();

    @Query("DELETE FROM farm_data WHERE isDeleted = 1 AND isSynced = 0 AND IFNULL(farmDataId, 0) = 0")
    void deleteLocalDeletedFarmData();
}