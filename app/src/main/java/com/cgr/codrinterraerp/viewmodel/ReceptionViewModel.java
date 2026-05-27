package com.cgr.codrinterraerp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.cgr.codrinterraerp.db.entities.FarmInventoryOrders;
import com.cgr.codrinterraerp.db.entities.ReceptionDetails;
import com.cgr.codrinterraerp.db.entities.ReceptionInventoryOrders;
import com.cgr.codrinterraerp.db.views.ReceptionView;
import com.cgr.codrinterraerp.repository.ReceptionRepository;
import com.cgr.codrinterraerp.wrapper.SingleLiveEvent;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ReceptionViewModel extends ViewModel {

    private final ReceptionRepository receptionRepository;
    private long receptionSavedId;
    private final SingleLiveEvent<Boolean> receptionStatus = new SingleLiveEvent<>();
    private final SingleLiveEvent<Boolean> progressState = new SingleLiveEvent<>();
    private final MutableLiveData<Boolean> filterStatus = new MutableLiveData<>(false);
    private final LiveData<List<ReceptionView>> receptionList;

    @Inject
    public ReceptionViewModel(ReceptionRepository receptionRepository) {
        this.receptionRepository = receptionRepository;
        receptionList = Transformations.switchMap(filterStatus, this.receptionRepository::getReceptionList);
    }

    public void saveReceptionDetails(ReceptionDetails receptionDetails, String oldIca, int oldSupplierId) {
        progressState.postValue(true);

        long reception = receptionRepository.saveReceptionDetails(receptionDetails);

        if (reception > 0) {

            // ================= DETERMINE DELETE VALUES =================
            String deleteIca = receptionDetails.isEdited() ? oldIca : receptionDetails.getIca();
            int deleteSupplierId = receptionDetails.isEdited() ? oldSupplierId : receptionDetails.getSupplierId();

            // ================= DELETE OLD INVENTORY =================
            receptionRepository.deleteReceptionInventoryOrder(deleteIca, deleteSupplierId);
            receptionRepository.deleteFarmInventoryOrder(deleteIca, deleteSupplierId, true);

            // ================= INSERT NEW RECEPTION INVENTORY =================
            ReceptionInventoryOrders receptionInventoryOrders = new ReceptionInventoryOrders();
            receptionInventoryOrders.setInventoryOrder(receptionDetails.getIca()); // NEW ICA
            receptionInventoryOrders.setSupplierId(receptionDetails.getSupplierId()); // NEW supplier

            receptionRepository.insertReceptionInventoryOrder(receptionInventoryOrders);

            // ================= FARM INVENTORY =================
            if (receptionDetails.isFarmEnabled()) {
                FarmInventoryOrders farmInventoryOrders = new FarmInventoryOrders();
                farmInventoryOrders.setInventoryOrder(receptionDetails.getIca());
                farmInventoryOrders.setSupplierId(receptionDetails.getSupplierId());
                farmInventoryOrders.setFromReception(true);

                receptionRepository.insertFarmInventoryOrder(farmInventoryOrders);
            }

            // ================= SUMMARY =================
            receptionRepository.updateSummary(receptionDetails.getTempReceptionId());

            setReceptionSavedId(reception);
            progressState.postValue(false);
            receptionStatus.postValue(true);

        } else {
            setReceptionSavedId(0);
            progressState.postValue(false);
            receptionStatus.postValue(false);
        }
    }

    public int getReceptionInventoryOrdersCount(String inventoryOrder, int supplierId) {
        return receptionRepository.getReceptionInventoryOrdersCount(inventoryOrder, supplierId);
    }

    public int getReceptionInventoryOrdersCountForEdit(String inventoryOrder, int supplierId, String tempReceptionId) {
        return receptionRepository.getReceptionInventoryOrdersCountForEdit(inventoryOrder, supplierId, tempReceptionId);
    }

    public int getFarmInventoryOrdersCount(String inventoryOrder, int supplierId) {
        return receptionRepository.getFarmInventoryOrdersCount(inventoryOrder, supplierId);
    }

    public LiveData<List<ReceptionView>> getReceptionList() {
        return receptionList;
    }

    public ReceptionDetails fetchReceptionDetailById(String tempReceptionId) {
        return receptionRepository.fetchReceptionDetailById(tempReceptionId);
    }

    public int deleteReceptionDetails(String tempReceptionId, long updatedAt) {
        progressState.postValue(true);

        List<String> getAllDispatchIds = receptionRepository.getAllDispatchIds(tempReceptionId);
        int receptionDelete = receptionRepository.deleteFullReception(tempReceptionId, updatedAt);
        if(receptionDelete > 0) {
            receptionRepository.updateSummary(tempReceptionId);
            receptionRepository.updateDispatchSummary(getAllDispatchIds);
        }

        progressState.postValue(false);
        return receptionDelete;
    }

    public boolean closeReceptionDetails(String tempReceptionId, long closedDate, int closedBy, boolean isClose) {
        return receptionRepository.closeReceptionDetails(tempReceptionId, closedDate, closedBy, isClose);
    }

    public void setFilter(boolean status) {
        filterStatus.setValue(status);
    }

    public LiveData<Boolean> getProgressState() {
        return progressState;
    }

    public LiveData<Boolean> getReceptionStatus() {
        return receptionStatus;
    }

    public long getReceptionSavedId() {
        return receptionSavedId;
    }

    public void setReceptionSavedId(long receptionSavedId) {
        this.receptionSavedId = receptionSavedId;
    }
}