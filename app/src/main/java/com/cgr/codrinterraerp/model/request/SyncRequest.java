package com.cgr.codrinterraerp.model.request;

import com.cgr.codrinterraerp.db.entities.ContainerData;
import com.cgr.codrinterraerp.db.entities.DispatchDetails;
import com.cgr.codrinterraerp.db.entities.FarmData;
import com.cgr.codrinterraerp.db.entities.ReceptionData;
import com.cgr.codrinterraerp.db.entities.ReceptionDetails;
import com.cgr.codrinterraerp.model.DispatchDetailsWithTotals;
import com.cgr.codrinterraerp.model.FarmDetailsWithTotals;
import com.cgr.codrinterraerp.model.ReceptionDetailsWithTotals;

import java.io.Serializable;
import java.util.List;

public class SyncRequest implements Serializable {

    public String deviceId;
    public List<ReceptionDetailsWithTotals> receptionDetails;
    public List<ReceptionData> receptionData;
    public List<DispatchDetailsWithTotals> dispatchDetails;
    public List<ContainerData> containerData;
    public List<FarmDetailsWithTotals> farmDetails;
    public List<FarmData> farmData;
}