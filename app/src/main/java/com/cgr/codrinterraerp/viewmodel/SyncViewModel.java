package com.cgr.codrinterraerp.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.constants.SyncResult;
import com.cgr.codrinterraerp.db.entities.ContainerCategories;
import com.cgr.codrinterraerp.db.entities.DispatchContainers;
import com.cgr.codrinterraerp.db.entities.DispatchDetails;
import com.cgr.codrinterraerp.db.entities.DispatchSummary;
import com.cgr.codrinterraerp.db.entities.FarmInventoryOrders;
import com.cgr.codrinterraerp.db.entities.MeasurementSystemFormulaVariables;
import com.cgr.codrinterraerp.db.entities.MeasurementSystemFormulas;
import com.cgr.codrinterraerp.db.entities.MeasurementSystems;
import com.cgr.codrinterraerp.db.entities.ProductTypes;
import com.cgr.codrinterraerp.db.entities.Products;
import com.cgr.codrinterraerp.db.entities.PurchaseContracts;
import com.cgr.codrinterraerp.db.entities.ReceptionDetails;
import com.cgr.codrinterraerp.db.entities.ReceptionInventoryOrders;
import com.cgr.codrinterraerp.db.entities.ReceptionSummary;
import com.cgr.codrinterraerp.db.entities.ShippingLines;
import com.cgr.codrinterraerp.db.entities.SupplierProductTypes;
import com.cgr.codrinterraerp.db.entities.SupplierProducts;
import com.cgr.codrinterraerp.db.entities.Suppliers;
import com.cgr.codrinterraerp.db.entities.Warehouses;
import com.cgr.codrinterraerp.helper.DispatchSummaryHelper;
import com.cgr.codrinterraerp.helper.PreferenceManager;
import com.cgr.codrinterraerp.helper.ReceptionSummaryHelper;
import com.cgr.codrinterraerp.model.response.DownloadMasterDataResponse;
import com.cgr.codrinterraerp.model.response.DownloadMasterResponse;
import com.cgr.codrinterraerp.model.response.DownloadTransactionsDataResponse;
import com.cgr.codrinterraerp.model.response.DownloadTransactionsResponse;
import com.cgr.codrinterraerp.model.response.masterdata.ContainerCategoriesResponse;
import com.cgr.codrinterraerp.model.response.transactiondata.DispatchContainersResponse;
import com.cgr.codrinterraerp.model.response.transactiondata.DispatchDetailsResponse;
import com.cgr.codrinterraerp.model.response.transactiondata.FarmInventoryOrdersResponse;
import com.cgr.codrinterraerp.model.response.masterdata.MeasurementSystemFormulaVariablesResponse;
import com.cgr.codrinterraerp.model.response.masterdata.MeasurementSystemFormulasResponse;
import com.cgr.codrinterraerp.model.response.masterdata.MeasurementSystemsResponse;
import com.cgr.codrinterraerp.model.response.masterdata.ProductTypesResponse;
import com.cgr.codrinterraerp.model.response.masterdata.ProductsResponse;
import com.cgr.codrinterraerp.model.response.masterdata.PurchaseContractsResponse;
import com.cgr.codrinterraerp.model.response.transactiondata.ReceptionDetailsResponse;
import com.cgr.codrinterraerp.model.response.transactiondata.ReceptionInventoryOrdersResponse;
import com.cgr.codrinterraerp.model.response.masterdata.ShippingLinesResponse;
import com.cgr.codrinterraerp.model.response.masterdata.SupplierProductTypesResponse;
import com.cgr.codrinterraerp.model.response.masterdata.SupplierProductsResponse;
import com.cgr.codrinterraerp.model.response.masterdata.SuppliersResponse;
import com.cgr.codrinterraerp.model.response.masterdata.WarehousesResponse;
import com.cgr.codrinterraerp.repository.MasterRepository;
import com.cgr.codrinterraerp.repository.SyncRepository;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.wrapper.DispatchMapper;
import com.cgr.codrinterraerp.wrapper.ReceptionMapper;
import com.cgr.codrinterraerp.wrapper.SingleLiveEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class SyncViewModel extends ViewModel {

    private final MasterRepository masterRepository;
    private final SyncRepository syncRepository;
    private final Context context;
    private final SingleLiveEvent<Boolean> hasUnsyncedData = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> errorTitle = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> errorMessage = new SingleLiveEvent<>();
    private final SingleLiveEvent<Boolean> progressState = new SingleLiveEvent<>();
    private final SingleLiveEvent<Boolean> syncStatus = new SingleLiveEvent<>();
    private final ReceptionSummaryHelper receptionSummaryHelper;
    private final DispatchSummaryHelper dispatchSummaryHelper;

    @Inject
    public SyncViewModel(SyncRepository syncRepository, MasterRepository masterRepository, ReceptionSummaryHelper receptionSummaryHelper,
                         DispatchSummaryHelper dispatchSummaryHelper, @ApplicationContext Context context) {
        this.syncRepository = syncRepository;
        this.masterRepository = masterRepository;
        this.receptionSummaryHelper = receptionSummaryHelper;
        this.dispatchSummaryHelper = dispatchSummaryHelper;
        this.context = context;
    }

    public void startFullSync() {
        progressState.postValue(true);

        syncRepository.clearLocalDeletedData();

        syncContainerPhotos();
    }

    private void syncContainerPhotos() {

        if (!syncRepository.getUnsyncedImages().isEmpty()) {
            syncRepository.uploadContainerImage(result -> {
                if (result == SyncResult.FAILED) {
                    progressState.postValue(false);
                    errorTitle.postValue(context.getString(R.string.error));
                    errorMessage.postValue(context.getString(R.string.container_photos_sync_failed));
                    return;
                }

                // SUCCESS or NO_DATA → next
                syncData();
            });
        } else {
            // SUCCESS or NO_DATA → next
            syncData();
        }
    }

    private void syncData() {

        if (syncRepository.hasUnsyncedData()) {
            syncRepository.syncData(context, result -> {
                if (result == SyncResult.FAILED) {
                    progressState.postValue(false);
                    errorTitle.postValue(context.getString(R.string.error));
                    errorMessage.postValue(context.getString(R.string.data_sync_failed));
                    return;
                }

                // SUCCESS or NO_DATA → next
                PreferenceManager.INSTANCE.setLastSyncTime(System.currentTimeMillis());
                masterDownload(false);
            });
        } else {
            // SUCCESS or NO_DATA → next
            masterDownload(false);
        }
    }

    public void masterDownload(boolean isProgressRequired) {

        if (isProgressRequired) {
            progressState.postValue(true);
        }

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

                                // Handle old data
                                ExecutorService executor = Executors.newSingleThreadExecutor();
                                executor.execute(() -> {
                                    long threeMonthsAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000);
                                    syncRepository.deleteOldReceptionDetails(threeMonthsAgo);
                                    syncRepository.deleteOldDispatchDetails(threeMonthsAgo);
                                    syncRepository.deleteOldFarmDetails(threeMonthsAgo);
                                });
                            }
                        } catch (Exception e) {
                            AppLogger.e(getClass(), "masterDownload", e);
                            syncStatus.postValue(false);
                        } finally {
                            // ALWAYS continue transaction download
                            transactionDownload();
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

                AppLogger.e(getClass(), "masterDownload", t);

                // EVEN IF MASTER FAILED → CONTINUE
                transactionDownload();
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

            if (!suppliersList.isEmpty()) {
                masterRepository.deleteSupplierData();
            }

            masterRepository.insertSuppliers(suppliersList);
            masterRepository.insertSupplierProducts(productsList);
            masterRepository.insertSupplierProductTypes(typesList);
        }

        // ---------------- WAREHOUSES ----------------
        List<WarehousesResponse> warehouses = data.getWarehouses();
        if (warehouses != null && !warehouses.isEmpty()) {

            if (!getWarehouses(warehouses).isEmpty()) {
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

                if (measurementSystemsResponse.getFormulas() != null && !measurementSystemsResponse.getFormulas().isEmpty()) {
                    for (MeasurementSystemFormulasResponse measurementSystemFormulasResponse : measurementSystemsResponse.getFormulas()) {
                        MeasurementSystemFormulas measurementSystemFormula = getMeasurementSystemFormulas(measurementSystemsResponse, measurementSystemFormulasResponse);

                        if (measurementSystemFormulasResponse.getVariables() != null && !measurementSystemFormulasResponse.getVariables().isEmpty()) {
                            for (MeasurementSystemFormulaVariablesResponse measurementSystemFormulaVariablesResponse : measurementSystemFormulasResponse.getVariables()) {
                                MeasurementSystemFormulaVariables measurementSystemFormulaVariable = getMeasurementSystemFormulaVariables(measurementSystemsResponse, measurementSystemFormulaVariablesResponse);

                                measurementSystemFormulaVariablesList.add(measurementSystemFormulaVariable);
                            }
                        }

                        measurementSystemFormulasList.add(measurementSystemFormula);
                    }
                }

                measurementSystemsList.add(measurementSystem);
            }

            if (!measurementSystemsList.isEmpty()) {
                masterRepository.deleteMeasurementSystems();
            }

            if (!measurementSystemFormulasList.isEmpty()) {
                masterRepository.deleteMeasurementSystemsFormulas();
            }

            if (!measurementSystemFormulaVariablesList.isEmpty()) {
                masterRepository.deleteMeasurementSystemsFormulaVariables();
            }

            masterRepository.insertMeasurementSystems(measurementSystemsList);
            masterRepository.insertMeasurementSystemsFormula(measurementSystemFormulasList);
            masterRepository.insertMeasurementSystemsFormulaVariables(measurementSystemFormulaVariablesList);
        }

        // ---------------- SHIPPING ----------------
        List<ShippingLinesResponse> shipping = data.getShippingLines();
        if (shipping != null && !shipping.isEmpty()) {

            if (!getShippingLines(shipping).isEmpty()) {
                masterRepository.deleteShippingLines();
            }

            masterRepository.insertShippingLines(getShippingLines(shipping));
        }

        // ---------------- CONTRACTS ----------------
        List<PurchaseContractsResponse> contracts = data.getPurchaseContracts();
        if (contracts != null && !contracts.isEmpty()) {

            if (!getPurchaseContracts(contracts).isEmpty()) {
                masterRepository.deletePurchaseContract();
            }

            masterRepository.insertPurchaseContracts(getPurchaseContracts(contracts));
        }

        // ---------------- PRODUCTS ----------------
        List<ProductsResponse> product = data.getProducts();
        if (product != null && !product.isEmpty()) {

            if (!getProducts(product).isEmpty()) {
                masterRepository.deleteProducts();
            }

            masterRepository.insertProducts(getProducts(product));
        }

        // ---------------- PRODUCT TYPES ----------------
        List<ProductTypesResponse> productType = data.getProductTypes();
        if (productType != null && !productType.isEmpty()) {

            if (!getProductTypes(productType).isEmpty()) {
                masterRepository.deleteProductTypes();
            }

            masterRepository.insertProductTypes(getProductTypes(productType));
        }

        // ---------------- CONTAINER CATEGORIES ----------------
        List<ContainerCategoriesResponse> category = data.getContainerCategories();
        if (category != null && !category.isEmpty()) {
            if (!getContainerCategories(category).isEmpty()) {
                masterRepository.deleteContainerCategories();
            }

            masterRepository.insertContainerCategories(getContainerCategories(category));
        }
    }

    public void transactionDownload() {

        long lastSync = PreferenceManager.INSTANCE.getLastTransactionSyncTime();

        masterRepository.transactionDownload(lastSync).enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<DownloadTransactionsResponse> call,
                                   @NonNull Response<DownloadTransactionsResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        try {
                            DownloadTransactionsDataResponse data = response.body().getData();
                            if (data != null) {
                                // 🔥 SINGLE TRANSACTION (BIG PERFORMANCE BOOST)
                                masterRepository.runInTransaction(() -> processAllTransactionData(data));
                                PreferenceManager.INSTANCE.setLastTransactionSyncTime(response.body().getServerTime());
                                syncStatus.postValue(true);
                            } else {
                                syncStatus.postValue(false);
                            }
                        } catch (Exception e) {
                            syncStatus.postValue(false);
                            AppLogger.e(getClass(), "masterDownload", e);
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
            public void onFailure(@NonNull Call<DownloadTransactionsResponse> call,
                                  @NonNull Throwable t) {
                AppLogger.e(getClass(), "masterDownload", t);

                progressState.postValue(false);
                syncStatus.postValue(false);
            }
        });
    }

    private void processAllTransactionData(DownloadTransactionsDataResponse data) {

        // ---------------- FARM ----------------
        List<FarmInventoryOrdersResponse> farm = data.getFarmInventoryOrders();
        if (farm != null && !farm.isEmpty()) {

            if (!getFarmInventoryOrders(farm).isEmpty()) {
                masterRepository.deleteFarmInventoryOrders();
            }

            masterRepository.insertFarmInventoryOrders(getFarmInventoryOrders(farm));
        }

        // ---------------- RECEPTION ----------------
        List<ReceptionInventoryOrdersResponse> reception = data.getReceptionInventoryOrders();
        if (reception != null && !reception.isEmpty()) {

            if (!getReceptionInventoryOrders(reception).isEmpty()) {
                masterRepository.deleteReceptionInventoryOrders();
            }

            masterRepository.insertReceptionInventoryOrders(getReceptionInventoryOrders(reception));
        }

        // ---------------- DISPATCH ----------------
        List<DispatchContainersResponse> dispatch = data.getDispatchContainers();
        if (dispatch != null && !dispatch.isEmpty()) {

            if (!getDispatchContainers(dispatch).isEmpty()) {
                masterRepository.deleteDispatchContainers();
            }

            masterRepository.insertDispatchContainers(getDispatchContainers(dispatch));
        }

        // ---------------- RECEPTION DETAILS ----------------
        List<ReceptionDetailsResponse> receptionDetail = data.getReceptionDetails();
        if (receptionDetail != null && !receptionDetail.isEmpty()) {

            ReceptionMapper.ReceptionSyncResult receptionSyncResult = ReceptionMapper.getReceptionDetails(receptionDetail);
            syncRepository.upsertReceptionDetails(receptionSyncResult.receptionDetailsList());
            syncRepository.upsertReceptionData(receptionSyncResult.receptionDataList());

            // =====================================
            // CREATE SUMMARIES
            // =====================================
            Set<String> tempReceptionIds = new HashSet<>();
            for (ReceptionDetails details : receptionSyncResult.receptionDetailsList()) {
                tempReceptionIds.add(details.getTempReceptionId());
            }

            // =====================================
            // CALCULATE SUMMARY
            // =====================================
            List<ReceptionSummary> summaries = new ArrayList<>();
            for (String tempReceptionId : tempReceptionIds) {
                ReceptionSummary summary = receptionSummaryHelper.calculate(tempReceptionId);
                summaries.add(summary);
            }
            syncRepository.upsertReceptionSummary(summaries);
        }

        // ---------------- DISPATCH DETAILS ----------------
        List<DispatchDetailsResponse> dispatchDetail = data.getDispatchDetails();
        if (dispatchDetail != null && !dispatchDetail.isEmpty()) {

            DispatchMapper.DispatchSyncResult dispatchSyncResult = DispatchMapper.getDispatchDetails(dispatchDetail);
            syncRepository.upsertDispatchDetails(dispatchSyncResult.dispatchDetailsList());
            syncRepository.upsertContainerData(dispatchSyncResult.containerDataList());

            // =====================================
            // CREATE SUMMARIES
            // =====================================
            Set<String> tempDispatchIds = new HashSet<>();
            for (DispatchDetails details : dispatchSyncResult.dispatchDetailsList()) {
                tempDispatchIds.add(details.getTempDispatchId());
            }

            // =====================================
            // CALCULATE SUMMARY
            // =====================================
            List<DispatchSummary> summaries = new ArrayList<>();
            for (String tempDispatchId : tempDispatchIds) {
                DispatchSummary summary = dispatchSummaryHelper.calculate(tempDispatchId);
                summaries.add(summary);
            }
            syncRepository.upsertDispatchSummary(summaries);
        }
    }

    @NonNull
    private static List<ContainerCategories> getContainerCategories(List<ContainerCategoriesResponse> containerCategoriesResponseList) {
        List<ContainerCategories> containerCategoriesList = new ArrayList<>();
        for (ContainerCategoriesResponse containerCategoriesResponse : containerCategoriesResponseList) {
            ContainerCategories containerCategories = new ContainerCategories();
            containerCategories.setId(containerCategoriesResponse.getId());
            containerCategories.setProductTypeId(containerCategoriesResponse.getProductTypeId());
            containerCategories.setCategory(containerCategoriesResponse.getCategory());

            containerCategoriesList.add(containerCategories);
        }
        return containerCategoriesList;
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
        measurementSystemFormula.setSortOrder(measurementSystemFormulasResponse.getSortOrder());
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
            boolean result = syncRepository.hasUnsyncedData();
            if (result) {
                hasUnsyncedData.postValue(true);
            } else {
                hasUnsyncedData.postValue(false);
            }
        }).start();
    }

    public LiveData<Integer> getUnreadCount() {
        return syncRepository.getUnreadCount();
    }
}