package com.cgr.codrinterraerp.services;

import com.cgr.codrinterraerp.constants.IAPIConstants;
import com.cgr.codrinterraerp.model.request.SyncRequest;
import com.cgr.codrinterraerp.model.response.syncdata.ImageUploadResponse;
import com.cgr.codrinterraerp.model.response.syncdata.SyncResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ISyncApiService {

    @Multipart
    @POST(IAPIConstants.UPLOAD_CONTAINER_PHOTOS)
    Call<ImageUploadResponse> uploadContainerPhoto(@Part MultipartBody.Part image, @Part("tempContainerImageId") RequestBody tempContainerImageId,
                                          @Part("tempDispatchId") RequestBody tempDispatchId);

    @Multipart
    @POST(IAPIConstants.UPLOAD_EXPENSE_INVOICES)
    Call<ImageUploadResponse> uploadExpenseInvoices(@Part MultipartBody.Part image, @Part("tempTransactionId") RequestBody tempTransactionId);

    @POST(IAPIConstants.SYNC_DATA)
    Call<SyncResponse> syncData(@Body SyncRequest request);
}