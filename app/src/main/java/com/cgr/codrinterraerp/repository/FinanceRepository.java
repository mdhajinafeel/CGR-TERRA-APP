package com.cgr.codrinterraerp.repository;

import androidx.lifecycle.LiveData;

import com.cgr.codrinterraerp.db.dao.BeneficiariesDao;
import com.cgr.codrinterraerp.db.dao.ExpenseDataDao;
import com.cgr.codrinterraerp.db.dao.ExpenseViewDao;
import com.cgr.codrinterraerp.db.dao.IncomeDataDao;
import com.cgr.codrinterraerp.db.entities.Beneficiaries;
import com.cgr.codrinterraerp.db.entities.ExpenseData;
import com.cgr.codrinterraerp.db.entities.FarmDetails;
import com.cgr.codrinterraerp.db.entities.IncomeData;
import com.cgr.codrinterraerp.db.views.ExpenseView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FinanceRepository {

    private final IncomeDataDao incomeDataDao;
    private final ExpenseDataDao expenseDataDao;
    private final BeneficiariesDao beneficiariesDao;
    private final ExpenseViewDao expenseViewDao;

    public FinanceRepository(IncomeDataDao incomeDataDao, ExpenseDataDao expenseDataDao, BeneficiariesDao beneficiariesDao, ExpenseViewDao expenseViewDao) {
        this.incomeDataDao = incomeDataDao;
        this.expenseDataDao = expenseDataDao;
        this.beneficiariesDao = beneficiariesDao;
        this.expenseViewDao = expenseViewDao;
    }

    public long saveExpenseData(ExpenseData expenseData) {
        return expenseDataDao.insertExpenseData(expenseData);
    }

    public void saveBeneficiary(Beneficiaries beneficiaries) {
        beneficiariesDao.insertBeneficiary(beneficiaries);
    }

    public LiveData<List<IncomeData>> getIncomeData() {
        return incomeDataDao.getIncomeData();
    }

    public LiveData<List<IncomeData>> getRecentIncomeData() {
        return incomeDataDao.getRecentIncomeData();
    }

    public LiveData<List<ExpenseView>> getExpenseList() {
        return expenseViewDao.getExpenseList();
    }

    public LiveData<List<ExpenseView>> getRecentExpenseList() {
        return expenseViewDao.getRecentExpenseList();
    }

    public boolean deleteExpense(long capturedTimeStamp) {
        int deletedCount = expenseDataDao.deleteExpenseDataManual(capturedTimeStamp);
        return deletedCount > 0;
    }

    public ExpenseData getExpenseByTempTransactionId(String tempTransactionId) {
        return expenseDataDao.getExpenseByTempTransactionId(tempTransactionId);
    }

    public LiveData<Double> getTotalDebit() {
        return expenseDataDao.getTotalDebit();
    }

    public LiveData<Double> getTotalCredit() {
        return incomeDataDao.getTotalCredit();
    }
    public Double getUnfilteredTotalCredit() {
        return incomeDataDao.getUnfilteredTotalCredit();
    }

    public Double getFilteredTotalCredit(int transactionId) {
        return incomeDataDao.getFilteredTotalCredit(transactionId);
    }

    public Double getFilteredTotalDebit(int transactionId, int accountHeadId, String startDate, String endDate) {
        return expenseDataDao.getFilteredTotalDebit(transactionId, accountHeadId, startDate, endDate);
    }

    public Double getUnfilteredTotalDebit() {
        return expenseDataDao.getUnfilteredTotalDebit();
    }
}