package com.cgr.codrinterraerp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.cgr.codrinterraerp.repository.MasterRepository;
import com.cgr.codrinterraerp.wrapper.SingleLiveEvent;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FinanceViewModel extends ViewModel {

    private final MasterRepository masterRepository;
    private final SingleLiveEvent<Boolean> progressState = new SingleLiveEvent<>();

    @Inject
    public FinanceViewModel(MasterRepository masterRepository) {
        this.masterRepository = masterRepository;
    }

    public LiveData<Boolean> getProgressState() {
        return progressState;
    }
}