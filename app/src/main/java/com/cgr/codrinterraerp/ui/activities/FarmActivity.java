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
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import com.cgr.codrinterraerp.db.entities.FarmDetails;
import com.cgr.codrinterraerp.db.entities.ProductTypes;
import com.cgr.codrinterraerp.db.entities.Products;
import com.cgr.codrinterraerp.db.entities.PurchaseContracts;
import com.cgr.codrinterraerp.db.entities.Suppliers;
import com.cgr.codrinterraerp.db.views.FarmView;
import com.cgr.codrinterraerp.helper.PreferenceManager;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.utils.DividerItemDecoration;
import com.cgr.codrinterraerp.viewmodel.FarmViewModel;
import com.cgr.codrinterraerp.viewmodel.MasterViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FarmActivity extends BaseActivity {

    private TextInputLayout tiSupplier, tiProduct, tiProductType, tiIca, tiPurchaseDate, tiPurchaseContract, tiTruckNumber, tiTruckDriverName;
    private AppCompatEditText etSupplier, etProduct, etProductType, etIca, etPurchaseDate, etPurchaseContract, etTruckNumber, etTruckDriverName;
    private MaterialButton btnOpenFarm, btnCloseFarm, btnSubmit;
    private FrameLayout progressBar;
    private AppCompatTextView tvNoDataFound;
    private List<Suppliers> suppliersList;
    private List<Products> productsList;
    private List<ProductTypes> productTypesList;
    private List<PurchaseContracts> purchaseContractsList;
    private RecyclerViewAdapter<Suppliers> suppliersRecyclerViewAdapter;
    private RecyclerViewAdapter<Products> productsRecyclerViewAdapter;
    private RecyclerViewAdapter<ProductTypes> productTypesRecyclerViewAdapter;
    private RecyclerViewAdapter<PurchaseContracts> purchaseContractsRecyclerViewAdapter;
    private MasterViewModel masterViewModel;
    private FarmViewModel farmViewModel;
    private boolean isFarmEdit = false;
    private FarmDetails existingFarmDetail;
    private FarmView farmView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm);
        statusBarSetting(false);
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            AppCompatImageView imgBack = findViewById(R.id.imgBack);
            AppCompatTextView txtTitle = findViewById(R.id.txtTitle);

            tiSupplier = findViewById(R.id.tiSupplier);
            tiProduct = findViewById(R.id.tiProduct);
            tiProductType = findViewById(R.id.tiProductType);
            tiIca = findViewById(R.id.tiIca);
            tiPurchaseDate = findViewById(R.id.tiPurchaseDate);
            tiPurchaseContract = findViewById(R.id.tiPurchaseContract);
            tiTruckNumber = findViewById(R.id.tiTruckNumber);
            tiTruckDriverName = findViewById(R.id.tiTruckDriverName);
            etSupplier = findViewById(R.id.etSupplier);
            etProduct = findViewById(R.id.etProduct);
            etProductType = findViewById(R.id.etProductType);
            etIca = findViewById(R.id.etIca);
            etPurchaseDate = findViewById(R.id.etPurchaseDate);
            etPurchaseContract = findViewById(R.id.etPurchaseContract);
            etTruckNumber = findViewById(R.id.etTruckNumber);
            etTruckDriverName = findViewById(R.id.etTruckDriverName);
            btnSubmit = findViewById(R.id.btnSubmit);
            progressBar = findViewById(R.id.progressBar);
            btnCloseFarm = findViewById(R.id.btnCloseFarm);
            btnOpenFarm = findViewById(R.id.btnOpenFarm);

            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);
                farmViewModel = new ViewModelProvider(this).get(FarmViewModel.class);

                isFarmEdit = bundle.getBoolean("isEdit");

                txtTitle.setText(isFarmEdit ? getString(R.string.edit_farm) : getString(R.string.add_farm));
                imgBack.setOnClickListener(v -> finish());

                CommonUtils.clearErrorOnTyping(etSupplier, tiSupplier);
                CommonUtils.clearErrorOnTyping(etProduct, tiProduct);
                CommonUtils.clearErrorOnTyping(etProductType, tiProductType);
                CommonUtils.clearErrorOnTyping(etIca, tiIca);
                CommonUtils.clearErrorOnTyping(etPurchaseDate, tiPurchaseDate);
                CommonUtils.clearErrorOnTyping(etPurchaseContract, tiPurchaseContract);
                CommonUtils.clearErrorOnTyping(etTruckNumber, tiTruckNumber);
                CommonUtils.clearErrorOnTyping(etTruckDriverName, tiTruckDriverName);

                farmViewModel.getProgressState().observe(this, aBoolean -> {
                    if (aBoolean) {
                        showProgress(progressBar);
                    } else {
                        hideProgress(progressBar);
                    }
                });

                if (isFarmEdit) {
                    farmView = (FarmView) bundle.getSerializable("farmDetails");

                    if (farmView != null) {
                        existingFarmDetail = farmViewModel.fetchFarmDetailById(farmView.tempFarmId);
                        fetchData(true, existingFarmDetail);
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

    private void fetchData(boolean isEdit, FarmDetails farmDetail) {
        try {
            suppliersList = masterViewModel.fetchSuppliers();
            productsList = masterViewModel.fetchProducts();
            productTypesList = masterViewModel.fetchProductTypes();
            resetDependentLists();

            if (isEdit) {

                if (farmView.totalPieces > 0) {

                    if (farmView.isClosed) {
                        btnOpenFarm.setVisibility(View.VISIBLE);
                        btnCloseFarm.setVisibility(View.GONE);
                    } else {
                        btnCloseFarm.setVisibility(View.VISIBLE);
                        btnOpenFarm.setVisibility(View.GONE);
                    }

                    int colorLightGrey = ContextCompat.getColor(this, R.color.colorLightGrey);

                    tiSupplier.setEnabled(false);
                    tiProduct.setEnabled(false);
                    tiProductType.setEnabled(false);
                    tiPurchaseContract.setEnabled(false);

                    tiSupplier.setBoxBackgroundColor(colorLightGrey);
                    tiProduct.setBoxBackgroundColor(colorLightGrey);
                    tiProductType.setBoxBackgroundColor(colorLightGrey);
                    tiPurchaseContract.setBoxBackgroundColor(colorLightGrey);

                    tiSupplier.setAlpha(0.9f);
                    tiProduct.setAlpha(0.9f);
                    tiProductType.setAlpha(0.9f);
                    tiPurchaseContract.setAlpha(0.9f);
                }

                // Supplier
                for (Suppliers s : suppliersList) {
                    if (s.getSupplierId() == farmDetail.getSupplierId()) {
                        etSupplier.setText(s.getSupplierName());
                        etSupplier.setTag(s.getSupplierId());
                        break;
                    }
                }

                // Product
                for (Products p : productsList) {
                    if (p.getProductId() == farmDetail.getProductId()) {
                        etProduct.setText(p.getProductName());
                        etProduct.setTag(p.getProductId());
                        break;
                    }
                }

                // Product Type
                for (ProductTypes pt : productTypesList) {
                    if (pt.getTypeId() == farmDetail.getProductTypeId()) {
                        etProductType.setText(pt.getProductTypeName());
                        etProductType.setTag(pt.getTypeId());
                        break;
                    }
                }

                // Purchase Contract
                purchaseContractsList = masterViewModel.fetchPurchaseContracts(existingFarmDetail.getSupplierId(), existingFarmDetail.getProductId(),
                        existingFarmDetail.getProductTypeId());

                for (PurchaseContracts pc : purchaseContractsList) {
                    if (pc.getContractId() == existingFarmDetail.getPurchaseContract()) {
                        etPurchaseContract.setText(pc.getContractCode());
                        etPurchaseContract.setTag(pc.getContractId());
                        break;
                    }
                }

                etIca.setText(existingFarmDetail.getIca());
                etPurchaseDate.setText(existingFarmDetail.getPurchaseDate());
                etTruckNumber.setText(existingFarmDetail.getTruckNumber());
                etTruckDriverName.setText(existingFarmDetail.getTruckDriverName());
            } else {
                btnCloseFarm.setVisibility(View.GONE);
                btnOpenFarm.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchData", e);
        }
    }

    private void actionListeners() {
        try {
            etSupplier.setKeyListener(null);
            etProduct.setKeyListener(null);
            etProductType.setKeyListener(null);

            etSupplier.setOnClickListener(v -> showDataDialog("Supplier"));
            etProduct.setOnClickListener(v -> showDataDialog("Product"));
            etProductType.setOnClickListener(v -> showDataDialog("ProductType"));
            etPurchaseContract.setOnClickListener(v -> showDataDialog("PurchaseContract"));

            tiSupplier.setEndIconOnClickListener(v -> showDataDialog("Supplier"));
            tiProduct.setEndIconOnClickListener(v -> showDataDialog("Product"));
            tiProductType.setEndIconOnClickListener(v -> showDataDialog("ProductType"));
            tiPurchaseContract.setEndIconOnClickListener(v -> showDataDialog("PurchaseContract"));

            etPurchaseDate.setOnClickListener(v -> CommonUtils.showDatePicker(this, etPurchaseDate));

            btnSubmit.setOnClickListener(v -> {
                btnSubmit.setEnabled(false);
                saveOrUpdateFarmDetails();
            });

            btnCloseFarm.setOnClickListener(v -> closeConfirmation(true));

            btnOpenFarm.setOnClickListener(v -> closeConfirmation(false));
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

                        etProduct.setText("");
                        etProductType.setText("");
                        etProduct.setTag(null);
                        etProductType.setTag(null);
                        etPurchaseContract.setText("");
                        etPurchaseContract.setTag(null);

                        dialog.dismiss();
                    });
                }
            } else if (tag.equalsIgnoreCase("Product")) {

                dialogTitle.setText(R.string.select_wood);

                if (productsList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);
                }

                productsRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(productsList), R.layout.row_dialog_list) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, Products products) {

                        AppCompatTextView tvName = holder.itemView.findViewById(R.id.tvName);
                        AppCompatImageView ivSelected = holder.itemView.findViewById(R.id.ivItemSelected);

                        tvName.setText(products.getProductName());

                        boolean isSelected = false;
                        if (etProduct.getTag() != null) {
                            isSelected = Objects.equals(products.getProductId(), etProduct.getTag());
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

                rvList.setAdapter(productsRecyclerViewAdapter);
                productsRecyclerViewAdapter.setOnItemClickListener((view, position) -> {

                    Products selected = productsRecyclerViewAdapter.getItem(position);

                    etProduct.setText(selected.getProductName());
                    etProduct.setTag(selected.getProductId());

                    etProductType.setText("");
                    etProductType.setTag(null);
                    etPurchaseContract.setText("");
                    etPurchaseContract.setTag(null);

                    if (purchaseContractsList != null) purchaseContractsList.clear();

                    dialog.dismiss(); // optional
                });
            } else if (tag.equalsIgnoreCase("ProductType")) {

                dialogTitle.setText(R.string.select_wood_type);

                if (productTypesList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);
                }

                productTypesRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(productTypesList), R.layout.row_dialog_list) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, ProductTypes productTypes) {

                        AppCompatTextView tvName = holder.itemView.findViewById(R.id.tvName);
                        AppCompatImageView ivSelected = holder.itemView.findViewById(R.id.ivItemSelected);

                        tvName.setText(productTypes.getProductTypeName());

                        boolean isSelected = false;
                        if (etProductType.getTag() != null) {
                            isSelected = Objects.equals(productTypes.getTypeId(), etProductType.getTag());
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

                rvList.setAdapter(productTypesRecyclerViewAdapter);
                productTypesRecyclerViewAdapter.setOnItemClickListener((view, position) -> {

                    ProductTypes selected = productTypesRecyclerViewAdapter.getItem(position);

                    etProductType.setText(selected.getProductTypeName());
                    etProductType.setTag(selected.getTypeId());

                    etPurchaseContract.setText("");
                    etPurchaseContract.setTag(null);

                    if (purchaseContractsList != null) purchaseContractsList.clear();

                    purchaseContractsList = masterViewModel.fetchPurchaseContracts(CommonUtils.getTagInt(etSupplier.getTag()), CommonUtils.getTagInt(etProduct.getTag()),
                            CommonUtils.getTagInt(etProductType.getTag()));

                    dialog.dismiss(); // optional
                });
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
                    }
                    if (tag.equalsIgnoreCase("Product")) {
                        if (query.isEmpty()) {
                            productsRecyclerViewAdapter.resetFilter();
                        } else {
                            productsRecyclerViewAdapter.filter(item ->
                                    item.getProductName() != null &&
                                            item.getProductName().toLowerCase().contains(query)
                            );
                        }

                        // Optional: Show "No Data Found"
                        if (productsRecyclerViewAdapter.getItemCount() == 0) {
                            tvNoDataFound.setVisibility(View.VISIBLE);
                        } else {
                            tvNoDataFound.setVisibility(View.GONE);
                        }
                    } else if (tag.equalsIgnoreCase("ProductType")) {
                        if (query.isEmpty()) {
                            productTypesRecyclerViewAdapter.resetFilter();
                        } else {
                            productTypesRecyclerViewAdapter.filter(item ->
                                    item.getProductTypeName() != null &&
                                            item.getProductTypeName().toLowerCase().contains(query)
                            );
                        }

                        // Optional: Show "No Data Found"
                        if (productTypesRecyclerViewAdapter.getItemCount() == 0) {
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
        if (purchaseContractsList != null) purchaseContractsList.clear();
    }

    private void saveOrUpdateFarmDetails() {
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

            if (TextUtils.isEmpty(etProduct.getText())) {
                tiProduct.setError(getString(R.string.required_field));
                tiProduct.setErrorEnabled(true);
                isValid = false;
            } else {
                tiProduct.setErrorEnabled(false);
                tiProduct.setError(null);
            }

            if (TextUtils.isEmpty(etProductType.getText())) {
                tiProductType.setError(getString(R.string.required_field));
                tiProductType.setErrorEnabled(true);
                isValid = false;
            } else {
                tiProductType.setErrorEnabled(false);
                tiProductType.setError(null);
            }

            if (TextUtils.isEmpty(etIca.getText())) {
                tiIca.setError(getString(R.string.required_field));
                tiIca.setErrorEnabled(true);
                isValid = false;
            } else {
                tiIca.setErrorEnabled(false);
                tiIca.setError(null);
            }

            if (TextUtils.isEmpty(etPurchaseDate.getText())) {
                tiPurchaseDate.setError(getString(R.string.required_field));
                tiPurchaseDate.setErrorEnabled(true);
                isValid = false;
            } else {
                tiPurchaseDate.setErrorEnabled(false);
                tiPurchaseDate.setError(null);
            }

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

            if (!isValid) {
                enableSubmit();
                return;
            }

            // ================= ICA VALIDATION =================
            int icaCount;
            if (isFarmEdit && existingFarmDetail != null) {
                icaCount = farmViewModel.getFarmInventoryOrdersCountForEdit(
                        etIca.getText().toString().trim(),
                        CommonUtils.getTagInt(etSupplier.getTag()),
                        existingFarmDetail.getTempFarmId()
                );

                if(icaCount <= 0) {
                    icaCount = farmViewModel.getFarmInventoryOrdersCount(etIca.getText().toString(), CommonUtils.getTagInt(etSupplier.getTag()));
                }
            } else {
                icaCount = farmViewModel.getFarmInventoryOrdersCount(etIca.getText().toString(), CommonUtils.getTagInt(etSupplier.getTag()));
            }

            if (icaCount > 0) {
                Toast.makeText(getApplicationContext(), R.string.ica_exists, Toast.LENGTH_SHORT).show();
                enableSubmit();
                return;
            }

            // ================= CREATE / UPDATE OBJECT =================
            FarmDetails farmDetail;
            String oldIca = null;
            int oldSupplierId = 0;
            long currentTimeStamp = CommonUtils.getCurrentLocalDateTimeStamp();

            if (isFarmEdit && existingFarmDetail != null) {
                oldIca = existingFarmDetail.getIca();
                oldSupplierId = existingFarmDetail.getSupplierId();
                farmDetail = existingFarmDetail; // UPDATE
            } else {
                farmDetail = new FarmDetails(); // CREATE
            }

            // ================= COMMON FIELD SET =================
            farmDetail.setSupplierId(CommonUtils.getTagInt(etSupplier.getTag()));
            farmDetail.setProductId(CommonUtils.getTagInt(etProduct.getTag()));
            farmDetail.setProductTypeId(CommonUtils.getTagInt(etProductType.getTag()));
            farmDetail.setIca(etIca.getText().toString().trim());
            farmDetail.setPurchaseDate(etPurchaseDate.getText().toString().trim());
            farmDetail.setPurchaseContract(CommonUtils.getTagInt(etPurchaseContract.getTag()));
            farmDetail.setTruckNumber(etTruckNumber.getText().toString().trim());
            farmDetail.setTruckDriverName(etTruckDriverName.getText().toString().trim());

            // ================= ID HANDLING =================
            if (isFarmEdit && existingFarmDetail != null) {
                farmDetail.setTempFarmId(existingFarmDetail.getTempFarmId());
                farmDetail.setFarmId(existingFarmDetail.getFarmId());
                farmDetail.setEdited(true);
            } else {
                farmDetail.setTempFarmId("FARM_" + currentTimeStamp);
                farmDetail.setFarmId(null);
                farmDetail.setEdited(false);
                farmDetail.setCreatedAt(System.currentTimeMillis());
            }

            farmDetail.setSynced(false);
            farmDetail.setDeleted(false);
            farmDetail.setUpdatedAt(System.currentTimeMillis());

            // ================= SAVE =================
            farmViewModel.saveFarmDetails(farmDetail, oldIca != null ? oldIca : farmDetail.getIca(),
                    oldSupplierId != 0 ? oldSupplierId : farmDetail.getSupplierId()
            );

            farmViewModel.getFarmStatus().observe(this, new Observer<>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    farmViewModel.getFarmStatus().removeObserver(this);

                    enableSubmit();

                    if (aBoolean) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("savedFarmId", farmViewModel.getFarmSavedId());
                        resultIntent.putExtra("isEdit", isFarmEdit);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        showCustomDialog(getString(R.string.error), getString(R.string.data_added_failed), false);
                    }
                }
            });
        } catch (Exception e) {
            enableSubmit();
            AppLogger.e(getClass(), "saveOrUpdateFarmDetails", e);
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

            if (farmView.isClosed) {
                dialogBody.setText(R.string.open_confirmation);
            } else {
                dialogBody.setText(R.string.close_confirmation);
            }

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnOk.setOnClickListener(v -> {
                dialog.dismiss();
                boolean closed = farmViewModel.closeFarmDetails(farmView.tempFarmId,
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
            AppLogger.e(getClass(), "performLogout", e);
        }
    }
}