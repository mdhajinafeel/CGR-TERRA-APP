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
import com.cgr.codrinterraerp.db.views.DispatchView;
import com.cgr.codrinterraerp.model.ContainerWithReception;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.viewmodel.DispatchDataViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DispatchDataActivity extends BaseActivity {

    private AppCompatTextView tvContainerNumber, tvShippingLine, tvPieces, tvGrossVolume, tvNetVolume, tvNoDispatchData;
    private MaterialCardView cardDispatch;
    private RecyclerView rvDispatchData;
    private DispatchDataViewModel dispatchDataViewModel;
    private RecyclerViewAdapter<ContainerWithReception> containerWithReceptionRecyclerViewAdapter;
    private final List<ContainerWithReception> containerWithReceptionList = new ArrayList<>();
    private DispatchView dispatchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container_data);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            AppCompatImageView imgBack = findViewById(R.id.imgBack);
            AppCompatTextView txtTitle = findViewById(R.id.txtTitle);
            AppCompatTextView txtSubTitle = findViewById(R.id.txtSubTitle);
            tvContainerNumber = findViewById(R.id.tvContainerNumber);
            tvShippingLine = findViewById(R.id.tvShippingLine);
            tvPieces = findViewById(R.id.tvPieces);
            tvGrossVolume = findViewById(R.id.tvGrossVolume);
            tvNetVolume = findViewById(R.id.tvNetVolume);
            tvNoDispatchData = findViewById(R.id.tvNoDispatchData);
            cardDispatch = findViewById(R.id.cardDispatch);
            rvDispatchData = findViewById(R.id.rvDispatchData);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                dispatchView = (DispatchView) bundle.getSerializable("dispatchDetails");

                if (dispatchView != null) {
                    txtTitle.setText(getString(R.string.dispatch_data));
                    imgBack.setOnClickListener(v -> finish());

                    txtSubTitle.setVisibility(View.VISIBLE);
                    txtSubTitle.setText(getString(R.string.reception_subtitle, dispatchView.containerNumber, dispatchView.shippingLine));

                    dispatchDataViewModel = new ViewModelProvider(this).get(DispatchDataViewModel.class);

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
        tvContainerNumber.setText(dispatchView.containerNumber);
        tvShippingLine.setText(dispatchView.shippingLine);
        dispatchDataViewModel.getDispatchSummary(dispatchView.tempDispatchId).observe(this, summary -> {

            if (summary != null) {
                tvPieces.setText(String.valueOf(summary.totalPieces));
                tvGrossVolume.setText(String.valueOf(summary.totalGrossVolume));
                tvNetVolume.setText(String.valueOf(summary.totalNetVolume));
            }
        });
    }

    private void setupRecyclerView() {
        containerWithReceptionRecyclerViewAdapter = new RecyclerViewAdapter<>(getApplicationContext(), containerWithReceptionList, R.layout.row_item_container_data) {

            @Override
            public void onPostBindViewHolder(ViewHolder holder, ContainerWithReception item) {

                holder.setViewText(R.id.tvGirth, CommonUtils.formatNumber(item.getCircumference()));
                holder.setViewText(R.id.tvLength, CommonUtils.formatNumber(item.getLength()));
                holder.setViewText(R.id.tvPieces, String.valueOf(item.getPieces()));
                holder.setViewText(R.id.tvIca, item.getIca());
                holder.setViewText(R.id.tvGrossVolume, CommonUtils.formatNumber(item.getGrossVolume()));
                holder.setViewText(R.id.tvNetVolume, CommonUtils.formatNumber(item.getNetVolume()));

                holder.getView(R.id.ivDelete).setOnClickListener(v -> deleteDispatchData(item.getTempReceptionDataId(), item.getTempReceptionId(), item.getTempDispatchId()));
            }
        };

        rvDispatchData.setLayoutManager(new LinearLayoutManager(this));
        rvDispatchData.setAdapter(containerWithReceptionRecyclerViewAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void observeData() {
        dispatchDataViewModel.fetchContainerData(dispatchView.dispatchId, dispatchView.tempDispatchId).observe(this, list -> {
            containerWithReceptionList.clear();
            if (list != null && !list.isEmpty()) {
                containerWithReceptionList.addAll(list);
                tvNoDispatchData.setVisibility(View.GONE);
                cardDispatch.setVisibility(View.VISIBLE);
            } else {
                tvNoDispatchData.setVisibility(View.VISIBLE);
                cardDispatch.setVisibility(View.GONE);
            }
            containerWithReceptionRecyclerViewAdapter.notifyDataSetChanged();
        });
    }

    private void deleteDispatchData(String tempReceptionDataId, String tempReceptionId, String tempDispatchId) {
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
                dispatchDataViewModel.deleteDispatchDataById(tempReceptionDataId, tempReceptionId, tempDispatchId, result ->
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