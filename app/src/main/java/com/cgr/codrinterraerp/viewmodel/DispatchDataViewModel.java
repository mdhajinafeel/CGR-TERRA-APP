package com.cgr.codrinterraerp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.cgr.codrinterraerp.db.entities.DispatchSummary;
import com.cgr.codrinterraerp.model.ContainerWithReception;
import com.cgr.codrinterraerp.repository.DispatchDataRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DispatchDataViewModel extends ViewModel {

    private final DispatchDataRepository dispatchDataRepository;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Inject
    public DispatchDataViewModel(DispatchDataRepository dispatchDataRepository) {
        this.dispatchDataRepository = dispatchDataRepository;
    }

    public LiveData<List<ContainerWithReception>> fetchContainerData(Integer dispatchId, String tempDispatchId) {
        return dispatchDataRepository.fetchContainerData(dispatchId, tempDispatchId);
    }

    public LiveData<DispatchSummary> getDispatchSummary(String tempDispatchId) {
        return dispatchDataRepository.getDispatchSummary(tempDispatchId);
    }

    public void deleteDispatchDataById(String tempReceptionDataId, String tempReceptionId, String tempDispatchId, ReceptionDataViewModel.Callback<Integer> callback) {
        executor.execute(() -> {
            int result = dispatchDataRepository.deleteDispatchDataById(tempReceptionDataId, tempReceptionId, tempDispatchId);
            callback.onComplete(result);
        });
    }

    public interface Callback<T> {
        void onComplete(T result);
    }
}