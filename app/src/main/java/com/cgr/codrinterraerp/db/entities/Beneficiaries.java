package com.cgr.codrinterraerp.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "beneficiaries",
        indices = {
                @Index(name = "idx_id_beneficiary", value = {"id"}),
                @Index(name = "idx_beneficiary_name", value = {"beneficiaryName"}),
                @Index(name = "idx_beneficiary_identification", value = {"beneficiaryIdentification"})
        })
public class Beneficiaries implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String beneficiaryName;
    public String beneficiaryIdentification;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(@NonNull String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    @NonNull
    public String getBeneficiaryIdentification() {
        return beneficiaryIdentification;
    }

    public void setBeneficiaryIdentification(@NonNull String beneficiaryIdentification) {
        this.beneficiaryIdentification = beneficiaryIdentification;
    }
}