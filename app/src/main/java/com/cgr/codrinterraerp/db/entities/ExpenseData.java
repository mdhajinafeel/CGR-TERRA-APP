package com.cgr.codrinterraerp.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "expense_data",
        indices = {
                @Index(name = "idx_temp_transaction_id", value = {"tempTransactionId"}, unique = true),
                @Index(name = "idx_credit_transaction_id", value = {"creditTransactionId"}),
                @Index(name = "idx_trans_id_expense", value = {"transactionId"}),
                @Index(name = "idx_catpured_timestamp_expense", value = {"capturedTimeStamp"}),
                @Index(name = "idx_trans_display_id_expense", value = {"transactionDisplayId"}),
                @Index(name = "idx_account_head_id_expense", value = {"accountHeadId"}),
                @Index(name = "idx_beneficiary_name_expense", value = {"beneficiaryName"}),
                @Index(name = "idx_identification_expense", value = {"beneficiaryIdentification"}),
                @Index(name = "idx_expense_date", value = {"expenseDate"}),
                @Index(name = "idx_amount_expense", value = {"amount"}),
        }
)
public class ExpenseData implements Serializable {

    @PrimaryKey
    @NonNull
    public String tempTransactionId = "";
    public int creditTransactionId;
    public long capturedTimeStamp;
    public int transactionId;
    public String transactionDisplayId;
    public int accountHeadId;
    public String beneficiaryName;
    public String beneficiaryIdentification;
    public String expenseDate;
    public double amount;
    public String attachFileUri;
    public String attachFileUrl;
    public boolean isAttachUpdated;
    public boolean isAttachUploaded;
    public boolean isSynced = false;
    public boolean isDeleted = false;
    public boolean isEdited = false;
    public long updatedAt = System.currentTimeMillis();
    public boolean isForestry;
    public int forestryCostType;

    @NonNull
    public String getTempTransactionId() {
        return tempTransactionId;
    }

    public void setTempTransactionId(@NonNull String tempTransactionId) {
        this.tempTransactionId = tempTransactionId;
    }

    public int getCreditTransactionId() {
        return creditTransactionId;
    }

    public void setCreditTransactionId(int creditTransactionId) {
        this.creditTransactionId = creditTransactionId;
    }

    public long getCapturedTimeStamp() {
        return capturedTimeStamp;
    }

    public void setCapturedTimeStamp(long capturedTimeStamp) {
        this.capturedTimeStamp = capturedTimeStamp;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionDisplayId() {
        return transactionDisplayId;
    }

    public void setTransactionDisplayId(String transactionDisplayId) {
        this.transactionDisplayId = transactionDisplayId;
    }

    public int getAccountHeadId() {
        return accountHeadId;
    }

    public void setAccountHeadId(int accountHeadId) {
        this.accountHeadId = accountHeadId;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getBeneficiaryIdentification() {
        return beneficiaryIdentification;
    }

    public void setBeneficiaryIdentification(String beneficiaryIdentification) {
        this.beneficiaryIdentification = beneficiaryIdentification;
    }

    public String getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(String expenseDate) {
        this.expenseDate = expenseDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getAttachFileUri() {
        return attachFileUri;
    }

    public void setAttachFileUri(String attachFileUri) {
        this.attachFileUri = attachFileUri;
    }

    public String getAttachFileUrl() {
        return attachFileUrl;
    }

    public void setAttachFileUrl(String attachFileUrl) {
        this.attachFileUrl = attachFileUrl;
    }

    public boolean isAttachUpdated() {
        return isAttachUpdated;
    }

    public void setAttachUpdated(boolean attachUpdated) {
        isAttachUpdated = attachUpdated;
    }

    public boolean isAttachUploaded() {
        return isAttachUploaded;
    }

    public void setAttachUploaded(boolean attachUploaded) {
        isAttachUploaded = attachUploaded;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isForestry() {
        return isForestry;
    }

    public void setForestry(boolean forestry) {
        isForestry = forestry;
    }

    public int getForestryCostType() {
        return forestryCostType;
    }

    public void setForestryCostType(int forestryCostType) {
        this.forestryCostType = forestryCostType;
    }
}