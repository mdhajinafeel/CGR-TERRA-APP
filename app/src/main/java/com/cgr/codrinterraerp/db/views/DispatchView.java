package com.cgr.codrinterraerp.db.views;

import androidx.room.DatabaseView;

import java.io.Serializable;

@DatabaseView(
        viewName = "dispatch_view",
        value = "SELECT d.dispatchId, d.tempDispatchId, d.containerNumber, d.dispatchDate, s.shippingLine, " +
                "IFNULL(ds.totalPieces,0) as totalPieces, IFNULL(ds.totalGrossVolume,0) as totalGrossVolume, " +
                "IFNULL(ds.totalNetVolume,0) as totalNetVolume, IFNULL(ds.totalVolumePie,0) as totalVolumePie," +
                "IFNULL(ds.avgGirth,0) as avgGirth, IFNULL(ds.cft,0) as cft, d.isClosed, d.productTypeId, c.category, d.categoryId, d.closedDate, p.productName, " +
                "pt.productTypeName " +
                "FROM dispatch_details d " +
                "INNER JOIN shipping_lines s ON s.id = d.shippingLineId " +
                "INNER JOIN container_categories c ON c.id = d.categoryId " +
                "INNER JOIN products p ON p.productId = d.productId " +
                "INNER JOIN product_types pt ON pt.typeId = d.productTypeId " +
                "LEFT JOIN dispatch_summary ds ON ds.tempDispatchId = d.tempDispatchId " +
                "WHERE isDeleted = 0"
)
public class DispatchView implements Serializable {

    public int dispatchId, totalPieces, productTypeId, categoryId;
    public String tempDispatchId, containerNumber, dispatchDate, shippingLine, category, productName, productTypeName;
    public double totalGrossVolume, totalNetVolume, totalVolumePie, cft, avgGirth;
    public boolean isClosed;
    public long closedDate;
}
