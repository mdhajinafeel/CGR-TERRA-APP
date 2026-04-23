package com.cgr.codrinterraerp.di.modules;

import android.content.Context;

import com.cgr.codrinterraerp.db.CGRTerraERPDatabase;
import com.cgr.codrinterraerp.db.dao.ApiLogsDao;
import com.cgr.codrinterraerp.db.dao.DispatchContainersDao;
import com.cgr.codrinterraerp.db.dao.FarmInventoryOrdersDao;
import com.cgr.codrinterraerp.db.dao.GirthClassificationDao;
import com.cgr.codrinterraerp.db.dao.LengthClassificationDao;
import com.cgr.codrinterraerp.db.dao.MeasurementSystemsDao;
import com.cgr.codrinterraerp.db.dao.OriginsDao;
import com.cgr.codrinterraerp.db.dao.ProductTypesDao;
import com.cgr.codrinterraerp.db.dao.ProductsDao;
import com.cgr.codrinterraerp.db.dao.PurchaseContractDao;
import com.cgr.codrinterraerp.db.dao.ReceptionDetailsDao;
import com.cgr.codrinterraerp.db.dao.ReceptionInventoryOrdersDao;
import com.cgr.codrinterraerp.db.dao.ShippingLinesDao;
import com.cgr.codrinterraerp.db.dao.SupplierProductTypesDao;
import com.cgr.codrinterraerp.db.dao.SupplierProductsDao;
import com.cgr.codrinterraerp.db.dao.SuppliersDao;
import com.cgr.codrinterraerp.db.dao.WarehousesDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DBModule {

    @Provides
    @Singleton
    public CGRTerraERPDatabase provideDatabase(@ApplicationContext Context context) {
        return CGRTerraERPDatabase.getInstance(context);
    }

    @Provides
    @Singleton
    public OriginsDao provideOriginsDao(CGRTerraERPDatabase db) {
        return db.originsDao();
    }

    @Provides
    @Singleton
    public ApiLogsDao provideApiLogsDao(CGRTerraERPDatabase db) {
        return db.apiLogsDao();
    }

    @Provides
    @Singleton
    public SuppliersDao provideSuppliersDao(CGRTerraERPDatabase db) {
        return db.suppliersDao();
    }

    @Provides
    @Singleton
    public SupplierProductsDao provideSupplierProductsDao(CGRTerraERPDatabase db) {
        return db.supplierProductsDao();
    }

    @Provides
    @Singleton
    public SupplierProductTypesDao provideSupplierProductTypesDao(CGRTerraERPDatabase db) {
        return db.supplierProductTypesDao();
    }

    @Provides
    @Singleton
    public WarehousesDao provideWarehousesDao(CGRTerraERPDatabase db) {
        return db.warehousesDao();
    }

    @Provides
    @Singleton
    public ShippingLinesDao provideShippingLinesDao(CGRTerraERPDatabase db) {
        return db.shippingLinesDao();
    }

    @Provides
    @Singleton
    public MeasurementSystemsDao provideMeasurementSystemsDao(CGRTerraERPDatabase db) {
        return db.measurementSystemsDao();
    }

    @Provides
    @Singleton
    public PurchaseContractDao providePurchaseContractDao(CGRTerraERPDatabase db) {
        return db.purchaseContractDao();
    }

    @Provides
    @Singleton
    public FarmInventoryOrdersDao provideFarmInventoryOrdersDao(CGRTerraERPDatabase db) {
        return db.farmInventoryOrdersDao();
    }

    @Provides
    @Singleton
    public ReceptionInventoryOrdersDao provideReceptionInventoryOrdersDao(CGRTerraERPDatabase db) {
        return db.receptionInventoryOrdersDao();
    }

    @Provides
    @Singleton
    public DispatchContainersDao provideDispatchContainersDao(CGRTerraERPDatabase db) {
        return db.dispatchContainersDao();
    }

    @Provides
    @Singleton
    public ProductsDao provideProductsDao(CGRTerraERPDatabase db) {
        return db.productsDao();
    }

    @Provides
    @Singleton
    public ProductTypesDao provideProductTypesDao(CGRTerraERPDatabase db) {
        return db.productTypesDao();
    }

    @Provides
    @Singleton
    public GirthClassificationDao provideGirthClassificationDao(CGRTerraERPDatabase db) {
        return db.girthClassificationDao();
    }

    @Provides
    @Singleton
    public LengthClassificationDao provideLengthClassificationDao(CGRTerraERPDatabase db) {
        return db.lengthClassificationDao();
    }

    @Provides
    @Singleton
    public ReceptionDetailsDao provideReceptionDetailsDao(CGRTerraERPDatabase db) {
        return db.receptionDetailsDao();
    }
}