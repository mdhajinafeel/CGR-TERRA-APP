package com.cgr.codrinterraerp.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.views.ReceptionView;
import com.cgr.codrinterraerp.model.ReceptionWithContainer;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.viewmodel.ReceptionDataViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReceptionDataActivity extends BaseActivity {

    private AppCompatTextView tvIca, tvSupplier, tvPieces, tvGrossVolume, tvNetVolume, tvNoReceptionData;
    private MaterialCardView cardReception;
    private RecyclerView rvReceptionData;
    private ReceptionDataViewModel receptionDataViewModel;
    private RecyclerViewAdapter<ReceptionWithContainer> receptionWithContainerRecyclerViewAdapter;
    private final List<ReceptionWithContainer> receptionWithContainerList = new ArrayList<>();
    private ReceptionView receptionView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reception_data);
        statusBarSetting();
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
            tvNoReceptionData = findViewById(R.id.tvNoReceptionData);
            cardReception = findViewById(R.id.cardReception);
            rvReceptionData = findViewById(R.id.rvReceptionData);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                receptionView = (ReceptionView) bundle.getSerializable("receptionDetails");

                if (receptionView != null) {
                    txtTitle.setText(getString(R.string.reception_data));
                    imgBack.setOnClickListener(v -> finish());

                    txtSubTitle.setVisibility(View.VISIBLE);
                    txtSubTitle.setText(getString(R.string.reception_subtitle, receptionView.ica, receptionView.supplierName));

                    receptionDataViewModel = new ViewModelProvider(this).get(ReceptionDataViewModel.class);

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
        tvIca.setText(receptionView.ica);
        tvSupplier.setText(receptionView.supplierName);
        receptionDataViewModel.getReceptionSummary(receptionView.tempReceptionId).observe(this, summary -> {

            if (summary != null) {
                tvPieces.setText(String.valueOf(summary.totalPieces));
                tvGrossVolume.setText(String.valueOf(summary.totalGrossVolume));
                tvNetVolume.setText(String.valueOf(summary.totalNetVolume));
            }
        });
    }

    private void setupRecyclerView() {
        receptionWithContainerRecyclerViewAdapter = new RecyclerViewAdapter<>(getApplicationContext(), receptionWithContainerList, R.layout.row_item_reception_data) {

            @Override
            public void onPostBindViewHolder(ViewHolder holder, ReceptionWithContainer item) {

                holder.setViewText(R.id.tvGirth, CommonUtils.formatNumber(item.getCircumference()));
                holder.setViewText(R.id.tvLength, CommonUtils.formatNumber(item.getLength()));
                holder.setViewText(R.id.tvPieces, String.valueOf(item.getPieces()));
                holder.setViewText(R.id.tvContainerNumber, item.getContainerNumber());
                holder.setViewText(R.id.tvGrossVolume, CommonUtils.formatNumber(item.getGrossVolume()));
                holder.setViewText(R.id.tvNetVolume, CommonUtils.formatNumber(item.getNetVolume()));

                holder.getView(R.id.ivDelete).setOnClickListener(v -> deleteReceptionData(item.getTempReceptionDataId(), item.getTempReceptionId()));
            }
        };

        rvReceptionData.setLayoutManager(new LinearLayoutManager(this));
        rvReceptionData.setAdapter(receptionWithContainerRecyclerViewAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void observeData() {
        receptionDataViewModel.fetchReceptionData(receptionView.receptionId, receptionView.tempReceptionId).observe(this, list -> {
            receptionWithContainerList.clear();
            if (list != null && !list.isEmpty()) {
                receptionWithContainerList.addAll(list);
                tvNoReceptionData.setVisibility(View.GONE);
                cardReception.setVisibility(View.VISIBLE);
            } else {
                tvNoReceptionData.setVisibility(View.VISIBLE);
                cardReception.setVisibility(View.GONE);
            }
            receptionWithContainerRecyclerViewAdapter.notifyDataSetChanged();
        });
    }

    private void deleteReceptionData(String tempReceptionDataId, String tempReceptionId) {
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
                receptionDataViewModel.deleteReceptionData(tempReceptionDataId, tempReceptionId, result ->
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