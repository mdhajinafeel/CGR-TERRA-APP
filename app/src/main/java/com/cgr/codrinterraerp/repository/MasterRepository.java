package com.cgr.codrinterraerp.repository;

import com.cgr.codrinterraerp.db.CGRTerraERPDatabase;
import com.cgr.codrinterraerp.db.dao.DispatchContainersDao;
import com.cgr.codrinterraerp.db.dao.FarmInventoryOrdersDao;
import com.cgr.codrinterraerp.db.dao.GirthClassificationDao;
import com.cgr.codrinterraerp.db.dao.LengthClassificationDao;
import com.cgr.codrinterraerp.db.dao.MeasurementSystemsDao;
import com.cgr.codrinterraerp.db.dao.OriginsDao;
import com.cgr.codrinterraerp.db.dao.ProductTypesDao;
import com.cgr.codrinterraerp.db.dao.ProductsDao;
import com.cgr.codrinterraerp.db.dao.PurchaseContractDao;
import com.cgr.codrinterraerp.db.dao.ReceptionInventoryOrdersDao;
import com.cgr.codrinterraerp.db.dao.ShippingLinesDao;
import com.cgr.codrinterraerp.db.dao.SupplierProductTypesDao;
import com.cgr.codrinterraerp.db.dao.SupplierProductsDao;
import com.cgr.codrinterraerp.db.dao.SuppliersDao;
import com.cgr.codrinterraerp.db.dao.WarehousesDao;
import com.cgr.codrinterraerp.db.entities.DispatchContainers;
import com.cgr.codrinterraerp.db.entities.FarmInventoryOrders;
import com.cgr.codrinterraerp.db.entities.GirthClassification;
import com.cgr.codrinterraerp.db.entities.LengthClassification;
import com.cgr.codrinterraerp.db.entities.MeasurementSystems;
import com.cgr.codrinterraerp.db.entities.Origins;
import com.cgr.codrinterraerp.db.entities.ProductTypes;
import com.cgr.codrinterraerp.db.entities.Products;
import com.cgr.codrinterraerp.db.entities.PurchaseContracts;
import com.cgr.codrinterraerp.db.entities.ReceptionInventoryOrders;
import com.cgr.codrinterraerp.db.entities.ShippingLines;
import com.cgr.codrinterraerp.db.entities.SupplierProductTypes;
import com.cgr.codrinterraerp.db.entities.SupplierProducts;
import com.cgr.codrinterraerp.db.entities.Suppliers;
import com.cgr.codrinterraerp.db.entities.Warehouses;
import com.cgr.codrinterraerp.model.response.DownloadMasterResponse;
import com.cgr.codrinterraerp.model.response.OriginsResponse;
import com.cgr.codrinterraerp.services.IMasterApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class MasterRepository {

    private final CGRTerraERPDatabase database;
    private final IMasterApiService iMasterApiService;
    private final OriginsDao originsDao;
    private final SuppliersDao suppliersDao;
    private final SupplierProductsDao supplierProductsDao;
    private final SupplierProductTypesDao supplierProductTypesDao;
    private final WarehousesDao warehousesDao;
    private final MeasurementSystemsDao measurementSystemsDao;
    private final ShippingLinesDao shippingLinesDao;
    private final PurchaseContractDao purchaseContractDao;
    private final FarmInventoryOrdersDao farmInventoryOrdersDao;
    private final ReceptionInventoryOrdersDao receptionInventoryOrdersDao;
    private final DispatchContainersDao dispatchContainersDao;
    private final ProductsDao productsDao;
    private final ProductTypesDao productTypesDao;
    private final GirthClassificationDao girthClassificationDao;
    private final LengthClassificationDao lengthClassificationDao;

    public MasterRepository(CGRTerraERPDatabase database, IMasterApiService iMasterApiService, OriginsDao originsDao, SuppliersDao suppliersDao, SupplierProductsDao supplierProductsDao,
                            SupplierProductTypesDao supplierProductTypesDao, WarehousesDao warehousesDao, MeasurementSystemsDao measurementSystemsDao,
                            ShippingLinesDao shippingLinesDao, PurchaseContractDao purchaseContractDao, FarmInventoryOrdersDao farmInventoryOrdersDao,
                            ReceptionInventoryOrdersDao receptionInventoryOrdersDao, DispatchContainersDao dispatchContainersDao, ProductsDao productsDao,
                            ProductTypesDao productTypesDao, GirthClassificationDao girthClassificationDao, LengthClassificationDao lengthClassificationDao) {
        this.database = database;
        this.iMasterApiService = iMasterApiService;
        this.originsDao = originsDao;
        this.suppliersDao = suppliersDao;
        this.supplierProductsDao = supplierProductsDao;
        this.supplierProductTypesDao = supplierProductTypesDao;
        this.warehousesDao = warehousesDao;
        this.measurementSystemsDao = measurementSystemsDao;
        this.shippingLinesDao = shippingLinesDao;
        this.purchaseContractDao = purchaseContractDao;
        this.farmInventoryOrdersDao = farmInventoryOrdersDao;
        this.receptionInventoryOrdersDao = receptionInventoryOrdersDao;
        this.dispatchContainersDao = dispatchContainersDao;
        this.productsDao = productsDao;
        this.productTypesDao = productTypesDao;
        this.girthClassificationDao = girthClassificationDao;
        this.lengthClassificationDao = lengthClassificationDao;
    }

    public Call<OriginsResponse> getOrigins() {
        return iMasterApiService.getOrigins();
    }

    public Call<DownloadMasterResponse> masterDownload() {
        return iMasterApiService.masterDownload();
    }

    public void runInTransaction(Runnable runnable) {
        database.runInTransaction(runnable);
    }

    // ORIGINS
    public void insertOrigins(List<Origins> originsList) {
        originsDao.clearAll();
        originsDao.insertOrigins(originsList);
    }

    public List<Origins> fetchOrigins() {
        return originsDao.getAllOrigins();
    }

    // SUPPLIERS
    public void deleteSupplierData() {
        suppliersDao.clearAll();
        supplierProductsDao.clearAll();
        supplierProductTypesDao.clearAll();
    }

    public void insertSuppliers(List<Suppliers> suppliersList) {
        suppliersDao.insertSuppliers(suppliersList);
    }

    public void insertSupplierProducts(List<SupplierProducts> supplierProductsList) {
        supplierProductsDao.insertSupplierProducts(supplierProductsList);
    }

    public void insertSupplierProductTypes(List<SupplierProductTypes> supplierProductTypesList) {
        supplierProductTypesDao.insertSupplierProductTypes(supplierProductTypesList);
    }

    public List<Suppliers> fetchSuppliers() {
        return suppliersDao.getAllSuppliers();
    }

    public List<SupplierProducts> fetchSupplierProducts(int supplierId) {
        return supplierProductsDao.getSupplierProducts(supplierId);
    }

    public List<SupplierProductTypes> fetchSupplierProductTypes(int supplierId, int productId) {
        return supplierProductTypesDao.getSupplierProductTypes(supplierId, productId);
    }

    // WAREHOUSES
    public void deleteWarehouseData() {
        warehousesDao.clearAll();
    }

    public void insertWarehouses(List<Warehouses> warehousesList) {
        warehousesDao.insertWarehouses(warehousesList);
    }

    public List<Warehouses> fetchWarehouses() {
        return warehousesDao.getAllWarehouses();
    }

    // MEASUREMENT SYSTEMS
    public void deleteMeasurementSystems() {
        measurementSystemsDao.clearAll();
    }

    public void insertMeasurementSystems(List<MeasurementSystems> measurementSystemsList) {
        measurementSystemsDao.insertMeasurementSystems(measurementSystemsList);
    }

    public List<MeasurementSystems> fetchMeasurementSystems(int productTypeId) {
        return measurementSystemsDao.getAllMeasurementSystems(productTypeId);
    }

    // SHIPPING LINES
    public void deleteShippingLines() {
        shippingLinesDao.clearAll();
    }

    public void insertShippingLines(List<ShippingLines> shippingLinesList) {
        shippingLinesDao.insertShippingLines(shippingLinesList);
    }

    public List<ShippingLines> fetchShippingLines() {
        return shippingLinesDao.getAllShippingLines();
    }

    // PURCHASE CONTRACTS
    public void deletePurchaseContract() {
        purchaseContractDao.clearAll();
    }

    public void insertPurchaseContracts(List<PurchaseContracts> purchaseContractsList) {
        purchaseContractDao.insertPurchaseContracts(purchaseContractsList);
    }

    public List<PurchaseContracts> fetchPurchaseContracts(int supplierId, int product, int productType) {

        List<Integer> productTypeList = new ArrayList<>();
        if(productType == 1 || productType == 3) {
            productTypeList.add(1);
            productTypeList.add(3);
        } else {
            productTypeList.add(2);
            productTypeList.add(4);
        }

        return purchaseContractDao.getPurchaseContracts(supplierId, product, productTypeList);
    }

    // FARM INVENTORY ORDERS
    public void deleteFarmInventoryOrders() {
        farmInventoryOrdersDao.clearAll();
    }

    public void insertFarmInventoryOrders(List<FarmInventoryOrders> farmInventoryOrdersDaoList) {
        farmInventoryOrdersDao.insertFarmInventoryOrders(farmInventoryOrdersDaoList);
    }

    // RECEPTION INVENTORY ORDERS
    public void deleteReceptionInventoryOrders() {
        receptionInventoryOrdersDao.clearAll();
    }

    public void insertReceptionInventoryOrders(List<ReceptionInventoryOrders> receptionInventoryOrdersList) {
        receptionInventoryOrdersDao.insertReceptionInventoryOrders(receptionInventoryOrdersList);
    }

    // DISPATCH CONTAINERS
    public void deleteDispatchContainers() {
        dispatchContainersDao.clearAll();
    }

    public void insertDispatchContainers(List<DispatchContainers> dispatchContainersList) {
        dispatchContainersDao.insertDispatchContainers(dispatchContainersList);
    }

    // PRODUCTS
    public void deleteProducts() {
        productsDao.clearAll();
    }

    public void insertProducts(List<Products> productsList) {
        productsDao.insertProducts(productsList);
    }

    public List<Products> fetchProducts() {
        return productsDao.getAllProducts();
    }

    // PRODUCT TYPES
    public void deleteProductTypes() {
        productTypesDao.clearAll();
    }

    public void insertProductTypes(List<ProductTypes> productTypesList) {
        productTypesDao.insertProductTypes(productTypesList);
    }

    public List<ProductTypes> fetchProductTypes() {
        return productTypesDao.getAllProductTypes();
    }

    // GIRTH CLASSIFICATION
    public void deleteGirthClassification() {
        girthClassificationDao.clearAll();
    }

    public void insertGirthClassification(List<GirthClassification> girthClassificationList) {
        girthClassificationDao.insertGirthClassification(girthClassificationList);
    }

    public List<GirthClassification> fetchGirthClassification() {
        return girthClassificationDao.getAllGirthClassification();
    }

    // LENGTH CLASSIFICATION
    public void deleteLengthClassification() {
        lengthClassificationDao.clearAll();
    }

    public void insertLengthClassification(List<LengthClassification> lengthClassificationList) {
        lengthClassificationDao.insertLengthClassification(lengthClassificationList);
    }

    public List<LengthClassification> fetchLengthClassification() {
        return lengthClassificationDao.getAllLengthClassification();
    }
}