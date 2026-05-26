package com.cgr.codrinterraerp.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "farm_summary",
        indices = {
                @Index(value = {"tempFarmId"}, unique = true)
        }
)
public class FarmSummary implements Serializable {

    @PrimaryKey
    @NonNull
    public String tempFarmId = "";
    public int totalPieces;
    public double totalGrossVolume;
    public double totalNetVolume;
    public double totalVolumePie;
    public long updatedAt;

    @NonNull
    public String getTempFarmId() {
        return tempFarmId;
    }

    public void setTempFarmId(@NonNull String tempFarmId) {
        this.tempFarmId = tempFarmId;
    }

    public int getTotalPieces() {
        return totalPieces;
    }

    public void setTotalPieces(int totalPieces) {
        this.totalPieces = totalPieces;
    }

    public double getTotalGrossVolume() {
        return totalGrossVolume;
    }

    public void setTotalGrossVolume(double totalGrossVolume) {
        this.totalGrossVolume = totalGrossVolume;
    }

    public double getTotalNetVolume() {
        return totalNetVolume;
    }

    public void setTotalNetVolume(double totalNetVolume) {
        this.totalNetVolume = totalNetVolume;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public double getTotalVolumePie() {
        return totalVolumePie;
    }

    public void setTotalVolumePie(double totalVolumePie) {
        this.totalVolumePie = totalVolumePie;
    }
}