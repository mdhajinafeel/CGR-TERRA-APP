package com.cgr.codrinterraerp.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import com.cgr.codrinterraerp.db.dao.FarmDataDao;
import com.cgr.codrinterraerp.db.dao.FarmDetailsDao;
import com.cgr.codrinterraerp.db.dao.FarmInventoryOrdersDao;
import com.cgr.codrinterraerp.db.dao.FarmSummaryDao;
import com.cgr.codrinterraerp.db.dao.FarmViewDao;
import com.cgr.codrinterraerp.db.entities.FarmDetails;
import com.cgr.codrinterraerp.db.entities.FarmInventoryOrders;
import com.cgr.codrinterraerp.db.entities.FarmSummary;
import com.cgr.codrinterraerp.db.views.FarmView;
import com.cgr.codrinterraerp.helper.FarmSummaryHelper;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FarmRepository {

    private final FarmDetailsDao farmDetailsDao;
    private final FarmInventoryOrdersDao farmInventoryOrdersDao;
    private final FarmSummaryDao farmSummaryDao;
    private final FarmViewDao farmViewDao;
    private final FarmDataDao farmDataDao;
    private final FarmSummaryHelper farmSummaryHelper;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public FarmRepository(FarmDetailsDao farmDetailsDao, FarmInventoryOrdersDao farmInventoryOrdersDao, FarmSummaryDao farmSummaryDao,
                          FarmViewDao farmViewDao, FarmDataDao farmDataDao, FarmSummaryHelper farmSummaryHelper) {
        this.farmDetailsDao = farmDetailsDao;
        this.farmInventoryOrdersDao = farmInventoryOrdersDao;
        this.farmSummaryDao = farmSummaryDao;
        this.farmSummaryHelper = farmSummaryHelper;
        this.farmViewDao = farmViewDao;
        this.farmDataDao = farmDataDao;
    }

    public long saveFarmDetails(FarmDetails farmDetails) {
       return farmDetailsDao.insertOrUpdateFarmDetails(farmDetails);
    }

    public int getFarmInventoryOrdersCount(String inventoryOrder, int supplierId) {
        return farmInventoryOrdersDao.getFarmInventoryOrdersCount(inventoryOrder, supplierId);
    }

    public int getFarmInventoryOrdersCountForEdit(String inventoryOrder, int supplierId, String tempFarmId) {
        return farmDetailsDao.getFarmInventoryOrdersCountForEdit(inventoryOrder, supplierId, tempFarmId);
    }

    public void insertFarmInventoryOrder(FarmInventoryOrders farmInventoryOrder) {
        farmInventoryOrdersDao.insertFarmInventoryOrder(farmInventoryOrder);
    }

    public FarmDetails fetchFarmDetailById(String tempFarmId) {
        return farmDetailsDao.fetchFarmDetailById(tempFarmId);
    }

    public void deleteFarmInventoryOrder(String ica, int supplierId, boolean isFromReception) {
        farmInventoryOrdersDao.deleteFarmInventoryOrder(ica, supplierId, isFromReception);
    }

    public boolean closeFarmDetails(String tempFarmId, long closedDate, int closedBy, boolean isClose) {
        int closed = farmDetailsDao.closeFarmDetails(tempFarmId, closedDate, closedBy, isClose);
        return closed > 0;
    }

    public void updateSummary(String tempFarmId) {
        executor.execute(() -> {
            FarmSummary s = farmSummaryHelper.calculate(tempFarmId);
            farmSummaryDao.upsert(s);
        });
    }

    public LiveData<List<FarmView>> getFarmList(boolean isClosed) {
        return farmViewDao.getFarmList(isClosed);
    }

    @Transaction
    public int deleteFullFarm(String tempFarmId, long updatedAt) {

        int totalDeleted = 0;

        // ✅ Delete child first
        totalDeleted += farmDataDao.deleteFarmData(tempFarmId, updatedAt);

        // ✅ Finally root
        totalDeleted += farmDetailsDao.deleteFarmDetails(tempFarmId, updatedAt);

        return totalDeleted;
    }
}