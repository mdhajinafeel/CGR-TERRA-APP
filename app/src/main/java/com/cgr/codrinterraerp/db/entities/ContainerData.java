package com.cgr.codrinterraerp.db.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "container_data",
        indices = {
                @Index(name = "idx_dispatch_id_cd", value = {"dispatchId"}),
                @Index(name = "idx_temp_dispatch_id_cd", value = {"tempDispatchId"}),
                @Index(name = "idx_reception_data_id_cd", value = {"receptionDataId"}),
                @Index(name = "idx_reception_id_cd", value = {"receptionId"}),
                @Index(name = "idx_temp_reception_data_id_cd", value = {"tempReceptionDataId"}),
                @Index(name = "idx_temp_reception_id_cd", value = {"tempReceptionId"}),
                @Index(name = "idx_deleted_cd", value = {"isDeleted"}),
                @Index(name = "idx_container_mapping_id_cd", value = {"containerReceptionMappingId"})
        })
public class ContainerData implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String tempReceptionDataId;
    private String tempDispatchId;
    private Integer dispatchId;
    private String tempReceptionId;
    private Integer receptionDataId;
    private Integer receptionId;
    private int pieces;
    private double grossVolume;
    private double netVolume;
    private boolean isSynced = false;
    private boolean isDeleted = false;
    private boolean isEdited = false;
    private long updatedAt = System.currentTimeMillis();
    private String containerReceptionMappingId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTempDispatchId() {
        return tempDispatchId;
    }

    public void setTempDispatchId(String tempDispatchId) {
        this.tempDispatchId = tempDispatchId;
    }

    public Integer getDispatchId() {
        return dispatchId;
    }

    public void setDispatchId(Integer dispatchId) {
        this.dispatchId = dispatchId;
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
}