package com.cgr.codrinterraerp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import com.cgr.codrinterraerp.db.entities.FarmData;
import com.cgr.codrinterraerp.db.entities.ReceptionSummary;
import com.cgr.codrinterraerp.model.ReceptionWithContainer;

import java.util.List;

@Dao
public interface FarmDataDao {

    @Upsert
    void upsert(List<FarmData> list);

    @Query("SELECT IFNULL(SUM(pieces),0) FROM farm_data WHERE tempFarmId = :tempId AND isDeleted = 0")
    int sumPiecesByTempFarmId(String tempId);

    @Query("SELECT IFNULL(SUM(grossVolume),0) FROM farm_data WHERE tempFarmId = :tempId AND isDeleted = 0")
    double sumGrossByTempFarmId(String tempId);

    @Query("SELECT IFNULL(SUM(netVolume),0) FROM farm_data WHERE tempFarmId = :tempId AND isDeleted = 0")
    double sumNetByTempFarmId(String tempId);

    @Query("SELECT IFNULL(SUM(volumePie),0) FROM farm_data WHERE tempFarmId = :tempId AND isDeleted = 0")
    double sumPieByTempFarmId(String tempId);

    @Query("SELECT r.circumference, r.length, r.pieces, d.containerNumber, r.grossVolume, r.netVolume, r.tempReceptionDataId, " +
            "r.receptionDataId, r.tempReceptionId, r.thickness, r.width, r.volumePie " +
            "FROM reception_data r " +
            "JOIN container_data c ON c.containerReceptionMappingId = r.containerReceptionMappingId " +
            "AND c.tempReceptionDataId = r.tempReceptionDataId " +
            "JOIN dispatch_details d ON d.tempDispatchId = c.tempDispatchId " +
            "WHERE r.tempReceptionId = :tempReceptionId AND r.isDeleted = 0 AND c.isDeleted = 0 AND d.isDeleted = 0 ORDER BY r.createdAt DESC")
    LiveData<List<ReceptionWithContainer>> fetchByTempFarmId(String tempReceptionId);

    @Query("UPDATE farm_data SET updatedAt = :updatedAt, isDeleted = 1 WHERE tempFarmId = :tempFarmId")
    int deleteFarmData(String tempFarmId, long updatedAt);

    @Query("UPDATE farm_data SET isDeleted = 1, updatedAt = :updatedAt WHERE tempFarmDataId IN (:tempFarmDataId)")
    int deleteByFarmDataIds(List<String> tempFarmDataId, long updatedAt);

    @Query("SELECT DISTINCT c.tempDispatchId " +
            "FROM reception_data r " +
            "JOIN container_data c ON c.containerReceptionMappingId = r.containerReceptionMappingId " +
            "AND c.tempReceptionDataId = r.tempReceptionDataId " +
            "WHERE r.tempReceptionId = :tempReceptionId AND r.isDeleted = 0 AND c.isDeleted = 0;")
    List<String> getAllDispatchIds(String tempReceptionId);

    @Query("UPDATE farm_data SET isDeleted = 1 WHERE tempFarmId = :tempFarmId AND tempFarmDataId = :tempFarmDataId")
    int deleteFarmDataById(String tempFarmDataId, String tempFarmId);

    @Query("SELECT 0 as updatedAt, tempReceptionId as tempReceptionId, " +
            "IFNULL(SUM(pieces),0) AS totalPieces, " +
            "IFNULL(SUM(grossVolume),0) AS totalGrossVolume, " +
            "IFNULL(SUM(netVolume),0) AS totalNetVolume, IFNULL(SUM(volumePie),0) AS totalVolumePie " +
            "FROM reception_data " +
            "WHERE tempReceptionId = :tempReceptionId AND isDeleted = 0")
    LiveData<ReceptionSummary> getSummaryByTempId(String tempReceptionId);

    @Query("SELECT tempFarmId FROM farm_data WHERE tempFarmDataId = :tempFarmDataId AND tempFarmId = :tempFarmId")
    String getAllFarmId(String tempFarmId, String tempFarmDataId);
}