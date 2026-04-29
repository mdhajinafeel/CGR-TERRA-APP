package com.cgr.codrinterraerp.model;

import java.io.Serializable;

public class ReceptionWithContainer implements Serializable {

    public double circumference, length, grossVolume, netVolume;
    public int pieces;
    private Integer receptionDataId;
    public String tempReceptionDataId, containerNumber;

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

    public int getPieces() {
        return pieces;
    }

    public void setPieces(int pieces) {
        this.pieces = pieces;
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

    public String getContainerNumber() {
        return containerNumber;
    }

    public void setContainerNumber(String containerNumber) {
        this.containerNumber = containerNumber;
    }
}