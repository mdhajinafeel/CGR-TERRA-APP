package com.cgr.codrinterraerp.db.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "reception_data",
        indices = {
                @Index(name = "idx_temp_id_reception_did_data", value = {"tempReceptionDataId"}),
                @Index(name = "idx_temp_id_reception_data", value = {"tempReceptionId"}),
                @Index(name = "idx_reception_id_data", value = {"receptionId"}),
                @Index(name = "idx_reception_data_id_data", value = {"receptionDataId"}),
                @Index(name = "idx_deleted_reception_data", value = {"isDeleted"}),
                @Index(name = "idx_container_mapping_id_data", value = {"containerReceptionMappingId"})
        })
public class ReceptionData implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String tempReceptionDataId;
    private String tempReceptionId;
    private Integer receptionDataId;
    private Integer receptionId;
    private double circumference;
    private double length;
    private int pieces;
    private double grossVolume;
    private double netVolume;
    private boolean isSynced = false;
    private boolean isDeleted = false;
    private boolean isEdited = false;
    private long updatedAt = System.currentTimeMillis();
    private String containerReceptionMappingId;
    @Ignore
    private String containerNumber;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTempReceptionId() {
        return tempReceptionId;
    }

    public void setTempReceptionId(String tempReceptionId) {
        this.tempReceptionId = tempReceptionId;
    }

    public Integer getReceptionId() {
        return receptionId;
    }

    public void setReceptionId(Integer receptionId) {
        this.receptionId = receptionId;
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

    public int getPieces() {
        return pieces;
    }

    public void setPieces(int pieces) {
        this.pieces = pieces;
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

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
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

    public Integer getReceptionDataId() {
        return receptionDataId;
    }

    public void setReceptionDataId(Integer receptionDataId) {
        this.receptionDataId = receptionDataId;
    }

    public String getTempReceptionDataId() {
        return tempReceptionDataId;
    }

    public void setTempReceptionDataId(String tempReceptionDataId) {
        this.tempReceptionDataId = tempReceptionDataId;
    }

    public String getContainerReceptionMappingId() {
        return containerReceptionMappingId;
    }

    public void setContainerReceptionMappingId(String containerReceptionMappingId) {
        this.containerReceptionMappingId = containerReceptionMappingId;
    }

    public String getContainerNumber() {
        return containerNumber;
    }

    public void setContainerNumber(String containerNumber) {
        this.containerNumber = containerNumber;
    }
}