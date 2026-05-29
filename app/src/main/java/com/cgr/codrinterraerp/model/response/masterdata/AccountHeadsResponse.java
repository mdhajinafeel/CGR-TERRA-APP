package com.cgr.codrinterraerp.model.response.masterdata;

import java.io.Serializable;

public class AccountHeadsResponse implements Serializable {

    private int accountHeadId, forestryCostType;
    private String accountHeadName, icon, colorCodePrimary, colorCodeSecondary;
    private boolean isForestry;

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
}