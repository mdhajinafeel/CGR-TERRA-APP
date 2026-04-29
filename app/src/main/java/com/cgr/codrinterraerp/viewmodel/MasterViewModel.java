package com.cgr.codrinterraerp.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.cgr.codrinterraerp.db.entities.MeasurementSystems;
import com.cgr.codrinterraerp.db.entities.Origins;
import com.cgr.codrinterraerp.db.entities.ProductTypes;
import com.cgr.codrinterraerp.db.entities.Products;
import com.cgr.codrinterraerp.db.entities.PurchaseContracts;
import com.cgr.codrinterraerp.db.entities.ShippingLines;
import com.cgr.codrinterraerp.db.entities.SupplierProductTypes;
import com.cgr.codrinterraerp.db.entities.SupplierProducts;
import com.cgr.codrinterraerp.db.entities.Suppliers;
import com.cgr.codrinterraerp.db.entities.Warehouses;
import com.cgr.codrinterraerp.model.response.OriginDataResponse;
import com.cgr.codrinterraerp.model.response.OriginsResponse;
import com.cgr.codrinterraerp.repository.MasterRepository;
import com.cgr.codrinterraerp.wrapper.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class MasterViewModel extends ViewModel {

    private final MasterRepository masterRepository;
    private final SingleLiveEvent<Boolean> progressState = new SingleLiveEvent<>();
    private final SingleLiveEvent<Boolean> originStatus = new SingleLiveEvent<>();

    @Inject
    public MasterViewModel(MasterRepository masterRepository) {
        this.masterRepository = masterRepository;
    }

    public void getOrigins() {
        progressState.postValue(true);

        masterRepository.getOrigins().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<OriginsResponse> call, @NonNull Response<OriginsResponse> response) {
                progressState.postValue(false);
                if (response.isSuccessful() && response.body() != null) {

                    List<Origins> originsList = new ArrayList<>();
                    List<OriginDataResponse> originDataResponse = response.body().getData();
                    if (originDataResponse != null && !originDataResponse.isEmpty()) {
                        for (OriginDataResponse origin : originDataResponse) {

                            Origins origins = new Origins();
                            origins.setOriginId(origin.getOriginId());
                            origins.setOriginName(origin.getOriginName());

                            originsList.add(origins);
                        }
                    }

                    if (!originsList.isEmpty()) {
                        masterRepository.insertOrigins(originsList);
                        originStatus.postValue(true);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<OriginsResponse> call, @NonNull Throwable t) {
                progressState.postValue(false);
                originStatus.postValue(false);
            }
        });
    }

    public List<Origins> fetchOrigins() {
        return masterRepository.fetchOrigins();
    }

    public LiveData<Boolean> getProgressState() {
        return progressState;
    }

    public LiveData<Boolean> getOriginStatus() {
        return originStatus;
    }

    public List<Suppliers> fetchSuppliers() {
        return masterRepository.fetchSuppliers();
    }

    public List<SupplierProducts> fetchSupplierProducts(int supplierId) {
        return masterRepository.fetchSupplierProducts(supplierId);
    }

    public List<SupplierProductTypes> fetchSupplierProductTypes(int supplierId, int productId) {
        return masterRepository.fetchSupplierProductTypes(supplierId, productId);
    }

    public List<Warehouses> fetchWarehouses() {
        return masterRepository.fetchWarehouses();
    }

    public List<MeasurementSystems> fetchMeasurementSystems(int productTypeId) {
        return masterRepository.fetchMeasurementSystems(productTypeId);
    }

    public List<PurchaseContracts> fetchPurchaseContracts(int supplierId, int product, int productType) {
        return masterRepository.fetchPurchaseContracts(supplierId, product, productType);
    }

    public List<Products> fetchProducts() {
        return masterRepository.fetchProducts();
    }

    public List<ProductTypes> fetchProductTypes() {
        return masterRepository.fetchProductTypes();
    }

    public List<ShippingLines> fetchShippingLines() {
        return masterRepository.fetchShippingLines();
    }
}