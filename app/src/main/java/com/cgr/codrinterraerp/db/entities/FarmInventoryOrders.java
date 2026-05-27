package com.cgr.codrinterraerp.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "farm_inventory_orders",
        indices = {
                @Index(name = "idx_inventory_order_farm", value = {"inventoryOrder"}),
                @Index(name = "idx_supplier_id_farm", value = {"supplierId"})
        }
)
public class FarmInventoryOrders implements Serializable {

    @PrimaryKey
    @NonNull
    private String inventoryOrder = ""; // ✅ FIXED
    private int supplierId;
    private boolean isFromReception;

    @NonNull
    public String getInventoryOrder() {
        return inventoryOrder;
    }

    public void setInventoryOrder(@NonNull String inventoryOrder) {
        this.inventoryOrder = inventoryOrder;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public boolean isFromReception() {
        return isFromReception;
    }

    public void setFromReception(boolean fromReception) {
        isFromReception = fromReception;
    }
}