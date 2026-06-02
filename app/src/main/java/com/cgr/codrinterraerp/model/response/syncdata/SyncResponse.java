package com.cgr.codrinterraerp.model.response.syncdata;

import java.io.Serializable;
import java.util.List;

public class SyncResponse implements Serializable {

    public boolean status;
    public String message;
    public List<ReceptionMappingsResponse> receptionMappings;
    public List<ReceptionDataMappingsResponse> receptionDataMappings;
    public List<DispatchMappingsResponse> dispatchMappings;
    public List<ContainerDataMappingsResponse> containerDataMappings;
    public List<FarmMappingsResponse> farmMappings;
    public List<FarmDataMappingsResponse> farmDataMappings;
    public List<ExpenseDataMappingsResponse> expenseDataMappings;
}