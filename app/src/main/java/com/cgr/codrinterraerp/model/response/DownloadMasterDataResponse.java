package com.cgr.codrinterraerp.model.response;

import com.cgr.codrinterraerp.model.response.masterdata.AccountHeadsResponse;
import com.cgr.codrinterraerp.model.response.masterdata.BeneficiariesResponse;
import com.cgr.codrinterraerp.model.response.masterdata.ContainerCategoriesResponse;
import com.cgr.codrinterraerp.model.response.masterdata.MeasurementSystemsResponse;
import com.cgr.codrinterraerp.model.response.masterdata.ProductTypesResponse;
import com.cgr.codrinterraerp.model.response.masterdata.ProductsResponse;
import com.cgr.codrinterraerp.model.response.masterdata.PurchaseContractsResponse;
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
    private List<ProductsResponse> products;
    private List<ProductTypesResponse> productTypes;
    private List<ContainerCategoriesResponse> containerCategories;
    private List<AccountHeadsResponse> accountHeads;
    private List<BeneficiariesResponse> beneficiaries;

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

    public List<ContainerCategoriesResponse> getContainerCategories() {
        return containerCategories;
    }

    public void setContainerCategories(List<ContainerCategoriesResponse> containerCategories) {
        this.containerCategories = containerCategories;
    }

    public List<AccountHeadsResponse> getAccountHeads() {
        return accountHeads;
    }

    public void setAccountHeads(List<AccountHeadsResponse> accountHeads) {
        this.accountHeads = accountHeads;
    }

    public List<BeneficiariesResponse> getBeneficiaries() {
        return beneficiaries;
    }

    public void setBeneficiaries(List<BeneficiariesResponse> beneficiaries) {
        this.beneficiaries = beneficiaries;
    }
}