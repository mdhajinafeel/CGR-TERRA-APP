package com.cgr.codrinterraerp.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.views.FarmView;
import com.cgr.codrinterraerp.db.views.ReceptionView;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.viewmodel.FarmViewModel;
import com.google.android.material.button.MaterialButton;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FarmListsActivity extends BaseActivity {

    private AppCompatTextView filterDropDown;
    private LinearLayout llNoData;
    private RecyclerView rvFarmsList;
    private RecyclerViewAdapter<FarmView> farmViewRecyclerViewAdapter;
    private FarmViewModel farmViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_lists);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            AppCompatImageView imgBack = findViewById(R.id.imgBack);
            AppCompatTextView txtTitle = findViewById(R.id.txtTitle);
            filterDropDown = findViewById(R.id.filterDropDown);
            MaterialButton btnAddFarm = findViewById(R.id.btnAddFarm);
            rvFarmsList = findViewById(R.id.rvFarmsList);
            llNoData = findViewById(R.id.llNoData);

            txtTitle.setText(getString(R.string.finca));
            imgBack.setOnClickListener(view -> finish());

            farmViewModel = new ViewModelProvider(this).get(FarmViewModel.class);

            rvFarmsList.setLayoutManager(new LinearLayoutManager(this));
            initializeAdapter();

            farmViewModel.getFarmList().observe(this, this::bindFarmData);

            btnAddFarm.setOnClickListener(v -> {

                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                Intent intent = new Intent(this, FarmActivity.class);
                intent.putExtra("isEdit", false);
                farmResultLauncher.launch(intent, options);
            });

            bindFilterOptions();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void initializeAdapter() {

        farmViewRecyclerViewAdapter = new RecyclerViewAdapter<>(getApplicationContext(), new ArrayList<>(), R.layout.row_item_farm) {
            @Override
            public void onPostBindViewHolder(ViewHolder holder, FarmView farmView) {
                if (farmView != null) {

                    AppCompatImageButton btnViewFarm = (AppCompatImageButton) holder.getView(R.id.btnViewFarm);
                    AppCompatImageButton btnEditFarm = (AppCompatImageButton) holder.getView(R.id.btnEditFarm);

                    holder.setViewText(R.id.tvIca, farmView.ica);
                    holder.setViewText(R.id.tvSupplier, farmView.supplierName);
                    holder.setViewText(R.id.tvPieces, String.valueOf(farmView.totalPieces));

                    if (farmView.productTypeId == 1 || farmView.productTypeId == 3) {
                        holder.setViewText(R.id.tvGrossTitle, getString(R.string.volume_pie));
                        holder.setViewText(R.id.tvGrossVolume, CommonUtils.formatNumber2(farmView.totalVolumePie));
                    } else {
                        holder.setViewText(R.id.tvGrossTitle, getString(R.string.gross_volume));
                        holder.setViewText(R.id.tvGrossVolume, CommonUtils.formatNumber3(farmView.totalGrossVolume));
                    }

                    holder.setViewText(R.id.tvNetVolume, CommonUtils.formatNumber3(farmView.totalNetVolume));
                    holder.setViewText(R.id.tvDate, farmView.purchaseDate);
                    holder.setViewText(R.id.tvContract, farmView.contractCode);

                    holder.getView(R.id.btnEditFarm).setTag(farmView);
                    holder.getView(R.id.btnDeleteFarm).setTag(farmView);

                    holder.itemView.setOnClickListener(v -> {
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                        Intent intent = new Intent(FarmListsActivity.this, ReceptionDataCaptureActivity.class);
                        intent.putExtra("ica", farmView.ica);
                        intent.putExtra("farmDetails", farmView);
                        farmDataCaptureResultLauncher.launch(intent, options);
                    });

                    //holder.getView(R.id.btnDeleteReception).setOnClickListener(v -> deleteReception(farmView));

//                    holder.getView(R.id.btnReceptionData).setOnClickListener(v -> {
//                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
//                        Intent intent = new Intent(FarmListsActivity.this, ReceptionDataActivity.class);
//                        intent.putExtra("farmDetails", farmView);
//                        farmDataResultLauncher.launch(intent, options);
//                    });

                    if (farmView.isClosed) {
                        btnViewFarm.setVisibility(View.VISIBLE);
                        btnEditFarm.setVisibility(View.GONE);

                        //btnViewFarm.setOnClickListener(v -> showReceptionDetails(farmView));
                    } else {
                        btnEditFarm.setVisibility(View.VISIBLE);
                        btnViewFarm.setVisibility(View.GONE);

                        btnEditFarm.setOnClickListener(v -> {
                            ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                            Intent intent = new Intent(FarmListsActivity.this, ReceptionActivity.class);
                            intent.putExtra("isEdit", true);
                            intent.putExtra("farmDetails", farmView);
                            farmResultLauncher.launch(intent, options);
                        });
                    }
                }
            }
        };

        rvFarmsList.setAdapter(farmViewRecyclerViewAdapter);
    }

    private void bindFarmData(List<FarmView> list) {
        try {
            if (list != null && !list.isEmpty()) {
                farmViewRecyclerViewAdapter.setItems(list); // 🔥 only update data
                rvFarmsList.setVisibility(View.VISIBLE);
                llNoData.setVisibility(View.GONE);
            } else {
                llNoData.setVisibility(View.VISIBLE);
                rvFarmsList.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindReceptionData", e);
        }
    }

    private void bindFilterOptions() {
        try {
            filterDropDown.setOnClickListener(v -> {

                Context wrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);
                PopupMenu popupMenu = new PopupMenu(wrapper, filterDropDown);
                popupMenu.getMenuInflater().inflate(R.menu.menu_status, popupMenu.getMenu());

                // FORCE SHOW ICONS
                try {

                    Field field = popupMenu.getClass().getDeclaredField("mPopup");
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(Objects.requireNonNull(menuPopupHelper).getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);

                } catch (Exception e) {
                    AppLogger.e(getClass(), "bindFilterOptions", e);
                }

                popupMenu.setOnMenuItemClickListener(item -> {

                    String selected = Objects.requireNonNull(item.getTitle()).toString();
                    filterDropDown.setText(selected);

                    if (item.getItemId() == R.id.status_open) {
                        farmViewModel.setFilter(false);
                        return true;
                    } else if (item.getItemId() == R.id.status_close) {
                        farmViewModel.setFilter(true);
                        return true;
                    }

                    return false;
                });

                popupMenu.show();
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindFilterOptions", e);
        }
    }

    private final ActivityResultLauncher<Intent> farmResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                int savedFarmId = (int) data.getLongExtra("savedFarmId", 0);
                                boolean isClosed = data.getBooleanExtra("isClosed", false);
                                boolean isOpened = data.getBooleanExtra("isOpened", false);
                                if (savedFarmId > 0) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.farm_added_successfully), Toast.LENGTH_SHORT).show();
                                } else if (isClosed) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.farm_closed_successfully), Toast.LENGTH_SHORT).show();
                                } else if (isOpened) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.farm_opened_successfully), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> farmDataResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Toast.makeText(getApplicationContext(), getString(R.string.data_updated_successfully), Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> farmDataCaptureResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            AppLogger.i(getClass(), "Data added");
                        }
                    }
            );

    private void deleteFarm(ReceptionView receptionView) {
        try {
            LayoutInflater dialogInflater = LayoutInflater.from(this);
            View dialogView = dialogInflater.inflate(R.layout.custom_dialog_delete, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            AppCompatTextView dialogHeader = dialogView.findViewById(R.id.dialogHeader);
            AppCompatTextView dialogBody = dialogView.findViewById(R.id.dialogBody);
            MaterialButton btnDelete = dialogView.findViewById(R.id.btnDelete);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

            dialogHeader.setText(R.string.confirmation);
            dialogBody.setText(R.string.delete_confirmation);

            btnCancel.setOnClickListener(v -> dialog.dismiss());

//            btnDelete.setOnClickListener(v -> new Thread(() -> {
//
//                int deleted = receptionViewModel.deleteReceptionDetails(receptionView.tempReceptionId, System.currentTimeMillis());
//
//                // Switch to main thread safely
//                new Handler(Looper.getMainLooper()).post(() -> {
//
//                    // ✅ Prevent crash if fragment is not attached
//                    if (!isAdded() || getContext() == null) return;
//
//                    if (deleted > 0) {
//                        Toast.makeText(getContext(), getString(R.string.data_deleted), Toast.LENGTH_SHORT).show();
//                        // ✅ Refresh data
//                    } else {
//                        Toast.makeText(getContext(), getString(R.string.data_deleted_failed), Toast.LENGTH_SHORT).show();
//                    }
//
//                    // ✅ Safe dialog dismiss
//                    if (dialog.isShowing()) {
//                        dialog.dismiss();
//                    }
//                });
//
//            }).start());

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteReception", e);
        }
    }
}