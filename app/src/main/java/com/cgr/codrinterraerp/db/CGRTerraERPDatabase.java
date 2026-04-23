package com.cgr.codrinterraerp.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.cgr.codrinterraerp.BuildConfig;
import com.cgr.codrinterraerp.constants.IAPIConstants;
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
import com.cgr.codrinterraerp.db.entities.ApiLogs;
import com.cgr.codrinterraerp.db.entities.DispatchContainers;
import com.cgr.codrinterraerp.db.entities.FarmInventoryOrders;
import com.cgr.codrinterraerp.db.entities.GirthClassification;
import com.cgr.codrinterraerp.db.entities.LengthClassification;
import com.cgr.codrinterraerp.db.entities.MeasurementSystems;
import com.cgr.codrinterraerp.db.entities.Origins;
import com.cgr.codrinterraerp.db.entities.ProductTypes;
import com.cgr.codrinterraerp.db.entities.Products;
import com.cgr.codrinterraerp.db.entities.PurchaseContracts;
import com.cgr.codrinterraerp.db.entities.ReceptionDetails;
import com.cgr.codrinterraerp.db.entities.ReceptionInventoryOrders;
import com.cgr.codrinterraerp.db.entities.ShippingLines;
import com.cgr.codrinterraerp.db.entities.SupplierProductTypes;
import com.cgr.codrinterraerp.db.entities.SupplierProducts;
import com.cgr.codrinterraerp.db.entities.Suppliers;
import com.cgr.codrinterraerp.db.entities.Warehouses;

@Database(entities = {Origins.class, ApiLogs.class, Suppliers.class, SupplierProducts.class, SupplierProductTypes.class, MeasurementSystems.class, PurchaseContracts.class,
        ShippingLines.class, Warehouses.class, FarmInventoryOrders.class, ReceptionInventoryOrders.class, DispatchContainers.class, Products.class, ProductTypes.class,
        GirthClassification.class, LengthClassification.class, ReceptionDetails.class}, version = 1)
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
    public abstract GirthClassificationDao girthClassificationDao();
    public abstract LengthClassificationDao lengthClassificationDao();
    public abstract ReceptionDetailsDao receptionDetailsDao();

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