package com.cgr.codrinterraerp.model.response.masterdata;

import java.io.Serializable;

public class BeneficiariesResponse implements Serializable {

    private String beneficiaryIdentification, beneficiaryName;

    public String getBeneficiaryIdentification() {
        return beneficiaryIdentification;
    }

    public void setBeneficiaryIdentification(String beneficiaryIdentification) {
        this.beneficiaryIdentification = beneficiaryIdentification;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }
}