package com.cgr.codrinterraerp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cgr.codrinterraerp.db.entities.ReceptionData;
import com.cgr.codrinterraerp.db.entities.ReceptionSummary;
import com.cgr.codrinterraerp.model.ReceptionWithContainer;

import java.util.List;

@Dao
public interface ReceptionDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReceptionData(List<ReceptionData> receptionDataList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertReceptionData(ReceptionData receptionData);

    @Query("SELECT IFNULL(ROUND(CASE WHEN SUM(c.pieces) = 0 THEN 0 ELSE SUM(r.circumference * c.pieces) / SUM(c.pieces) END, 3), 0) " +
            "FROM container_data c " +
            "JOIN reception_data r ON r.receptionDataId = c.receptionDataId AND r.receptionId = c.receptionId " +
            "WHERE dispatchId = :dispatchId AND c.isDeleted = 0")
    double avgGirthByDispatch(Integer dispatchId);

    @Query("SELECT IFNULL(ROUND(CASE WHEN SUM(c.pieces) = 0 THEN 0 ELSE SUM(r.circumference * c.pieces) / SUM(c.pieces) END, 3), 0) " +
            "FROM container_data c " +
            "JOIN reception_data r ON r.tempReceptionDataId = c.tempReceptionDataId AND r.tempReceptionId = c.tempReceptionId " +
            "WHERE tempDispatchId = :tempDispatchId AND c.isDeleted = 0")
    double avgGirthByTempDispatchId(String tempDispatchId);

    @Query("SELECT IFNULL(SUM(pieces),0) FROM reception_data WHERE receptionId = :receptionId AND isDeleted = 0")
    int sumPiecesByReceptionId(Integer receptionId);

    @Query("SELECT IFNULL(SUM(pieces),0) FROM reception_data WHERE tempReceptionId = :tempId AND isDeleted = 0")
    int sumPiecesByTempReceptionId(String tempId);

    @Query("SELECT IFNULL(SUM(grossVolume),0) FROM reception_data WHERE receptionId = :receptionId AND isDeleted = 0")
    double sumGrossByReceptionId(Integer receptionId);

    @Query("SELECT IFNULL(SUM(grossVolume),0) FROM reception_data WHERE tempReceptionId = :tempId AND isDeleted = 0")
    double sumGrossByTempReceptionId(String tempId);

    @Query("SELECT IFNULL(SUM(netVolume),0) FROM reception_data WHERE receptionId = :receptionId AND isDeleted = 0")
    double sumNetByReceptionId(Integer receptionId);

    @Query("SELECT IFNULL(SUM(netVolume),0) FROM reception_data WHERE tempReceptionId = :tempId AND isDeleted = 0")
    double sumNetByTempReceptionId(String tempId);

    @Query("SELECT r.circumference, r.length, r.pieces, d.containerNumber, r.grossVolume, r.netVolume, r.tempReceptionDataId, r.receptionDataId, r.tempReceptionId " +
            "FROM reception_data r " +
            "JOIN container_data c ON c.containerReceptionMappingId = r.containerReceptionMappingId " +
            "AND c.receptionDataId = r.receptionDataId " +
            "JOIN dispatch_details d ON d.tempDispatchId = c.tempDispatchId " +
            "WHERE r.receptionId = :receptionId AND r.isDeleted = 0 AND c.isDeleted = 0 AND d.isDeleted = 0;")
    LiveData<List<ReceptionWithContainer>> fetchByReceptionId(Integer receptionId);

    @Query("SELECT r.circumference, r.length, r.pieces, d.containerNumber, r.grossVolume, r.netVolume, r.tempReceptionDataId, r.receptionDataId, r.tempReceptionId " +
            "FROM reception_data r " +
            "JOIN container_data c ON c.containerReceptionMappingId = r.containerReceptionMappingId " +
            "AND c.tempReceptionDataId = r.tempReceptionDataId " +
            "JOIN dispatch_details d ON d.tempDispatchId = c.tempDispatchId " +
            "WHERE r.tempReceptionId = :tempReceptionId AND r.isDeleted = 0 AND c.isDeleted = 0 AND d.isDeleted = 0;")
    LiveData<List<ReceptionWithContainer>> fetchByTempReceptionId(String tempReceptionId);

    @Query("UPDATE reception_data SET updatedAt = :updatedAt, isDeleted = 1 WHERE tempReceptionId = :tempReceptionId")
    int deleteReceptionData(String tempReceptionId, long updatedAt);

    @Query("UPDATE reception_data SET isDeleted = 1, updatedAt = :updatedAt WHERE tempReceptionDataId IN (:tempReceptionDataIds)")
    int deleteByReceptionDataIds(List<String> tempReceptionDataIds, long updatedAt);

    @Query("SELECT DISTINCT c.tempDispatchId " +
            "FROM reception_data r " +
            "JOIN container_data c ON c.containerReceptionMappingId = r.containerReceptionMappingId " +
            "AND c.tempReceptionDataId = r.tempReceptionDataId " +
            "WHERE r.tempReceptionId = :tempReceptionId AND r.isDeleted = 0 AND c.isDeleted = 0;")
    List<String> getAllDispatchIds(String tempReceptionId);

    @Query("UPDATE reception_data SET isDeleted = 1 WHERE tempReceptionId = :tempReceptionId AND tempReceptionDataId = :tempReceptionDataId")
    int deleteReceptionDataById(String tempReceptionDataId, String tempReceptionId);

    @Query("SELECT 0 as id, 0 as updatedAt, '' as tempReceptionId, '' as receptionId, " +
            "IFNULL(SUM(pieces),0) AS totalPieces, " +
            "IFNULL(SUM(grossVolume),0) AS totalGrossVolume, " +
            "IFNULL(SUM(netVolume),0) AS totalNetVolume " +
            "FROM reception_data " +
            "WHERE tempReceptionId = :tempReceptionId AND isDeleted = 0")
    LiveData<ReceptionSummary> getSummaryByTempId(String tempReceptionId);

    @Query("SELECT tempReceptionId FROM reception_data WHERE tempReceptionDataId = :tempReceptionDataId AND tempReceptionId = :tempReceptionId")
    String getAllReceptionId(String tempReceptionId, String tempReceptionDataId);
}