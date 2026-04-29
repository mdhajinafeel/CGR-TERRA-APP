package com.cgr.codrinterraerp.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.cgr.codrinterraerp.BuildConfig;
import com.cgr.codrinterraerp.constants.IAPIConstants;
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
import com.cgr.codrinterraerp.db.entities.ApiLogs;
import com.cgr.codrinterraerp.db.entities.ContainerData;
import com.cgr.codrinterraerp.db.entities.DispatchContainers;
import com.cgr.codrinterraerp.db.entities.DispatchDetails;
import com.cgr.codrinterraerp.db.entities.DispatchSummary;
import com.cgr.codrinterraerp.db.entities.FarmInventoryOrders;
import com.cgr.codrinterraerp.db.entities.MeasurementSystemFormulaVariables;
import com.cgr.codrinterraerp.db.entities.MeasurementSystemFormulas;
import com.cgr.codrinterraerp.db.entities.MeasurementSystems;
import com.cgr.codrinterraerp.db.entities.Origins;
import com.cgr.codrinterraerp.db.entities.ProductTypes;
import com.cgr.codrinterraerp.db.entities.Products;
import com.cgr.codrinterraerp.db.entities.PurchaseContracts;
import com.cgr.codrinterraerp.db.entities.ReceptionData;
import com.cgr.codrinterraerp.db.entities.ReceptionDetails;
import com.cgr.codrinterraerp.db.entities.ReceptionInventoryOrders;
import com.cgr.codrinterraerp.db.entities.ReceptionSummary;
import com.cgr.codrinterraerp.db.entities.ShippingLines;
import com.cgr.codrinterraerp.db.entities.SupplierProductTypes;
import com.cgr.codrinterraerp.db.entities.SupplierProducts;
import com.cgr.codrinterraerp.db.entities.Suppliers;
import com.cgr.codrinterraerp.db.entities.Warehouses;
import com.cgr.codrinterraerp.db.views.DispatchView;
import com.cgr.codrinterraerp.db.views.ReceptionView;

@Database(entities = {Origins.class, ApiLogs.class, Suppliers.class, SupplierProducts.class, SupplierProductTypes.class, MeasurementSystems.class, PurchaseContracts.class,
        ShippingLines.class, Warehouses.class, FarmInventoryOrders.class, ReceptionInventoryOrders.class, DispatchContainers.class, Products.class, ProductTypes.class,
        ReceptionDetails.class, DispatchDetails.class, ReceptionData.class, ContainerData.class,
        DispatchSummary.class, ReceptionSummary.class, MeasurementSystemFormulas.class, MeasurementSystemFormulaVariables.class},
        views = {ReceptionView.class, DispatchView.class},
        version = 1)
public abstract class CGRTerraERPDatabase extends RoomDatabase {

    private static volatile CGRTerraERPDatabase INSTANCE;

    public abstract OriginsDao originsDao();

    public abstract ApiLogsDao apiLogsDao();

    public abstract SuppliersDao suppliersDao();

    public abstract SupplierProductsDao supplierProductsDao();

    public abstract SupplierProductTypesDao supplierProductTypesDao();

    public abstract PurchaseContractDao purchaseContractDao();

    public abstract WarehousesDao warehousesDao();

    public abstract ShippingLinesDao shippingLinesDao();

    public abstract MeasurementSystemsDao measurementSystemsDao();

    public abstract FarmInventoryOrdersDao farmInventoryOrdersDao();

    public abstract ReceptionInventoryOrdersDao receptionInventoryOrdersDao();

    public abstract DispatchContainersDao dispatchContainersDao();

    public abstract ProductsDao productsDao();

    public abstract ProductTypesDao productTypesDao();

    public abstract ReceptionDetailsDao receptionDetailsDao();

    public abstract DispatchDetailsDao dispatchDetailsDao();

    public abstract ReceptionDataDao receptionDataDao();

    public abstract ContainerDataDao containerDataDao();

    public abstract DispatchSummaryDao dispatchSummaryDao();

    public abstract ReceptionSummaryDao receptionSummaryDao();

    public abstract MeasurementSystemFormulasDao measurementSystemFormulasDao();

    public abstract MeasurementSystemFormulaVariablesDao measurementSystemFormulaVariablesDao();

    //VIEWS
    public abstract ReceptionViewDao receptionViewDao();

    public abstract DispatchViewDao dispatchViewDao();

    // TRANSACTIONS
    public abstract ReceptionTransactionDao receptionTransactionDao();

    public static CGRTerraERPDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CGRTerraERPDatabase.class) {
                if (INSTANCE == null) {
                    // ✅ Build Room database
                    RoomDatabase.Builder<CGRTerraERPDatabase> builder = Room.databaseBuilder(context.getApplicationContext(), CGRTerraERPDatabase.class,
                                    IAPIConstants.DBNAME)
                            .fallbackToDestructiveMigration(true);

                    // ✅ Allow main thread queries in DEBUG only
                    if (BuildConfig.DEBUG) {
                        builder.allowMainThreadQueries();
                    }

                    INSTANCE = builder.build();
                }
            }
        }
        return INSTANCE;
    }
}