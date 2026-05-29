package com.cgr.codrinterraerp.db.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "income_data",
        indices = {
                @Index(name = "idx_credit_trans_income", value = {"creditTransactionId"}),
                @Index(name = "idx_transaction_date_income", value = {"transactionDate"}),
                @Index(name = "idx_concept_general_income", value = {"conceptGeneral"}),
                @Index(name = "idx_trans_display_id_income", value = {"transactionDisplayId"})
        }
)
public class IncomeData implements Serializable {

    @PrimaryKey
    private int creditTransactionId;
    private String transactionDisplayId;
    private double amount;
    private String transactionDate;
    private long transactionTimestamp;
    private String conceptGeneral;
    private long updatedAt;

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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public long getTransactionTimestamp() {
        return transactionTimestamp;
    }

    public void setTransactionTimestamp(long transactionTimestamp) {
        this.transactionTimestamp = transactionTimestamp;
    }

    public String getConceptGeneral() {
        return conceptGeneral;
    }

    public void setConceptGeneral(String conceptGeneral) {
        this.conceptGeneral = conceptGeneral;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}