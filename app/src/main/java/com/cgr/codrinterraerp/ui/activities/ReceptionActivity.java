package com.cgr.codrinterraerp.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.MeasurementSystems;
import com.cgr.codrinterraerp.db.entities.PurchaseContracts;
import com.cgr.codrinterraerp.db.entities.ReceptionDetails;
import com.cgr.codrinterraerp.db.entities.SupplierProductTypes;
import com.cgr.codrinterraerp.db.entities.SupplierProducts;
import com.cgr.codrinterraerp.db.entities.Suppliers;
import com.cgr.codrinterraerp.db.entities.Warehouses;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.utils.DividerItemDecoration;
import com.cgr.codrinterraerp.viewmodel.MasterViewModel;
import com.cgr.codrinterraerp.viewmodel.ReceptionViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReceptionActivity extends BaseActivity {

    private TextInputLayout tiSupplier, tiSupplierProduct, tiSupplierProductType, tiIca, tiMeasurementSystem,
            tiWarehouse, tiReceptionDate, tiPurchaseContract, tiTruckNumber, tiTruckDriverName;
    private AppCompatEditText etSupplier, etSupplierProduct, etSupplierProductType, etIca, etMeasurementSystem,
            etWarehouse, etReceptionDate, etPurchaseContract, etTruckNumber, etTruckDriverName;
    private AppCompatTextView tvNoDataFound;
    private MaterialCheckBox cbEnableFarm;
    private LinearLayout llFarm;
    private MaterialButton btnSubmit;
    private List<Suppliers> suppliersList;
    private List<SupplierProducts> supplierProductsList;
    private List<SupplierProductTypes> supplierProductTypesList;
    private List<Warehouses> warehousesList;
    private List<MeasurementSystems> measurementSystemsList;
    private List<PurchaseContracts> purchaseContractsList;
    private RecyclerViewAdapter<Suppliers> suppliersRecyclerViewAdapter;
    private RecyclerViewAdapter<SupplierProducts> supplierProductsRecyclerViewAdapter;
    private RecyclerViewAdapter<SupplierProductTypes> supplierProductTypesRecyclerViewAdapter;
    private RecyclerViewAdapter<Warehouses> warehousesRecyclerViewAdapter;
    private RecyclerViewAdapter<MeasurementSystems> measurementSystemsRecyclerViewAdapter;
    private RecyclerViewAdapter<PurchaseContracts> purchaseContractsRecyclerViewAdapter;
    private MasterViewModel masterViewModel;
    private ReceptionViewModel receptionViewModel;
    private FrameLayout progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reception);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            AppCompatImageView imgBack = findViewById(R.id.imgBack);
            AppCompatTextView txtTitle = findViewById(R.id.txtTitle);

            tiSupplier = findViewById(R.id.tiSupplier);
            tiSupplierProduct = findViewById(R.id.tiSupplierProduct);
            tiSupplierProductType = findViewById(R.id.tiSupplierProductType);
            tiIca = findViewById(R.id.tiIca);
            tiMeasurementSystem = findViewById(R.id.tiMeasurementSystem);
            tiWarehouse = findViewById(R.id.tiWarehouse);
            tiReceptionDate = findViewById(R.id.tiReceptionDate);
            tiPurchaseContract = findViewById(R.id.tiPurchaseContract);
            tiTruckNumber = findViewById(R.id.tiTruckNumber);
            tiTruckDriverName = findViewById(R.id.tiTruckDriverName);
            etSupplier = findViewById(R.id.etSupplier);
            etSupplierProduct = findViewById(R.id.etSupplierProduct);
            etSupplierProductType = findViewById(R.id.etSupplierProductType);
            etIca = findViewById(R.id.etIca);
            etMeasurementSystem = findViewById(R.id.etMeasurementSystem);
            etWarehouse = findViewById(R.id.etWarehouse);
            etReceptionDate = findViewById(R.id.etReceptionDate);
            etPurchaseContract = findViewById(R.id.etPurchaseContract);
            etTruckNumber = findViewById(R.id.etTruckNumber);
            etTruckDriverName = findViewById(R.id.etTruckDriverName);
            cbEnableFarm = findViewById(R.id.cbEnableFarm);
            llFarm = findViewById(R.id.llFarm);
            btnSubmit = findViewById(R.id.btnSubmit);
            progressBar = findViewById(R.id.progressBar);

            masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);
            receptionViewModel = new ViewModelProvider(this).get(ReceptionViewModel.class);

            txtTitle.setText(getString(R.string.add_reception));
            imgBack.setOnClickListener(v -> finish());

            CommonUtils.clearErrorOnTyping(etSupplier, tiSupplier);
            CommonUtils.clearErrorOnTyping(etSupplierProduct, tiSupplierProduct);
            CommonUtils.clearErrorOnTyping(etSupplierProductType, tiSupplierProductType);
            CommonUtils.clearErrorOnTyping(etIca, tiIca);
            CommonUtils.clearErrorOnTyping(etMeasurementSystem, tiMeasurementSystem);
            CommonUtils.clearErrorOnTyping(etWarehouse, tiWarehouse);
            CommonUtils.clearErrorOnTyping(etReceptionDate, tiReceptionDate);
            CommonUtils.clearErrorOnTyping(etPurchaseContract, tiPurchaseContract);
            CommonUtils.clearErrorOnTyping(etTruckNumber, tiTruckNumber);
            CommonUtils.clearErrorOnTyping(etTruckDriverName, tiTruckDriverName);

            fetchData();
            actionListeners();

            receptionViewModel.getProgressState().observe(this, aBoolean -> {
                if(aBoolean) {
                    showProgress(progressBar);
                } else {
                    hideProgress(progressBar);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void fetchData() {
        try {
            suppliersList = new ArrayList<>();
            suppliersList = masterViewModel.fetchSuppliers();

            supplierProductsList = new ArrayList<>();
            supplierProductTypesList = new ArrayList<>();
            measurementSystemsList = new ArrayList<>();
            purchaseContractsList = new ArrayList<>();

            warehousesList = new ArrayList<>();
            warehousesList = masterViewModel.fetchWarehouses();
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchData", e);
        }
    }

    private void actionListeners() {
        try {
            etSupplier.setKeyListener(null);
            etSupplierProduct.setKeyListener(null);
            etSupplierProductType.setKeyListener(null);
            etWarehouse.setKeyListener(null);
            etMeasurementSystem.setKeyListener(null);

            etSupplier.setOnClickListener(v -> showDataDialog("Supplier"));
            etSupplierProduct.setOnClickListener(v -> showDataDialog("SupplierProduct"));
            etSupplierProductType.setOnClickListener(v -> showDataDialog("SupplierProductType"));
            etWarehouse.setOnClickListener(v -> showDataDialog("Warehouse"));
            etMeasurementSystem.setOnClickListener(v -> showDataDialog("MeasurementSystem"));
            etPurchaseContract.setOnClickListener(v -> showDataDialog("PurchaseContract"));

            tiSupplier.setEndIconOnClickListener(v -> showDataDialog("Supplier"));
            tiSupplierProduct.setEndIconOnClickListener(v -> showDataDialog("SupplierProduct"));
            tiSupplierProductType.setEndIconOnClickListener(v -> showDataDialog("SupplierProductType"));
            tiWarehouse.setEndIconOnClickListener(v -> showDataDialog("Warehouse"));
            tiMeasurementSystem.setOnClickListener(v -> showDataDialog("MeasurementSystem"));
            tiPurchaseContract.setEndIconOnClickListener(v -> showDataDialog("PurchaseContract"));

            etReceptionDate.setOnClickListener(v -> CommonUtils.showDatePicker(this, etReceptionDate));

            cbEnableFarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    llFarm.setVisibility(View.VISIBLE);
                } else {
                    llFarm.setVisibility(View.GONE);
                }

                etPurchaseContract.setText("");
                etPurchaseContract.setTag("");
                etTruckNumber.setText("");
                etTruckDriverName.setText("");
            });

            btnSubmit.setOnClickListener(v -> saveOrUpdateReceptionDetails());

        } catch (Exception e) {
            AppLogger.e(getClass(), "clickListeners", e);
        }
    }

    private void showDataDialog(String tag) {
        try {

            Dialog dialog = new Dialog(this, R.style.DialogTheme);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            dialog.getWindow().setDimAmount(0.6f);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.CENTER;
            dialog.getWindow().setAttributes(layoutParams);
            dialog.setContentView(R.layout.list_dialog);

            AppCompatTextView dialogTitle = dialog.findViewById(R.id.tvDialogTitle);
            tvNoDataFound = dialog.findViewById(R.id.tvNoDataFound);
            AppCompatImageView closeDialog = dialog.findViewById(R.id.imgClose);
            AppCompatEditText etSearch = dialog.findViewById(R.id.etSearch);
            RecyclerView rvList = dialog.findViewById(R.id.rvList);
            closeDialog.setOnClickListener(v -> dialog.dismiss());
            tvNoDataFound.setVisibility(View.GONE);

            rvList.setLayoutManager(new LinearLayoutManager(this));
            rvList.addItemDecoration(new DividerItemDecoration(this));

            if (tag.equalsIgnoreCase("Supplier")) {
                dialogTitle.setText(R.string.select_supplier);

                if (suppliersList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);

                    suppliersRecyclerViewAdapter = new RecyclerViewAdapter<>(this, suppliersList, R.layout.row_dialog_list) {
                        @Override
                        public void onPostBindViewHolder(ViewHolder holder, Suppliers suppliers) {

                            AppCompatTextView tvName = holder.itemView.findViewById(R.id.tvName);
                            AppCompatImageView ivSelected = holder.itemView.findViewById(R.id.ivItemSelected);

                            tvName.setText(suppliers.getSupplierName());

                            boolean isSelected = false;
                            if (etSupplier.getTag() != null) {
                                isSelected = Objects.equals(suppliers.getSupplierId(), etSupplier.getTag());
                            }

                            ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

                            if (isSelected) {
                                holder.setViewTypeface(R.id.tvName,
                                        ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_bold));
                            } else {
                                holder.setViewTypeface(R.id.tvName,
                                        ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_medium));
                            }
                        }
                    };

                    rvList.setAdapter(suppliersRecyclerViewAdapter);
                    suppliersRecyclerViewAdapter.setOnItemClickListener((view, position) -> {

                        Suppliers selected = suppliersRecyclerViewAdapter.getItem(position);

                        etSupplier.setText(selected.getSupplierName());
                        etSupplier.setTag(selected.getSupplierId());

                        supplierProductsList = new ArrayList<>();
                        supplierProductTypesList = new ArrayList<>();
                        measurementSystemsList = new ArrayList<>();
                        purchaseContractsList = new ArrayList<>();

                        etSupplierProduct.setText("");
                        etSupplierProductType.setText("");
                        etSupplierProduct.setTag("");
                        etSupplierProductType.setTag("");
                        etMeasurementSystem.setText("");
                        etMeasurementSystem.setTag("");
                        etPurchaseContract.setText("");
                        etPurchaseContract.setTag("");

                        supplierProductsList = masterViewModel.fetchSupplierProducts(selected.getSupplierId());

                        dialog.dismiss(); // optional
                    });
                }
            } else if (tag.equalsIgnoreCase("Warehouse")) {

                dialogTitle.setText(R.string.select_warehouse);

                if (warehousesList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);
                }

                warehousesRecyclerViewAdapter = new RecyclerViewAdapter<>(this, warehousesList, R.layout.row_dialog_list) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, Warehouses warehouses) {

                        AppCompatTextView tvName = holder.itemView.findViewById(R.id.tvName);
                        AppCompatImageView ivSelected = holder.itemView.findViewById(R.id.ivItemSelected);

                        tvName.setText(warehouses.getWarehouseName());

                        boolean isSelected = false;
                        if (etWarehouse.getTag() != null) {
                            isSelected = Objects.equals(warehouses.getId(), etWarehouse.getTag());
                        }

                        ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

                        if (isSelected) {
                            holder.setViewTypeface(R.id.tvName,
                                    ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_bold));
                        } else {
                            holder.setViewTypeface(R.id.tvName,
                                    ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_medium));
                        }
                    }
                };

                rvList.setAdapter(warehousesRecyclerViewAdapter);
                warehousesRecyclerViewAdapter.setOnItemClickListener((view, position) -> {

                    Warehouses selected = warehousesRecyclerViewAdapter.getItem(position);

                    etWarehouse.setText(selected.getWarehouseName());
                    etWarehouse.setTag(selected.getId());

                    dialog.dismiss(); // optional
                });
            } else if (tag.equalsIgnoreCase("SupplierProduct")) {

                dialogTitle.setText(R.string.select_wood);

                if (supplierProductsList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);
                }

                supplierProductsRecyclerViewAdapter = new RecyclerViewAdapter<>(this, supplierProductsList, R.layout.row_dialog_list) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, SupplierProducts supplierProducts) {

                        AppCompatTextView tvName = holder.itemView.findViewById(R.id.tvName);
                        AppCompatImageView ivSelected = holder.itemView.findViewById(R.id.ivItemSelected);

                        tvName.setText(supplierProducts.getProductName());

                        boolean isSelected = false;
                        if (etSupplierProduct.getTag() != null) {
                            isSelected = Objects.equals(supplierProducts.getSupplierProductId(), etSupplierProduct.getTag());
                        }

                        ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

                        if (isSelected) {
                            holder.setViewTypeface(R.id.tvName,
                                    ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_bold));
                        } else {
                            holder.setViewTypeface(R.id.tvName,
                                    ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_medium));
                        }
                    }
                };

                rvList.setAdapter(supplierProductsRecyclerViewAdapter);
                supplierProductsRecyclerViewAdapter.setOnItemClickListener((view, position) -> {

                    SupplierProducts selected = supplierProductsRecyclerViewAdapter.getItem(position);

                    etSupplierProduct.setText(selected.getProductName());
                    etSupplierProduct.setTag(selected.getSupplierProductId());

                    etSupplierProductType.setText("");
                    etSupplierProductType.setTag("");
                    etMeasurementSystem.setText("");
                    etMeasurementSystem.setTag("");
                    etPurchaseContract.setText("");
                    etPurchaseContract.setTag("");

                    supplierProductTypesList = new ArrayList<>();
                    measurementSystemsList = new ArrayList<>();
                    purchaseContractsList = new ArrayList<>();

                    supplierProductTypesList = masterViewModel.fetchSupplierProductTypes(selected.getSupplierId(), selected.getProductId());

                    dialog.dismiss(); // optional
                });
            } else if (tag.equalsIgnoreCase("SupplierProductType")) {

                dialogTitle.setText(R.string.select_wood_type);

                if (supplierProductTypesList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);
                }

                supplierProductTypesRecyclerViewAdapter = new RecyclerViewAdapter<>(this, supplierProductTypesList, R.layout.row_dialog_list) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, SupplierProductTypes supplierProductTypes) {

                        AppCompatTextView tvName = holder.itemView.findViewById(R.id.tvName);
                        AppCompatImageView ivSelected = holder.itemView.findViewById(R.id.ivItemSelected);

                        tvName.setText(supplierProductTypes.getProductTypeName());

                        boolean isSelected = false;
                        if (etSupplierProductType.getTag() != null) {
                            isSelected = Objects.equals(supplierProductTypes.getProductTypeId(), etSupplierProductType.getTag());
                        }

                        ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

                        if (isSelected) {
                            holder.setViewTypeface(R.id.tvName,
                                    ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_bold));
                        } else {
                            holder.setViewTypeface(R.id.tvName,
                                    ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_medium));
                        }
                    }
                };

                rvList.setAdapter(supplierProductTypesRecyclerViewAdapter);
                supplierProductTypesRecyclerViewAdapter.setOnItemClickListener((view, position) -> {

                    SupplierProductTypes selected = supplierProductTypesRecyclerViewAdapter.getItem(position);

                    etSupplierProductType.setText(selected.getProductTypeName());
                    etSupplierProductType.setTag(selected.getProductTypeId());

                    etMeasurementSystem.setText("");
                    etMeasurementSystem.setTag("");
                    etPurchaseContract.setText("");
                    etPurchaseContract.setTag("");

                    measurementSystemsList = new ArrayList<>();
                    purchaseContractsList = new ArrayList<>();

                    measurementSystemsList = masterViewModel.fetchMeasurementSystems(selected.getProductTypeId());
                    purchaseContractsList = masterViewModel.fetchPurchaseContracts(selected.getSupplierId(), selected.getProductId(), selected.getProductTypeId());

                    dialog.dismiss(); // optional
                });
            } else if (tag.equalsIgnoreCase("MeasurementSystem")) {

                dialogTitle.setText(R.string.select_measurement_system);

                if (measurementSystemsList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);

                    measurementSystemsRecyclerViewAdapter = new RecyclerViewAdapter<>(this, measurementSystemsList, R.layout.row_dialog_list) {
                        @Override
                        public void onPostBindViewHolder(ViewHolder holder, MeasurementSystems measurementSystems) {

                            AppCompatTextView tvName = holder.itemView.findViewById(R.id.tvName);
                            AppCompatImageView ivSelected = holder.itemView.findViewById(R.id.ivItemSelected);

                            tvName.setText(measurementSystems.getMeasurementName());

                            boolean isSelected = false;
                            if (etMeasurementSystem.getTag() != null) {
                                isSelected = Objects.equals(measurementSystems.getId(), etMeasurementSystem.getTag());
                            }

                            ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

                            if (isSelected) {
                                holder.setViewTypeface(R.id.tvName,
                                        ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_bold));
                            } else {
                                holder.setViewTypeface(R.id.tvName,
                                        ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_medium));
                            }
                        }
                    };

                    rvList.setAdapter(measurementSystemsRecyclerViewAdapter);
                    measurementSystemsRecyclerViewAdapter.setOnItemClickListener((view, position) -> {

                        MeasurementSystems selected = measurementSystemsRecyclerViewAdapter.getItem(position);

                        etMeasurementSystem.setText(selected.getMeasurementName());
                        etMeasurementSystem.setTag(selected.getId());

                        dialog.dismiss(); // optional
                    });
                }
            } else if (tag.equalsIgnoreCase("PurchaseContract")) {

                dialogTitle.setText(R.string.select_purchase_contract);

                if (purchaseContractsList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);

                    purchaseContractsRecyclerViewAdapter = new RecyclerViewAdapter<>(this, purchaseContractsList, R.layout.row_dialog_list) {
                        @Override
                        public void onPostBindViewHolder(ViewHolder holder, PurchaseContracts purchaseContracts) {

                            AppCompatTextView tvName = holder.itemView.findViewById(R.id.tvName);
                            AppCompatImageView ivSelected = holder.itemView.findViewById(R.id.ivItemSelected);

                            if (purchaseContracts.getDescription() != null && !Objects.equals(purchaseContracts.getDescription(), "")) {
                                tvName.setText(String.format("%s - %s", purchaseContracts.getContractCode(), purchaseContracts.getDescription()));
                            } else {
                                tvName.setText(purchaseContracts.getContractCode());
                            }

                            boolean isSelected = false;
                            if (etPurchaseContract.getTag() != null) {
                                isSelected = Objects.equals(purchaseContracts.getContractId(), etPurchaseContract.getTag());
                            }

                            ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

                            if (isSelected) {
                                holder.setViewTypeface(R.id.tvName,
                                        ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_bold));
                            } else {
                                holder.setViewTypeface(R.id.tvName,
                                        ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_medium));
                            }
                        }
                    };

                    rvList.setAdapter(purchaseContractsRecyclerViewAdapter);
                    purchaseContractsRecyclerViewAdapter.setOnItemClickListener((view, position) -> {

                        PurchaseContracts selected = purchaseContractsRecyclerViewAdapter.getItem(position);

                        etPurchaseContract.setText(selected.getContractCode());
                        etPurchaseContract.setTag(selected.getContractId());

                        dialog.dismiss(); // optional
                    });
                }
            }

            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    String query = s.toString().trim().toLowerCase();

                    if (tag.equalsIgnoreCase("Supplier")) {
                        if (query.isEmpty()) {
                            suppliersRecyclerViewAdapter.resetFilter();
                        } else {
                            suppliersRecyclerViewAdapter.filter(item ->
                                    item.getSupplierName() != null &&
                                            item.getSupplierName().toLowerCase().contains(query)
                            );
                        }

                        // Optional: Show "No Data Found"
                        if (suppliersRecyclerViewAdapter.getItemCount() == 0) {
                            tvNoDataFound.setVisibility(View.VISIBLE);
                        } else {
                            tvNoDataFound.setVisibility(View.GONE);
                        }
                    } else if (tag.equalsIgnoreCase("Warehouse")) {
                        if (query.isEmpty()) {
                            warehousesRecyclerViewAdapter.resetFilter();
                        } else {
                            warehousesRecyclerViewAdapter.filter(item ->
                                    item.getWarehouseName() != null &&
                                            item.getWarehouseName().toLowerCase().contains(query)
                            );
                        }

                        // Optional: Show "No Data Found"
                        if (warehousesRecyclerViewAdapter.getItemCount() == 0) {
                            tvNoDataFound.setVisibility(View.VISIBLE);
                        } else {
                            tvNoDataFound.setVisibility(View.GONE);
                        }
                    } else if (tag.equalsIgnoreCase("SupplierProduct")) {
                        if (query.isEmpty()) {
                            supplierProductsRecyclerViewAdapter.resetFilter();
                        } else {
                            supplierProductsRecyclerViewAdapter.filter(item ->
                                    item.getProductName() != null &&
                                            item.getProductName().toLowerCase().contains(query)
                            );
                        }

                        // Optional: Show "No Data Found"
                        if (supplierProductsRecyclerViewAdapter.getItemCount() == 0) {
                            tvNoDataFound.setVisibility(View.VISIBLE);
                        } else {
                            tvNoDataFound.setVisibility(View.GONE);
                        }
                    } else if (tag.equalsIgnoreCase("SupplierProductType")) {
                        if (query.isEmpty()) {
                            supplierProductTypesRecyclerViewAdapter.resetFilter();
                        } else {
                            supplierProductTypesRecyclerViewAdapter.filter(item ->
                                    item.getProductTypeName() != null &&
                                            item.getProductTypeName().toLowerCase().contains(query)
                            );
                        }

                        // Optional: Show "No Data Found"
                        if (supplierProductTypesRecyclerViewAdapter.getItemCount() == 0) {
                            tvNoDataFound.setVisibility(View.VISIBLE);
                        } else {
                            tvNoDataFound.setVisibility(View.GONE);
                        }
                    } else if (tag.equalsIgnoreCase("MeasurementSystem")) {
                        if (query.isEmpty()) {
                            measurementSystemsRecyclerViewAdapter.resetFilter();
                        } else {
                            measurementSystemsRecyclerViewAdapter.filter(item ->
                                    item.getMeasurementName() != null &&
                                            item.getMeasurementName().toLowerCase().contains(query)
                            );
                        }

                        // Optional: Show "No Data Found"
                        if (measurementSystemsRecyclerViewAdapter.getItemCount() == 0) {
                            tvNoDataFound.setVisibility(View.VISIBLE);
                        } else {
                            tvNoDataFound.setVisibility(View.GONE);
                        }
                    } else if (tag.equalsIgnoreCase("PurchaseContract")) {
                        if (query.isEmpty()) {
                            purchaseContractsRecyclerViewAdapter.resetFilter();
                        } else {
                            purchaseContractsRecyclerViewAdapter.filter(item ->
                                    (item.getContractCode() != null &&
                                            item.getDescription().toLowerCase().contains(query)) || (item.getContractCode() != null &&
                                            item.getContractCode().toLowerCase().contains(query))
                            );
                        }

                        // Optional: Show "No Data Found"
                        if (purchaseContractsRecyclerViewAdapter.getItemCount() == 0) {
                            tvNoDataFound.setVisibility(View.VISIBLE);
                        } else {
                            tvNoDataFound.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showDataDialog", e);
        }
    }

    private void saveOrUpdateReceptionDetails() {
        try {
            boolean isValid = true;

            if (Objects.requireNonNull(etSupplier.getText()).toString().trim().isEmpty()) {
                tiSupplier.setError(getString(R.string.required_field));
                tiSupplier.setErrorEnabled(true);
                isValid = false;
            } else {
                tiSupplier.setErrorEnabled(false);
                tiSupplier.setError(null);
            }

            if (Objects.requireNonNull(etSupplierProduct.getText()).toString().trim().isEmpty()) {
                tiSupplierProduct.setError(getString(R.string.required_field));
                tiSupplierProduct.setErrorEnabled(true);
                isValid = false;
            } else {
                tiSupplierProduct.setErrorEnabled(false);
                tiSupplierProduct.setError(null);
            }

            if (Objects.requireNonNull(etSupplierProductType.getText()).toString().trim().isEmpty()) {
                tiSupplierProductType.setError(getString(R.string.required_field));
                tiSupplierProductType.setErrorEnabled(true);
                isValid = false;
            } else {
                tiSupplierProductType.setErrorEnabled(false);
                tiSupplierProductType.setError(null);
            }

            if (Objects.requireNonNull(etIca.getText()).toString().trim().isEmpty()) {
                tiIca.setError(getString(R.string.required_field));
                tiIca.setErrorEnabled(true);
                isValid = false;
            } else {
                tiIca.setErrorEnabled(false);
                tiIca.setError(null);
            }

            if (Objects.requireNonNull(etMeasurementSystem.getText()).toString().trim().isEmpty()) {
                tiMeasurementSystem.setError(getString(R.string.required_field));
                tiMeasurementSystem.setErrorEnabled(true);
                isValid = false;
            } else {
                tiMeasurementSystem.setErrorEnabled(false);
                tiMeasurementSystem.setError(null);
            }

            if (Objects.requireNonNull(etWarehouse.getText()).toString().trim().isEmpty()) {
                tiWarehouse.setError(getString(R.string.required_field));
                tiWarehouse.setErrorEnabled(true);
                isValid = false;
            } else {
                tiWarehouse.setErrorEnabled(false);
                tiWarehouse.setError(null);
            }

            if (Objects.requireNonNull(etReceptionDate.getText()).toString().trim().isEmpty()) {
                tiReceptionDate.setError(getString(R.string.required_field));
                tiReceptionDate.setErrorEnabled(true);
                isValid = false;
            } else {
                tiReceptionDate.setErrorEnabled(false);
                tiReceptionDate.setError(null);
            }

            if (cbEnableFarm.isChecked()) {
                if (Objects.requireNonNull(etPurchaseContract.getText()).toString().trim().isEmpty()) {
                    tiPurchaseContract.setError(getString(R.string.required_field));
                    tiPurchaseContract.setErrorEnabled(true);
                    isValid = false;
                } else {
                    tiPurchaseContract.setErrorEnabled(false);
                    tiPurchaseContract.setError(null);
                }

                if (Objects.requireNonNull(etTruckNumber.getText()).toString().trim().isEmpty()) {
                    tiTruckNumber.setError(getString(R.string.required_field));
                    tiTruckNumber.setErrorEnabled(true);
                    isValid = false;
                } else {
                    tiTruckNumber.setErrorEnabled(false);
                    tiTruckNumber.setError(null);
                }

                if (Objects.requireNonNull(etTruckDriverName.getText()).toString().trim().isEmpty()) {
                    tiTruckDriverName.setError(getString(R.string.required_field));
                    tiTruckDriverName.setErrorEnabled(true);
                    isValid = false;
                } else {
                    tiTruckDriverName.setErrorEnabled(false);
                    tiTruckDriverName.setError(null);
                }
            }

            if (isValid) {
                ReceptionDetails receptionDetail = new ReceptionDetails();
                receptionDetail.setSupplier((int) etSupplier.getTag());
                receptionDetail.setSupplierProductId((int) etSupplierProduct.getTag());
                receptionDetail.setSupplierProductTypeId((int) etSupplierProductType.getTag());
                receptionDetail.setIca(etIca.getText().toString());
                receptionDetail.setMeasurementSystem((int) etMeasurementSystem.getTag());
                receptionDetail.setWarehouse((int) etWarehouse.getTag());
                receptionDetail.setReceptionDate(etReceptionDate.getText().toString());
                receptionDetail.setFarmEnabled(cbEnableFarm.isChecked());
                receptionDetail.setPurchaseContract((int) etPurchaseContract.getTag());
                receptionDetail.setTruckNumber(Objects.requireNonNull(etTruckNumber.getText()).toString());
                receptionDetail.setTruckDriverName(Objects.requireNonNull(etPurchaseContract.getText()).toString());
                receptionDetail.setTempReceptionId("R_" + CommonUtils.getCurrentLocalDateTimeStamp());
                receptionDetail.setReceptionId(null);
                receptionDetail.setSynced(false);
                receptionDetail.setDeleted(false);
                receptionDetail.setEdited(false);
                receptionDetail.setUpdatedAt(System.currentTimeMillis());

                receptionViewModel.saveReceptionDetails(receptionDetail);

                receptionViewModel.getReceptionStatus().observe(this, aBoolean -> {
                    if(aBoolean) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("key", "Hello from Second Activity");
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        showCustomDialog(getString(R.string.error), getString(R.string.data_added_failed), false);
                    }
                });
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "saveOrUpdateReceptionDetails", e);
        }
    }
}