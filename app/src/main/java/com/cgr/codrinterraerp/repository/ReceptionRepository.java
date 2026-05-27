package com.cgr.codrinterraerp.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import com.cgr.codrinterraerp.db.dao.ContainerDataDao;
import com.cgr.codrinterraerp.db.dao.DispatchSummaryDao;
import com.cgr.codrinterraerp.db.dao.FarmInventoryOrdersDao;
import com.cgr.codrinterraerp.db.dao.ReceptionDataDao;
import com.cgr.codrinterraerp.db.dao.ReceptionDetailsDao;
import com.cgr.codrinterraerp.db.dao.ReceptionInventoryOrdersDao;
import com.cgr.codrinterraerp.db.dao.ReceptionSummaryDao;
import com.cgr.codrinterraerp.db.dao.ReceptionViewDao;
import com.cgr.codrinterraerp.db.entities.DispatchSummary;
import com.cgr.codrinterraerp.db.entities.FarmInventoryOrders;
import com.cgr.codrinterraerp.db.entities.ReceptionDetails;
import com.cgr.codrinterraerp.db.entities.ReceptionInventoryOrders;
import com.cgr.codrinterraerp.db.entities.ReceptionSummary;
import com.cgr.codrinterraerp.db.views.ReceptionView;
import com.cgr.codrinterraerp.helper.DispatchSummaryHelper;
import com.cgr.codrinterraerp.helper.ReceptionSummaryHelper;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ReceptionRepository {

    private final ReceptionDetailsDao receptionDetailsDao;
    private final ReceptionInventoryOrdersDao receptionInventoryOrdersDao;
    private final FarmInventoryOrdersDao farmInventoryOrdersDao;
    private final ReceptionViewDao receptionViewDao;
    private final ReceptionSummaryDao receptionSummaryDao;
    private final DispatchSummaryDao dispatchSummaryDao;
    private final DispatchSummaryHelper dispatchSummaryHelper;
    private final ReceptionSummaryHelper receptionSummaryHelper;
    private final ContainerDataDao containerDataDao;
    private final ReceptionDataDao receptionDataDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public ReceptionRepository(ReceptionDetailsDao receptionDetailsDao, ReceptionInventoryOrdersDao receptionInventoryOrdersDao, ReceptionViewDao receptionViewDao,
                               FarmInventoryOrdersDao farmInventoryOrdersDao, ReceptionSummaryDao receptionSummaryDao, ReceptionSummaryHelper receptionSummaryHelper,
                               ContainerDataDao containerDataDao, ReceptionDataDao receptionDataDao, DispatchSummaryHelper dispatchSummaryHelper,
                               DispatchSummaryDao dispatchSummaryDao) {
        this.receptionDetailsDao = receptionDetailsDao;
        this.receptionInventoryOrdersDao = receptionInventoryOrdersDao;
        this.receptionViewDao = receptionViewDao;
        this.farmInventoryOrdersDao = farmInventoryOrdersDao;
        this.receptionSummaryDao = receptionSummaryDao;
        this.receptionSummaryHelper = receptionSummaryHelper;
        this.containerDataDao = containerDataDao;
        this.receptionDataDao = receptionDataDao;
        this.dispatchSummaryHelper = dispatchSummaryHelper;
        this.dispatchSummaryDao = dispatchSummaryDao;
    }

    public long saveReceptionDetails(ReceptionDetails receptionDetails) {
       return receptionDetailsDao.insertOrUpdateReceptionDetails(receptionDetails);
    }

    public int getReceptionInventoryOrdersCount(String inventoryOrder, int supplierId) {
        return receptionInventoryOrdersDao.getReceptionInventoryOrdersCount(inventoryOrder, supplierId);
    }

    public int getFarmInventoryOrdersCount(String inventoryOrder, int supplierId) {
        return farmInventoryOrdersDao.getFarmInventoryOrdersCount(inventoryOrder, supplierId);
    }

    public int getReceptionInventoryOrdersCountForEdit(String inventoryOrder, int supplierId, String tempReceptionId) {
        return receptionDetailsDao.getReceptionInventoryOrdersCountForEdit(inventoryOrder, supplierId, tempReceptionId);
    }

    public LiveData<List<ReceptionView>> getReceptionList(boolean isClosed) {
        return receptionViewDao.getReceptionList(isClosed);
    }

    public void insertReceptionInventoryOrder(ReceptionInventoryOrders receptionInventoryOrder) {
        receptionInventoryOrdersDao.insertReceptionInventoryOrder(receptionInventoryOrder);
    }

    public void insertFarmInventoryOrder(FarmInventoryOrders farmInventoryOrder) {
        farmInventoryOrdersDao.insertFarmInventoryOrder(farmInventoryOrder);
    }

    public void updateSummary(String tempReceptionId) {
        executor.execute(() -> {
            ReceptionSummary s = receptionSummaryHelper.calculate(tempReceptionId);
            receptionSummaryDao.upsert(s);
        });
    }

    public void updateDispatchSummary(List<String> dispatchIds) {
        executor.execute(() -> {

            for (String dispatchId : dispatchIds) {
                DispatchSummary s = dispatchSummaryHelper.calculate(dispatchId);
                dispatchSummaryDao.upsert(s);
            }
        });
    }

    public ReceptionDetails fetchReceptionDetailById(String tempReceptionId) {
        return receptionDetailsDao.fetchReceptionDetailById(tempReceptionId);
    }

    public void deleteReceptionInventoryOrder(String ica, int supplierId) {
        receptionInventoryOrdersDao.deleteReceptionInventoryOrder(ica, supplierId);
    }

    public void deleteFarmInventoryOrder(String ica, int supplierId, boolean isFromReception) {
        farmInventoryOrdersDao.deleteFarmInventoryOrder(ica, supplierId, isFromReception);
    }

    @Transaction
    public int deleteFullReception(String tempReceptionId, long updatedAt) {

        int totalDeleted = 0;

        // ✅ Delete child first
        totalDeleted += containerDataDao.deleteContainerData(tempReceptionId, updatedAt);

        // ✅ Then parent
        totalDeleted += receptionDataDao.deleteReceptionData(tempReceptionId, updatedAt);

        // ✅ Finally root
        totalDeleted += receptionDetailsDao.deleteReceptionDetails(tempReceptionId, updatedAt);

        return totalDeleted;
    }

    public List<String> getAllDispatchIds(String tempReceptionId) {
        return receptionDataDao.getAllDispatchIds(tempReceptionId);
    }

    public boolean closeReceptionDetails(String tempReceptionId, long closedDate, int closedBy, boolean isClose) {
        int closed = receptionDetailsDao.closeReceptionDetails(tempReceptionId, closedDate, closedBy, isClose);
        return closed > 0;
    }
}