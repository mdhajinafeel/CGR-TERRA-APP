package com.cgr.codrinterraerp.repository;

import com.cgr.codrinterraerp.db.dao.ReceptionDetailsDao;
import com.cgr.codrinterraerp.db.entities.ReceptionDetails;

public class ReceptionRepository {

    private final ReceptionDetailsDao receptionDetailsDao;

    public ReceptionRepository(ReceptionDetailsDao receptionDetailsDao) {
        this.receptionDetailsDao = receptionDetailsDao;
    }

    public int saveReceptionDetails(ReceptionDetails receptionDetails) {
       return receptionDetailsDao.insertOrUpdateReceptionDetails(receptionDetails);
    }
}