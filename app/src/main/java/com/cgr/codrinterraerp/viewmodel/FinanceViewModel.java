package com.cgr.codrinterraerp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cgr.codrinterraerp.db.entities.Beneficiaries;
import com.cgr.codrinterraerp.db.entities.ExpenseData;
import com.cgr.codrinterraerp.db.entities.IncomeData;
import com.cgr.codrinterraerp.db.views.ExpenseView;
import com.cgr.codrinterraerp.repository.FinanceRepository;
import com.cgr.codrinterraerp.wrapper.SingleLiveEvent;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FinanceViewModel extends ViewModel {

    private final FinanceRepository financeRepository;
    private long expenseSavedId;
    private final SingleLiveEvent<Boolean> expenseStatus = new SingleLiveEvent<>();
    private final SingleLiveEvent<Boolean> progressState = new SingleLiveEvent<>();
    private final LiveData<Double> totalCredit;
    private final LiveData<Double> totalDebit;
    private final MediatorLiveData<Double> balance = new MediatorLiveData<>();
    private final MutableLiveData<Double> totalCreditAmount = new MutableLiveData<>();
    private final MutableLiveData<Double> totalDebitAmount = new MutableLiveData<>();

    @Inject
    public FinanceViewModel(FinanceRepository financeRepository) {
        this.financeRepository = financeRepository;

        totalCredit = financeRepository.getTotalCredit();
        totalDebit = financeRepository.getTotalDebit();

        balance.addSource(totalCredit, credit -> calculateBalance());
        balance.addSource(totalDebit, debit -> calculateBalance());
    }

    public void saveExpenseDetails(ExpenseData expenseData) {
        progressState.postValue(true);

        long expenseDetail = financeRepository.saveExpenseData(expenseData);

        if (expenseDetail > 0) {

            Beneficiaries beneficiaries = new Beneficiaries();
            beneficiaries.setBeneficiaryName(expenseData.getBeneficiaryName());
            beneficiaries.setBeneficiaryIdentification(expenseData.getBeneficiaryIdentification());
            financeRepository.saveBeneficiary(beneficiaries);

            setExpenseSavedId(expenseDetail);
            progressState.postValue(false);
            expenseStatus.postValue(true);

        } else {
            setExpenseSavedId(0);
            progressState.postValue(false);
            expenseStatus.postValue(false);
        }
    }

    public LiveData<List<IncomeData>> getRecentIncomeData() {
        return financeRepository.getRecentIncomeData();
    }

    public LiveData<List<ExpenseView>> getRecentExpenseList() {
        return financeRepository.getRecentExpenseList();
    }

    public boolean deleteExpense(long timeStamp) {
        return financeRepository.deleteExpense(timeStamp);
    }

    public ExpenseData fetchExpenseById(String tempTransactionId) {
        return financeRepository.getExpenseByTempTransactionId(tempTransactionId);
    }

    private void calculateBalance() {
        Double credit = totalCredit.getValue();
        Double debit = totalDebit.getValue();

        if (credit == null) credit = 0.0;
        if (debit == null) debit = 0.0;

        balance.setValue(credit - debit);
    }

    public LiveData<Double> getBalance() {
        return balance;
    }

    public LiveData<Double> getTotalCredit() {
        return totalCredit;
    }

    public LiveData<Double> getTotalDebit() {
        return totalDebit;
    }

    public LiveData<Double> getTotalCreditAmountTransactions() {
        return totalCreditAmount;
    }

    public LiveData<Double> getTotalDebitAmountTransactions() {
        return totalDebitAmount;
    }

    public void fetchTotalCreditAmount(boolean isFilter, int transactionId) {
        if (isFilter) {
            totalCreditAmount.postValue(financeRepository.getFilteredTotalCredit(transactionId));
        } else {
            totalCreditAmount.postValue(financeRepository.getUnfilteredTotalCredit());
        }
    }

    public void fetchTotalDebitAmount(boolean isFilter, int transactionId, int accountHeadId, String startDate, String endDate) {
        if (isFilter) {
            totalDebitAmount.postValue(financeRepository.getFilteredTotalDebit(transactionId, accountHeadId, startDate, endDate));
        } else {
            totalDebitAmount.postValue(financeRepository.getUnfilteredTotalDebit());
        }
    }

    public LiveData<Boolean> getProgressState() {
        return progressState;
    }

    public LiveData<Boolean> getExpenseStatus() {
        return expenseStatus;
    }

    public long getExpenseSavedId() {
        return expenseSavedId;
    }

    public void setExpenseSavedId(long expenseSavedId) {
        this.expenseSavedId = expenseSavedId;
    }
}