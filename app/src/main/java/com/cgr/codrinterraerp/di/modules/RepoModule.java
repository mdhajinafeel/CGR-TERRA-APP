package com.cgr.codrinterraerp.di.modules;

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
import com.cgr.codrinterraerp.repository.AppMaintenanceRepository;
import com.cgr.codrinterraerp.repository.AuthRepository;
import com.cgr.codrinterraerp.repository.MasterRepository;
import com.cgr.codrinterraerp.repository.ReceptionRepository;
import com.cgr.codrinterraerp.services.IAuthApiService;
import com.cgr.codrinterraerp.services.IMasterApiService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module(includes = {ApiModule.class, DBModule.class})
@InstallIn(SingletonComponent.class)
public class RepoModule {
    @Provides
    @Singleton
    MasterRepository provideMasterRepository(CGRTerraERPDatabase cgrTerraERPDatabase, IMasterApiService iMasterApiService, OriginsDao originsDao, SuppliersDao suppliersDao, SupplierProductsDao supplierProductsDao,
                                             SupplierProductTypesDao supplierProductTypesDao, WarehousesDao warehousesDao, MeasurementSystemsDao measurementSystemsDao,
                                             ShippingLinesDao shippingLinesDao, PurchaseContractDao purchaseContractDao, FarmInventoryOrdersDao farmInventoryOrdersDao,
                                             ReceptionInventoryOrdersDao receptionInventoryOrdersDao, DispatchContainersDao dispatchContainersDao, ProductsDao productsDao,
                                             ProductTypesDao productTypesDao, GirthClassificationDao girthClassificationDao, LengthClassificationDao lengthClassificationDao) {
        return new MasterRepository(cgrTerraERPDatabase, iMasterApiService, originsDao, suppliersDao, supplierProductsDao, supplierProductTypesDao, warehousesDao, measurementSystemsDao,
                shippingLinesDao, purchaseContractDao, farmInventoryOrdersDao, receptionInventoryOrdersDao, dispatchContainersDao, productsDao, productTypesDao, girthClassificationDao, lengthClassificationDao);
    }

    @Provides
    @Singleton
    AuthRepository provideAuthRepository(IAuthApiService iAuthApiService) {
        return new AuthRepository(iAuthApiService);
    }

    @Provides
    @Singleton
    AppMaintenanceRepository provideAppMaintenanceRepository(ApiLogsDao apiLogsDao) {
        return new AppMaintenanceRepository(apiLogsDao);
    }

    @Provides
    @Singleton
    ReceptionRepository provideReceptionRepository(ReceptionDetailsDao receptionDetailsDao) {
        return new ReceptionRepository(receptionDetailsDao);
    }
}