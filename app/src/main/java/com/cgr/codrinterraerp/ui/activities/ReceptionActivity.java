package com.cgr.codrinterraerp.ui.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
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
import com.cgr.codrinterraerp.db.views.ReceptionView;
import com.cgr.codrinterraerp.helper.PreferenceManager;
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
    private MaterialButton btnCloseReception, btnOpenReception, btnSubmit;
    private AppCompatTextView tvNoDataFound;
    private MaterialCheckBox cbEnableFarm;
    private LinearLayout llFarm;
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
    private boolean isReceptionEdit = false;
    private ReceptionDetails existingReceptionDetail;
    private ReceptionView receptionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reception);
        statusBarSetting(false);
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
            btnCloseReception = findViewById(R.id.btnCloseReception);
            btnOpenReception = findViewById(R.id.btnOpenReception);

            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);
                receptionViewModel = new ViewModelProvider(this).get(ReceptionViewModel.class);

                isReceptionEdit = bundle.getBoolean("isEdit");

                txtTitle.setText(isReceptionEdit ? getString(R.string.edit_reception) : getString(R.string.add_reception));
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

                receptionViewModel.getProgressState().observe(this, aBoolean -> {
                    if (aBoolean) {
                        showProgress(progressBar);
                    } else {
                        hideProgress(progressBar);
                    }
                });

                if (isReceptionEdit) {

                    receptionView = (ReceptionView) bundle.getSerializable("receptionDetails");

                    if (receptionView != null) {
                        existingReceptionDetail = receptionViewModel.fetchReceptionDetailById(receptionView.tempReceptionId);
                        fetchData(true, existingReceptionDetail);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.common_error), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    fetchData(false, null);
                }

                actionListeners();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.common_error), Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void fetchData(boolean isEdit, ReceptionDetails receptionDetail) {
        try {
            suppliersList = masterViewModel.fetchSuppliers();
            warehousesList = masterViewModel.fetchWarehouses();
            resetDependentLists();

            if (isEdit) {

                if (receptionView.totalPieces > 0) {

                    if (receptionView.isClosed) {
                        btnOpenReception.setVisibility(View.VISIBLE);
                        btnCloseReception.setVisibility(View.GONE);
                    } else {
                        btnCloseReception.setVisibility(View.VISIBLE);
                        btnOpenReception.setVisibility(View.GONE);
                    }

                    int colorLightGrey = ContextCompat.getColor(this, R.color.colorLightGrey);

                    tiSupplier.setEnabled(false);
                    tiSupplierProduct.setEnabled(false);
                    tiSupplierProductType.setEnabled(false);
                    tiMeasurementSystem.setEnabled(false);

                    tiSupplier.setBoxBackgroundColor(colorLightGrey);
                    tiSupplierProduct.setBoxBackgroundColor(colorLightGrey);
                    tiSupplierProductType.setBoxBackgroundColor(colorLightGrey);
                    tiMeasurementSystem.setBoxBackgroundColor(colorLightGrey);

                    tiSupplier.setAlpha(0.9f);
                    tiSupplierProduct.setAlpha(0.9f);
                    tiSupplierProductType.setAlpha(0.9f);
                    tiMeasurementSystem.setAlpha(0.9f);

                    if (receptionView.isFarmEnabled) {
                        tiPurchaseContract.setEnabled(false);
                        tiPurchaseContract.setBoxBackgroundColor(colorLightGrey);
                        tiPurchaseContract.setAlpha(0.9f);
                    }
                }

                // Supplier
                for (Suppliers s : suppliersList) {
                    if (s.getSupplierId() == receptionDetail.getSupplierId()) {
                        etSupplier.setText(s.getSupplierName());
                        etSupplier.setTag(s.getSupplierId());
                        break;
                    }
                }

                // Load dependent data (IMPORTANT)
                supplierProductsList = masterViewModel.fetchSupplierProducts(receptionDetail.getSupplierId());

                // Supplier Product
                for (SupplierProducts sp : supplierProductsList) {
                    if (sp.getSupplierProductId() == receptionDetail.getSupplierProductId()) {
                        etSupplierProduct.setText(sp.getProductName());
                        etSupplierProduct.setTag(R.id.tag_supplier_product_id, sp.getSupplierProductId());
                        etSupplierProduct.setTag(R.id.tag_product_id, sp.getProductId());
                        break;
                    }
                }

                // Load next dependency
                supplierProductTypesList = masterViewModel.fetchSupplierProductTypes(receptionDetail.getSupplierId(), receptionDetail.getProductId());

                // Product Type
                for (SupplierProductTypes spt : supplierProductTypesList) {
                    if (spt.getTypeId() == receptionDetail.getSupplierProductTypeId()) {
                        etSupplierProductType.setText(spt.getProductTypeName());
                        etSupplierProductType.setTag(R.id.tag_supplier_product_type_id, spt.getTypeId());
                        etSupplierProductType.setTag(R.id.tag_product_type_id, spt.getProductTypeId());
                        break;
                    }
                }

                // Measurement System
                measurementSystemsList = masterViewModel.fetchMeasurementSystems(receptionDetail.getProductTypeId());
                for (MeasurementSystems ms : measurementSystemsList) {
                    if (ms.getId() == receptionDetail.getMeasurementSystem()) {
                        etMeasurementSystem.setText(ms.getMeasurementName());
                        etMeasurementSystem.setTag(ms.getId());
                        break;
                    }
                }

                // Warehouse
                for (Warehouses w : warehousesList) {
                    if (w.getId() == receptionDetail.getWarehouse()) {
                        etWarehouse.setText(w.getWarehouseName());
                        etWarehouse.setTag(w.getId());
                        break;
                    }
                }

                // ICA + Date
                etIca.setText(receptionDetail.getIca());
                etReceptionDate.setText(receptionDetail.getReceptionDate());

                // Farm section
                cbEnableFarm.setChecked(receptionDetail.isFarmEnabled());

                if (receptionDetail.isFarmEnabled()) {
                    purchaseContractsList = masterViewModel.fetchPurchaseContracts(receptionDetail.getSupplierId(), receptionDetail.getProductId(), receptionDetail.getProductTypeId());

                    for (PurchaseContracts pc : purchaseContractsList) {
                        if (pc.getContractId() == receptionDetail.getPurchaseContract()) {
                            etPurchaseContract.setText(pc.getContractCode());
                            etPurchaseContract.setTag(pc.getContractId());
                            break;
                        }
                    }

                    llFarm.setVisibility(View.VISIBLE);

                    etTruckNumber.setText(receptionDetail.getTruckNumber());
                    etTruckDriverName.setText(receptionDetail.getTruckDriverName());
                } else {
                    llFarm.setVisibility(View.GONE);
                }
            } else {
                btnCloseReception.setVisibility(View.GONE);
                btnOpenReception.setVisibility(View.GONE);
            }
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

                    if (isReceptionEdit && existingReceptionDetail != null) {
                        purchaseContractsList = masterViewModel.fetchPurchaseContracts(existingReceptionDetail.getSupplierId(), existingReceptionDetail.getProductId(),
                                existingReceptionDetail.getProductTypeId());
                    }

                    llFarm.setVisibility(View.VISIBLE);
                } else {
                    llFarm.setVisibility(View.GONE);
                }

                etPurchaseContract.setText("");
                etPurchaseContract.setTag(null);
                etTruckNumber.setText("");
                etTruckDriverName.setText("");
            });

            btnSubmit.setOnClickListener(v -> {
                btnSubmit.setEnabled(false);
                saveOrUpdateReceptionDetails();
            });

            btnCloseReception.setOnClickListener(v -> closeConfirmation(true));

            btnOpenReception.setOnClickListener(v -> closeConfirmation(false));
        } catch (Exception e) {
            AppLogger.e(getClass(), "clickListeners", e);
        }
    }

    private void showDataDialog(String tag) {
        try {
            hideKeyboard(this);
            Dialog dialog = new Dialog(this, R.style.DialogTheme);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            dialog.getWindow().setDimAmount(0.6f);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);
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

                if (suppliersList != null && suppliersList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);

                    suppliersRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(suppliersList), R.layout.row_dialog_list) {
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

                        resetDependentLists();

                        etSupplierProduct.setText("");
                        etSupplierProductType.setText("");
                        etSupplierProduct.setTag(R.id.tag_supplier_product_id, null);
                        etSupplierProduct.setTag(R.id.tag_product_id, null);
                        etSupplierProductType.setTag(R.id.tag_supplier_product_type_id, null);
                        etSupplierProductType.setTag(R.id.tag_product_type_id, null);
                        etMeasurementSystem.setText("");
                        etMeasurementSystem.setTag(null);
                        etPurchaseContract.setText("");
                        etPurchaseContract.setTag(null);

                        supplierProductsList = masterViewModel.fetchSupplierProducts(selected.getSupplierId());

                        dialog.dismiss();
                    });
                }
            } else if (tag.equalsIgnoreCase("Warehouse")) {

                dialogTitle.setText(R.string.select_warehouse);

                if (warehousesList != null && warehousesList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);
                }

                warehousesRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(warehousesList), R.layout.row_dialog_list) {
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

                    dialog.dismiss();
                });
            } else if (tag.equalsIgnoreCase("SupplierProduct")) {

                dialogTitle.setText(R.string.select_wood);

                if (supplierProductsList != null && supplierProductsList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);
                }

                supplierProductsRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(supplierProductsList), R.layout.row_dialog_list) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, SupplierProducts supplierProducts) {

                        AppCompatTextView tvName = holder.itemView.findViewById(R.id.tvName);
                        AppCompatImageView ivSelected = holder.itemView.findViewById(R.id.ivItemSelected);

                        tvName.setText(supplierProducts.getProductName());

                        boolean isSelected = false;
                        if (etSupplierProduct.getTag(R.id.tag_supplier_product_id) != null) {
                            isSelected = Objects.equals(supplierProducts.getSupplierProductId(), etSupplierProduct.getTag(R.id.tag_supplier_product_id));
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
                    etSupplierProduct.setTag(R.id.tag_supplier_product_id, selected.getSupplierProductId());
                    etSupplierProduct.setTag(R.id.tag_product_id, selected.getProductId());

                    etSupplierProductType.setText("");
                    etSupplierProductType.setTag(R.id.tag_supplier_product_type_id, null);
                    etSupplierProductType.setTag(R.id.tag_product_type_id, null);
                    etMeasurementSystem.setText("");
                    etMeasurementSystem.setTag(null);
                    etPurchaseContract.setText("");
                    etPurchaseContract.setTag(null);

                    if (supplierProductTypesList != null) supplierProductTypesList.clear();
                    if (measurementSystemsList != null) measurementSystemsList.clear();
                    if (purchaseContractsList != null) purchaseContractsList.clear();

                    supplierProductTypesList = masterViewModel.fetchSupplierProductTypes(selected.getSupplierId(), selected.getProductId());

                    dialog.dismiss();
                });
            } else if (tag.equalsIgnoreCase("SupplierProductType")) {

                dialogTitle.setText(R.string.select_wood_type);

                if (supplierProductTypesList != null && supplierProductTypesList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);
                }

                supplierProductTypesRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(supplierProductTypesList), R.layout.row_dialog_list) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, SupplierProductTypes supplierProductTypes) {

                        AppCompatTextView tvName = holder.itemView.findViewById(R.id.tvName);
                        AppCompatImageView ivSelected = holder.itemView.findViewById(R.id.ivItemSelected);

                        tvName.setText(supplierProductTypes.getProductTypeName());

                        boolean isSelected = false;
                        if (etSupplierProductType.getTag(R.id.tag_product_type_id) != null) {
                            isSelected = Objects.equals(supplierProductTypes.getProductTypeId(), etSupplierProductType.getTag(R.id.tag_product_type_id));
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
                    etSupplierProductType.setTag(R.id.tag_supplier_product_type_id, selected.getTypeId());
                    etSupplierProductType.setTag(R.id.tag_product_type_id, selected.getProductTypeId());

                    etMeasurementSystem.setText("");
                    etMeasurementSystem.setTag(null);
                    etPurchaseContract.setText("");
                    etPurchaseContract.setTag(null);

                    if (measurementSystemsList != null) measurementSystemsList.clear();
                    if (purchaseContractsList != null) purchaseContractsList.clear();

                    measurementSystemsList = masterViewModel.fetchMeasurementSystems(selected.getProductTypeId());
                    purchaseContractsList = masterViewModel.fetchPurchaseContracts(selected.getSupplierId(), selected.getProductId(), selected.getProductTypeId());

                    dialog.dismiss();
                });
            } else if (tag.equalsIgnoreCase("MeasurementSystem")) {

                dialogTitle.setText(R.string.select_measurement_system);

                if (measurementSystemsList != null && measurementSystemsList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);

                    measurementSystemsRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(measurementSystemsList), R.layout.row_dialog_list) {
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

                        dialog.dismiss();
                    });
                }
            } else if (tag.equalsIgnoreCase("PurchaseContract")) {

                dialogTitle.setText(R.string.select_purchase_contract);

                if (purchaseContractsList != null && purchaseContractsList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);

                    purchaseContractsRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(purchaseContractsList), R.layout.row_dialog_list) {
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

                        dialog.dismiss();
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
                                    ((item.getDescription() != null && item.getDescription().toLowerCase().contains(query)))
                                            || (item.getContractCode() != null && item.getContractCode().toLowerCase().contains(query))
                            );
                        }

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

    private void resetDependentLists() {
        if (supplierProductsList != null) supplierProductsList.clear();
        if (supplierProductTypesList != null) supplierProductTypesList.clear();
        if (measurementSystemsList != null) measurementSystemsList.clear();
        if (purchaseContractsList != null) purchaseContractsList.clear();
    }

    private void saveOrUpdateReceptionDetails() {
        try {
            hideKeyboard(this);

            boolean isValid = true;

            if (TextUtils.isEmpty(etSupplier.getText())) {
                tiSupplier.setError(getString(R.string.required_field));
                tiSupplier.setErrorEnabled(true);
                isValid = false;
            } else {
                tiSupplier.setErrorEnabled(false);
                tiSupplier.setError(null);
            }

            if (TextUtils.isEmpty(etSupplierProduct.getText())) {
                tiSupplierProduct.setError(getString(R.string.required_field));
                tiSupplierProduct.setErrorEnabled(true);
                isValid = false;
            } else {
                tiSupplierProduct.setErrorEnabled(false);
                tiSupplierProduct.setError(null);
            }

            if (TextUtils.isEmpty(etSupplierProductType.getText())) {
                tiSupplierProductType.setError(getString(R.string.required_field));
                tiSupplierProductType.setErrorEnabled(true);
                isValid = false;
            } else {
                tiSupplierProductType.setErrorEnabled(false);
                tiSupplierProductType.setError(null);
            }

            if (TextUtils.isEmpty(etIca.getText())) {
                tiIca.setError(getString(R.string.required_field));
                tiIca.setErrorEnabled(true);
                isValid = false;
            } else {
                tiIca.setErrorEnabled(false);
                tiIca.setError(null);
            }

            if (TextUtils.isEmpty(etMeasurementSystem.getText())) {
                tiMeasurementSystem.setError(getString(R.string.required_field));
                tiMeasurementSystem.setErrorEnabled(true);
                isValid = false;
            } else {
                tiMeasurementSystem.setErrorEnabled(false);
                tiMeasurementSystem.setError(null);
            }

            if (TextUtils.isEmpty(etWarehouse.getText())) {
                tiWarehouse.setError(getString(R.string.required_field));
                tiWarehouse.setErrorEnabled(true);
                isValid = false;
            } else {
                tiWarehouse.setErrorEnabled(false);
                tiWarehouse.setError(null);
            }

            if (TextUtils.isEmpty(etReceptionDate.getText())) {
                tiReceptionDate.setError(getString(R.string.required_field));
                tiReceptionDate.setErrorEnabled(true);
                isValid = false;
            } else {
                tiReceptionDate.setErrorEnabled(false);
                tiReceptionDate.setError(null);
            }

            if (cbEnableFarm.isChecked()) {
                if (TextUtils.isEmpty(etPurchaseContract.getText())) {
                    tiPurchaseContract.setError(getString(R.string.required_field));
                    tiPurchaseContract.setErrorEnabled(true);
                    isValid = false;
                } else {
                    tiPurchaseContract.setErrorEnabled(false);
                    tiPurchaseContract.setError(null);
                }

                if (TextUtils.isEmpty(etTruckNumber.getText())) {
                    tiTruckNumber.setError(getString(R.string.required_field));
                    tiTruckNumber.setErrorEnabled(true);
                    isValid = false;
                } else {
                    tiTruckNumber.setErrorEnabled(false);
                    tiTruckNumber.setError(null);
                }

                if (TextUtils.isEmpty(etTruckDriverName.getText())) {
                    tiTruckDriverName.setError(getString(R.string.required_field));
                    tiTruckDriverName.setErrorEnabled(true);
                    isValid = false;
                } else {
                    tiTruckDriverName.setErrorEnabled(false);
                    tiTruckDriverName.setError(null);
                }
            }

            if (!isValid) {
                enableSubmit();
                return;
            }

            // ================= ICA VALIDATION =================
            int icaCount;
            if (isReceptionEdit && existingReceptionDetail != null) {
                icaCount = receptionViewModel.getReceptionInventoryOrdersCountForEdit(
                        etIca.getText().toString().trim(),
                        CommonUtils.getTagInt(etSupplier.getTag()),
                        existingReceptionDetail.getTempReceptionId()
                );

                if(icaCount <= 0) {
                    icaCount = receptionViewModel.getReceptionInventoryOrdersCount(etIca.getText().toString(), CommonUtils.getTagInt(etSupplier.getTag()));
                }
            } else {
                icaCount = receptionViewModel.getReceptionInventoryOrdersCount(etIca.getText().toString(), CommonUtils.getTagInt(etSupplier.getTag()));
            }

            if (icaCount > 0) {
                Toast.makeText(getApplicationContext(), R.string.ica_exists, Toast.LENGTH_SHORT).show();
                enableSubmit();
                return;
            }

            int farmIcaCount = 0;
            if(cbEnableFarm.isChecked()) {
                farmIcaCount = receptionViewModel.getFarmInventoryOrdersCount(etIca.getText().toString(), CommonUtils.getTagInt(etSupplier.getTag()));
            }

            if (farmIcaCount > 0) {
                Toast.makeText(getApplicationContext(), R.string.farm_ica_exists, Toast.LENGTH_SHORT).show();
                enableSubmit();
                return;
            }

            // ================= CREATE / UPDATE OBJECT =================
            ReceptionDetails receptionDetail;
            String oldIca = null;
            int oldSupplierId = 0;
            long currentTimeStamp = CommonUtils.getCurrentLocalDateTimeStamp();

            if (isReceptionEdit && existingReceptionDetail != null) {
                oldIca = existingReceptionDetail.getIca();
                oldSupplierId = existingReceptionDetail.getSupplierId();
                receptionDetail = existingReceptionDetail; // UPDATE
            } else {
                receptionDetail = new ReceptionDetails(); // CREATE
            }

            // ================= COMMON FIELD SET =================
            receptionDetail.setSupplierId(CommonUtils.getTagInt(etSupplier.getTag()));
            receptionDetail.setSupplierProductId(CommonUtils.getTagInt(etSupplierProduct.getTag(R.id.tag_supplier_product_id)));
            receptionDetail.setProductId(CommonUtils.getTagInt(etSupplierProduct.getTag(R.id.tag_product_id)));
            receptionDetail.setSupplierProductTypeId(CommonUtils.getTagInt(etSupplierProductType.getTag(R.id.tag_supplier_product_type_id)));
            receptionDetail.setProductTypeId(CommonUtils.getTagInt(etSupplierProductType.getTag(R.id.tag_product_type_id)));
            receptionDetail.setIca(etIca.getText().toString().trim());
            receptionDetail.setMeasurementSystem(CommonUtils.getTagInt(etMeasurementSystem.getTag()));
            receptionDetail.setWarehouse(CommonUtils.getTagInt(etWarehouse.getTag()));
            receptionDetail.setReceptionDate(etReceptionDate.getText().toString().trim());
            receptionDetail.setFarmEnabled(cbEnableFarm.isChecked());

            // ================= MAPPING ID =================
            String mappingId;

            if (isReceptionEdit && existingReceptionDetail != null) {
                mappingId = existingReceptionDetail.getContainerReceptionMappingId();
            } else {
                mappingId = "CRM_" + currentTimeStamp;
            }

            receptionDetail.setContainerReceptionMappingId(mappingId);

            // ================= FARM DATA =================
            if (cbEnableFarm.isChecked()) {
                receptionDetail.setPurchaseContract(CommonUtils.getTagInt(etPurchaseContract.getTag()));
                receptionDetail.setTruckNumber(Objects.requireNonNull(etTruckNumber.getText()).toString().trim());
                receptionDetail.setTruckDriverName(Objects.requireNonNull(etTruckDriverName.getText()).toString().trim());
            } else {
                receptionDetail.setPurchaseContract(0);
                receptionDetail.setTruckNumber("");
                receptionDetail.setTruckDriverName("");
            }

            // ================= ID HANDLING =================
            if (isReceptionEdit && existingReceptionDetail != null) {
                receptionDetail.setTempReceptionId(existingReceptionDetail.getTempReceptionId());
                receptionDetail.setReceptionId(existingReceptionDetail.getReceptionId());
                receptionDetail.setEdited(true);
            } else {
                receptionDetail.setTempReceptionId("REC_" + currentTimeStamp);
                receptionDetail.setReceptionId(null);
                receptionDetail.setEdited(false);
                receptionDetail.setCreatedAt(System.currentTimeMillis());
            }

            receptionDetail.setSynced(false);
            receptionDetail.setDeleted(false);
            receptionDetail.setUpdatedAt(System.currentTimeMillis());

            // ================= SAVE =================
            receptionViewModel.saveReceptionDetails(receptionDetail, oldIca != null ? oldIca : receptionDetail.getIca(),
                    oldSupplierId != 0 ? oldSupplierId : receptionDetail.getSupplierId()
            );

            receptionViewModel.getReceptionStatus().observe(this, new Observer<>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    receptionViewModel.getReceptionStatus().removeObserver(this);

                    enableSubmit();

                    if (aBoolean) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("savedReceptionId", receptionViewModel.getReceptionSavedId());
                        resultIntent.putExtra("isEdit", isReceptionEdit);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        showCustomDialog(getString(R.string.error), getString(R.string.data_added_failed), false);
                    }
                }
            });
        } catch (Exception e) {
            enableSubmit();
            AppLogger.e(getClass(), "saveOrUpdateReceptionDetails", e);
        }
    }

    private void enableSubmit() {
        btnSubmit.setEnabled(true);
    }

    private void closeConfirmation(boolean isClose) {
        try {
            LayoutInflater dialogInflater = LayoutInflater.from(this);
            View dialogView = dialogInflater.inflate(R.layout.custom_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            AppCompatTextView dialogHeader = dialogView.findViewById(R.id.dialogHeader);
            AppCompatTextView dialogBody = dialogView.findViewById(R.id.dialogBody);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
            MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);

            dialogHeader.setText(getString(R.string.confirmation));

            if (receptionView.isClosed) {
                dialogBody.setText(R.string.open_confirmation);
            } else {
                dialogBody.setText(R.string.close_confirmation);
            }


            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnOk.setOnClickListener(v -> {
                dialog.dismiss();
                boolean closed = receptionViewModel.closeReceptionDetails(receptionView.tempReceptionId,
                        CommonUtils.getCurrentLocalDateTimeStamp(), PreferenceManager.INSTANCE.getUserId(), isClose);

                if (closed) {
                    Intent resultIntent = new Intent();
                    if (isClose) {
                        resultIntent.putExtra("isClosed", true);
                    } else {
                        resultIntent.putExtra("isOpened", true);
                    }
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "closeConfirmation", e);
        }
    }
}