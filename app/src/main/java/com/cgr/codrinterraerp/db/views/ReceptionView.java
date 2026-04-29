package com.cgr.codrinterraerp.db.views;

import androidx.room.DatabaseView;

import java.io.Serializable;

@DatabaseView(
        viewName = "reception_view",
        value = "SELECT r.id, r.tempReceptionId, r.receptionId, r.ica, " +
                "s.supplierName, m.measurementName, r.receptionDate, " +
                "IFNULL(ds.totalPieces,0) as totalPieces, IFNULL(ds.totalGrossVolume,0) as totalGrossVolume, " +
                "IFNULL(ds.totalNetVolume,0) as totalNetVolume, r.measurementSystem, r.productTypeId, p.productName, pt.productTypeName," +
                "r.isFarmEnabled, pc.contractCode, pc.description " +
                "FROM reception_details r " +
                "INNER JOIN suppliers s ON s.supplierId = r.supplierId " +
                "INNER JOIN measurement_systems m ON m.id = r.measurementSystem " +
                "INNER JOIN products p ON p.productId = r.productId " +
                "INNER JOIN product_types pt ON pt.typeId = r.productTypeId " +
                "INNER JOIN purchase_contracts pc ON pc.contractId = r.purchaseContract " +
                "LEFT JOIN reception_summary ds ON (ds.receptionId = r.receptionId OR ds.tempReceptionId = r.tempReceptionId) " +
                "WHERE r.isDeleted = 0"
)
public class ReceptionView implements Serializable {

    public int id, receptionId, totalPieces, measurementSystem, productTypeId;

    public String tempReceptionId, ica, supplierName, measurementName, receptionDate, productName, productTypeName, contractCode, description;

    public double totalGrossVolume, totalNetVolume;

    public boolean isFarmEnabled;
}
