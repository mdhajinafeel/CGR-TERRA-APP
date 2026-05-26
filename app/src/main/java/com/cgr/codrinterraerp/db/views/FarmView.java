package com.cgr.codrinterraerp.db.views;

import androidx.room.DatabaseView;

import java.io.Serializable;

@DatabaseView(
        viewName = "farm_view",
        value = "SELECT f.farmId, f.tempFarmId, f.ica, s.supplierName, f.purchaseDate, " +
                "IFNULL(fs.totalPieces,0) as totalPieces, IFNULL(fs.totalGrossVolume,0) as totalGrossVolume, IFNULL(fs.totalVolumePie,0) as totalVolumePie, " +
                "IFNULL(fs.totalNetVolume,0) as totalNetVolume, f.productTypeId, p.productName, pt.productTypeName, " +
                "pc.contractCode, pc.description, f.supplierId, f.isClosed, f.closedDate " +
                "FROM farm_details f " +
                "INNER JOIN suppliers s ON s.supplierId = f.supplierId " +
                "INNER JOIN products p ON p.productId = f.productId " +
                "INNER JOIN product_types pt ON pt.typeId = f.productTypeId " +
                "INNER JOIN purchase_contracts pc ON pc.contractId = f.purchaseContract " +
                "LEFT JOIN farm_summary fs ON fs.tempFarmId = f.tempFarmId " +
                "WHERE f.isDeleted = 0"
)
public class FarmView implements Serializable {

    public int farmId, totalPieces, productTypeId, supplierId;
    public String tempFarmId, ica, supplierName, purchaseDate, productName, productTypeName, contractCode, description;
    public double totalGrossVolume, totalNetVolume, totalVolumePie;
    public boolean isClosed;
    public long closedDate;
}
