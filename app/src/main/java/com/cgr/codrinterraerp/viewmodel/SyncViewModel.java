package com.cgr.codrinterraerp.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.DispatchContainers;
import com.cgr.codrinterraerp.db.entities.FarmInventoryOrders;
import com.cgr.codrinterraerp.db.entities.MeasurementSystemFormulaVariables;
import com.cgr.codrinterraerp.db.entities.MeasurementSystemFormulas;
import com.cgr.codrinterraerp.db.entities.MeasurementSystems;
import com.cgr.codrinterraerp.db.entities.ProductTypes;
import com.cgr.codrinterraerp.db.entities.Products;
import com.cgr.codrinterraerp.db.entities.PurchaseContracts;
import com.cgr.codrinterraerp.db.entities.ReceptionInventoryOrders;
import com.cgr.codrinterraerp.db.entities.ShippingLines;
import com.cgr.codrinterraerp.db.entities.SupplierProductTypes;
import com.cgr.codrinterraerp.db.entities.SupplierProducts;
import com.cgr.codrinterraerp.db.entities.Suppliers;
import com.cgr.codrinterraerp.db.entities.Warehouses;
import com.cgr.codrinterraerp.helper.PreferenceManager;
import com.cgr.codrinterraerp.model.response.DownloadMasterDataResponse;
import com.cgr.codrinterraerp.model.response.DownloadMasterResponse;
import com.cgr.codrinterraerp.model.response.masterdata.DispatchContainersResponse;
import com.cgr.codrinterraerp.model.response.masterdata.FarmInventoryOrdersResponse;
import com.cgr.codrinterraerp.model.response.masterdata.MeasurementSystemFormulaVariablesResponse;
import com.cgr.codrinterraerp.model.response.masterdata.MeasurementSystemFormulasResponse;
import com.cgr.codrinterraerp.model.response.masterdata.MeasurementSystemsResponse;
import com.cgr.codrinterraerp.model.response.masterdata.ProductTypesResponse;
import com.cgr.codrinterraerp.model.response.masterdata.ProductsResponse;
import com.cgr.codrinterraerp.model.response.masterdata.PurchaseContractsResponse;
import com.cgr.codrinterraerp.model.response.masterdata.ReceptionInventoryOrdersResponse;
import com.cgr.codrinterraerp.model.response.masterdata.ShippingLinesResponse;
import com.cgr.codrinterraerp.model.response.masterdata.SupplierProductTypesResponse;
import com.cgr.codrinterraerp.model.response.masterdata.SupplierProductsResponse;
import com.cgr.codrinterraerp.model.response.masterdata.SuppliersResponse;
import com.cgr.codrinterraerp.model.response.masterdata.WarehousesResponse;
import com.cgr.codrinterraerp.repository.MasterRepository;
import com.cgr.codrinterraerp.wrapper.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class SyncViewModel extends ViewModel {

    private final MasterRepository masterRepository;
    private final Context context;
    private final SingleLiveEvent<Boolean> hasUnsyncedData = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> errorTitle = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> errorMessage = new SingleLiveEvent<>();
    private final SingleLiveEvent<Boolean> progressState = new SingleLiveEvent<>();
    private final SingleLiveEvent<Boolean> syncStatus = new SingleLiveEvent<>();

    @Inject
    public SyncViewModel(MasterRepository masterRepository, @ApplicationContext Context context) {
        this.masterRepository = masterRepository;
        this.context = context;
    }

    public void masterDownload() {

        progressState.postValue(true);

        masterRepository.masterDownload().enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<DownloadMasterResponse> call,
                                   @NonNull Response<DownloadMasterResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        try {
                            PreferenceManager.INSTANCE.setDownloadMasterVersion(response.body().getVersion());
                            DownloadMasterDataResponse data = response.body().getData();
                            if (data != null) {
                                // 🔥 SINGLE TRANSACTION (BIG PERFORMANCE BOOST)
                                masterRepository.runInTransaction(() -> processAllData(data));
                                syncStatus.postValue(true);
                            } else {
                                syncStatus.postValue(false);
                            }
                        } catch (Exception e) {
                            syncStatus.postValue(false);
                        } finally {
                            progressState.postValue(false);
                        }
                    }).start();

                } else {
                    progressState.postValue(false);
                    errorTitle.postValue(context.getString(R.string.error));
                    errorMessage.postValue(context.getString(R.string.common_error));
                    syncStatus.postValue(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DownloadMasterResponse> call,
                                  @NonNull Throwable t) {
                progressState.postValue(false);
                syncStatus.postValue(false);
            }
        });
    }

    private void processAllData(DownloadMasterDataResponse data) {

        // ---------------- SUPPLIERS ----------------
        List<SuppliersResponse> suppliersResponseList = data.getSuppliers();
        if (suppliersResponseList != null && !suppliersResponseList.isEmpty()) {

            List<Suppliers> suppliersList = new ArrayList<>(suppliersResponseList.size());
            List<SupplierProducts> productsList = new ArrayList<>();
            List<SupplierProductTypes> typesList = new ArrayList<>();

            for (SuppliersResponse s : suppliersResponseList) {

                Suppliers supplier = new Suppliers();
                supplier.setSupplierId(s.getSupplierId());
                supplier.setSupplierName(s.getSupplierName());

                // ---------------- SUPPLIER PRODUCTS ----------------
                if (s.getSupplierProducts() != null) {
                    for (SupplierProductsResponse p : s.getSupplierProducts()) {

                        SupplierProducts product = getSupplierProducts(p, supplier);

                        // ---------------- SUPPLIER PRODUCT TYPES ----------------
                        if (p.getSupplierProductTypes() != null) {
                            for (SupplierProductTypesResponse t : p.getSupplierProductTypes()) {
                                typesList.add(getSupplierProductTypes(p, t, supplier));
                            }
                        }

                        productsList.add(product);
                    }
                }

                suppliersList.add(supplier);
            }

            if(!suppliersList.isEmpty()) {
                masterRepository.deleteSupplierData();
            }

            masterRepository.insertSuppliers(suppliersList);
            masterRepository.insertSupplierProducts(productsList);
            masterRepository.insertSupplierProductTypes(typesList);
        }

        // ---------------- WAREHOUSES ----------------
        List<WarehousesResponse> warehouses = data.getWarehouses();
        if (warehouses != null && !warehouses.isEmpty()) {

            if(!getWarehouses(warehouses).isEmpty()) {
                masterRepository.deleteWarehouseData();
            }

            masterRepository.insertWarehouses(getWarehouses(warehouses));
        }

        // ---------------- MEASUREMENT SYSTEMS ----------------
        List<MeasurementSystemsResponse> measurement = data.getMeasurementSystems();
        if (measurement != null && !measurement.isEmpty()) {

            List<MeasurementSystems> measurementSystemsList = new ArrayList<>();
            List<MeasurementSystemFormulas> measurementSystemFormulasList = new ArrayList<>();
            List<MeasurementSystemFormulaVariables> measurementSystemFormulaVariablesList = new ArrayList<>();

            for (MeasurementSystemsResponse measurementSystemsResponse : measurement) {
                MeasurementSystems measurementSystem = new MeasurementSystems();
                measurementSystem.setId(measurementSystemsResponse.getId());
                measurementSystem.setMeasurementName(measurementSystemsResponse.getMeasurementName());
                measurementSystem.setProductTypeId(measurementSystemsResponse.getProductTypeId());

                if(measurementSystemsResponse.getFormulas()!= null && !measurementSystemsResponse.getFormulas().isEmpty()) {
                    for (MeasurementSystemFormulasResponse measurementSystemFormulasResponse : measurementSystemsResponse.getFormulas()) {
                        MeasurementSystemFormulas measurementSystemFormula = getMeasurementSystemFormulas(measurementSystemsResponse, measurementSystemFormulasResponse);

                        if(measurementSystemFormulasResponse.getVariables() != null && !measurementSystemFormulasResponse.getVariables().isEmpty()) {
                            for(MeasurementSystemFormulaVariablesResponse measurementSystemFormulaVariablesResponse : measurementSystemFormulasResponse.getVariables()) {
                                MeasurementSystemFormulaVariables measurementSystemFormulaVariable = getMeasurementSystemFormulaVariables(measurementSystemsResponse, measurementSystemFormulaVariablesResponse);

                                measurementSystemFormulaVariablesList.add(measurementSystemFormulaVariable);
                            }
                        }

                        measurementSystemFormulasList.add(measurementSystemFormula);
                    }
                }

                measurementSystemsList.add(measurementSystem);
            }

            if(!measurementSystemsList.isEmpty()) {
                masterRepository.deleteMeasurementSystems();
            }

            if(!measurementSystemFormulasList.isEmpty()) {
                masterRepository.deleteMeasurementSystemsFormulas();
            }

            if(!measurementSystemFormulaVariablesList.isEmpty()) {
                masterRepository.deleteMeasurementSystemsFormulaVariables();
            }

            masterRepository.insertMeasurementSystems(measurementSystemsList);
            masterRepository.insertMeasurementSystemsFormula(measurementSystemFormulasList);
            masterRepository.insertMeasurementSystemsFormulaVariables(measurementSystemFormulaVariablesList);
        }

        // ---------------- SHIPPING ----------------
        List<ShippingLinesResponse> shipping = data.getShippingLines();
        if (shipping != null && !shipping.isEmpty()) {

            if(!getShippingLines(shipping).isEmpty()) {
                masterRepository.deleteShippingLines();
            }

            masterRepository.insertShippingLines(getShippingLines(shipping));
        }

        // ---------------- CONTRACTS ----------------
        List<PurchaseContractsResponse> contracts = data.getPurchaseContracts();
        if (contracts != null && !contracts.isEmpty()) {

            if(!getPurchaseContracts(contracts).isEmpty()) {
                masterRepository.deletePurchaseContract();
            }

            masterRepository.insertPurchaseContracts(getPurchaseContracts(contracts));
        }

        // ---------------- FARM ----------------
        List<FarmInventoryOrdersResponse> farm = data.getFarmInventoryOrders();
        if (farm != null && !farm.isEmpty()) {

            if(!getFarmInventoryOrders(farm).isEmpty()) {
                masterRepository.deleteFarmInventoryOrders();
            }

            masterRepository.insertFarmInventoryOrders(getFarmInventoryOrders(farm));
        }

        // ---------------- RECEPTION ----------------
        List<ReceptionInventoryOrdersResponse> reception = data.getReceptionInventoryOrders();
        if (reception != null && !reception.isEmpty()) {

            if(!getReceptionInventoryOrders(reception).isEmpty()) {
                masterRepository.deleteReceptionInventoryOrders();
            }

            masterRepository.insertReceptionInventoryOrders(getReceptionInventoryOrders(reception));
        }

        // ---------------- DISPATCH ----------------
        List<DispatchContainersResponse> dispatch = data.getDispatchContainers();
        if (dispatch != null && !dispatch.isEmpty()) {

            if(!getDispatchContainers(dispatch).isEmpty()) {
                masterRepository.deleteDispatchContainers();
            }

            masterRepository.insertDispatchContainers(getDispatchContainers(dispatch));
        }

        // ---------------- PRODUCTS ----------------
        List<ProductsResponse> product = data.getProducts();
        if(product != null && !product.isEmpty()) {

            if(!getProducts(product).isEmpty()) {
                masterRepository.deleteProducts();
            }

            masterRepository.insertProducts(getProducts(product));
        }

        // ---------------- PRODUCT TYPES ----------------
        List<ProductTypesResponse> productType = data.getProductTypes();
        if(productType != null && !productType.isEmpty()) {

            if(!getProductTypes(productType).isEmpty()) {
                masterRepository.deleteProductTypes();
            }

            masterRepository.insertProductTypes(getProductTypes(productType));
        }
    }

    @NonNull
    private static MeasurementSystemFormulaVariables getMeasurementSystemFormulaVariables(MeasurementSystemsResponse measurementSystemsResponse, MeasurementSystemFormulaVariablesResponse measurementSystemFormulaVariablesResponse) {
        MeasurementSystemFormulaVariables measurementSystemFormulaVariable = new MeasurementSystemFormulaVariables();
        measurementSystemFormulaVariable.setMeasurementSystemId(measurementSystemsResponse.getId());
        measurementSystemFormulaVariable.setFormulaMasterId(measurementSystemFormulaVariablesResponse.getFormulaMasterId());
        measurementSystemFormulaVariable.setUnit(measurementSystemFormulaVariablesResponse.getUnit());
        measurementSystemFormulaVariable.setDisplayName(measurementSystemFormulaVariablesResponse.getDisplayName());
        measurementSystemFormulaVariable.setSortOrder(measurementSystemFormulaVariablesResponse.getSortOrder());
        measurementSystemFormulaVariable.setVarName(measurementSystemFormulaVariablesResponse.getVarName());
        return measurementSystemFormulaVariable;
    }

    @NonNull
    private static List<ProductTypes> getProductTypes(List<ProductTypesResponse> productTypesResponseList) {
        List<ProductTypes> productTypesList = new ArrayList<>();
        for (ProductTypesResponse productTypesResponse : productTypesResponseList) {
            ProductTypes productTypes = new ProductTypes();
            productTypes.setTypeId(productTypesResponse.getTypeId());
            productTypes.setProductTypeName(productTypesResponse.getProductTypeName());

            productTypesList.add(productTypes);
        }
        return productTypesList;
    }

    @NonNull
    private static List<Products> getProducts(List<ProductsResponse> productsResponseList) {
        List<Products> productsList = new ArrayList<>();
        for (ProductsResponse productsResponse : productsResponseList) {
            Products product = new Products();
            product.setProductId(productsResponse.getProductId());
            product.setProductName(productsResponse.getProductName());

            productsList.add(product);
        }
        return productsList;
    }

    @NonNull
    private static List<DispatchContainers> getDispatchContainers(List<DispatchContainersResponse> dispatchContainersResponseList) {
        List<DispatchContainers> dispatchContainersList = new ArrayList<>();
        for (DispatchContainersResponse dispatchContainersResponse : dispatchContainersResponseList) {
            DispatchContainers dispatchContainer = new DispatchContainers();
            dispatchContainer.setContainerNumber(dispatchContainersResponse.getContainerNumber());
            dispatchContainer.setShippingLineId(dispatchContainersResponse.getShippingLineId());

            dispatchContainersList.add(dispatchContainer);
        }
        return dispatchContainersList;
    }

    @NonNull
    private static List<ReceptionInventoryOrders> getReceptionInventoryOrders(List<ReceptionInventoryOrdersResponse> receptionInventoryOrdersResponseList) {
        List<ReceptionInventoryOrders> receptionInventoryOrdersList = new ArrayList<>();
        for (ReceptionInventoryOrdersResponse receptionInventoryOrdersResponse : receptionInventoryOrdersResponseList) {
            ReceptionInventoryOrders receptionInventoryOrder = new ReceptionInventoryOrders();
            receptionInventoryOrder.setSupplierId(receptionInventoryOrdersResponse.getSupplierId());
            receptionInventoryOrder.setInventoryOrder(receptionInventoryOrdersResponse.getInventoryOrder());

            receptionInventoryOrdersList.add(receptionInventoryOrder);
        }
        return receptionInventoryOrdersList;
    }

    @NonNull
    private static List<FarmInventoryOrders> getFarmInventoryOrders(List<FarmInventoryOrdersResponse> farmInventoryOrdersResponseList) {
        List<FarmInventoryOrders> farmInventoryOrdersList = new ArrayList<>();
        for (FarmInventoryOrdersResponse farmInventoryOrdersResponse : farmInventoryOrdersResponseList) {
            FarmInventoryOrders farmInventoryOrder = new FarmInventoryOrders();
            farmInventoryOrder.setSupplierId(farmInventoryOrdersResponse.getSupplierId());
            farmInventoryOrder.setInventoryOrder(farmInventoryOrdersResponse.getInventoryOrder());

            farmInventoryOrdersList.add(farmInventoryOrder);
        }
        return farmInventoryOrdersList;
    }

    @NonNull
    private static List<PurchaseContracts> getPurchaseContracts(List<PurchaseContractsResponse> purchaseContractsResponseList) {
        List<PurchaseContracts> purchaseContractsList = new ArrayList<>();
        for (PurchaseContractsResponse purchaseContractsResponse : purchaseContractsResponseList) {
            PurchaseContracts purchaseContract = new PurchaseContracts();
            purchaseContract.setContractId(purchaseContractsResponse.getContractId());
            purchaseContract.setContractCode(purchaseContractsResponse.getContractCode());
            purchaseContract.setCurrency(purchaseContractsResponse.getCurrency());
            purchaseContract.setPurchaseAllowance(purchaseContractsResponse.getPurchaseAllowance());
            purchaseContract.setPurchaseAllowanceLength(purchaseContractsResponse.getPurchaseAllowanceLength());
            purchaseContract.setSupplierId(purchaseContractsResponse.getSupplierId());
            purchaseContract.setDescription(purchaseContractsResponse.getDescription());
            purchaseContract.setPurchaseUnitId(purchaseContractsResponse.getPurchaseUnitId());
            purchaseContract.setProduct(purchaseContractsResponse.getProduct());
            purchaseContract.setProductType(purchaseContractsResponse.getProductType());
            purchaseContract.setPurchaseUnit(purchaseContractsResponse.getPurchaseUnit());
            purchaseContract.setPurchaseUnitId(purchaseContractsResponse.getPurchaseUnitId());

            purchaseContractsList.add(purchaseContract);
        }
        return purchaseContractsList;
    }

    @NonNull
    private static List<ShippingLines> getShippingLines(List<ShippingLinesResponse> shippingLinesResponseList) {
        List<ShippingLines> shippingLinesList = new ArrayList<>();
        for (ShippingLinesResponse shippingLinesResponse : shippingLinesResponseList) {
            ShippingLines shippingLines = new ShippingLines();
            shippingLines.setId(shippingLinesResponse.getId());
            shippingLines.setShippingLine(shippingLinesResponse.getShippingLine());

            shippingLinesList.add(shippingLines);
        }
        return shippingLinesList;
    }

    @NonNull
    private static MeasurementSystemFormulas getMeasurementSystemFormulas(MeasurementSystemsResponse measurementSystemsResponse, MeasurementSystemFormulasResponse measurementSystemFormulasResponse) {
        MeasurementSystemFormulas measurementSystemFormula = new MeasurementSystemFormulas();
        measurementSystemFormula.setFormulaMasterId(measurementSystemFormulasResponse.getFormulaMasterId());
        measurementSystemFormula.setMeasurementSystemId(measurementSystemsResponse.getId());
        measurementSystemFormula.setFormula(measurementSystemFormulasResponse.getFormula());
        measurementSystemFormula.setRoundPrecision(measurementSystemFormulasResponse.getRoundPrecision());
        measurementSystemFormula.setRoundingType(measurementSystemFormulasResponse.getRoundingType());
        measurementSystemFormula.setContext(measurementSystemFormulasResponse.getContext());
        return measurementSystemFormula;
    }

    @NonNull
    private static List<Warehouses> getWarehouses(List<WarehousesResponse> warehousesResponseList) {
        List<Warehouses> warehousesList = new ArrayList<>();
        for (WarehousesResponse warehousesResponse : warehousesResponseList) {
            Warehouses warehouse = new Warehouses();
            warehouse.setId(warehousesResponse.getId());
            warehouse.setWarehouseName(warehousesResponse.getWarehouseName());

            warehousesList.add(warehouse);
        }
        return warehousesList;
    }

    @NonNull
    private static SupplierProductTypes getSupplierProductTypes(SupplierProductsResponse supplierProductsResponse,
                                                                SupplierProductTypesResponse supplierProductTypesResponse, Suppliers supplier) {
        SupplierProductTypes supplierProductTypes = new SupplierProductTypes();
        supplierProductTypes.setSupplierId(supplier.getSupplierId());
        supplierProductTypes.setProductId(supplierProductsResponse.getProductId());
        supplierProductTypes.setProductTypeId(supplierProductTypesResponse.getProductTypeId());
        supplierProductTypes.setProductTypeName(supplierProductTypesResponse.getProductTypeName());
        supplierProductTypes.setTypeId(supplierProductTypesResponse.getTypeId());
        supplierProductTypes.setSupplierProductId(supplierProductsResponse.getSupplierProductId());
        return supplierProductTypes;
    }

    @NonNull
    private static SupplierProducts getSupplierProducts(SupplierProductsResponse supplierProductsResponse, Suppliers supplier) {
        SupplierProducts supplierProducts = new SupplierProducts();
        supplierProducts.setSupplierId(supplier.getSupplierId());
        supplierProducts.setProductId(supplierProductsResponse.getProductId());
        supplierProducts.setProductName(supplierProductsResponse.getProductName());
        supplierProducts.setSupplierProductId(supplierProductsResponse.getSupplierProductId());
        return supplierProducts;
    }

    public LiveData<Boolean> getProgressState() {
        return progressState;
    }

    public LiveData<Boolean> getSyncStatus() {
        return syncStatus;
    }

    public LiveData<String> getErrorTitle() {
        return errorTitle;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> hasUnsyncedDataLiveData() {
        return hasUnsyncedData;
    }

    public void checkUnsyncedData() {
        new Thread(() -> {
            //boolean result = expenseRepository.hasUnsyncedData();
            hasUnsyncedData.postValue(true);
        }).start();
    }
}