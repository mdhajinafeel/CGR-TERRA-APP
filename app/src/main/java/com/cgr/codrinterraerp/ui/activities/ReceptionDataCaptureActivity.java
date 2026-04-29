package com.cgr.codrinterraerp.ui.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.ContainerData;
import com.cgr.codrinterraerp.db.entities.MeasurementSystemFormulaVariables;
import com.cgr.codrinterraerp.db.entities.ReceptionData;
import com.cgr.codrinterraerp.db.relations.FormulaWithVariables;
import com.cgr.codrinterraerp.db.views.DispatchView;
import com.cgr.codrinterraerp.db.views.ReceptionView;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.utils.FormulaEngine;
import com.cgr.codrinterraerp.viewmodel.DispatchViewModel;
import com.cgr.codrinterraerp.viewmodel.ReceptionDataViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReceptionDataCaptureActivity extends BaseActivity {

    private TextInputLayout tiCircumference, tiLength, tiPieces;
    private AppCompatTextView tvNoDataFound;
    private TextInputEditText etCircumference, etLength, etPieces;
    private RecyclerView rvAvailableContainers;
    private RecyclerViewAdapter<DispatchView> dispatchViewRecyclerViewAdapter;
    private int normalColor, errorColor;
    private DispatchViewModel dispatchViewModel;
    private ReceptionDataViewModel receptionDataViewModel;
    private ReceptionView receptionView;
    private List<FormulaWithVariables> formulaData;
    private DispatchView selectedContainer = null;
    private int selectedPosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reception_data_capture);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            AppCompatImageView imgBack = findViewById(R.id.imgBack);
            AppCompatTextView txtTitle = findViewById(R.id.txtTitle);
            AppCompatTextView txtSubTitle = findViewById(R.id.txtSubTitle);
            tvNoDataFound = findViewById(R.id.tvNoDataFound);
            rvAvailableContainers = findViewById(R.id.rvAvailableContainers);
            tiCircumference = findViewById(R.id.tiCircumference);
            tiLength = findViewById(R.id.tiLength);
            tiPieces = findViewById(R.id.tiPieces);
            etCircumference = findViewById(R.id.etCircumference);
            etLength = findViewById(R.id.etLength);
            etPieces = findViewById(R.id.etPieces);
            MaterialButton mbSubmit = findViewById(R.id.mbSubmit);
            MaterialButton mbClear = findViewById(R.id.mbClear);
            AppCompatImageView ivReceptionInfo = findViewById(R.id.ivReceptionInfo);
            AppCompatImageView ivReceptionData = findViewById(R.id.ivReceptionData);

            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {

                receptionView = (ReceptionView) bundle.getSerializable("receptionDetails");

                if (receptionView != null) {
                    txtTitle.setText(getString(R.string.measurement_data));
                    imgBack.setOnClickListener(v -> finish());

                    dispatchViewModel = new ViewModelProvider(this).get(DispatchViewModel.class);
                    receptionDataViewModel = new ViewModelProvider(this).get(ReceptionDataViewModel.class);

                    txtSubTitle.setVisibility(View.VISIBLE);
                    txtSubTitle.setText(getString(R.string.reception_subtitle, receptionView.ica, receptionView.supplierName));

                    normalColor = getColor(R.color.colorDarkGreen);
                    errorColor = getColor(R.color.colorErrorOrange);

                    // ✅ Setup RecyclerView (HORIZONTAL)
                    rvAvailableContainers.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

                    // ✅ Initialize adapter ONCE
                    initializeAdapter();

                    // ✅ Observe data (auto updates)
                    dispatchViewModel.getAvailableContainerList().observe(this, this::bindAvailableContainerData);
                    dispatchViewModel.availableContainerload();

                    CommonUtils.clearErrorOnTyping(etCircumference, tiCircumference);
                    CommonUtils.clearErrorOnTyping(etLength, tiLength);
                    CommonUtils.clearErrorOnTyping(etPieces, tiPieces);

                    mbClear.setOnClickListener(v -> clearFields());

                    mbSubmit.setOnClickListener(v -> {
                        if (validateInputs()) {

                            // ✅ Check container selected
                            if (selectedContainer == null) {
                                Toast.makeText(getApplicationContext(), getString(R.string.container_select_error), Toast.LENGTH_SHORT).show();
                                rvAvailableContainers.smoothScrollToPosition(0);
                                return;
                            }

                            //if(receptionView.productTypeId == 1 || receptionView.productTypeId == 3) {
                            //    // TODO SQUARE BLOCKS
                            //} else {
                            submitMeasurementData();
                            //}
                        }
                    });

                    ivReceptionInfo.setOnClickListener(v -> showReceptionDetails());

                    ivReceptionData.setOnClickListener(v -> {
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                        Intent intent = new Intent(this, ReceptionDataActivity.class);
                        intent.putExtra("receptionDetails", receptionView);
                        receptionDataResultLauncher.launch(intent, options);
                    });

                    clearFields();
                    fetchFormulas();
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

    private void initializeAdapter() {

        dispatchViewRecyclerViewAdapter = new RecyclerViewAdapter<>(getApplicationContext(), new ArrayList<>(), R.layout.row_item_available_containers) {
            @Override
            public void onPostBindViewHolder(ViewHolder holder, DispatchView dispatchView) {
                if (dispatchView != null) {

                    int position = holder.getBindingAdapterPosition();
                    boolean isSelected = position == selectedPosition;

                    holder.setViewText(R.id.tvContainerNumber, dispatchView.containerNumber);
                    holder.setViewText(R.id.tvShippingLine, dispatchView.shippingLine);
                    holder.setViewText(R.id.tvPieces, String.valueOf(dispatchView.totalPieces));
                    holder.setViewText(R.id.tvGrossVolume, String.valueOf(dispatchView.totalGrossVolume));
                    holder.setViewText(R.id.tvNetVolume, String.valueOf(dispatchView.totalNetVolume));
                    holder.setViewText(R.id.tvAvgGirth, String.valueOf(dispatchView.avgGirth));

                    View cardBg = holder.getView(R.id.cardBackground);
                    AppCompatImageView tick = (AppCompatImageView) holder.getView(R.id.ivSelected);

                    // ✅ UI update using position
                    if (isSelected) {
                        cardBg.setBackgroundResource(R.drawable.bg_card_selected);
                        tick.setVisibility(View.VISIBLE);
                    } else {
                        cardBg.setBackgroundResource(R.drawable.bg_card);
                        tick.setVisibility(View.GONE);
                    }

                    // ✅ Click logic (toggle)
                    holder.itemView.setOnClickListener(v -> {

                        int clickedPosition = holder.getBindingAdapterPosition();

                        // Toggle OFF
                        if (selectedPosition == clickedPosition) {
                            int previousPosition = selectedPosition;

                            selectedPosition = -1;
                            selectedContainer = null;

                            dispatchViewRecyclerViewAdapter.notifyItemChanged(previousPosition);
                            return;
                        }

                        // Select new
                        int previousPosition = selectedPosition;

                        selectedPosition = clickedPosition;
                        selectedContainer = dispatchView;

                        if (previousPosition != -1) {
                            dispatchViewRecyclerViewAdapter.notifyItemChanged(previousPosition);
                        }

                        dispatchViewRecyclerViewAdapter.notifyItemChanged(selectedPosition);
                    });
                }
            }
        };

        rvAvailableContainers.setAdapter(dispatchViewRecyclerViewAdapter);
    }

    // ✅ Bind data (only update adapter)
    private void bindAvailableContainerData(List<DispatchView> list) {
        try {
            if (list != null && !list.isEmpty()) {
                dispatchViewRecyclerViewAdapter.setItems(list); // 🔥 only update data
                rvAvailableContainers.setVisibility(View.VISIBLE);
                tvNoDataFound.setVisibility(View.GONE);
            } else {
                tvNoDataFound.setVisibility(View.VISIBLE);
                rvAvailableContainers.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindDispatchData", e);
        }
    }

    private boolean validateInputs() {

        hideKeyboard(this);

        String circ = Objects.requireNonNull(etCircumference.getText()).toString().trim();
        String length = Objects.requireNonNull(etLength.getText()).toString().trim();
        String pieces = Objects.requireNonNull(etPieces.getText()).toString().trim();

        boolean isValid = true;

        // ❗ Reset first
        resetBorders();

        if (circ.isEmpty()) {
            tiCircumference.setBoxStrokeColor(errorColor);
            etCircumference.requestFocus();
            isValid = false;
        }

        if (length.isEmpty()) {
            tiLength.setBoxStrokeColor(errorColor);
            if (isValid) etLength.requestFocus();
            isValid = false;
        }

        if (!length.isEmpty()) {
            double l = Double.parseDouble(length);
            if (l <= 100) {
                Toast.makeText(getApplicationContext(), getString(R.string.length_must_be_greater_than_100), Toast.LENGTH_SHORT).show();
                isValid = false;
            }
        }

        if (pieces.isEmpty()) {
            tiPieces.setBoxStrokeColor(errorColor);
            if (isValid) etPieces.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void resetBorders() {
        tiCircumference.setBoxStrokeColor(normalColor);
        tiLength.setBoxStrokeColor(normalColor);
        tiPieces.setBoxStrokeColor(normalColor);
    }

    private void clearFields() {
        etCircumference.setText("");
        etLength.setText("");
        etPieces.setText("1");
        etCircumference.requestFocus();
    }

    private void resetContainerSelection() {
        int previousPosition = selectedPosition;

        selectedPosition = -1;
        selectedContainer = null;

        if (previousPosition != -1) {
            dispatchViewRecyclerViewAdapter.notifyItemChanged(previousPosition);
        }
    }

    private void fetchFormulas() {
        try {
            formulaData = receptionDataViewModel.getFormulasWithVariables(receptionView.measurementSystem);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchFormulas", e);
        }
    }

    private void submitMeasurementData() {
        try {
            double c = Double.parseDouble(Objects.requireNonNull(etCircumference.getText()).toString());
            double l = Double.parseDouble(Objects.requireNonNull(etLength.getText()).toString());
            int pieces = Integer.parseInt(Objects.requireNonNull(etPieces.getText()).toString());

            double netVolume = 0;
            double grossVolume = 0;

            for (FormulaWithVariables f : formulaData) {
                // ✅ Build variable map
                Map<String, Double> inputValues = new HashMap<>();

                for (MeasurementSystemFormulaVariables v : f.variables) {
                    switch (v.getVarName()) {
                        case "c":
                            inputValues.put("c", c);
                            break;
                        case "l":
                            inputValues.put("l", l);
                            break;
                    }
                }

                // ✅ Evaluate formula
                double value = FormulaEngine.evaluate(f.formula.getFormula(), inputValues);

                // ✅ Apply rounding
                double finalValue = FormulaEngine.applyRounding(
                        value,
                        f.formula.getRoundPrecision(),
                        f.formula.getRoundingType()
                );

                // ✅ Separate by context
                if ("NET".equalsIgnoreCase(f.formula.getContext())) {
                    netVolume = finalValue;
                } else if ("GROSS".equalsIgnoreCase(f.formula.getContext())) {
                    grossVolume = finalValue;
                }
            }

            // ✅ Multiply by pieces
            double totalNet = netVolume * pieces;
            double totalGross = grossVolume * pieces;

            BigDecimal net = BigDecimal.valueOf(totalNet).setScale(3, RoundingMode.HALF_UP);
            BigDecimal gross = BigDecimal.valueOf(totalGross).setScale(3, RoundingMode.HALF_UP);
            double netToSave = Math.round(net.doubleValue() * 1000.0) / 1000.0;
            double grossToSave = Math.round(gross.doubleValue() * 1000.0) / 1000.0;

            // ✅ Show result
            Toast.makeText(getApplicationContext(), "Total Net: " + net.doubleValue() + " - Total Gross: " + gross.doubleValue(), Toast.LENGTH_LONG).show();

            String tempReceptionDataId = "TRD_" + CommonUtils.getCurrentLocalDateTimeStamp();

            ReceptionData receptionData = new ReceptionData();
            receptionData.setTempReceptionDataId(tempReceptionDataId);
            receptionData.setTempReceptionId(receptionView.tempReceptionId);
            receptionData.setReceptionId(receptionView.receptionId);
            receptionData.setReceptionDataId(null);
            receptionData.setCircumference(c);
            receptionData.setLength(l);
            receptionData.setPieces(pieces);
            receptionData.setGrossVolume(grossToSave);
            receptionData.setNetVolume(netToSave);
            receptionData.setSynced(false);
            receptionData.setDeleted(false);
            receptionData.setEdited(false);
            receptionData.setUpdatedAt(System.currentTimeMillis());
            receptionData.setContainerReceptionMappingId(receptionView.containerReceptionMappingId);

            ContainerData containerData = new ContainerData();
            containerData.setTempReceptionDataId(tempReceptionDataId);
            containerData.setTempDispatchId(selectedContainer.tempDispatchId);
            containerData.setDispatchId(selectedContainer.dispatchId);
            containerData.setReceptionId(receptionView.receptionId);
            containerData.setTempReceptionId(receptionView.tempReceptionId);
            containerData.setReceptionDataId(null);
            containerData.setPieces(pieces);
            containerData.setGrossVolume(grossToSave);
            containerData.setNetVolume(netToSave);
            containerData.setSynced(false);
            containerData.setDeleted(false);
            containerData.setEdited(false);
            containerData.setUpdatedAt(System.currentTimeMillis());
            containerData.setContainerReceptionMappingId(receptionView.containerReceptionMappingId);

            receptionDataViewModel.saveMeasurementData(receptionData, containerData, success ->
                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(this, getString(R.string.pieces_has_been_added_successfully), Toast.LENGTH_SHORT).show();
                            resetContainerSelection();
                            clearFields();
                            dispatchViewModel.availableContainerload();
                        } else {
                            Toast.makeText(this, getString(R.string.data_added_failed), Toast.LENGTH_SHORT).show();
                        }
                    }));
        } catch (Exception e) {
            AppLogger.e(getClass(), "submitMeasurementData", e);
        }
    }

    private void showReceptionDetails() {
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
            AppCompatTextView tvGrossVolume = dialog.findViewById(R.id.tvGrossVolume);
            AppCompatTextView tvNetVolume = dialog.findViewById(R.id.tvNetVolume);
            AppCompatTextView tvContractCode = dialog.findViewById(R.id.tvContractCode);
            AppCompatTextView tvContractDesc = dialog.findViewById(R.id.tvContractDesc);
            LinearLayout llFarmContractDetails = dialog.findViewById(R.id.llFarmContractDetails);
            LinearLayout llContractDesc = dialog.findViewById(R.id.llContractDesc);

            tvIca.setText(receptionView.ica);
            tvSupplier.setText(receptionView.supplierName);
            tvWood.setText(receptionView.productName);
            tvWoodType.setText(receptionView.productTypeName);
            tvMeasurement.setText(receptionView.measurementName);
            tvPieces.setText(String.valueOf(receptionView.totalPieces));
            tvGrossVolume.setText(String.valueOf(receptionView.totalGrossVolume));
            tvNetVolume.setText(String.valueOf(receptionView.totalNetVolume));

            if(receptionView.isFarmEnabled) {
                tvContractCode.setText(receptionView.contractCode);

                if(receptionView.description != null && !receptionView.description.isEmpty()) {
                    tvContractDesc.setText(receptionView.description);
                    llContractDesc.setVisibility(View.VISIBLE);
                } else {
                    llContractDesc.setVisibility(View.GONE);
                }
                llFarmContractDetails.setVisibility(View.VISIBLE);
            } else {
                llFarmContractDetails.setVisibility(View.GONE);
            }

            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showDataDialog", e);
        }
    }

    private final ActivityResultLauncher<Intent> receptionDataResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            hideKeyboard(this);
                            Toast.makeText(getApplicationContext(), getString(R.string.data_added_successfully), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
}