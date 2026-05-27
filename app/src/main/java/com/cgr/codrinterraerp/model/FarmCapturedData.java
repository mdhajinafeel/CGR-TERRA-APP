package com.cgr.codrinterraerp.model;

import java.io.Serializable;

public class FarmCapturedData implements Serializable {

    public double circumference, thickness, width, length, grossVolume, netVolume, volumePie;
    public int pieces;
    private Integer farmDataId;
    public String tempFarmDataId, tempFarmId;

    public double getCircumference() {
        return circumference;
    }

    public void setCircumference(double circumference) {
        this.circumference = circumference;
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

    public double getVolumePie() {
        return volumePie;
    }

    public void setVolumePie(double volumePie) {
        this.volumePie = volumePie;
    }

    public int getPieces() {
        return pieces;
    }

    public void setPieces(int pieces) {
        this.pieces = pieces;
    }

    public Integer getFarmDataId() {
        return farmDataId;
    }

    public void setFarmDataId(Integer farmDataId) {
        this.farmDataId = farmDataId;
    }

    public String getTempFarmDataId() {
        return tempFarmDataId;
    }

    public void setTempFarmDataId(String tempFarmDataId) {
        this.tempFarmDataId = tempFarmDataId;
    }

    public String getTempFarmId() {
        return tempFarmId;
    }

    public void setTempFarmId(String tempFarmId) {
        this.tempFarmId = tempFarmId;
    }
}