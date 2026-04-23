package com.cgr.codrinterraerp.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "reception_details",
        indices = {
                @Index(name = "idx_ica_reception", value = {"ica"}, unique = true)
        }
)
public class ReceptionDetails implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public String tempReceptionId = "";
    public Integer receptionId;
    @NonNull
    public String ica = "";
    public int supplier;
    public int supplierProductId;
    public int supplierProductTypeId;
    public int measurementSystem;
    public int warehouse;
    public String receptionDate;
    public boolean isFarmEnabled;
    public int purchaseContract;
    public String truckNumber;
    public String truckDriverName;
    public boolean isSynced = false;
    public boolean isDeleted = false;
    public boolean isEdited = false;
    public long updatedAt = System.currentTimeMillis();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getTempReceptionId() {
        return tempReceptionId;
    }

    public void setTempReceptionId(@NonNull String tempReceptionId) {
        this.tempReceptionId = tempReceptionId;
    }

    public Integer getReceptionId() {
        return receptionId;
    }

    public void setReceptionId(Integer receptionId) {
        this.receptionId = receptionId;
    }

    @NonNull
    public String getIca() {
        return ica;
    }

    public void setIca(@NonNull String ica) {
        this.ica = ica;
    }

    public int getSupplier() {
        return supplier;
    }

    public void setSupplier(int supplier) {
        this.supplier = supplier;
    }

    public int getSupplierProductId() {
        return supplierProductId;
    }

    public void setSupplierProductId(int supplierProductId) {
        this.supplierProductId = supplierProductId;
    }

    public int getSupplierProductTypeId() {
        return supplierProductTypeId;
    }

    public void setSupplierProductTypeId(int supplierProductTypeId) {
        this.supplierProductTypeId = supplierProductTypeId;
    }

    public int getMeasurementSystem() {
        return measurementSystem;
    }

    public void setMeasurementSystem(int measurementSystem) {
        this.measurementSystem = measurementSystem;
    }

    public int getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(int warehouse) {
        this.warehouse = warehouse;
    }

    public String getReceptionDate() {
        return receptionDate;
    }

    public void setReceptionDate(String receptionDate) {
        this.receptionDate = receptionDate;
    }

    public boolean isFarmEnabled() {
        return isFarmEnabled;
    }

    public void setFarmEnabled(boolean farmEnabled) {
        isFarmEnabled = farmEnabled;
    }

    public int getPurchaseContract() {
        return purchaseContract;
    }

    public void setPurchaseContract(int purchaseContract) {
        this.purchaseContract = purchaseContract;
    }

    public String getTruckNumber() {
        return truckNumber;
    }

    public void setTruckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
    }

    public String getTruckDriverName() {
        return truckDriverName;
    }

    public void setTruckDriverName(String truckDriverName) {
        this.truckDriverName = truckDriverName;
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
}