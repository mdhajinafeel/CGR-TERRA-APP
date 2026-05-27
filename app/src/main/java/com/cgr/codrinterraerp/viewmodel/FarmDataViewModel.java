package com.cgr.codrinterraerp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.cgr.codrinterraerp.db.entities.FarmData;
import com.cgr.codrinterraerp.db.entities.FarmSummary;
import com.cgr.codrinterraerp.db.relations.FormulaWithVariables;
import com.cgr.codrinterraerp.model.FarmCapturedData;
import com.cgr.codrinterraerp.repository.FarmDataRepository;
import com.cgr.codrinterraerp.repository.ReceptionDataRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FarmDataViewModel extends ViewModel {

    private final ReceptionDataRepository receptionDataRepository;
    private final FarmDataRepository farmDataRepository;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Inject
    public FarmDataViewModel(ReceptionDataRepository receptionDataRepository, FarmDataRepository farmDataRepository) {
        this.receptionDataRepository = receptionDataRepository;
        this.farmDataRepository = farmDataRepository;
    }

    public List<FormulaWithVariables> getFormulasWithVariables(int measurementSystemId) {
        return receptionDataRepository.getFormulasWithVariables(measurementSystemId);
    }

    public void saveMeasurementData(FarmData farmData, Callback<Boolean> callback) {
        executor.execute(() -> {
            boolean result = farmDataRepository.saveMeasurementData(farmData);
            callback.onComplete(result);
        });
    }

    public LiveData<FarmSummary> getFarmSummary(String tempFarmId) {
        return farmDataRepository.getFarmSummary(tempFarmId);
    }

    public LiveData<List<FarmCapturedData>> fetchFarmData(String tempFarmId) {
        return farmDataRepository.fetchFarmData(tempFarmId);
    }

    public void deleteFarmData(String tempFarmDataId, String tempFarmId, Callback<Integer> callback) {
        executor.execute(() -> {
            int result = farmDataRepository.deleteFarmDataById(tempFarmDataId, tempFarmId);
            callback.onComplete(result);
        });
    }

    public interface Callback<T> {
        void onComplete(T result);
    }
}