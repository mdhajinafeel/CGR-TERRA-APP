package com.cgr.codrinterraerp.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cgr.codrinterraerp.db.entities.FarmInventoryOrders;

import java.util.List;

@Dao
public interface FarmInventoryOrdersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFarmInventoryOrders(List<FarmInventoryOrders> farmInventoryOrdersList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFarmInventoryOrder(FarmInventoryOrders farmInventoryOrder);

    @Query("SELECT COUNT(inventoryOrder) FROM farm_inventory_orders WHERE inventoryOrder = :inventoryOrder AND supplierId = :supplierId")
    int getFarmInventoryOrdersCount(String inventoryOrder, int supplierId);

    @Query("DELETE FROM farm_inventory_orders")
    void clearAll();

    @Query("DELETE FROM farm_inventory_orders WHERE inventoryOrder = :ica AND supplierId = :supplierId AND isFromReception = :isFromReception")
    void deleteFarmInventoryOrder(String ica, int supplierId, boolean isFromReception);
}