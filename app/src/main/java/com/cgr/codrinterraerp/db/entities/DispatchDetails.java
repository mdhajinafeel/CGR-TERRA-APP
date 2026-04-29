package com.cgr.codrinterraerp.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "dispatch_details",
        indices = {
                @Index(name = "idx_container_number_dispatch", value = {"containerNumber"}, unique = true)
        }
)
public class DispatchDetails implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public String tempDispatchId = "";
    public Integer dispatchId;
    @NonNull
    public String containerNumber = "";
    public int productId;
    public int productTypeId;
    public int warehouseId;
    public int shippingLineId;
    public String dispatchDate;
    public boolean isClosed = false;
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
    public String getTempDispatchId() {
        return tempDispatchId;
    }

    public void setTempDispatchId(@NonNull String tempDispatchId) {
        this.tempDispatchId = tempDispatchId;
    }

    public Integer getDispatchId() {
        return dispatchId;
    }

    public void setDispatchId(Integer dispatchId) {
        this.dispatchId = dispatchId;
    }

    @NonNull
    public String getContainerNumber() {
        return containerNumber;
    }

    public void setContainerNumber(@NonNull String containerNumber) {
        this.containerNumber = containerNumber;
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

    public int getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }

    public int getShippingLineId() {
        return shippingLineId;
    }

    public void setShippingLineId(int shippingLineId) {
        this.shippingLineId = shippingLineId;
    }

    public String getDispatchDate() {
        return dispatchDate;
    }

    public void setDispatchDate(String dispatchDate) {
        this.dispatchDate = dispatchDate;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
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