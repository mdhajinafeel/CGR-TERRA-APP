package com.cgr.codrinterraerp.repository;

import androidx.lifecycle.LiveData;

import com.cgr.codrinterraerp.db.dao.ContainerDataDao;
import com.cgr.codrinterraerp.db.dao.ReceptionDataDao;
import com.cgr.codrinterraerp.db.entities.DispatchSummary;
import com.cgr.codrinterraerp.db.entities.ReceptionSummary;
import com.cgr.codrinterraerp.model.ContainerWithReception;

import java.util.List;

public class DispatchDataRepository {

    private final ContainerDataDao containerDataDao;
    private final ReceptionDataDao receptionDataDao;
    private final DispatchRepository dispatchRepository;
    private final ReceptionRepository receptionRepository;

    public DispatchDataRepository(ContainerDataDao containerDataDao, ReceptionDataDao receptionDataDao, DispatchRepository dispatchRepository,
                                  ReceptionRepository receptionRepository) {
        this.containerDataDao = containerDataDao;
        this.receptionDataDao = receptionDataDao;
        this.dispatchRepository = dispatchRepository;
        this.receptionRepository = receptionRepository;
    }

    public LiveData<List<ContainerWithReception>> fetchContainerData(Integer dispatchId, String tempDispatchId) {
        if (dispatchId != null && dispatchId > 0) {
            return containerDataDao.fetchByDispatchId(dispatchId);
        } else {
            return containerDataDao.fetchByTempDispatchId(tempDispatchId);
        }
    }

    public LiveData<DispatchSummary> getDispatchSummary(String tempDispatchId) {
        return containerDataDao.getSummaryByTempId(tempDispatchId);
    }

    public int deleteDispatchDataById(String tempReceptionDataId, String tempReceptionId, String tempDispatchId) {
        int deleteData = containerDataDao.deleteContainerDataById(tempReceptionDataId, tempReceptionId, tempDispatchId);

        if(deleteData > 0) {

            receptionDataDao.deleteReceptionDataById(tempReceptionDataId, tempReceptionId);
            dispatchRepository.updateSummary(null, tempDispatchId);

            String getReceptionId = receptionDataDao.getAllReceptionId(tempReceptionId, tempReceptionDataId);
            if(getReceptionId != null && !getReceptionId.isEmpty()) {
                receptionRepository.updateSummary(null, getReceptionId);
            }
        }

        return deleteData;
    }
}