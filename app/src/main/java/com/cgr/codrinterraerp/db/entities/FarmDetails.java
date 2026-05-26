package com.cgr.codrinterraerp.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "farm_details",
        indices = {
                @Index(name = "idx_temp_farm_details", value = {"tempFarmId"}, unique = true),
                @Index(name = "idx_ica_farm", value = {"ica"}, unique = true)
        }
)
public class FarmDetails implements Serializable {

    @PrimaryKey
    @NonNull
    public String tempFarmId = "";
    public Integer farmId;
    @NonNull
    public String ica = "";
    public int supplierId;
    public int productId;
    public int productTypeId;
    public String purchaseDate;
    public int purchaseContract;
    public String truckNumber;
    public String truckDriverName;
    public boolean isSynced = false;
    public boolean isDeleted = false;
    public boolean isEdited = false;
    public long updatedAt = System.currentTimeMillis();
    public boolean isClosed = false;
    public int closedBy;
    public long closedDate;
    public long createdAt;

    @NonNull
    public String getTempFarmId() {
        return tempFarmId;
    }

    public void setTempFarmId(@NonNull String tempFarmId) {
        this.tempFarmId = tempFarmId;
    }

    public Integer getFarmId() {
        return farmId;
    }

    public void setFarmId(Integer farmId) {
        this.farmId = farmId;
    }

    @NonNull
    public String getIca() {
        return ica;
    }

    public void setIca(@NonNull String ica) {
        this.ica = ica;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getProductTypeId() {
        return productTypeId;
    }

    public void setProductTypeId(int productTypeId) {
        this.productTypeId = productTypeId;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
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

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public int getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(int closedBy) {
        this.closedBy = closedBy;
    }

    public long getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(long closedDate) {
        this.closedDate = closedDate;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}