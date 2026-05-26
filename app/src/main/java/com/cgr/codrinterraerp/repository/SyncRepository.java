package com.cgr.codrinterraerp.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.cgr.codrinterraerp.constants.SyncResult;
import com.cgr.codrinterraerp.db.CGRTerraERPDatabase;
import com.cgr.codrinterraerp.db.dao.ContainerDataDao;
import com.cgr.codrinterraerp.db.dao.DispatchDetailsDao;
import com.cgr.codrinterraerp.db.dao.DispatchSummaryDao;
import com.cgr.codrinterraerp.db.dao.FarmDetailsDao;
import com.cgr.codrinterraerp.db.dao.PushNotificationsDao;
import com.cgr.codrinterraerp.db.dao.ReceptionDataDao;
import com.cgr.codrinterraerp.db.dao.ReceptionDetailsDao;
import com.cgr.codrinterraerp.db.dao.ReceptionSummaryDao;
import com.cgr.codrinterraerp.db.dao.SyncDao;
import com.cgr.codrinterraerp.db.entities.ContainerData;
import com.cgr.codrinterraerp.db.entities.ContainerImages;
import com.cgr.codrinterraerp.db.entities.DispatchDetails;
import com.cgr.codrinterraerp.db.entities.DispatchSummary;
import com.cgr.codrinterraerp.db.entities.ReceptionData;
import com.cgr.codrinterraerp.db.entities.ReceptionDetails;
import com.cgr.codrinterraerp.db.entities.ReceptionSummary;
import com.cgr.codrinterraerp.model.request.SyncRequest;
import com.cgr.codrinterraerp.model.response.syncdata.ContainerDataMappingsResponse;
import com.cgr.codrinterraerp.model.response.syncdata.DispatchMappingsResponse;
import com.cgr.codrinterraerp.model.response.syncdata.ImageUploadResponse;
import com.cgr.codrinterraerp.model.response.syncdata.ReceptionDataMappingsResponse;
import com.cgr.codrinterraerp.model.response.syncdata.ReceptionMappingsResponse;
import com.cgr.codrinterraerp.model.response.syncdata.SyncResponse;
import com.cgr.codrinterraerp.services.ISyncApiService;
import com.cgr.codrinterraerp.services.SyncCallback;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class SyncRepository {

    private final SyncDao syncDao;
    private final ISyncApiService iSyncApiService;
    private final ReceptionDetailsDao receptionDetailsDao;
    private final DispatchDetailsDao dispatchDetailsDao;
    private final FarmDetailsDao farmDetailsDao;
    private final ReceptionDataDao receptionDataDao;
    private final ContainerDataDao containerDataDao;
    private final ReceptionSummaryDao receptionSummaryDao;
    private final DispatchSummaryDao dispatchSummaryDao;
    private final PushNotificationsDao pushNotificationsDao;

    public SyncRepository(Context context) {
        CGRTerraERPDatabase db = CGRTerraERPDatabase.getInstance(context);
        this.syncDao = db.syncDao();
        this.receptionDetailsDao = db.receptionDetailsDao();
        this.dispatchDetailsDao = db.dispatchDetailsDao();
        this.farmDetailsDao = db.farmDetailsDao();
        this.receptionDataDao = db.receptionDataDao();
        this.containerDataDao = db.containerDataDao();
        this.receptionSummaryDao = db.receptionSummaryDao();
        this.dispatchSummaryDao = db.dispatchSummaryDao();
        this.pushNotificationsDao = db.pushNotificationsDao();
        this.iSyncApiService = null;
    }

    public SyncRepository(SyncDao syncDao, ReceptionDetailsDao receptionDetailsDao, DispatchDetailsDao dispatchDetailsDao, FarmDetailsDao farmDetailsDao,
                          ReceptionDataDao receptionDataDao,  ContainerDataDao containerDataDao, ReceptionSummaryDao receptionSummaryDao,
                          DispatchSummaryDao dispatchSummaryDao, PushNotificationsDao pushNotificationsDao, ISyncApiService iSyncApiService) {
        this.syncDao = syncDao;
        this.receptionDetailsDao = receptionDetailsDao;
        this.dispatchDetailsDao = dispatchDetailsDao;
        this.farmDetailsDao = farmDetailsDao;
        this.receptionDataDao = receptionDataDao;
        this.containerDataDao = containerDataDao;
        this.receptionSummaryDao = receptionSummaryDao;
        this.dispatchSummaryDao = dispatchSummaryDao;
        this.pushNotificationsDao = pushNotificationsDao;
        this.iSyncApiService = iSyncApiService;
    }

    public List<ContainerImages> getUnsyncedImages() {
        return syncDao.getUnsyncedImages();
    }

    // =====================
    // CONTAINER IMAGE SYNC
    // =====================
    public void uploadContainerImage(SyncCallback callback) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                List<ContainerImages> images = getUnsyncedImages();

                if (images == null || images.isEmpty()) {
                    callback.onResult(SyncResult.NO_DATA);
                    return;
                }

                for (ContainerImages containerImage : images) {
                    // =================
                    // FILE CHECK
                    // =================
                    File file = new File(containerImage.imagePath);

                    if (!file.exists()) {
                        syncDao.markFailed(containerImage.tempContainerImageId);
                        continue;
                    }

                    // =================
                    // REQUEST BODY
                    // =================
                    RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));

                    MultipartBody.Part part = MultipartBody.Part.createFormData("image", file.getName(), reqFile);
                    RequestBody tempImageId = RequestBody.create(containerImage.tempContainerImageId, MultipartBody.FORM);
                    RequestBody tempDispatchId = RequestBody.create(containerImage.tempDispatchId, MultipartBody.FORM);

                    // =================
                    // API CALL
                    // =================
                    Response<ImageUploadResponse> response = iSyncApiService.uploadContainerPhoto(part, tempImageId, tempDispatchId).execute();

                    // =================
                    // SUCCESS
                    // =================
                    if (response.isSuccessful() && response.body() != null && response.body().status && response.body().url != null
                            && !response.body().url.isEmpty()) {

                        // ✅ UPDATE DB
                        syncDao.updateImageSync(response.body().tempContainerImageId, response.body().tempDispatchId, response.body().url);

                        AppLogger.d(getClass(), "File path: " + file.getAbsolutePath());
                        AppLogger.d(getClass(), "Exists before delete: " + file.exists());

                        // ✅ DELETE LOCAL FILE
                        boolean deleted = file.delete();
                        AppLogger.d(getClass(), "Deleted: " + deleted);
                        AppLogger.d(getClass(), "Exists after delete: " + file.exists());

                        if (deleted) {
                            syncDao.clearLocalFilePath(response.body().tempDispatchId, response.body().tempContainerImageId);
                        }
                    }
                }

                callback.onResult(SyncResult.SUCCESS);

            } catch (Exception e) {
                AppLogger.e(getClass(), "uploadContainerImage", e);
                callback.onResult(SyncResult.FAILED);
            } finally {
                // ✅ Properly shutdown executor
                executor.shutdown();
            }
        });
    }

    // =====================
    // MAIN SYNC
    // =====================
    public void syncData(Context context, SyncCallback callback) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {

                // =================
                // REQUEST
                // =================
                SyncRequest request = new SyncRequest();
                request.deviceId = CommonUtils.getDeviceId(context);
                request.receptionDetails = syncDao.getUnsyncedReceptionDetails();
                request.receptionData = syncDao.getUnsyncedReceptionData();
                request.dispatchDetails = syncDao.getUnsyncedDispatch();
                request.containerData = syncDao.getUnsyncedContainerData();

                // =================
                // API CALL
                // =================
                Response<SyncResponse> response = iSyncApiService.syncData(request).execute();
                if (response.isSuccessful() && response.body() != null && response.body().status) {
                    SyncResponse res = response.body();

                    // RECEPTION
                    for (ReceptionMappingsResponse receptionMappingsResponse : res.receptionMappings) {
                        syncDao.updateReceptionMapping(receptionMappingsResponse.tempReceptionId, receptionMappingsResponse.receptionId);
                    }

                    // RECEPTION DATA
                    for (ReceptionDataMappingsResponse receptionDataMappingsResponse : res.receptionDataMappings) {
                        syncDao.updateReceptionDataMapping(receptionDataMappingsResponse.tempReceptionDataId, receptionDataMappingsResponse.tempReceptionId,
                                receptionDataMappingsResponse.receptionDataId, receptionDataMappingsResponse.receptionDataId, receptionDataMappingsResponse.receptionDataId);
                    }

                    // DISPATCH
                    for (DispatchMappingsResponse dispatchMappingsResponse : res.dispatchMappings) {
                        syncDao.updateDispatchMapping(dispatchMappingsResponse.tempDispatchId, dispatchMappingsResponse.dispatchId);
                    }

                    // CONTAINER DATA
                    for (ContainerDataMappingsResponse containerDataMappingsResponse : res.containerDataMappings) {
                        syncDao.updateContainerDataMapping(containerDataMappingsResponse.tempReceptionDataId, containerDataMappingsResponse.tempDispatchId,
                                containerDataMappingsResponse.dispatchDataId, containerDataMappingsResponse.containerReceptionMappingId,
                                containerDataMappingsResponse.receptionDataId, containerDataMappingsResponse.tempReceptionId, containerDataMappingsResponse.receptionDataId,
                                containerDataMappingsResponse.dispatchId);
                    }
                }

                callback.onResult(SyncResult.SUCCESS);

            } catch (Exception e) {
                AppLogger.e(getClass(), "syncWarehouseData", e);
                callback.onResult(SyncResult.FAILED);
            } finally {
                // ✅ Properly shutdown executor
                executor.shutdown();
            }
        });
    }

    public boolean hasUnsyncedData() {
        int receptionDetailsCount = syncDao.getUnsyncedReceptionDetailsCount();
        int dispatchDetailsCount = syncDao.getUnsyncedDispatchDetailsCount();
        int receptionDataCount = syncDao.getUnsyncedReceptionDataCount();
        int containerDataCount = syncDao.getUnsyncedContainerDataCount();

        int totalUnsyncedData = receptionDetailsCount + dispatchDetailsCount + receptionDataCount + containerDataCount;

        return totalUnsyncedData > 0;
    }

    public void deleteOldReceptionDetails(long threeMonthsAgo) {
        receptionDetailsDao.deleteOldData(threeMonthsAgo);
    }

    public void deleteOldDispatchDetails(long threeMonthsAgo) {
        dispatchDetailsDao.deleteOldData(threeMonthsAgo);
    }

    public void deleteOldFarmDetails(long threeMonthsAgo) {
        farmDetailsDao.deleteOldData(threeMonthsAgo);
    }

    public void upsertReceptionDetails(List<ReceptionDetails> receptionDetailsList) {
        receptionDetailsDao.upsert(receptionDetailsList);
    }

    public void upsertReceptionData(List<ReceptionData> receptionDataList) {
        receptionDataDao.upsert(receptionDataList);
    }

    public void upsertDispatchDetails(List<DispatchDetails> dispatchDetailsList) {
        dispatchDetailsDao.upsert(dispatchDetailsList);
    }

    public void upsertContainerData(List<ContainerData> containerDataList) {
        containerDataDao.upsert(containerDataList);
    }

    public void upsertReceptionSummary(List<ReceptionSummary> receptionSummaries) {
        receptionSummaryDao.upsert(receptionSummaries);
    }

    public void upsertDispatchSummary(List<DispatchSummary> dispatchSummaries) {
        dispatchSummaryDao.upsert(dispatchSummaries);
    }

    public LiveData<Integer> getUnreadCount() {
        return pushNotificationsDao.getUnreadCount();
    }
}