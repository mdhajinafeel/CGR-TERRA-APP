package com.cgr.codrinterraerp.model.response.syncdata;

import java.io.Serializable;

public class ImageUploadResponse implements Serializable {

    public boolean status;
    public String message, url, tempContainerImageId, tempDispatchId, tempTransactionId;
}