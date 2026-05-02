package com.cgr.codrinterraerp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.cgr.codrinterraerp.db.entities.ContainerData;
import com.cgr.codrinterraerp.db.entities.ReceptionData;
import com.cgr.codrinterraerp.db.entities.ReceptionSummary;
import com.cgr.codrinterraerp.db.relations.FormulaWithVariables;
import com.cgr.codrinterraerp.model.ReceptionWithContainer;
import com.cgr.codrinterraerp.repository.ReceptionDataRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Callback;

@HiltViewModel
public class ReceptionDataViewModel extends ViewModel {

    private final ReceptionDataRepository receptionDataRepository;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Inject
    public ReceptionDataViewModel(ReceptionDataRepository receptionDataRepository) {
        this.receptionDataRepository = receptionDataRepository;
    }

    public List<FormulaWithVariables> getFormulasWithVariables(int measurementSystemId) {
        return receptionDataRepository.getFormulasWithVariables(measurementSystemId);
    }

    public void saveMeasurementData(ReceptionData receptionData, ContainerData containerData, Callback<Boolean> callback) {
        executor.execute(() -> {
            boolean result = receptionDataRepository.saveMeasurementData(receptionData, containerData);
            callback.onComplete(result);
        });
    }

    public LiveData<List<ReceptionWithContainer>> fetchReceptionData(Integer receptionId, String tempReceptionId) {
        return receptionDataRepository.fetchReceptionData(receptionId, tempReceptionId);
    }

    public void deleteReceptionData(String tempReceptionDataId, String tempReceptionId, Callback<Integer> callback) {
        executor.execute(() -> {
            int result = receptionDataRepository.deleteReceptionDataById(tempReceptionDataId, tempReceptionId);
            callback.onComplete(result);
        });
    }

    public LiveData<ReceptionSummary> getReceptionSummary(String tempReceptionId) {
        return receptionDataRepository.getReceptionSummary(tempReceptionId);
    }

    public interface Callback<T> {
        void onComplete(T result);
    }
}