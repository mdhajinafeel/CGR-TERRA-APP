package com.cgr.codrinterraerp.ui.fragments;

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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.views.ReceptionView;
import com.cgr.codrinterraerp.ui.activities.ReceptionActivity;
import com.cgr.codrinterraerp.ui.activities.ReceptionDataActivity;
import com.cgr.codrinterraerp.ui.activities.ReceptionDataCaptureActivity;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.viewmodel.ReceptionViewModel;
import com.google.android.material.button.MaterialButton;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReceptionFragment extends Fragment {

    private AppCompatTextView filterDropDown;
    private RecyclerView rvReceptionLists;
    private LinearLayout llNoData;
    private RecyclerViewAdapter<ReceptionView> receptionViewRecyclerViewAdapter;
    private ReceptionViewModel receptionViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reception, container, false);
        try {
            MaterialButton btnAddReception = view.findViewById(R.id.btnAddReception);
            rvReceptionLists = view.findViewById(R.id.rvReceptionLists);
            filterDropDown = view.findViewById(R.id.filterDropDown);
            llNoData = view.findViewById(R.id.llNoData);

            receptionViewModel = new ViewModelProvider(this).get(ReceptionViewModel.class);

            btnAddReception.setOnClickListener(v -> {

                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(requireContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                Intent intent = new Intent(requireActivity(), ReceptionActivity.class);
                intent.putExtra("isEdit", false);
                receptionResultLauncher.launch(intent, options);
            });

            // ✅ Setup RecyclerView
            rvReceptionLists.setLayoutManager(new LinearLayoutManager(getContext()));

            // ✅ Initialize adapter ONCE
            initializeAdapter();

            // ✅ Observe data (auto updates)
            receptionViewModel.getReceptionList().observe(getViewLifecycleOwner(), this::bindReceptionData);

            bindFilterOption();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void initializeAdapter() {

        receptionViewRecyclerViewAdapter = new RecyclerViewAdapter<>(getContext(), new ArrayList<>(), R.layout.row_item_warehouse_reception) {
            @Override
            public void onPostBindViewHolder(ViewHolder holder, ReceptionView receptionView) {
                if (receptionView != null) {

                    AppCompatImageButton btnViewReception = (AppCompatImageButton) holder.getView(R.id.btnViewReception);
                    AppCompatImageButton btnEditReception = (AppCompatImageButton) holder.getView(R.id.btnEditReception);

                    holder.setViewText(R.id.tvIca, receptionView.ica);
                    holder.setViewText(R.id.tvSupplier, receptionView.supplierName);
                    holder.setViewText(R.id.tvPieces, String.valueOf(receptionView.totalPieces));

                    if (receptionView.productTypeId == 1 || receptionView.productTypeId == 3) {
                        holder.setViewText(R.id.tvGrossTitle, getString(R.string.volume_pie));
                        holder.setViewText(R.id.tvGrossVolume, CommonUtils.formatNumber2(receptionView.totalVolumePie));
                    } else {
                        holder.setViewText(R.id.tvGrossTitle, getString(R.string.gross_volume));
                        holder.setViewText(R.id.tvGrossVolume, CommonUtils.formatNumber3(receptionView.totalGrossVolume));
                    }

                    holder.setViewText(R.id.tvNetVolume, CommonUtils.formatNumber3(receptionView.totalNetVolume));
                    holder.setViewText(R.id.tvDate, receptionView.receptionDate);
                    holder.setViewText(R.id.tvMeasurement, receptionView.measurementName);

                    holder.getView(R.id.btnEditReception).setTag(receptionView);
                    holder.getView(R.id.btnDeleteReception).setTag(receptionView);

                    holder.itemView.setOnClickListener(v -> {
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(requireContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                        Intent intent = new Intent(requireActivity(), ReceptionDataCaptureActivity.class);
                        intent.putExtra("ica", receptionView.ica);
                        intent.putExtra("receptionDetails", receptionView);
                        receptionDataCaptureResultLauncher.launch(intent, options);
                    });

                    holder.getView(R.id.btnDeleteReception).setOnClickListener(v -> deleteReception(receptionView));

                    holder.getView(R.id.btnReceptionData).setOnClickListener(v -> {
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(requireContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                        Intent intent = new Intent(requireActivity(), ReceptionDataActivity.class);
                        intent.putExtra("receptionDetails", receptionView);
                        receptionDataResultLauncher.launch(intent, options);
                    });

                    if (receptionView.isClosed) {
                        btnViewReception.setVisibility(View.VISIBLE);
                        btnEditReception.setVisibility(View.GONE);

                        btnViewReception.setOnClickListener(v -> showReceptionDetails(receptionView));
                    } else {
                        btnEditReception.setVisibility(View.VISIBLE);
                        btnViewReception.setVisibility(View.GONE);

                        btnEditReception.setOnClickListener(v -> {
                            ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(requireContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                            Intent intent = new Intent(requireActivity(), ReceptionActivity.class);
                            intent.putExtra("isEdit", true);
                            intent.putExtra("receptionDetails", receptionView);
                            receptionResultLauncher.launch(intent, options);
                        });
                    }
                }
            }
        };

        rvReceptionLists.setAdapter(receptionViewRecyclerViewAdapter);
    }

    // ✅ Bind data (only update adapter)
    private void bindReceptionData(List<ReceptionView> list) {
        try {
            if (list != null && !list.isEmpty()) {
                receptionViewRecyclerViewAdapter.setItems(list); // 🔥 only update data
                rvReceptionLists.setVisibility(View.VISIBLE);
                llNoData.setVisibility(View.GONE);
            } else {
                llNoData.setVisibility(View.VISIBLE);
                rvReceptionLists.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindReceptionData", e);
        }
    }

    // ✅ Activity result launcher
    private final ActivityResultLauncher<Intent> receptionResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                int savedReceptionId = (int) data.getLongExtra("savedReceptionId", 0);
                                boolean isClosed = data.getBooleanExtra("isClosed", false);
                                boolean isOpened = data.getBooleanExtra("isOpened", false);
                                boolean isReceptionEdit = data.getBooleanExtra("isEdit", false);
                                if (savedReceptionId > 0) {
                                    if(isReceptionEdit) {
                                        Toast.makeText(requireContext(), getString(R.string.reception_updated_successfully), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(requireContext(), getString(R.string.reception_added_successfully), Toast.LENGTH_SHORT).show();
                                    }
                                } else if (isClosed) {
                                    Toast.makeText(requireContext(), getString(R.string.data_closed_successfully), Toast.LENGTH_SHORT).show();
                                } else if (isOpened) {
                                    Toast.makeText(requireContext(), getString(R.string.data_opened_successfully), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> receptionDataCaptureResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            AppLogger.i(getClass(), "Data added");
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> receptionDataResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            AppLogger.i(getClass(), "Reception Data");
                        }
                    }
            );

    private void deleteReception(ReceptionView receptionView) {
        try {
            LayoutInflater dialogInflater = LayoutInflater.from(requireContext());
            View dialogView = dialogInflater.inflate(R.layout.custom_dialog_delete, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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

                int deleted = receptionViewModel.deleteReceptionDetails(receptionView.tempReceptionId, System.currentTimeMillis());

                // Switch to main thread safely
                new Handler(Looper.getMainLooper()).post(() -> {

                    // ✅ Prevent crash if fragment is not attached
                    if (!isAdded() || getContext() == null) return;

                    if (deleted > 0) {
                        Toast.makeText(getContext(), getString(R.string.data_deleted), Toast.LENGTH_SHORT).show();
                        // ✅ Refresh data
                    } else {
                        Toast.makeText(getContext(), getString(R.string.data_deleted_failed), Toast.LENGTH_SHORT).show();
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

    private void bindFilterOption() {
        try {
            filterDropDown.setOnClickListener(v -> {

                Context wrapper = new ContextThemeWrapper(requireContext(), R.style.CustomPopupMenu);
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
                        receptionViewModel.setFilter(false);
                        return true;
                    } else if (item.getItemId() == R.id.status_close) {
                        receptionViewModel.setFilter(true);
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

    private void showReceptionDetails(ReceptionView receptionView) {
        try {
            Dialog dialog = new Dialog(requireContext(), R.style.DialogTheme);
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
            dialog.setContentView(R.layout.dialog_reception_details);

            AppCompatTextView dialogTitle = dialog.findViewById(R.id.tvDialogTitle);
            AppCompatImageView closeDialog = dialog.findViewById(R.id.imgClose);
            closeDialog.setOnClickListener(v -> dialog.dismiss());
            dialogTitle.setText(getString(R.string.reception_detail));

            AppCompatTextView tvIca = dialog.findViewById(R.id.tvIca);
            AppCompatTextView tvSupplier = dialog.findViewById(R.id.tvSupplier);
            AppCompatTextView tvWood = dialog.findViewById(R.id.tvWood);
            AppCompatTextView tvWoodType = dialog.findViewById(R.id.tvWoodType);
            AppCompatTextView tvMeasurement = dialog.findViewById(R.id.tvMeasurement);
            AppCompatTextView tvPieces = dialog.findViewById(R.id.tvPieces);
            AppCompatTextView tvVolumePie = dialog.findViewById(R.id.tvVolumePie);
            AppCompatTextView tvGrossVolume = dialog.findViewById(R.id.tvGrossVolume);
            AppCompatTextView tvNetVolume = dialog.findViewById(R.id.tvNetVolume);
            AppCompatTextView tvContractCode = dialog.findViewById(R.id.tvContractCode);
            AppCompatTextView tvContractDesc = dialog.findViewById(R.id.tvContractDesc);
            AppCompatTextView tvClosedDate = dialog.findViewById(R.id.tvClosedDate);
            LinearLayout llFarmContractDetails = dialog.findViewById(R.id.llFarmContractDetails);
            LinearLayout llContractDesc = dialog.findViewById(R.id.llContractDesc);
            LinearLayout llVolumePie = dialog.findViewById(R.id.llVolumePie);
            LinearLayout llClosedDate = dialog.findViewById(R.id.llClosedDate);
            MaterialButton btnOpenReception = dialog.findViewById(R.id.btnOpenReception);

            tvIca.setText(receptionView.ica);
            tvSupplier.setText(receptionView.supplierName);
            tvWood.setText(receptionView.productName);
            tvWoodType.setText(receptionView.productTypeName);
            tvMeasurement.setText(receptionView.measurementName);
            tvPieces.setText(String.valueOf(receptionView.totalPieces));

            if (receptionView.productTypeId == 1 || receptionView.productTypeId == 3) {
                llVolumePie.setVisibility(View.VISIBLE);
                tvVolumePie.setText(CommonUtils.formatNumber2(receptionView.totalVolumePie));
            } else {
                llVolumePie.setVisibility(View.GONE);
            }

            tvGrossVolume.setText(CommonUtils.formatNumber3(receptionView.totalGrossVolume));
            tvNetVolume.setText(CommonUtils.formatNumber3(receptionView.totalNetVolume));

            if (receptionView.isFarmEnabled) {
                tvContractCode.setText(receptionView.contractCode);

                if (receptionView.description != null && !receptionView.description.isEmpty()) {
                    tvContractDesc.setText(receptionView.description);
                    llContractDesc.setVisibility(View.VISIBLE);
                } else {
                    llContractDesc.setVisibility(View.GONE);
                }
                llFarmContractDetails.setVisibility(View.VISIBLE);
            } else {
                llFarmContractDetails.setVisibility(View.GONE);
            }

            if (receptionView.isClosed) {
                llClosedDate.setVisibility(View.VISIBLE);
                btnOpenReception.setVisibility(View.VISIBLE);
                tvClosedDate.setText(CommonUtils.convertTimeStampToDate(receptionView.closedDate, "dd/MM/yyyy", requireContext()));

                btnOpenReception.setOnClickListener(view -> openConfirmation(receptionView, dialog));
            } else {
                llClosedDate.setVisibility(View.GONE);
                btnOpenReception.setVisibility(View.GONE);
            }

            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showReceptionDetails", e);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void openConfirmation(ReceptionView receptionView, Dialog receptionDialog) {
        try {
            LayoutInflater dialogInflater = LayoutInflater.from(requireContext());
            View dialogView = dialogInflater.inflate(R.layout.custom_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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
                boolean opened = receptionViewModel.closeReceptionDetails(receptionView.tempReceptionId, 0, 0, false);

                if (opened) {
                    receptionViewRecyclerViewAdapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(), getString(R.string.data_opened_successfully), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                receptionDialog.dismiss();
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "openConfirmation", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}