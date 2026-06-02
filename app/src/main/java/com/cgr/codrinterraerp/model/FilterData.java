package com.cgr.codrinterraerp.model;

public class FilterData {

    public boolean isFilterApplied;
    public int transactionId;
    public int accountHeadId;
    public String startDate;
    public String endDate;

    public FilterData(
            boolean isFilterApplied,
            int transactionId,
            int accountHeadId,
            String startDate,
            String endDate) {

        this.isFilterApplied = isFilterApplied;
        this.transactionId = transactionId;
        this.accountHeadId = accountHeadId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}