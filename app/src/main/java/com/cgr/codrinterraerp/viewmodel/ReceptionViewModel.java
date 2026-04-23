package com.cgr.codrinterraerp.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.ReceptionDetails;
import com.cgr.codrinterraerp.repository.ReceptionRepository;
import com.cgr.codrinterraerp.wrapper.SingleLiveEvent;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class ReceptionViewModel extends ViewModel {

    private final ReceptionRepository receptionRepository;
    private final Context context;
    private String errorTitle, errorMessage;
    private final SingleLiveEvent<Boolean> receptionStatus = new SingleLiveEvent<>();
    private final SingleLiveEvent<Boolean> progressState = new SingleLiveEvent<>();

    @Inject
    public ReceptionViewModel(ReceptionRepository receptionRepository, @ApplicationContext Context context) {
        this.receptionRepository = receptionRepository;
        this.context = context;
    }

    public void saveReceptionDetails(ReceptionDetails receptionDetails) {
        progressState.postValue(true);
        int reception = receptionRepository.saveReceptionDetails(receptionDetails);
        progressState.postValue(false);
        if(reception > 0) {
            receptionStatus.postValue(true);
        } else {
            receptionStatus.postValue(false);
            setErrorTitle(context.getString(R.string.error));
            setErrorMessage(context.getString(R.string.common_error));
        }
    }

    public LiveData<Boolean> getProgressState() {
        return progressState;
    }

    public LiveData<Boolean> getReceptionStatus() {
        return receptionStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public void setErrorTitle(String errorTitle) {
        this.errorTitle = errorTitle;
    }
}