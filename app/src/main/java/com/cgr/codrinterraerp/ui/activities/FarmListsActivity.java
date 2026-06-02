package com.cgr.codrinterraerp.ui.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.views.FarmView;
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
        statusBarSetting(false);
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

                    AppCompatImageView btnViewFarm = holder.getView(R.id.btnViewFarm);
                    AppCompatImageView btnEditFarm = holder.getView(R.id.btnEditFarm);

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
                    holder.setViewText(R.id.tvPlateNumber, farmView.truckNumber);

                    holder.getView(R.id.btnEditFarm).setTag(farmView);
                    holder.getView(R.id.btnDeleteFarm).setTag(farmView);

                    holder.itemView.setOnClickListener(v -> {
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                        Intent intent = new Intent(FarmListsActivity.this, FarmDataCaptureActivity.class);
                        intent.putExtra("ica", farmView.ica);
                        intent.putExtra("farmDetails", farmView);
                        farmDataCaptureResultLauncher.launch(intent, options);
                    });

                    holder.getView(R.id.btnDeleteFarm).setOnClickListener(v -> deleteFarm(farmView));

                    holder.getView(R.id.btnFarmData).setOnClickListener(v -> {
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                        Intent intent = new Intent(FarmListsActivity.this, FarmDataActivity.class);
                        intent.putExtra("farmDetails", farmView);
                        farmDataResultLauncher.launch(intent, options);
                    });

                    if (farmView.isClosed) {
                        btnViewFarm.setVisibility(View.VISIBLE);
                        btnEditFarm.setVisibility(View.GONE);

                        btnViewFarm.setOnClickListener(v -> showFarmDetails(farmView));
                    } else {
                        btnEditFarm.setVisibility(View.VISIBLE);
                        btnViewFarm.setVisibility(View.GONE);

                        btnEditFarm.setOnClickListener(v -> {
                            ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                            Intent intent = new Intent(FarmListsActivity.this, FarmActivity.class);
                            intent.putExtra("isEdit", true);
                            intent.putExtra("farmDetails", farmView);
                            farmResultLauncher.launch(intent, options);
                        });
                    }
                }
            }
        };

        rvFarmsList.setAdapter(farmViewRecyclerViewAdapter);
        rvFarmsList.setHasFixedSize(true);
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
            AppLogger.e(getClass(), "bindFarmData", e);
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
                                boolean isFarmEdit = data.getBooleanExtra("isEdit", false);
                                if (savedFarmId > 0) {
                                    if(isFarmEdit) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.farm_updated_successfully), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), getString(R.string.farm_added_successfully), Toast.LENGTH_SHORT).show();
                                    }
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
                            AppLogger.i(getClass(), "Farm Data");
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

    private void showFarmDetails(FarmView farmView) {
        try {
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
            dialog.setContentView(R.layout.dialog_farm_details);

            AppCompatTextView dialogTitle = dialog.findViewById(R.id.tvDialogTitle);
            AppCompatImageView closeDialog = dialog.findViewById(R.id.imgClose);
            closeDialog.setOnClickListener(v -> dialog.dismiss());
            dialogTitle.setText(getString(R.string.farm_detail));

            AppCompatTextView tvIca = dialog.findViewById(R.id.tvIca);
            AppCompatTextView tvSupplier = dialog.findViewById(R.id.tvSupplier);
            AppCompatTextView tvWood = dialog.findViewById(R.id.tvWood);
            AppCompatTextView tvWoodType = dialog.findViewById(R.id.tvWoodType);
            AppCompatTextView tvPieces = dialog.findViewById(R.id.tvPieces);
            AppCompatTextView tvTruckPlateNumber = dialog.findViewById(R.id.tvTruckPlateNumber);
            AppCompatTextView tvDriverName = dialog.findViewById(R.id.tvDriverName);
            AppCompatTextView tvVolumePie = dialog.findViewById(R.id.tvVolumePie);
            AppCompatTextView tvGrossVolume = dialog.findViewById(R.id.tvGrossVolume);
            AppCompatTextView tvNetVolume = dialog.findViewById(R.id.tvNetVolume);
            AppCompatTextView tvContractCode = dialog.findViewById(R.id.tvContractCode);
            AppCompatTextView tvContractDesc = dialog.findViewById(R.id.tvContractDesc);
            AppCompatTextView tvClosedDate = dialog.findViewById(R.id.tvClosedDate);
            LinearLayout llVolumePie = dialog.findViewById(R.id.llVolumePie);
            LinearLayout llClosedDate = dialog.findViewById(R.id.llClosedDate);
            LinearLayout llContractDesc = dialog.findViewById(R.id.llContractDesc);
            MaterialButton btnOpenFarm = dialog.findViewById(R.id.btnOpenFarm);

            tvIca.setText(farmView.ica);
            tvSupplier.setText(farmView.supplierName);
            tvWood.setText(farmView.productName);
            tvWoodType.setText(farmView.productTypeName);
            tvPieces.setText(String.valueOf(farmView.totalPieces));
            tvTruckPlateNumber.setText(String.valueOf(farmView.truckNumber));
            tvDriverName.setText(String.valueOf(farmView.truckDriverName));
            tvPieces.setText(String.valueOf(farmView.totalPieces));
            tvContractCode.setText(farmView.contractCode);

            if (farmView.productTypeId == 1 || farmView.productTypeId == 3) {
                llVolumePie.setVisibility(View.VISIBLE);
                tvVolumePie.setText(CommonUtils.formatNumber2(farmView.totalVolumePie));
            } else {
                llVolumePie.setVisibility(View.GONE);
            }

            tvGrossVolume.setText(CommonUtils.formatNumber3(farmView.totalGrossVolume));
            tvNetVolume.setText(CommonUtils.formatNumber3(farmView.totalNetVolume));

            if (farmView.description != null && !farmView.description.isEmpty()) {
                llContractDesc.setVisibility(View.VISIBLE);
                tvContractDesc.setText(farmView.description);
            } else {
                llContractDesc.setVisibility(View.GONE);
            }

            if (farmView.isClosed) {
                llClosedDate.setVisibility(View.VISIBLE);
                btnOpenFarm.setVisibility(View.VISIBLE);
                tvClosedDate.setText(CommonUtils.convertTimeStampToDate(farmView.closedDate, "dd/MM/yyyy", getApplicationContext()));

                btnOpenFarm.setOnClickListener(view -> openConfirmation(farmView, dialog));
            } else {
                llClosedDate.setVisibility(View.GONE);
                btnOpenFarm.setVisibility(View.GONE);
            }

            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showReceptionDetails", e);
        }
    }

    private void deleteFarm(FarmView farmView) {
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

            btnDelete.setOnClickListener(v -> new Thread(() -> {

                int deleted = farmViewModel.deleteFarmDetails(farmView.tempFarmId, System.currentTimeMillis());

                // Switch to main thread safely
                new Handler(Looper.getMainLooper()).post(() -> {

                    if (deleted > 0) {
                        Toast.makeText(getApplicationContext(), getString(R.string.data_deleted), Toast.LENGTH_SHORT).show();
                        // ✅ Refresh data
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.data_deleted_failed), Toast.LENGTH_SHORT).show();
                    }

                    // ✅ Safe dialog dismiss
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                });

            }).start());

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteReception", e);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void openConfirmation(FarmView farmView, Dialog farmDialog) {
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

            dialogBody.setText(R.string.open_confirmation);

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnOk.setOnClickListener(v -> {
                boolean opened = farmViewModel.closeFarmDetails(farmView.tempFarmId, 0, 0, false);

                if (opened) {
                    farmViewRecyclerViewAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), getString(R.string.farm_opened_successfully), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                farmDialog.dismiss();
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "openConfirmation", e);
        }
    }
}