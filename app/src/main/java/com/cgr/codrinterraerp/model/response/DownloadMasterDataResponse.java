package com.cgr.codrinterraerp.model.response;

import com.cgr.codrinterraerp.model.response.masterdata.DispatchContainersResponse;
import com.cgr.codrinterraerp.model.response.masterdata.FarmInventoryOrdersResponse;
import com.cgr.codrinterraerp.model.response.masterdata.MeasurementSystemsResponse;
import com.cgr.codrinterraerp.model.response.masterdata.ProductTypesResponse;
import com.cgr.codrinterraerp.model.response.masterdata.ProductsResponse;
import com.cgr.codrinterraerp.model.response.masterdata.PurchaseContractsResponse;
import com.cgr.codrinterraerp.model.response.masterdata.ReceptionInventoryOrdersResponse;
import com.cgr.codrinterraerp.model.response.masterdata.ShippingLinesResponse;
import com.cgr.codrinterraerp.model.response.masterdata.SuppliersResponse;
import com.cgr.codrinterraerp.model.response.masterdata.WarehousesResponse;

import java.io.Serializable;
import java.util.List;

public class DownloadMasterDataResponse implements Serializable {

    private List<SuppliersResponse> suppliers;
    private List<WarehousesResponse> warehouses;
    private List<ShippingLinesResponse> shippingLines;
    private List<MeasurementSystemsResponse> measurementSystems;
    private List<PurchaseContractsResponse> purchaseContracts;
    private List<FarmInventoryOrdersResponse> farmInventoryOrders;
    private List<ReceptionInventoryOrdersResponse> receptionInventoryOrders;
    private List<DispatchContainersResponse> dispatchContainers;
    private List<ProductsResponse> products;
    private List<ProductTypesResponse> productTypes;

    public List<SuppliersResponse> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<SuppliersResponse> suppliers) {
        this.suppliers = suppliers;
    }

    public List<WarehousesResponse> getWarehouses() {
        return warehouses;
    }

    public void setWarehouses(List<WarehousesResponse> warehouses) {
        this.warehouses = warehouses;
    }

    public List<ShippingLinesResponse> getShippingLines() {
        return shippingLines;
    }

    public void setShippingLines(List<ShippingLinesResponse> shippingLines) {
        this.shippingLines = shippingLines;
    }

    public List<MeasurementSystemsResponse> getMeasurementSystems() {
        return measurementSystems;
    }

    public void setMeasurementSystems(List<MeasurementSystemsResponse> measurementSystems) {
        this.measurementSystems = measurementSystems;
    }

    public List<PurchaseContractsResponse> getPurchaseContracts() {
        return purchaseContracts;
    }

    public void setPurchaseContracts(List<PurchaseContractsResponse> purchaseContracts) {
        this.purchaseContracts = purchaseContracts;
    }

    public List<FarmInventoryOrdersResponse> getFarmInventoryOrders() {
        return farmInventoryOrders;
    }

    public void setFarmInventoryOrders(List<FarmInventoryOrdersResponse> farmInventoryOrders) {
        this.farmInventoryOrders = farmInventoryOrders;
    }

    public List<ReceptionInventoryOrdersResponse> getReceptionInventoryOrders() {
        return receptionInventoryOrders;
    }

    public void setReceptionInventoryOrders(List<ReceptionInventoryOrdersResponse> receptionInventoryOrders) {
        this.receptionInventoryOrders = receptionInventoryOrders;
    }

    public List<DispatchContainersResponse> getDispatchContainers() {
        return dispatchContainers;
    }

    public void setDispatchContainers(List<DispatchContainersResponse> dispatchContainers) {
        this.dispatchContainers = dispatchContainers;
    }

    public List<ProductsResponse> getProducts() {
        return products;
    }

    public void setProducts(List<ProductsResponse> products) {
        this.products = products;
    }

    public List<ProductTypesResponse> getProductTypes() {
        return productTypes;
    }

    public void setProductTypes(List<ProductTypesResponse> productTypes) {
        this.productTypes = productTypes;
    }
}