package com.cgr.codrinterraerp.model.response.transactiondata;

import java.io.Serializable;

public class DebitTransactionsResponse implements Serializable {

    private int creditTransactionId, transactionId, accountHeadId, forestryCostType;
    private String tempTransactionId, transactionDisplayId, expenseDate, beneficiaryName, documentNumber, expenseUploadedImage;
    private double amount;
    private long expenseTimestamp, updatedAt;
    private boolean isForestry;

    public int getCreditTransactionId() {
        return creditTransactionId;
    }

    public void setCreditTransactionId(int creditTransactionId) {
        this.creditTransactionId = creditTransactionId;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getAccountHeadId() {
        return accountHeadId;
    }

    public void setAccountHeadId(int accountHeadId) {
        this.accountHeadId = accountHeadId;
    }

    public String getTempTransactionId() {
        return tempTransactionId;
    }

    public void setTempTransactionId(String tempTransactionId) {
        this.tempTransactionId = tempTransactionId;
    }

    public String getTransactionDisplayId() {
        return transactionDisplayId;
    }

    public void setTransactionDisplayId(String transactionDisplayId) {
        this.transactionDisplayId = transactionDisplayId;
    }

    public String getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(String expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getExpenseUploadedImage() {
        return expenseUploadedImage;
    }

    public void setExpenseUploadedImage(String expenseUploadedImage) {
        this.expenseUploadedImage = expenseUploadedImage;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getExpenseTimestamp() {
        return expenseTimestamp;
    }

    public void setExpenseTimestamp(long expenseTimestamp) {
        this.expenseTimestamp = expenseTimestamp;
    }

    public int getForestryCostType() {
        return forestryCostType;
    }

    public void setForestryCostType(int forestryCostType) {
        this.forestryCostType = forestryCostType;
    }

    public boolean isForestry() {
        return isForestry;
    }

    public void setForestry(boolean forestry) {
        isForestry = forestry;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}