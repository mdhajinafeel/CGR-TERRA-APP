package com.cgr.codrinterraerp.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "farm_data",
        indices = {
                @Index(name = "idx_temp_id_farm_did_data", value = {"tempFarmDataId"}, unique = true),
                @Index(name = "idx_temp_id_farm_data", value = {"tempFarmId"}),
                @Index(name = "idx_farm_id_data", value = {"farmId"}),
                @Index(name = "idx_farm_data_id_data", value = {"farmDataId"}),
                @Index(name = "idx_deleted_farm_data", value = {"isDeleted"}),
        })
public class FarmData implements Serializable {

    @PrimaryKey
    @NonNull
    private String tempFarmDataId = "";
    private String tempFarmId;
    private Integer farmDataId;
    private Integer farmId;
    private double circumference;
    private double length;
    private double thickness;
    private double width;
    private int pieces;
    private double grossVolume;
    private double netVolume;
    private double volumePie;
    private boolean isSynced = false;
    private boolean isDeleted = false;
    private boolean isEdited = false;
    private long createdAt;
    private long updatedAt = System.currentTimeMillis();

    @NonNull
    public String getTempFarmDataId() {
        return tempFarmDataId;
    }

    public void setTempFarmDataId(@NonNull String tempFarmDataId) {
        this.tempFarmDataId = tempFarmDataId;
    }

    public String getTempFarmId() {
        return tempFarmId;
    }

    public void setTempFarmId(String tempFarmId) {
        this.tempFarmId = tempFarmId;
    }

    public Integer getFarmDataId() {
        return farmDataId;
    }

    public void setFarmDataId(Integer farmDataId) {
        this.farmDataId = farmDataId;
    }

    public Integer getFarmId() {
        return farmId;
    }

    public void setFarmId(Integer farmId) {
        this.farmId = farmId;
    }

    public double getCircumference() {
        return circumference;
    }

    public void setCircumference(double circumference) {
        this.circumference = circumference;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getThickness() {
        return thickness;
    }

    public void setThickness(double thickness) {
        this.thickness = thickness;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public int getPieces() {
        return pieces;
    }

    public void setPieces(int pieces) {
        this.pieces = pieces;
    }

    public double getGrossVolume() {
        return grossVolume;
    }

    public void setGrossVolume(double grossVolume) {
        this.grossVolume = grossVolume;
    }

    public double getNetVolume() {
        return netVolume;
    }

    public void setNetVolume(double netVolume) {
        this.netVolume = netVolume;
    }

    public double getVolumePie() {
        return volumePie;
    }

    public void setVolumePie(double volumePie) {
        this.volumePie = volumePie;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}