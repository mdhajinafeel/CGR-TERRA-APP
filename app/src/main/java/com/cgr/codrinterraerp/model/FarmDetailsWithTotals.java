package com.cgr.codrinterraerp.model;

import java.io.Serializable;

public class FarmDetailsWithTotals implements Serializable {

    // =========================
    // FARM DETAILS
    // =========================
    public String tempFarmId;
    public Integer farmId;
    public String ica;
    public int supplierId;
    public int productId;
    public int productTypeId;
    public String purchaseDate;
    public int purchaseContract;
    public String truckNumber;
    public String truckDriverName;
    public boolean isSynced;
    public boolean isDeleted;
    public boolean isEdited;
    public long updatedAt;
    public boolean isClosed;
    public int closedBy;
    public String closedDate;
    public long createdAt;

    // =========================
    // SUMMARY TABLE
    // =========================
    public int totalPieces;
    public double totalGrossVolume;
    public double totalNetVolume;
    public double totalVolumePie;
}