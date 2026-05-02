package com.cgr.codrinterraerp.di;

import com.cgr.codrinterraerp.db.CGRTerraERPDatabase;
import com.cgr.codrinterraerp.db.dao.ApiLogsDao;
import com.cgr.codrinterraerp.db.dao.ContainerDataDao;
import com.cgr.codrinterraerp.db.dao.DispatchContainersDao;
import com.cgr.codrinterraerp.db.dao.DispatchDetailsDao;
import com.cgr.codrinterraerp.db.dao.DispatchSummaryDao;
import com.cgr.codrinterraerp.db.dao.DispatchViewDao;
import com.cgr.codrinterraerp.db.dao.FarmInventoryOrdersDao;
import com.cgr.codrinterraerp.db.dao.MeasurementSystemFormulaVariablesDao;
import com.cgr.codrinterraerp.db.dao.MeasurementSystemFormulasDao;
import com.cgr.codrinterraerp.db.dao.MeasurementSystemsDao;
import com.cgr.codrinterraerp.db.dao.OriginsDao;
import com.cgr.codrinterraerp.db.dao.ProductTypesDao;
import com.cgr.codrinterraerp.db.dao.ProductsDao;
import com.cgr.codrinterraerp.db.dao.PurchaseContractDao;
import com.cgr.codrinterraerp.db.dao.ReceptionDataDao;
import com.cgr.codrinterraerp.db.dao.ReceptionDetailsDao;
import com.cgr.codrinterraerp.db.dao.ReceptionInventoryOrdersDao;
import com.cgr.codrinterraerp.db.dao.ReceptionSummaryDao;
import com.cgr.codrinterraerp.db.dao.ReceptionTransactionDao;
import com.cgr.codrinterraerp.db.dao.ReceptionViewDao;
import com.cgr.codrinterraerp.db.dao.ShippingLinesDao;
import com.cgr.codrinterraerp.db.dao.SupplierProductTypesDao;
import com.cgr.codrinterraerp.db.dao.SupplierProductsDao;
import com.cgr.codrinterraerp.db.dao.SuppliersDao;
import com.cgr.codrinterraerp.db.dao.WarehousesDao;
import com.cgr.codrinterraerp.helper.DispatchSummaryHelper;
import com.cgr.codrinterraerp.helper.ReceptionSummaryHelper;
import com.cgr.codrinterraerp.repository.AppMaintenanceRepository;
import com.cgr.codrinterraerp.repository.AuthRepository;
import com.cgr.codrinterraerp.repository.DispatchDataRepository;
import com.cgr.codrinterraerp.repository.DispatchRepository;
import com.cgr.codrinterraerp.repository.MasterRepository;
import com.cgr.codrinterraerp.repository.ReceptionDataRepository;
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
                                             MeasurementSystemFormulasDao measurementSystemFormulasDao, MeasurementSystemFormulaVariablesDao measurementSystemFormulaVariablesDao,
                                             ShippingLinesDao shippingLinesDao, PurchaseContractDao purchaseContractDao, FarmInventoryOrdersDao farmInventoryOrdersDao,
                                             ReceptionInventoryOrdersDao receptionInventoryOrdersDao, DispatchContainersDao dispatchContainersDao, ProductsDao productsDao,
                                             ProductTypesDao productTypesDao) {
        return new MasterRepository(cgrTerraERPDatabase, iMasterApiService, originsDao, suppliersDao, supplierProductsDao, supplierProductTypesDao, warehousesDao,
                measurementSystemsDao, measurementSystemFormulasDao, measurementSystemFormulaVariablesDao, shippingLinesDao, purchaseContractDao, farmInventoryOrdersDao,
                receptionInventoryOrdersDao, dispatchContainersDao, productsDao, productTypesDao);
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
    ReceptionRepository provideReceptionRepository(ReceptionDetailsDao receptionDetailsDao, ReceptionInventoryOrdersDao receptionInventoryOrdersDao,
                                                   ReceptionViewDao receptionViewDao, FarmInventoryOrdersDao farmInventoryOrdersDao,
                                                   ReceptionSummaryDao receptionSummaryDao, ReceptionSummaryHelper receptionSummaryHelper,
                                                   ContainerDataDao containerDataDao, ReceptionDataDao receptionDataDao, DispatchSummaryHelper dispatchSummaryHelper,
                                                   DispatchSummaryDao dispatchSummaryDao) {
        return new ReceptionRepository(receptionDetailsDao, receptionInventoryOrdersDao, receptionViewDao, farmInventoryOrdersDao, receptionSummaryDao,
                receptionSummaryHelper, containerDataDao, receptionDataDao, dispatchSummaryHelper, dispatchSummaryDao);
    }

    @Provides
    @Singleton
    DispatchRepository provideDispatchRepository(DispatchDetailsDao dispatchDetailsDao, DispatchContainersDao dispatchContainersDao, DispatchViewDao dispatchViewDao,
                                                 DispatchSummaryDao dispatchSummaryDao, DispatchSummaryHelper dispatchSummaryHelper, ContainerDataDao containerDataDao,
                                                 ReceptionDataDao receptionDataDao, ReceptionSummaryHelper receptionSummaryHelper, ReceptionSummaryDao receptionSummaryDao) {
        return new DispatchRepository(dispatchDetailsDao, dispatchContainersDao, dispatchViewDao, dispatchSummaryDao, dispatchSummaryHelper, containerDataDao,
                receptionDataDao, receptionSummaryHelper, receptionSummaryDao);
    }

    @Provides
    @Singleton
    ReceptionDataRepository provideReceptionDataRepository(MeasurementSystemFormulasDao measurementSystemFormulasDao, ReceptionTransactionDao receptionTransactionDao,
                                                           ContainerDataDao containerDataDao, ReceptionDataDao receptionDataDao, ReceptionRepository receptionRepository,
                                                           DispatchRepository dispatchRepository) {
        return new ReceptionDataRepository(measurementSystemFormulasDao, receptionTransactionDao, receptionDataDao, containerDataDao, receptionRepository, dispatchRepository);
    }

    @Provides
    @Singleton
    DispatchDataRepository provideDispatchDataRepository(ContainerDataDao containerDataDao, ReceptionDataDao receptionDataDao, DispatchRepository dispatchRepository,
                                                         ReceptionRepository receptionRepository) {
        return new DispatchDataRepository(containerDataDao, receptionDataDao, dispatchRepository, receptionRepository);
    }
}