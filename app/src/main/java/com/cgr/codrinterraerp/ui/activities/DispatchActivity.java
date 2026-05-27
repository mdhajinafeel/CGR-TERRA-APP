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
import com.cgr.codrinterraerp.db.entities.ContainerCategories;
import com.cgr.codrinterraerp.db.entities.DispatchDetails;
import com.cgr.codrinterraerp.db.entities.ProductTypes;
import com.cgr.codrinterraerp.db.entities.Products;
import com.cgr.codrinterraerp.db.entities.ShippingLines;
import com.cgr.codrinterraerp.db.entities.Warehouses;
import com.cgr.codrinterraerp.db.views.DispatchView;
import com.cgr.codrinterraerp.helper.PreferenceManager;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.utils.DividerItemDecoration;
import com.cgr.codrinterraerp.viewmodel.DispatchViewModel;
import com.cgr.codrinterraerp.viewmodel.MasterViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DispatchActivity extends BaseActivity {

    private TextInputLayout tiContainerNumber, tiProduct, tiProductType, tiShippingLine, tiWarehouse, tiDispatchDate, tiCategory;
    private AppCompatEditText etContainerNumber, etProduct, etProductType, etShippingLine, etWarehouse, etDispatchDate, etCategory;
    private MaterialButton btnSubmit, btnCloseDispatch, btnOpenDispatch;
    private AppCompatTextView tvNoDataFound;
    private List<Products> productsList;
    private List<ProductTypes> productTypesList;
    private List<ShippingLines> shippingLinesList;
    private List<Warehouses> warehousesList;
    private List<ContainerCategories> containerCategoriesList;
    private RecyclerViewAdapter<Products> productsRecyclerViewAdapter;
    private RecyclerViewAdapter<ProductTypes> productTypesRecyclerViewAdapter;
    private RecyclerViewAdapter<ShippingLines> shippingLinesRecyclerViewAdapter;
    private RecyclerViewAdapter<Warehouses> warehousesRecyclerViewAdapter;
    private RecyclerViewAdapter<ContainerCategories> containerCategoriesRecyclerViewAdapter;
    private MasterViewModel masterViewModel;
    private DispatchViewModel dispatchViewModel;
    private FrameLayout progressBar;
    private boolean isDispatchEdit = false;
    private DispatchDetails existingDispatchDetail;
    private DispatchView dispatchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatch);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            AppCompatImageView imgBack = findViewById(R.id.imgBack);
            AppCompatTextView txtTitle = findViewById(R.id.txtTitle);

            tiContainerNumber = findViewById(R.id.tiContainerNumber);
            tiProduct = findViewById(R.id.tiProduct);
            tiProductType = findViewById(R.id.tiProductType);
            tiShippingLine = findViewById(R.id.tiShippingLine);
            tiWarehouse = findViewById(R.id.tiWarehouse);
            tiDispatchDate = findViewById(R.id.tiDispatchDate);
            tiCategory = findViewById(R.id.tiCategory);
            etContainerNumber = findViewById(R.id.etContainerNumber);
            etProduct = findViewById(R.id.etProduct);
            etProductType = findViewById(R.id.etProductType);
            etShippingLine = findViewById(R.id.etShippingLine);
            etWarehouse = findViewById(R.id.etWarehouse);
            etDispatchDate = findViewById(R.id.etDispatchDate);
            etCategory = findViewById(R.id.etCategory);
            btnSubmit = findViewById(R.id.btnSubmit);
            progressBar = findViewById(R.id.progressBar);
            btnCloseDispatch = findViewById(R.id.btnCloseDispatch);
            btnOpenDispatch = findViewById(R.id.btnOpenDispatch);

            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);
                dispatchViewModel = new ViewModelProvider(this).get(DispatchViewModel.class);

                isDispatchEdit = bundle.getBoolean("isEdit");

                txtTitle.setText(isDispatchEdit ? getString(R.string.edit_dispatch) : getString(R.string.add_dispatch));
                imgBack.setOnClickListener(v -> finish());

                CommonUtils.clearErrorOnTyping(etContainerNumber, tiContainerNumber);
                CommonUtils.clearErrorOnTyping(etProduct, tiProduct);
                CommonUtils.clearErrorOnTyping(etProductType, tiProductType);
                CommonUtils.clearErrorOnTyping(etShippingLine, tiShippingLine);
                CommonUtils.clearErrorOnTyping(etWarehouse, tiWarehouse);
                CommonUtils.clearErrorOnTyping(etDispatchDate, tiDispatchDate);
                CommonUtils.clearErrorOnTyping(etCategory, tiCategory);

                actionListeners();

                dispatchViewModel.getProgressState().observe(this, aBoolean -> {
                    if (aBoolean) {
                        showProgress(progressBar);
                    } else {
                        hideProgress(progressBar);
                    }
                });

                if (isDispatchEdit) {
                    dispatchView = (DispatchView) bundle.getSerializable("dispatchDetails");

                    if (dispatchView != null) {
                        existingDispatchDetail = dispatchViewModel.fetchDispatchDetailById(dispatchView.tempDispatchId);
                        fetchData(true, existingDispatchDetail);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.common_error), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    fetchData(false, null);
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.common_error), Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void fetchData(boolean isEdit, DispatchDetails dispatchDetail) {
        try {
            productsList = masterViewModel.fetchProducts();
            productTypesList = masterViewModel.fetchProductTypes();
            shippingLinesList = masterViewModel.fetchShippingLines();
            warehousesList = masterViewModel.fetchWarehouses();

            if (isEdit) {

                if (dispatchView.totalPieces > 0) {

                    if (dispatchView.isClosed) {
                        btnOpenDispatch.setVisibility(View.VISIBLE);
                        btnCloseDispatch.setVisibility(View.GONE);
                    } else {
                        btnCloseDispatch.setVisibility(View.VISIBLE);
                        btnOpenDispatch.setVisibility(View.GONE);
                    }

                    int colorLightGrey = ContextCompat.getColor(this, R.color.colorLightGrey);

                    tiProduct.setEnabled(false);
                    tiProductType.setEnabled(false);
                    tiCategory.setEnabled(false);

                    tiProduct.setBoxBackgroundColor(colorLightGrey);
                    tiProductType.setBoxBackgroundColor(colorLightGrey);
                    tiCategory.setBoxBackgroundColor(colorLightGrey);

                    tiProduct.setAlpha(0.9f);
                    tiProductType.setAlpha(0.9f);
                    tiCategory.setAlpha(0.9f);
                }

                // Wood
                for (Products p : productsList) {
                    if (p.getProductId() == dispatchDetail.getProductId()) {
                        etProduct.setText(p.getProductName());
                        etProduct.setTag(p.getProductId());
                        break;
                    }
                }

                // Wood Type
                for (ProductTypes pt : productTypesList) {
                    if (pt.getTypeId() == dispatchDetail.getProductTypeId()) {
                        etProductType.setText(pt.getProductTypeName());
                        etProductType.setTag(pt.getTypeId());
                        break;
                    }
                }

                // Load dependent data (IMPORTANT)
                containerCategoriesList = masterViewModel.fetchContainerCategories(dispatchDetail.getProductTypeId());

                // Category
                for (ContainerCategories cc : containerCategoriesList) {
                    if (cc.getId() == dispatchDetail.getCategoryId()) {
                        etCategory.setText(cc.getCategory());
                        etCategory.setTag(cc.getId());
                        break;
                    }
                }

                // Shipping Line
                for (ShippingLines sl : shippingLinesList) {
                    if (sl.getId() == dispatchDetail.getShippingLineId()) {
                        etShippingLine.setText(sl.getShippingLine());
                        etShippingLine.setTag(sl.getId());
                        break;
                    }
                }

                // Warehouse
                for (Warehouses w : warehousesList) {
                    if (w.getId() == dispatchDetail.getWarehouseId()) {
                        etWarehouse.setText(w.getWarehouseName());
                        etWarehouse.setTag(w.getId());
                        break;
                    }
                }

                // Container # + Date
                etContainerNumber.setText(dispatchDetail.getContainerNumber());
                etDispatchDate.setText(dispatchDetail.getDispatchDate());
            } else {
                btnCloseDispatch.setVisibility(View.GONE);
                btnOpenDispatch.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchData", e);
        }
    }

    private void actionListeners() {
        try {
            etProduct.setKeyListener(null);
            etProductType.setKeyListener(null);
            etShippingLine.setKeyListener(null);
            etWarehouse.setKeyListener(null);
            etCategory.setKeyListener(null);

            etProduct.setOnClickListener(v -> showDataDialog("Product"));
            etProductType.setOnClickListener(v -> showDataDialog("ProductType"));
            etShippingLine.setOnClickListener(v -> showDataDialog("ShippingLine"));
            etWarehouse.setOnClickListener(v -> showDataDialog("Warehouse"));
            etCategory.setOnClickListener(v -> showDataDialog("Category"));

            tiProduct.setEndIconOnClickListener(v -> showDataDialog("Product"));
            tiProductType.setEndIconOnClickListener(v -> showDataDialog("ProductType"));
            tiShippingLine.setEndIconOnClickListener(v -> showDataDialog("ShippingLine"));
            tiWarehouse.setEndIconOnClickListener(v -> showDataDialog("Warehouse"));
            tiCategory.setOnClickListener(v -> showDataDialog("Category"));

            etDispatchDate.setOnClickListener(v -> CommonUtils.showDatePicker(this, etDispatchDate));

            btnSubmit.setOnClickListener(v -> {
                btnSubmit.setEnabled(false);
                saveOrUpdateDispatchDetails();
            });

            btnCloseDispatch.setOnClickListener(v -> closeConfirmation(true));

            btnOpenDispatch.setOnClickListener(v -> closeConfirmation(false));
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

            if (tag.equalsIgnoreCase("Product")) {

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

                    etCategory.setText("");
                    etCategory.setTag(null);

                    containerCategoriesList = masterViewModel.fetchContainerCategories(selected.getTypeId());

                    dialog.dismiss(); // optional
                });
            } else if (tag.equalsIgnoreCase("Warehouse")) {

                dialogTitle.setText(R.string.select_warehouse);

                if (warehousesList.isEmpty()) {
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

                    dialog.dismiss(); // optional
                });
            } else if (tag.equalsIgnoreCase("ShippingLine")) {

                dialogTitle.setText(R.string.select_shipping_line);

                if (shippingLinesList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);
                }

                shippingLinesRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(shippingLinesList), R.layout.row_dialog_list) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, ShippingLines shippingLines) {

                        AppCompatTextView tvName = holder.itemView.findViewById(R.id.tvName);
                        AppCompatImageView ivSelected = holder.itemView.findViewById(R.id.ivItemSelected);

                        tvName.setText(shippingLines.getShippingLine());

                        boolean isSelected = false;
                        if (etShippingLine.getTag() != null) {
                            isSelected = Objects.equals(shippingLines.getId(), etShippingLine.getTag());
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

                rvList.setAdapter(shippingLinesRecyclerViewAdapter);
                shippingLinesRecyclerViewAdapter.setOnItemClickListener((view, position) -> {

                    ShippingLines selected = shippingLinesRecyclerViewAdapter.getItem(position);

                    etShippingLine.setText(selected.getShippingLine());
                    etShippingLine.setTag(selected.getId());

                    dialog.dismiss(); // optional
                });
            } else if (tag.equalsIgnoreCase("Category")) {

                dialogTitle.setText(R.string.select_warehouse);

                if (containerCategoriesList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);
                }

                containerCategoriesRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(containerCategoriesList), R.layout.row_dialog_list) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, ContainerCategories containerCategory) {

                        AppCompatTextView tvName = holder.itemView.findViewById(R.id.tvName);
                        AppCompatImageView ivSelected = holder.itemView.findViewById(R.id.ivItemSelected);

                        tvName.setText(containerCategory.getCategory());

                        boolean isSelected = false;
                        if (etCategory.getTag() != null) {
                            isSelected = Objects.equals(containerCategory.getId(), etCategory.getTag());
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

                rvList.setAdapter(containerCategoriesRecyclerViewAdapter);
                containerCategoriesRecyclerViewAdapter.setOnItemClickListener((view, position) -> {

                    ContainerCategories selected = containerCategoriesRecyclerViewAdapter.getItem(position);

                    etCategory.setText(selected.getCategory());
                    etCategory.setTag(selected.getId());

                    dialog.dismiss(); // optional
                });
            }

            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    String query = s.toString().trim().toLowerCase();

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
                    } else if (tag.equalsIgnoreCase("ShippingLine")) {
                        if (query.isEmpty()) {
                            shippingLinesRecyclerViewAdapter.resetFilter();
                        } else {
                            shippingLinesRecyclerViewAdapter.filter(item ->
                                    item.getShippingLine() != null &&
                                            item.getShippingLine().toLowerCase().contains(query)
                            );
                        }

                        // Optional: Show "No Data Found"
                        if (shippingLinesRecyclerViewAdapter.getItemCount() == 0) {
                            tvNoDataFound.setVisibility(View.VISIBLE);
                        } else {
                            tvNoDataFound.setVisibility(View.GONE);
                        }
                    } else if (tag.equalsIgnoreCase("Category")) {
                        if (query.isEmpty()) {
                            containerCategoriesRecyclerViewAdapter.resetFilter();
                        } else {
                            containerCategoriesRecyclerViewAdapter.filter(item ->
                                    item.getCategory() != null &&
                                            item.getCategory().toLowerCase().contains(query)
                            );
                        }

                        // Optional: Show "No Data Found"
                        if (containerCategoriesRecyclerViewAdapter.getItemCount() == 0) {
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

    private void saveOrUpdateDispatchDetails() {
        try {
            hideKeyboard(this);

            boolean isValid = true;

            if (TextUtils.isEmpty(etContainerNumber.getText())) {
                tiContainerNumber.setError(getString(R.string.required_field));
                tiContainerNumber.setErrorEnabled(true);
                isValid = false;
            } else {
                tiContainerNumber.setErrorEnabled(false);
                tiContainerNumber.setError(null);
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

            if (TextUtils.isEmpty(etCategory.getText())) {
                tiCategory.setError(getString(R.string.required_field));
                tiCategory.setErrorEnabled(true);
                isValid = false;
            } else {
                tiCategory.setErrorEnabled(false);
                tiCategory.setError(null);
            }

            if (TextUtils.isEmpty(etShippingLine.getText())) {
                tiShippingLine.setError(getString(R.string.required_field));
                tiShippingLine.setErrorEnabled(true);
                isValid = false;
            } else {
                tiShippingLine.setErrorEnabled(false);
                tiShippingLine.setError(null);
            }

            if (TextUtils.isEmpty(etWarehouse.getText())) {
                tiWarehouse.setError(getString(R.string.required_field));
                tiWarehouse.setErrorEnabled(true);
                isValid = false;
            } else {
                tiWarehouse.setErrorEnabled(false);
                tiWarehouse.setError(null);
            }

            if (TextUtils.isEmpty(etDispatchDate.getText())) {
                tiDispatchDate.setError(getString(R.string.required_field));
                tiDispatchDate.setErrorEnabled(true);
                isValid = false;
            } else {
                tiDispatchDate.setErrorEnabled(false);
                tiDispatchDate.setError(null);
            }

            if (!isValid) {
                enableSubmit();
                return;
            }

            // ================= CONTAINER VALIDATION =================
            int containerCount;
            if (isDispatchEdit && existingDispatchDetail != null) {
                containerCount = dispatchViewModel.getDispatchContainersCountForEdit(
                        etContainerNumber.getText().toString().trim(),
                        CommonUtils.getTagInt(etShippingLine.getTag()),
                        existingDispatchDetail.getTempDispatchId()
                );

                if(containerCount <= 0) {
                    containerCount = dispatchViewModel.getDispatchContainersCount(etContainerNumber.getText().toString(), CommonUtils.getTagInt(etShippingLine.getTag()));
                }
            } else {
                containerCount = dispatchViewModel.getDispatchContainersCount(etContainerNumber.getText().toString(), CommonUtils.getTagInt(etShippingLine.getTag()));
            }

            if (containerCount > 0) {
                Toast.makeText(getApplicationContext(), R.string.container_exists, Toast.LENGTH_SHORT).show();
                enableSubmit();
                return;
            }

            // ================= CREATE / UPDATE OBJECT =================
            DispatchDetails dispatchDetail;
            String oldContainerNumber = null;
            int oldShippingLineId = 0;
            long currentTimeStamp = CommonUtils.getCurrentLocalDateTimeStamp();

            if (isDispatchEdit && existingDispatchDetail != null) {
                oldContainerNumber = existingDispatchDetail.getContainerNumber();
                oldShippingLineId = existingDispatchDetail.getShippingLineId();
                dispatchDetail = existingDispatchDetail; // UPDATE
            } else {
                dispatchDetail = new DispatchDetails(); // CREATE
            }

            // ================= COMMON FIELD SET =================
            dispatchDetail.setContainerNumber(etContainerNumber.getText().toString().trim());
            dispatchDetail.setProductId(CommonUtils.getTagInt(etProduct.getTag()));
            dispatchDetail.setProductTypeId(CommonUtils.getTagInt(etProductType.getTag()));
            dispatchDetail.setWarehouseId(CommonUtils.getTagInt(etWarehouse.getTag()));
            dispatchDetail.setShippingLineId(CommonUtils.getTagInt(etShippingLine.getTag()));
            dispatchDetail.setCategoryId(CommonUtils.getTagInt(etCategory.getTag()));
            dispatchDetail.setDispatchDate(etDispatchDate.getText().toString().trim());

            // ================= ID HANDLING =================
            if (isDispatchEdit && existingDispatchDetail != null) {
                dispatchDetail.setTempDispatchId(existingDispatchDetail.getTempDispatchId());
                dispatchDetail.setDispatchId(existingDispatchDetail.getDispatchId());
                dispatchDetail.setEdited(true);
                dispatchDetail.setCreatedAt(System.currentTimeMillis());
            } else {
                dispatchDetail.setTempDispatchId("DISP_" + currentTimeStamp);
                dispatchDetail.setDispatchId(null);
                dispatchDetail.setEdited(false);
            }

            dispatchDetail.setSynced(false);
            dispatchDetail.setDeleted(false);
            dispatchDetail.setUpdatedAt(System.currentTimeMillis());

            // ================= SAVE =================

            dispatchViewModel.saveDispatchDetails(dispatchDetail, oldContainerNumber != null ? oldContainerNumber : dispatchDetail.getContainerNumber(),
                    oldShippingLineId != 0 ? oldShippingLineId : dispatchDetail.getShippingLineId()
            );

            dispatchViewModel.getDispatchStatus().observe(this, new Observer<>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    dispatchViewModel.getDispatchStatus().removeObserver(this);

                    enableSubmit();

                    if (aBoolean) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("savedDispatchId", dispatchViewModel.getDispatchSavedId());
                        resultIntent.putExtra("isEdit", isDispatchEdit);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        showCustomDialog(getString(R.string.error), getString(R.string.data_added_failed), false);
                    }
                }
            });

        } catch (Exception e) {
            enableSubmit();
            AppLogger.e(getClass(), "saveOrUpdateDispatchDetails", e);
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

            if(dispatchView.isClosed) {
                dialogBody.setText(R.string.open_confirmation);
            } else {
                dialogBody.setText(R.string.close_confirmation);
            }

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnOk.setOnClickListener(v -> {
                dialog.dismiss();
                boolean closed = dispatchViewModel.closeDispatchDetails(dispatchView.tempDispatchId,
                        CommonUtils.getCurrentLocalDateTimeStamp(), PreferenceManager.INSTANCE.getUserId(), isClose);

                if(closed) {
                    Intent resultIntent = new Intent();
                    if(isClose) {
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