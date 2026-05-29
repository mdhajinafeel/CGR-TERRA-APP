package com.cgr.codrinterraerp.model.response;

import com.cgr.codrinterraerp.model.response.transactiondata.CreditTransactionsResponse;
import com.cgr.codrinterraerp.model.response.transactiondata.DebitTransactionsResponse;
import com.cgr.codrinterraerp.model.response.transactiondata.DispatchContainersResponse;
import com.cgr.codrinterraerp.model.response.transactiondata.DispatchDetailsResponse;
import com.cgr.codrinterraerp.model.response.transactiondata.FarmInventoryOrdersResponse;
import com.cgr.codrinterraerp.model.response.transactiondata.ReceptionDetailsResponse;
import com.cgr.codrinterraerp.model.response.transactiondata.ReceptionInventoryOrdersResponse;

import java.io.Serializable;
import java.util.List;

public class DownloadTransactionsDataResponse implements Serializable {

    private List<FarmInventoryOrdersResponse> farmInventoryOrders;
    private List<ReceptionInventoryOrdersResponse> receptionInventoryOrders;
    private List<DispatchContainersResponse> dispatchContainers;
    private List<ReceptionDetailsResponse> receptionDetails;
    private List<DispatchDetailsResponse> dispatchDetails;
    private List<CreditTransactionsResponse> creditTransactions;
    private List<DebitTransactionsResponse> debitTransactions;

    public List<FarmInventoryOrdersResponse> getFarmInventoryOrders() {
        return farmInventoryOrders;
    }

    public void setFarmInventoryOrders(List<FarmInventoryOrdersResponse> farmInventoryOrders) {
        this.farmInventoryOrders = farmInventoryOrders;
    }

    public List<ReceptionInventoryOrdersResponse> getReceptionInventoryOrders() {
        return receptionInventoryOrders;
    }

    public void setReceptionInventoryOrders(List<ReceptionInventoryOrdersResponse> receptionInventoryOrders) {
        this.receptionInventoryOrders = receptionInventoryOrders;
    }

    public List<DispatchContainersResponse> getDispatchContainers() {
        return dispatchContainers;
    }

    public void setDispatchContainers(List<DispatchContainersResponse> dispatchContainers) {
        this.dispatchContainers = dispatchContainers;
    }

    public List<ReceptionDetailsResponse> getReceptionDetails() {
        return receptionDetails;
    }

    public void setReceptionDetails(List<ReceptionDetailsResponse> receptionDetails) {
        this.receptionDetails = receptionDetails;
    }

    public List<DispatchDetailsResponse> getDispatchDetails() {
        return dispatchDetails;
    }

    public void setDispatchDetails(List<DispatchDetailsResponse> dispatchDetails) {
        this.dispatchDetails = dispatchDetails;
    }

    public List<CreditTransactionsResponse> getCreditTransactions() {
        return creditTransactions;
    }

    public void setCreditTransactions(List<CreditTransactionsResponse> creditTransactions) {
        this.creditTransactions = creditTransactions;
    }

    public List<DebitTransactionsResponse> getDebitTransactions() {
        return debitTransactions;
    }

    public void setDebitTransactions(List<DebitTransactionsResponse> debitTransactions) {
        this.debitTransactions = debitTransactions;
    }
}