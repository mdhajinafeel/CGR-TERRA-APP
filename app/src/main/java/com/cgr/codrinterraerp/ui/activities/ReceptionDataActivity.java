package com.cgr.codrinterraerp.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProvider;
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

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReceptionDataActivity extends BaseActivity {

    private AppCompatTextView tvIca, tvSupplier, tvPieces, tvGrossVolume, tvNetVolume, tvNoReceptionData;
    private HorizontalScrollView hsvReceptionData;
    private RecyclerView rvReceptionData;
    private ReceptionDataViewModel receptionDataViewModel;

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
            hsvReceptionData = findViewById(R.id.hsvReceptionData);
            rvReceptionData = findViewById(R.id.rvReceptionData);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                ReceptionView receptionView = (ReceptionView) bundle.getSerializable("receptionDetails");

                if (receptionView != null) {
                    txtTitle.setText(getString(R.string.reception_data));
                    imgBack.setOnClickListener(v -> finish());

                    txtSubTitle.setVisibility(View.VISIBLE);
                    txtSubTitle.setText(getString(R.string.reception_subtitle, receptionView.ica, receptionView.supplierName));

                    receptionDataViewModel = new ViewModelProvider(this).get(ReceptionDataViewModel.class);

                    bindData(receptionView);
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

    private void bindData(ReceptionView receptionView) {
        try {
            tvIca.setText(receptionView.ica);
            tvSupplier.setText(receptionView.supplierName);
            tvPieces.setText(String.valueOf(receptionView.totalPieces));
            tvGrossVolume.setText(String.valueOf(receptionView.totalGrossVolume));
            tvNetVolume.setText(String.valueOf(receptionView.totalNetVolume));

            List<ReceptionWithContainer> receptionWithContainerList = receptionDataViewModel.fetchReceptionData(receptionView.receptionId, receptionView.tempReceptionId);

            if(receptionWithContainerList !=null && !receptionWithContainerList.isEmpty()) {

                RecyclerViewAdapter<ReceptionWithContainer> receptionDataRecyclerViewAdapter = new RecyclerViewAdapter<>(getApplicationContext(), receptionWithContainerList,
                        R.layout.row_item_reception_data) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, ReceptionWithContainer receptionWithContainer) {
                        holder.setViewText(R.id.tvGirth, CommonUtils.formatNumber(receptionWithContainer.getCircumference()));
                        holder.setViewText(R.id.tvLength, CommonUtils.formatNumber(receptionWithContainer.getLength()));
                        holder.setViewText(R.id.tvPieces, String.valueOf(receptionWithContainer.getPieces()));
                        holder.setViewText(R.id.tvContainerNumber, receptionWithContainer.getContainerNumber());
                        holder.setViewText(R.id.tvGrossVolume, CommonUtils.formatNumber(receptionWithContainer.getGrossVolume()));
                        holder.setViewText(R.id.tvNetVolume, CommonUtils.formatNumber(receptionWithContainer.getNetVolume()));
                    }
                };

                rvReceptionData.setAdapter(receptionDataRecyclerViewAdapter);
                tvNoReceptionData.setVisibility(View.GONE);
                hsvReceptionData.setVisibility(View.VISIBLE);
            } else {
                tvNoReceptionData.setVisibility(View.VISIBLE);
                hsvReceptionData.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }
}