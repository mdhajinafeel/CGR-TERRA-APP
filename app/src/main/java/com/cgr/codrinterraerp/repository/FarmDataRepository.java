package com.cgr.codrinterraerp.repository;

import androidx.lifecycle.LiveData;

import com.cgr.codrinterraerp.db.dao.FarmDataDao;
import com.cgr.codrinterraerp.db.dao.FarmTransactionDao;
import com.cgr.codrinterraerp.db.entities.FarmData;
import com.cgr.codrinterraerp.db.entities.FarmSummary;
import com.cgr.codrinterraerp.model.FarmCapturedData;

import java.util.List;

public class FarmDataRepository {

    private final FarmTransactionDao farmTransactionDao;
    private final FarmDataDao farmDataDao;
    private final FarmRepository farmRepository;

    public FarmDataRepository(FarmTransactionDao farmTransactionDao, FarmDataDao farmDataDao, FarmRepository farmRepository) {
        this.farmTransactionDao = farmTransactionDao;
        this.farmDataDao = farmDataDao;
        this.farmRepository = farmRepository;
    }

    // ✅ SAVE (TRANSACTION SAFE)
    public boolean saveMeasurementData(FarmData farmData) {

        boolean isSaved = farmTransactionDao.saveMeasurementData(farmData);

        if (isSaved) {
            farmRepository.updateSummary(farmData.getTempFarmId());
        }

        return isSaved;
    }

    public LiveData<List<FarmCapturedData>> fetchFarmData(String tempFarmId) {
        return farmDataDao.fetchByTempFarmId(tempFarmId);
    }

    public int deleteFarmDataById(String tempFarmDataId, String tempFarmId) {
        int deleteData = farmDataDao.deleteFarmDataById(tempFarmDataId, tempFarmId);

        if (deleteData > 0) {
            farmRepository.updateSummary(tempFarmId);
        }

        return deleteData;
    }

    public LiveData<FarmSummary> getFarmSummary(String tempFarmId) {
        return farmDataDao.getSummaryByTempId(tempFarmId);
    }
}