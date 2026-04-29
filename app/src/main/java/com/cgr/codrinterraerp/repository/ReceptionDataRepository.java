package com.cgr.codrinterraerp.repository;

import com.cgr.codrinterraerp.db.dao.MeasurementSystemFormulasDao;
import com.cgr.codrinterraerp.db.dao.ReceptionDataDao;
import com.cgr.codrinterraerp.db.dao.ReceptionTransactionDao;
import com.cgr.codrinterraerp.db.entities.ContainerData;
import com.cgr.codrinterraerp.db.entities.ReceptionData;
import com.cgr.codrinterraerp.db.relations.FormulaWithVariables;
import com.cgr.codrinterraerp.model.ReceptionWithContainer;

import java.util.List;

public class ReceptionDataRepository {

    private final MeasurementSystemFormulasDao measurementSystemFormulasDao;
    private final ReceptionTransactionDao receptionTransactionDao;
    private final ReceptionDataDao receptionDataDao;
    private final ReceptionRepository receptionRepository;
    private final DispatchRepository dispatchRepository;

    public ReceptionDataRepository(MeasurementSystemFormulasDao measurementSystemFormulasDao, ReceptionTransactionDao receptionTransactionDao, ReceptionDataDao receptionDataDao,
                                   ReceptionRepository receptionRepository, DispatchRepository dispatchRepository) {
        this.measurementSystemFormulasDao = measurementSystemFormulasDao;
        this.receptionTransactionDao = receptionTransactionDao;
        this.receptionDataDao = receptionDataDao;
        this.receptionRepository = receptionRepository;
        this.dispatchRepository = dispatchRepository;
    }

    // ✅ FETCH FORMULA
    public List<FormulaWithVariables> getFormulasWithVariables(int measurementSystemId) {
        return measurementSystemFormulasDao.getFormulasWithVariables(measurementSystemId);
    }

    // ✅ SAVE (TRANSACTION SAFE)
    public boolean saveMeasurementData(ReceptionData receptionData, ContainerData containerData) {

        boolean isSaved = receptionTransactionDao.saveMeasurementData(receptionData, containerData);

        if (isSaved) {
            receptionRepository.updateSummary(receptionData.getReceptionId(), receptionData.getTempReceptionId());
            dispatchRepository.updateSummary(containerData.getDispatchId(), containerData.getTempDispatchId());
        }

        return isSaved;
    }

    public List<ReceptionWithContainer> fetchReceptionData(Integer receptionId, String tempReceptionId) {
        if (receptionId != null && receptionId > 0) {
            return receptionDataDao.fetchByReceptionId(receptionId);
        } else {
            return receptionDataDao.fetchByTempReceptionId(tempReceptionId);
        }
    }
}