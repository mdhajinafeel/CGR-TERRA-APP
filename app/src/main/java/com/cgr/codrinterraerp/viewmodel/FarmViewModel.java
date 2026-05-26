package com.cgr.codrinterraerp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.cgr.codrinterraerp.db.entities.FarmDetails;
import com.cgr.codrinterraerp.db.entities.FarmInventoryOrders;
import com.cgr.codrinterraerp.db.views.FarmView;
import com.cgr.codrinterraerp.repository.FarmRepository;
import com.cgr.codrinterraerp.wrapper.SingleLiveEvent;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FarmViewModel extends ViewModel {

    private final FarmRepository farmRepository;
    private long farmSavedId;
    private final SingleLiveEvent<Boolean> farmStatus = new SingleLiveEvent<>();
    private final SingleLiveEvent<Boolean> progressState = new SingleLiveEvent<>();
    private final MutableLiveData<Boolean> filterStatus = new MutableLiveData<>(false);
    private final LiveData<List<FarmView>> farmList;

    @Inject
    public FarmViewModel(FarmRepository farmRepository) {
        this.farmRepository = farmRepository;
        farmList = Transformations.switchMap(filterStatus, this.farmRepository::getFarmList);
    }

    public void saveFarmDetails(FarmDetails farmDetails, String oldIca, int oldSupplierId) {
        progressState.postValue(true);

        long reception = farmRepository.saveFarmDetails(farmDetails);

        if (reception > 0) {

            // ================= DETERMINE DELETE VALUES =================
            String deleteIca = farmDetails.isEdited() ? oldIca : farmDetails.getIca();
            int deleteSupplierId = farmDetails.isEdited() ? oldSupplierId : farmDetails.getSupplierId();

            // ================= DELETE OLD INVENTORY =================
            farmRepository.deleteFarmInventoryOrder(deleteIca, deleteSupplierId);

            // ================= INSERT NEW FARM INVENTORY =================
            FarmInventoryOrders farmInventoryOrders = new FarmInventoryOrders();
            farmInventoryOrders.setInventoryOrder(farmDetails.getIca()); // NEW ICA
            farmInventoryOrders.setSupplierId(farmDetails.getSupplierId()); // NEW supplier

            farmRepository.insertFarmInventoryOrder(farmInventoryOrders);

            // ================= SUMMARY =================
            farmRepository.updateSummary(farmDetails.getTempFarmId());

            setFarmSavedId(reception);
            progressState.postValue(false);
            farmStatus.postValue(true);

        } else {
            setFarmSavedId(0);
            progressState.postValue(false);
            farmStatus.postValue(false);
        }
    }

    public int getFarmInventoryOrdersCount(String inventoryOrder, int supplierId) {
        return farmRepository.getFarmInventoryOrdersCount(inventoryOrder, supplierId);
    }

    public int getFarmInventoryOrdersCountForEdit(String inventoryOrder, int supplierId, String tempFarmId) {
        return farmRepository.getFarmInventoryOrdersCountForEdit(inventoryOrder, supplierId, tempFarmId);
    }

    public LiveData<List<FarmView>> getFarmList() {
        return farmList;
    }

    public FarmDetails fetchFarmDetailById(String tempFarmId) {
        return farmRepository.fetchFarmDetailById(tempFarmId);
    }

//    public int deleteFarmDetails(String tempFarmId, long updatedAt) {
//        progressState.postValue(true);
//
//        List<String> getAllDispatchIds = receptionRepository.getAllDispatchIds(tempReceptionId);
//        int receptionDelete = receptionRepository.deleteFullReception(tempReceptionId, updatedAt);
//        if(receptionDelete > 0) {
//            receptionRepository.updateSummary(tempReceptionId);
//            receptionRepository.updateDispatchSummary(getAllDispatchIds);
//        }
//
//        progressState.postValue(false);
//        return receptionDelete;
//    }

    public boolean closeFarmDetails(String tempFarmId, long closedDate, int closedBy, boolean isClose) {
        return farmRepository.closeFarmDetails(tempFarmId, closedDate, closedBy, isClose);
    }

    public void setFilter(boolean status) {
        filterStatus.setValue(status);
    }

    public LiveData<Boolean> getProgressState() {
        return progressState;
    }

    public LiveData<Boolean> getFarmStatus() {
        return farmStatus;
    }

    public long getFarmSavedId() {
        return farmSavedId;
    }

    public void setFarmSavedId(long farmSavedId) {
        this.farmSavedId = farmSavedId;
    }
}