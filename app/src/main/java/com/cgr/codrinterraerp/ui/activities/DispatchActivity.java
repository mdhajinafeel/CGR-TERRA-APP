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
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.DispatchDetails;
import com.cgr.codrinterraerp.db.entities.ProductTypes;
import com.cgr.codrinterraerp.db.entities.Products;
import com.cgr.codrinterraerp.db.entities.ShippingLines;
import com.cgr.codrinterraerp.db.entities.Warehouses;
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

    private TextInputLayout tiContainerNumber, tiProduct, tiProductType, tiShippingLine, tiWarehouse, tiDispatchDate;
    private AppCompatEditText etContainerNumber, etProduct, etProductType, etShippingLine, etWarehouse, etDispatchDate;
    private MaterialButton btnSubmit;
    private AppCompatTextView tvNoDataFound;
    private List<Products> productsList;
    private List<ProductTypes> productTypesList;
    private List<ShippingLines> shippingLinesList;
    private List<Warehouses> warehousesList;
    private RecyclerViewAdapter<Products> productsRecyclerViewAdapter;
    private RecyclerViewAdapter<ProductTypes> productTypesRecyclerViewAdapter;
    private RecyclerViewAdapter<ShippingLines> shippingLinesRecyclerViewAdapter;
    private RecyclerViewAdapter<Warehouses> warehousesRecyclerViewAdapter;
    private MasterViewModel masterViewModel;
    private DispatchViewModel dispatchViewModel;
    private FrameLayout progressBar;

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
            etContainerNumber = findViewById(R.id.etContainerNumber);
            etProduct = findViewById(R.id.etProduct);
            etProductType = findViewById(R.id.etProductType);
            etShippingLine = findViewById(R.id.etShippingLine);
            etWarehouse = findViewById(R.id.etWarehouse);
            etDispatchDate = findViewById(R.id.etDispatchDate);
            btnSubmit = findViewById(R.id.btnSubmit);
            progressBar = findViewById(R.id.progressBar);

            masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);
            dispatchViewModel = new ViewModelProvider(this).get(DispatchViewModel.class);

            txtTitle.setText(getString(R.string.add_dispatch));
            imgBack.setOnClickListener(v -> finish());

            CommonUtils.clearErrorOnTyping(etContainerNumber, tiContainerNumber);
            CommonUtils.clearErrorOnTyping(etProduct, tiProduct);
            CommonUtils.clearErrorOnTyping(etProductType, tiProductType);
            CommonUtils.clearErrorOnTyping(etShippingLine, tiShippingLine);
            CommonUtils.clearErrorOnTyping(etWarehouse, tiWarehouse);
            CommonUtils.clearErrorOnTyping(etDispatchDate, tiDispatchDate);

            fetchData();
            actionListeners();

            dispatchViewModel.getProgressState().observe(this, aBoolean -> {
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
            productsList = new ArrayList<>();
            productsList = masterViewModel.fetchProducts();

            productTypesList = new ArrayList<>();
            productTypesList = masterViewModel.fetchProductTypes();

            shippingLinesList = new ArrayList<>();
            shippingLinesList = masterViewModel.fetchShippingLines();

            warehousesList = new ArrayList<>();
            warehousesList = masterViewModel.fetchWarehouses();
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

            etProduct.setOnClickListener(v -> showDataDialog("Product"));
            etProductType.setOnClickListener(v -> showDataDialog("ProductType"));
            etShippingLine.setOnClickListener(v -> showDataDialog("ShippingLine"));
            etWarehouse.setOnClickListener(v -> showDataDialog("Warehouse"));

            tiProduct.setEndIconOnClickListener(v -> showDataDialog("Product"));
            tiProductType.setEndIconOnClickListener(v -> showDataDialog("ProductType"));
            tiShippingLine.setEndIconOnClickListener(v -> showDataDialog("ShippingLine"));
            tiWarehouse.setEndIconOnClickListener(v -> showDataDialog("Warehouse"));

            etDispatchDate.setOnClickListener(v -> CommonUtils.showDatePicker(this, etDispatchDate));

            btnSubmit.setOnClickListener(v -> saveOrUpdateDispatchDetails());
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

            if (Objects.requireNonNull(etContainerNumber.getText()).toString().trim().isEmpty()) {
                tiContainerNumber.setError(getString(R.string.required_field));
                tiContainerNumber.setErrorEnabled(true);
                isValid = false;
            } else {
                tiContainerNumber.setErrorEnabled(false);
                tiContainerNumber.setError(null);
            }

            if (Objects.requireNonNull(etProduct.getText()).toString().trim().isEmpty()) {
                tiProduct.setError(getString(R.string.required_field));
                tiProduct.setErrorEnabled(true);
                isValid = false;
            } else {
                tiProduct.setErrorEnabled(false);
                tiProduct.setError(null);
            }

            if (Objects.requireNonNull(etProductType.getText()).toString().trim().isEmpty()) {
                tiProductType.setError(getString(R.string.required_field));
                tiProductType.setErrorEnabled(true);
                isValid = false;
            } else {
                tiProductType.setErrorEnabled(false);
                tiProductType.setError(null);
            }

            if (Objects.requireNonNull(etShippingLine.getText()).toString().trim().isEmpty()) {
                tiShippingLine.setError(getString(R.string.required_field));
                tiShippingLine.setErrorEnabled(true);
                isValid = false;
            } else {
                tiShippingLine.setErrorEnabled(false);
                tiShippingLine.setError(null);
            }

            if (Objects.requireNonNull(etWarehouse.getText()).toString().trim().isEmpty()) {
                tiWarehouse.setError(getString(R.string.required_field));
                tiWarehouse.setErrorEnabled(true);
                isValid = false;
            } else {
                tiWarehouse.setErrorEnabled(false);
                tiWarehouse.setError(null);
            }

            if (Objects.requireNonNull(etDispatchDate.getText()).toString().trim().isEmpty()) {
                tiDispatchDate.setError(getString(R.string.required_field));
                tiDispatchDate.setErrorEnabled(true);
                isValid = false;
            } else {
                tiDispatchDate.setErrorEnabled(false);
                tiDispatchDate.setError(null);
            }

            if (isValid) {

                int containerCount = dispatchViewModel.getDispatchContainersCount(etContainerNumber.getText().toString(), (int) etShippingLine.getTag());

                if (containerCount > 0) {
                    Toast.makeText(getApplicationContext(), R.string.container_exists, Toast.LENGTH_SHORT).show();
                } else {

                    DispatchDetails dispatchDetail = new DispatchDetails();
                    dispatchDetail.setContainerNumber(etContainerNumber.getText().toString().trim());
                    dispatchDetail.setTempDispatchId("D_" + CommonUtils.getCurrentLocalDateTimeStamp());
                    dispatchDetail.setDispatchId(null);
                    dispatchDetail.setProductId((int) etProduct.getTag());
                    dispatchDetail.setProductTypeId((int) etProductType.getTag());
                    dispatchDetail.setWarehouseId((int) etWarehouse.getTag());
                    dispatchDetail.setShippingLineId((int) etShippingLine.getTag());
                    dispatchDetail.setDispatchDate(etDispatchDate.getText().toString().trim());
                    dispatchDetail.setSynced(false);
                    dispatchDetail.setDeleted(false);
                    dispatchDetail.setEdited(false);
                    dispatchDetail.setUpdatedAt(System.currentTimeMillis());

                    dispatchViewModel.saveDispatchDetails(dispatchDetail);

                    dispatchViewModel.getDispatchStatus().observe(this, aBoolean -> {
                        if (aBoolean) {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("savedDispatchId", dispatchViewModel.getDispatchSavedId());
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            showCustomDialog(getString(R.string.error), getString(R.string.data_added_failed), false);
                        }
                    });
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "saveOrUpdateDispatchDetails", e);
        }
    }
}