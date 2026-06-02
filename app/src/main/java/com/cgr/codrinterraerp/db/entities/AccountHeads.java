package com.cgr.codrinterraerp.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "account_heads",
        indices = {
                @Index(name = "idx_account_head_id", value = {"accountHeadId"}),
                @Index(name = "idx_account_head_name", value = {"accountHeadName"}),
                @Index(name = "idx_icon", value = {"icon"}),
                @Index(name = "idx_color_primary", value = {"colorCodePrimary"}),
                @Index(name = "idx_color_secondary", value = {"colorCodeSecondary"}),
                @Index(name = "idx_forestry", value = {"isForestry"}),
                @Index(name = "idx_forestry_type", value = {"forestryCostType"})
        })
public class AccountHeads implements Serializable {

    @PrimaryKey
    public int accountHeadId;
    public String accountHeadName;
    public String icon;
    public String colorCodePrimary;
    public String colorCodeSecondary;
    public boolean isForestry;
    public int forestryCostType;

    public int getAccountHeadId() {
        return accountHeadId;
    }

    public void setAccountHeadId(int accountHeadId) {
        this.accountHeadId = accountHeadId;
    }

    public String getAccountHeadName() {
        return accountHeadName;
    }

    public void setAccountHeadName(String accountHeadName) {
        this.accountHeadName = accountHeadName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColorCodePrimary() {
        return colorCodePrimary;
    }

    public void setColorCodePrimary(String colorCodePrimary) {
        this.colorCodePrimary = colorCodePrimary;
    }

    public String getColorCodeSecondary() {
        return colorCodeSecondary;
    }

    public void setColorCodeSecondary(String colorCodeSecondary) {
        this.colorCodeSecondary = colorCodeSecondary;
    }

    public boolean isForestry() {
        return isForestry;
    }

    public void setForestry(boolean forestry) {
        isForestry = forestry;
    }

    public int getForestryCostType() {
        return forestryCostType;
    }

    public void setForestryCostType(int forestryCostType) {
        this.forestryCostType = forestryCostType;
    }

    @NonNull
    @Override
    public String toString() {
        return accountHeadName;
    }
}