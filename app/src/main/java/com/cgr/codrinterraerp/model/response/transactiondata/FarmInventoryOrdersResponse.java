package com.cgr.codrinterraerp.model.response.transactiondata;

import java.io.Serializable;

public class FarmInventoryOrdersResponse implements Serializable {

    private String inventoryOrder;
    private int supplierId;
    private boolean isFromReception;

    public String getInventoryOrder() {
        return inventoryOrder;
    }

    public void setInventoryOrder(String inventoryOrder) {
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