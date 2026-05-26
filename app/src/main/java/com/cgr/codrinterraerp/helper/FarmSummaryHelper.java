package com.cgr.codrinterraerp.helper;

import com.cgr.codrinterraerp.db.dao.FarmDataDao;
import com.cgr.codrinterraerp.db.entities.FarmSummary;
import com.cgr.codrinterraerp.utils.CommonUtils;

import javax.inject.Inject;

public class FarmSummaryHelper {

    private final FarmDataDao farmDataDao;

    @Inject
    public FarmSummaryHelper(FarmDataDao farmDataDao) {
        this.farmDataDao = farmDataDao;
    }

    public FarmSummary calculate(String tempFarmId) {

        FarmSummary s = new FarmSummary();

        // ✅ ONLY use tempReceptionId
        s.tempFarmId = tempFarmId;

        s.totalPieces = farmDataDao.sumPiecesByTempFarmId(tempFarmId);
        s.totalGrossVolume = CommonUtils.round(farmDataDao.sumGrossByTempFarmId(tempFarmId), 3);
        s.totalNetVolume = CommonUtils.round(farmDataDao.sumNetByTempFarmId(tempFarmId), 3);
        s.totalVolumePie = CommonUtils.round(farmDataDao.sumPieByTempFarmId(tempFarmId), 3);

        s.updatedAt = System.currentTimeMillis();
        return s;
    }
}