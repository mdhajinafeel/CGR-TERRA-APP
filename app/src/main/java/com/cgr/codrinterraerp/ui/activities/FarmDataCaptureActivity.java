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
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.FarmData;
import com.cgr.codrinterraerp.db.entities.MeasurementSystemFormulaVariables;
import com.cgr.codrinterraerp.db.relations.FormulaWithVariables;
import com.cgr.codrinterraerp.db.views.FarmView;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.utils.FormulaEngine;
import com.cgr.codrinterraerp.viewmodel.FarmDataViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FarmDataCaptureActivity extends BaseActivity {

    private AppCompatTextView tvPieces, tvGrossVolume, tvNetVolume, tvVolumePie;
    private LinearLayout llVolumePie;
    private TextInputLayout tiCircumference, tiLength, tiPieces, tiThickness, tiWidth, tiLengthSquare, tiPiecesSquare;
    private TextInputEditText etCircumference, etLength, etPieces, etThickness, etWidth, etLengthSquare, etPiecesSquare;
    private FarmView farmView;
    private int normalColor, errorColor;
    private List<FormulaWithVariables> formulaData;
    private FarmDataViewModel farmDataViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_data_capture);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            AppCompatImageView imgBack = findViewById(R.id.imgBack);
            AppCompatTextView txtTitle = findViewById(R.id.txtTitle);
            AppCompatTextView txtSubTitle = findViewById(R.id.txtSubTitle);

            tvPieces = findViewById(R.id.tvPieces);
            tvGrossVolume = findViewById(R.id.tvGrossVolume);
            tvNetVolume = findViewById(R.id.tvNetVolume);
            tvVolumePie = findViewById(R.id.tvVolumePie);
            tiCircumference = findViewById(R.id.tiCircumference);
            tiLength = findViewById(R.id.tiLength);
            tiPieces = findViewById(R.id.tiPieces);
            tiThickness = findViewById(R.id.tiThickness);
            tiWidth = findViewById(R.id.tiWidth);
            tiLengthSquare = findViewById(R.id.tiLengthSquare);
            tiPiecesSquare = findViewById(R.id.tiPiecesSquare);
            etCircumference = findViewById(R.id.etCircumference);
            etLength = findViewById(R.id.etLength);
            etPieces = findViewById(R.id.etPieces);
            etThickness = findViewById(R.id.etThickness);
            etWidth = findViewById(R.id.etWidth);
            etLengthSquare = findViewById(R.id.etLengthSquare);
            etPiecesSquare = findViewById(R.id.etPiecesSquare);
            llVolumePie = findViewById(R.id.llVolumePie);
            LinearLayout llRoundLogs = findViewById(R.id.llRoundLogs);
            LinearLayout llSquareBlocks = findViewById(R.id.llSquareBlocks);
            MaterialButton mbSubmit = findViewById(R.id.mbSubmit);
            MaterialButton mbClear = findViewById(R.id.mbClear);
            AppCompatImageView ivFarmInfo = findViewById(R.id.ivFarmInfo);
            AppCompatImageView ivFarmData = findViewById(R.id.ivFarmData);

            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                farmView = (FarmView) bundle.getSerializable("farmDetails");

                if (farmView != null) {
                    txtTitle.setText(getString(R.string.measurement_data));
                    imgBack.setOnClickListener(v -> finish());

                    farmDataViewModel = new ViewModelProvider(this).get(FarmDataViewModel.class);

                    txtSubTitle.setVisibility(View.VISIBLE);
                    txtSubTitle.setText(getString(R.string.reception_subtitle, farmView.ica, farmView.supplierName));

                    normalColor = getColor(R.color.colorDarkGreen);
                    errorColor = getColor(R.color.colorErrorOrange);

                    CommonUtils.clearErrorOnTyping(etCircumference, tiCircumference);
                    CommonUtils.clearErrorOnTyping(etLength, tiLength);
                    CommonUtils.clearErrorOnTyping(etPieces, tiPieces);
                    CommonUtils.clearErrorOnTyping(etThickness, tiThickness);
                    CommonUtils.clearErrorOnTyping(etWidth, tiWidth);
                    CommonUtils.clearErrorOnTyping(etLengthSquare, tiLengthSquare);
                    CommonUtils.clearErrorOnTyping(etPiecesSquare, tiPiecesSquare);

                    mbClear.setOnClickListener(v -> clearFields(farmView.productTypeId));

                    mbSubmit.setOnClickListener(v -> {
                        if (validateInputs(farmView.productTypeId)) {
                            submitMeasurementData(farmView.productTypeId);
                        }
                    });

                    ivFarmInfo.setOnClickListener(v -> showFarmDetails(farmView));

                    ivFarmData.setOnClickListener(v -> {
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                        Intent intent = new Intent(this, ReceptionDataActivity.class);
                        intent.putExtra("farmDetails", farmView);
                        farmDataResultLauncher.launch(intent, options);
                    });

                    if (farmView.productTypeId == 1 || farmView.productTypeId == 3) {
                        llSquareBlocks.setVisibility(View.VISIBLE);
                        llRoundLogs.setVisibility(View.GONE);
                    } else {
                        llSquareBlocks.setVisibility(View.GONE);
                        llRoundLogs.setVisibility(View.VISIBLE);
                    }

                    fetchSummaryData();
                    clearFields(farmView.productTypeId);
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

    private void fetchSummaryData() {
        try {
            farmDataViewModel.getFarmSummary(farmView.tempFarmId).observe(this, farmSummary -> {
                tvPieces.setText(String.valueOf(farmSummary.totalPieces));
                tvGrossVolume.setText(CommonUtils.formatNumber3(farmSummary.totalGrossVolume));
                tvNetVolume.setText(CommonUtils.formatNumber3(farmSummary.totalNetVolume));

                if(farmView.productTypeId == 1 || farmView.productTypeId == 3) {
                    llVolumePie.setVisibility(View.VISIBLE);
                    tvVolumePie.setText(CommonUtils.formatNumber2(farmSummary.totalVolumePie));
                } else {
                    llVolumePie.setVisibility(View.GONE);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchSummaryData", e);
        }
    }

    private void fetchFormulas() {
        try {
            int measurementSystemId = 2;
            if(farmView.productTypeId == 1 || farmView.productTypeId == 3) {
                measurementSystemId = 1;
            }

            formulaData = farmDataViewModel.getFormulasWithVariables(measurementSystemId);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchFormulas", e);
        }
    }

    private void clearFields(int productTypeId) {

        if (productTypeId == 1 || productTypeId == 3) {
            etThickness.setText("");
            etWidth.setText("");
            etLengthSquare.setText("");
            etPiecesSquare.setText("1");
            etThickness.requestFocus();
        } else {
            etCircumference.setText("");
            etLength.setText("");
            etPieces.setText("1");
            etCircumference.requestFocus();
        }
    }

    private void resetBorders(int productTypeId) {

        if (productTypeId == 1 || productTypeId == 3) {
            tiThickness.setBoxStrokeColor(normalColor);
            tiWidth.setBoxStrokeColor(normalColor);
            tiLengthSquare.setBoxStrokeColor(normalColor);
            tiPiecesSquare.setBoxStrokeColor(normalColor);
        } else {
            tiCircumference.setBoxStrokeColor(normalColor);
            tiLength.setBoxStrokeColor(normalColor);
            tiPieces.setBoxStrokeColor(normalColor);
        }
    }

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
            LinearLayout llVolumePie = dialog.findViewById(R.id.llVolumePie);
            LinearLayout llContractDesc = dialog.findViewById(R.id.llContractDesc);

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

            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showReceptionDetails", e);
        }
    }

    private boolean validateInputs(int productTypeId) {

        hideKeyboard(this);

        if (productTypeId == 1 || productTypeId == 3) {

            String thickness = Objects.requireNonNull(etThickness.getText()).toString().trim();
            String width = Objects.requireNonNull(etWidth.getText()).toString().trim();
            String length = Objects.requireNonNull(etLengthSquare.getText()).toString().trim();
            String pieces = Objects.requireNonNull(etPiecesSquare.getText()).toString().trim();

            boolean isValid = true;

            // ❗ Reset first
            resetBorders(productTypeId);

            if (thickness.isEmpty()) {
                tiThickness.setBoxStrokeColor(errorColor);
                etThickness.requestFocus();
                isValid = false;
            }

            if (width.isEmpty()) {
                tiWidth.setBoxStrokeColor(errorColor);
                etWidth.requestFocus();
                isValid = false;
            }

            if (length.isEmpty()) {
                tiLengthSquare.setBoxStrokeColor(errorColor);
                if (isValid) etLengthSquare.requestFocus();
                isValid = false;
            }

            if (pieces.isEmpty()) {
                tiPiecesSquare.setBoxStrokeColor(errorColor);
                if (isValid) etPiecesSquare.requestFocus();
                isValid = false;
            }

            return isValid;
        } else {

            String circ = Objects.requireNonNull(etCircumference.getText()).toString().trim();
            String length = Objects.requireNonNull(etLength.getText()).toString().trim();
            String pieces = Objects.requireNonNull(etPieces.getText()).toString().trim();

            boolean isValid = true;

            // ❗ Reset first
            resetBorders(productTypeId);

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
                if (l <= 10) {
                    tiLength.setBoxStrokeColor(errorColor);
                    Toast.makeText(getApplicationContext(), getString(R.string.length_must_be_greater_than_10), Toast.LENGTH_SHORT).show();
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
    }

    private void submitMeasurementData(int productTypeId) {
        try {

            if (productTypeId == 1 || productTypeId == 3) {
                double t = Double.parseDouble(Objects.requireNonNull(etThickness.getText()).toString());
                double w = Double.parseDouble(Objects.requireNonNull(etWidth.getText()).toString());
                double l = Double.parseDouble(Objects.requireNonNull(etLengthSquare.getText()).toString());
                int pieces = Integer.parseInt(Objects.requireNonNull(etPiecesSquare.getText()).toString());

                double volumePie = 0;
                double widthExport = 0;
                double thicknessExport = 0;
                double lengthExport = 0;
                double grossVolume = 0;
                double netVolume = 0;

                for (FormulaWithVariables f : formulaData) {
                    // ✅ Build variable map
                    Map<String, Double> inputValues = new HashMap<>();
                    for (MeasurementSystemFormulaVariables v : f.variables) {
                        switch (v.getVarName()) {
                            case "t":
                                inputValues.put("t", t);
                                break;
                            case "w":
                                inputValues.put("w", w);
                                break;
                            case "l":
                                inputValues.put("l", l);
                                break;
                            case "WIDEXP":
                                inputValues.put("WIDEXP", widthExport);
                                break;
                            case "THKEXP":
                                inputValues.put("THKEXP", thicknessExport);
                                break;
                            case "LENEXP":
                                inputValues.put("LENEXP", lengthExport);
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
                    if ("PIE".equalsIgnoreCase(f.formula.getContext())) {
                        volumePie = finalValue;
                    } else if ("GROSS".equalsIgnoreCase(f.formula.getContext())) {
                        grossVolume = finalValue;
                    } else if ("WIDEXP".equalsIgnoreCase(f.formula.getContext())) {
                        widthExport = finalValue;
                    } else if ("THKEXP".equalsIgnoreCase(f.formula.getContext())) {
                        thicknessExport = finalValue;
                    } else if ("LENEXP".equalsIgnoreCase(f.formula.getContext())) {
                        lengthExport = finalValue;
                    } else if ("NET".equalsIgnoreCase(f.formula.getContext())) {
                        netVolume = finalValue;
                    }
                }

                // ✅ Multiply by pieces
                double totalPie = volumePie * pieces;
                double totalGross = grossVolume * pieces;
                double totalNet = netVolume * pieces;

                BigDecimal pie = BigDecimal.valueOf(totalPie).setScale(3, RoundingMode.HALF_UP);
                double pieToSave = Math.round(pie.doubleValue() * 1000.0) / 1000.0;

                BigDecimal gross = BigDecimal.valueOf(totalGross).setScale(3, RoundingMode.HALF_UP);
                double grossToSave = Math.round(gross.doubleValue() * 1000.0) / 1000.0;

                BigDecimal net = BigDecimal.valueOf(totalNet).setScale(3, RoundingMode.HALF_UP);
                double netToSave = Math.round(net.doubleValue() * 1000.0) / 1000.0;

                long currentTimeStamp = CommonUtils.getCurrentLocalDateTimeStamp();
                String tempFarmDataId = "FARMDATA_" + currentTimeStamp;

                FarmData farmData = new FarmData();
                farmData.setTempFarmId(farmView.tempFarmId);
                farmData.setTempFarmDataId(tempFarmDataId);
                farmData.setFarmDataId(null);
                farmData.setFarmId(farmView.farmId);
                farmData.setCircumference(0);
                farmData.setLength(l);
                farmData.setThickness(t);
                farmData.setWidth(w);
                farmData.setPieces(pieces);
                farmData.setGrossVolume(grossToSave);
                farmData.setNetVolume(netToSave);
                farmData.setVolumePie(pieToSave);
                farmData.setSynced(false);
                farmData.setDeleted(false);
                farmData.setEdited(false);
                farmData.setUpdatedAt(System.currentTimeMillis());
                farmData.setCreatedAt(System.currentTimeMillis());

                saveMeasurementData(farmData);

            } else {

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

                long currentTimeStamp = CommonUtils.getCurrentLocalDateTimeStamp();
                String tempFarmDataId = "FARMDATA_" + currentTimeStamp;

                FarmData farmData = new FarmData();
                farmData.setTempFarmId(farmView.tempFarmId);
                farmData.setTempFarmDataId(tempFarmDataId);
                farmData.setFarmDataId(null);
                farmData.setFarmId(farmView.farmId);
                farmData.setCircumference(c);
                farmData.setLength(l);
                farmData.setThickness(0);
                farmData.setWidth(0);
                farmData.setPieces(pieces);
                farmData.setGrossVolume(grossToSave);
                farmData.setNetVolume(netToSave);
                farmData.setVolumePie(0);
                farmData.setSynced(false);
                farmData.setDeleted(false);
                farmData.setEdited(false);
                farmData.setUpdatedAt(System.currentTimeMillis());
                farmData.setCreatedAt(System.currentTimeMillis());

                saveMeasurementData(farmData);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "submitMeasurementData", e);
        }
    }

    private void saveMeasurementData(FarmData farmData) {
        farmDataViewModel.saveMeasurementData(farmData, success ->
                runOnUiThread(() -> {
                    if (success) {
                        clearFields(farmView.productTypeId);
                    } else {
                        Toast.makeText(this, getString(R.string.data_added_failed), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private final ActivityResultLauncher<Intent> farmDataResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            hideKeyboard(this);
                            AppLogger.i(getClass(), "Farm Data");
                        }
                    }
            );
}