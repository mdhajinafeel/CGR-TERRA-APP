package com.cgr.codrinterraerp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cgr.codrinterraerp.db.entities.ContainerData;
import com.cgr.codrinterraerp.db.entities.DispatchSummary;
import com.cgr.codrinterraerp.db.entities.ReceptionSummary;
import com.cgr.codrinterraerp.model.ContainerWithReception;

import java.util.List;

@Dao
public interface ContainerDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertContainerData(List<ContainerData> containerDataList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertContainerData(ContainerData receptionData);

    @Query("SELECT IFNULL(SUM(pieces),0) FROM container_data WHERE dispatchId = :dispatchId AND isDeleted = 0")
    int sumPiecesByDispatchId(Integer dispatchId);

    @Query("SELECT IFNULL(SUM(pieces),0) FROM container_data WHERE tempDispatchId = :tempId AND isDeleted = 0")
    int sumPiecesByTempDispatchId(String tempId);

    @Query("SELECT IFNULL(SUM(grossVolume),0) FROM container_data WHERE dispatchId = :dispatchId AND isDeleted = 0")
    double sumGrossByDispatchId(Integer dispatchId);

    @Query("SELECT IFNULL(SUM(grossVolume),0) FROM container_data WHERE tempDispatchId = :tempId AND isDeleted = 0")
    double sumGrossByTempDispatchId(String tempId);

    @Query("SELECT IFNULL(SUM(netVolume),0) FROM container_data WHERE dispatchId = :dispatchId AND isDeleted = 0")
    double sumNetByDispatchId(Integer dispatchId);

    @Query("SELECT IFNULL(SUM(netVolume),0) FROM container_data WHERE tempDispatchId = :tempId AND isDeleted = 0")
    double sumNetByTempDispatchId(String tempId);

    @Query("UPDATE container_data SET updatedAt = :updatedAt, isDeleted = 1 WHERE tempReceptionId = :tempReceptionId")
    int deleteContainerData(String tempReceptionId, long updatedAt);

    @Query("UPDATE container_data SET isDeleted = 1, updatedAt = :updatedAt WHERE tempDispatchId = :tempDispatchId")
    int deleteByTempDispatchId(String tempDispatchId, long updatedAt);

    // 🔥 Get tempReceptionIds linked to dispatch
    @Query("SELECT DISTINCT tempReceptionDataId FROM container_data WHERE tempDispatchId = :tempDispatchId")
    List<String> getReceptionDataIdsByDispatch(String tempDispatchId);

    @Query("SELECT r.circumference, r.length, r.pieces, rd.ica, r.grossVolume, r.netVolume, c.tempReceptionId, c.tempReceptionDataId, c.tempDispatchId, r.receptionDataId " +
            "FROM container_data c " +
            "JOIN reception_data r ON r.containerReceptionMappingId = c.containerReceptionMappingId AND r.tempReceptionDataId = c.tempReceptionDataId " +
            "JOIN reception_details rd ON rd.tempReceptionId = r.tempReceptionId " +
            "WHERE c.dispatchId = :dispatchId AND c.isDeleted = 0 AND r.isDeleted = 0 AND rd.isDeleted = 0;")
    LiveData<List<ContainerWithReception>> fetchByDispatchId(Integer dispatchId);

    @Query("SELECT r.circumference, r.length, r.pieces, rd.ica, r.grossVolume, r.netVolume, c.tempReceptionId, c.tempReceptionDataId, c.tempDispatchId, r.receptionDataId " +
            "FROM container_data c " +
            "JOIN reception_data r ON r.containerReceptionMappingId = c.containerReceptionMappingId AND r.tempReceptionDataId = c.tempReceptionDataId " +
            "JOIN reception_details rd ON rd.tempReceptionId = r.tempReceptionId " +
            "WHERE c.tempDispatchId = :tempDispatchId AND c.isDeleted = 0 AND r.isDeleted = 0 AND rd.isDeleted = 0;")
    LiveData<List<ContainerWithReception>> fetchByTempDispatchId(String tempDispatchId);

    @Query("SELECT DISTINCT tempReceptionId FROM container_data WHERE isDeleted = 0 AND tempDispatchId = :tempDispatchId")
    List<String> getAllReceptionIds(String tempDispatchId);

    @Query("UPDATE container_data SET isDeleted = 1 WHERE tempReceptionDataId = :tempReceptionDataId AND tempReceptionId = :tempReceptionId")
    void deleteByReceptionDataId(String tempReceptionDataId, String tempReceptionId);

    @Query("SELECT tempDispatchId FROM container_data WHERE tempReceptionDataId = :tempReceptionDataId AND tempReceptionId = :tempReceptionId")
    String getAllDispatchId(String tempReceptionId, String tempReceptionDataId);

    @Query("SELECT 0 AS id, 0 AS dispatchId, '' AS tempDispatchId, 0 AS avgGirth, 0 AS updatedAt, " +
            "IFNULL(SUM(r.pieces), 0) AS totalPieces, IFNULL(SUM(r.grossVolume), 0) AS totalGrossVolume, IFNULL(SUM(r.netVolume), 0) AS totalNetVolume " +
            "FROM container_data c " +
            "JOIN reception_data r ON r.tempReceptionId = c.tempReceptionId AND r.tempReceptionDataId = c.tempReceptionDataId " +
            "WHERE c.isDeleted = 0 AND c.tempDispatchId = :tempDispatchId")
    LiveData<DispatchSummary> getSummaryByTempId(String tempDispatchId);

    @Query("UPDATE container_data SET isDeleted = 1 WHERE tempReceptionId = :tempReceptionId AND tempReceptionDataId = :tempReceptionDataId AND tempDispatchId = :tempDispatchId")
    int deleteContainerDataById(String tempReceptionDataId, String tempReceptionId, String tempDispatchId);
}