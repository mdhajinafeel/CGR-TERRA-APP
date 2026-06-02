package com.cgr.codrinterraerp.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.views.FarmView;
import com.cgr.codrinterraerp.model.FarmCapturedData;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.viewmodel.FarmDataViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FarmDataActivity extends BaseActivity {

    private AppCompatTextView tvIca, tvSupplier, tvPieces, tvGrossVolume, tvNetVolume, tvVolumePie;
    private MaterialCardView cardFarm;
    private RecyclerView rvFarmData;
    private FarmDataViewModel farmDataViewModel;
    private RecyclerViewAdapter<FarmCapturedData> farmCapturedDataRecyclerViewAdapter;
    private LinearLayout llDataSquareLogs, llDataRoundLogs, llVolumePie, llNoData;
    private final List<FarmCapturedData> farmCapturedDataList = new ArrayList<>();
    private FarmView farmView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_data);
        statusBarSetting(false);
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            AppCompatImageView imgBack = findViewById(R.id.imgBack);
            AppCompatTextView txtTitle = findViewById(R.id.txtTitle);
            AppCompatTextView txtSubTitle = findViewById(R.id.txtSubTitle);
            tvIca = findViewById(R.id.tvIca);
            tvSupplier = findViewById(R.id.tvSupplier);
            tvPieces = findViewById(R.id.tvPieces);
            tvGrossVolume = findViewById(R.id.tvGrossVolume);
            tvNetVolume = findViewById(R.id.tvNetVolume);
            tvVolumePie = findViewById(R.id.tvVolumePie);
            llNoData = findViewById(R.id.llNoData);
            cardFarm = findViewById(R.id.cardFarm);
            rvFarmData = findViewById(R.id.rvFarmData);
            llDataSquareLogs = findViewById(R.id.llDataSquareLogs);
            llDataRoundLogs = findViewById(R.id.llDataRoundLogs);
            llVolumePie = findViewById(R.id.llVolumePie);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                farmView = (FarmView) bundle.getSerializable("farmDetails");

                if (farmView != null) {
                    txtTitle.setText(getString(R.string.reception_data));
                    imgBack.setOnClickListener(v -> finish());

                    txtSubTitle.setVisibility(View.VISIBLE);
                    txtSubTitle.setText(getString(R.string.reception_subtitle, farmView.ica, farmView.supplierName));

                    farmDataViewModel = new ViewModelProvider(this).get(FarmDataViewModel.class);

                    setupRecyclerView();
                    observeHeader();
                    observeData();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.common_error), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.common_error), Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void observeHeader() {
        tvIca.setText(farmView.ica);
        tvSupplier.setText(farmView.supplierName);
        farmDataViewModel.getFarmSummary(farmView.tempFarmId).observe(this, summary -> {

            if (summary != null) {
                tvPieces.setText(String.valueOf(summary.totalPieces));
                tvNetVolume.setText(CommonUtils.formatNumber3(CommonUtils.round(summary.totalNetVolume, 3)));
                tvGrossVolume.setText(CommonUtils.formatNumber3(CommonUtils.round(summary.totalGrossVolume, 3)));

                if(farmView.productTypeId == 1 || farmView.productTypeId == 3) {
                    llDataRoundLogs.setVisibility(View.GONE);
                    llDataSquareLogs.setVisibility(View.VISIBLE);
                    llVolumePie.setVisibility(View.VISIBLE);

                    tvVolumePie.setText(CommonUtils.formatNumber2(summary.totalVolumePie));
                } else {
                    llDataRoundLogs.setVisibility(View.VISIBLE);
                    llDataSquareLogs.setVisibility(View.GONE);
                    llVolumePie.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupRecyclerView() {

        int layoutId = R.layout.row_item_farm_data;
        if(farmView.productTypeId == 1 || farmView.productTypeId == 3) {
            layoutId = R.layout.row_item_farm_data_square;
        }

        farmCapturedDataRecyclerViewAdapter = new RecyclerViewAdapter<>(getApplicationContext(), farmCapturedDataList, layoutId) {

            @Override
            public void onPostBindViewHolder(ViewHolder holder, FarmCapturedData item) {

                if(farmView.productTypeId == 1 || farmView.productTypeId == 3) {
                    holder.setViewText(R.id.tvThickness, CommonUtils.formatNumber(item.getThickness()));
                    holder.setViewText(R.id.tvWidth, CommonUtils.formatNumber(item.getWidth()));
                    holder.setViewText(R.id.tvVolumePie, CommonUtils.formatNumber2(item.getVolumePie()));
                } else {
                    holder.setViewText(R.id.tvGirth, CommonUtils.formatNumber(item.getCircumference()));
                    holder.setViewText(R.id.tvGrossVolume, CommonUtils.formatNumber3(item.getGrossVolume()));
                }

                holder.setViewText(R.id.tvLength, CommonUtils.formatNumber(item.getLength()));
                holder.setViewText(R.id.tvPieces, String.valueOf(item.getPieces()));
                holder.setViewText(R.id.tvNetVolume, CommonUtils.formatNumber3(item.getNetVolume()));

                holder.getView(R.id.ivDelete).setOnClickListener(v -> deleteFarmData(item.getTempFarmDataId(), item.getTempFarmId()));
            }
        };

        rvFarmData.setLayoutManager(new LinearLayoutManager(this));
        rvFarmData.setAdapter(farmCapturedDataRecyclerViewAdapter);
        rvFarmData.setHasFixedSize(true);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void observeData() {
        farmDataViewModel.fetchFarmData(farmView.tempFarmId).observe(this, list -> {
            farmCapturedDataList.clear();
            if (list != null && !list.isEmpty()) {
                farmCapturedDataList.addAll(list);
                llNoData.setVisibility(View.GONE);
                cardFarm.setVisibility(View.VISIBLE);
            } else {
                llNoData.setVisibility(View.VISIBLE);
                cardFarm.setVisibility(View.GONE);
            }
            farmCapturedDataRecyclerViewAdapter.notifyDataSetChanged();
        });
    }

    private void deleteFarmData(String tempFarmDataId, String tempFarmId) {
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

            btnDelete.setOnClickListener(v -> {
                farmDataViewModel.deleteFarmData(tempFarmDataId, tempFarmId, result ->
                        runOnUiThread(() -> {
                            if (result > 0) {
                                Toast.makeText(this, getString(R.string.data_deleted), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, getString(R.string.data_deleted_failed), Toast.LENGTH_SHORT).show();
                            }
                        }));

                dialog.dismiss();
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteReception", e);
        }
    }
}