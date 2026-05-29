package com.cgr.codrinterraerp.model.response.transactiondata;

import java.io.Serializable;

public class CreditTransactionsResponse implements Serializable {

    private int creditTransactionId;
    private String transactionDisplayId, transactionDate, conceptGeneral;
    private double amount;
    private long transactionTimestamp, updatedAt;

    public int getCreditTransactionId() {
        return creditTransactionId;
    }

    public void setCreditTransactionId(int creditTransactionId) {
        this.creditTransactionId = creditTransactionId;
    }

    public String getTransactionDisplayId() {
        return transactionDisplayId;
    }

    public void setTransactionDisplayId(String transactionDisplayId) {
        this.transactionDisplayId = transactionDisplayId;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getConceptGeneral() {
        return conceptGeneral;
    }

    public void setConceptGeneral(String conceptGeneral) {
        this.conceptGeneral = conceptGeneral;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTransactionTimestamp() {
        return transactionTimestamp;
    }

    public void setTransactionTimestamp(long transactionTimestamp) {
        this.transactionTimestamp = transactionTimestamp;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}