package com.cgr.codrinterraerp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cgr.codrinterraerp.db.entities.Beneficiaries;
import com.cgr.codrinterraerp.db.entities.ExpenseData;
import com.cgr.codrinterraerp.db.entities.IncomeData;
import com.cgr.codrinterraerp.db.views.ExpenseView;
import com.cgr.codrinterraerp.model.AccountHeadReportData;
import com.cgr.codrinterraerp.model.FilterData;
import com.cgr.codrinterraerp.repository.FinanceRepository;
import com.cgr.codrinterraerp.wrapper.SingleLiveEvent;

import java.util.ArrayList;
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
    private static final int PAGE_SIZE = 20;
    private final MutableLiveData<List<ExpenseView>> expensePage = new MutableLiveData<>();
    private final MutableLiveData<List<IncomeData>> incomePage = new MutableLiveData<>();
    private int expensePageIndex = 0;
    private int incomePageIndex = 0;
    private List<ExpenseView> allExpenses;
    private List<IncomeData> allIncome;
    private final MutableLiveData<FilterData> filterLiveData = new MutableLiveData<>();

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

    /* ---------- INITIAL LOAD ---------- */

    public void loadInitialExpenseData(boolean isFilter, int transactionId, int accountHeadId, String startDate, String endDate) {

        if (allExpenses != null) return;

        if (isFilter) {
            allExpenses = financeRepository.getFilteredExpenseTransactions(transactionId, accountHeadId, startDate, endDate);
        } else {
            allExpenses = financeRepository.getAllExpenseData();
        }
        loadNextExpensePage();
    }

    public void loadInitialIncomeData(boolean isFilter, int transactionId) {
        if (allIncome != null) return;

        if (isFilter) {
            allIncome = financeRepository.getFilteredIncomeTransactions(transactionId);
        } else {
            allIncome = financeRepository.getAllIncomeData();
        }
        loadNextIncomePage();
    }

    /* ---------- PAGINATION ---------- */

    public void loadNextExpensePage() {

        if (allExpenses == null || allExpenses.isEmpty()) {
            expensePage.setValue(new ArrayList<>()); // ✅ NOTIFY EMPTY
            return;
        }

        int start = expensePageIndex * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allExpenses.size());

        if (start < end) {
            expensePage.setValue(allExpenses.subList(start, end));
            expensePageIndex++;
        }
    }

    public void loadNextIncomePage() {

        if (allIncome == null || allIncome.isEmpty()) {
            incomePage.setValue(new ArrayList<>()); // ✅ NOTIFY EMPTY
            return;
        }

        int start = incomePageIndex * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allIncome.size());

        if (start < end) {
            incomePage.setValue(allIncome.subList(start, end));
            incomePageIndex++;
        }
    }

    public LiveData<List<ExpenseView>> getExpensePage() {
        return expensePage;
    }

    public LiveData<List<IncomeData>> getIncomePage() {
        return incomePage;
    }

    public void resetIncomePaging() {
        incomePageIndex = 0;
        allIncome = null;
    }

    public void resetExpensePaging() {
        expensePageIndex = 0;
        allExpenses = null;
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

    public LiveData<FilterData> getFilterLiveData() {
        return filterLiveData;
    }

    public void applyFilter(boolean isFilterApplied, int transactionId, int accountHeadId, String startDate, String endDate) {
        filterLiveData.setValue(new FilterData(isFilterApplied, transactionId, accountHeadId, startDate, endDate));
    }

    public List<AccountHeadReportData> getExpenseByAccountHead(int transactionId, String startDate, String endDate) {
        return financeRepository.getExpenseByAccountHead(transactionId, startDate, endDate);
    }

    public double getIncomeTotal(int transactionId) {
        return financeRepository.getIncomeTotal(transactionId);
    }

    public double getExpenseTotal(int transactionId, String startDate, String endDate) {
        return financeRepository.getExpenseTotal(transactionId, startDate, endDate);
    }
}