package com.cgr.codrinterraerp.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.ContainerData;
import com.cgr.codrinterraerp.db.entities.ContainerImages;
import com.cgr.codrinterraerp.db.entities.MeasurementSystemFormulaVariables;
import com.cgr.codrinterraerp.db.entities.ReceptionData;
import com.cgr.codrinterraerp.db.relations.FormulaWithVariables;
import com.cgr.codrinterraerp.db.views.DispatchView;
import com.cgr.codrinterraerp.db.views.ReceptionView;
import com.cgr.codrinterraerp.helper.PreferenceManager;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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

    private TextInputLayout tiCircumference, tiLength, tiPieces, tiThickness, tiWidth, tiLengthSquare, tiPiecesSquare;
    private TextInputEditText etCircumference, etLength, etPieces, etThickness, etWidth, etLengthSquare, etPiecesSquare;
    private AppCompatTextView tvNoDataFound, tvNoContainerPhotoDataFound;
    private RecyclerView rvAvailableContainers, rvContainerImages;
    private RecyclerViewAdapter<DispatchView> dispatchViewRecyclerViewAdapter;
    private int normalColor, errorColor;
    private DispatchViewModel dispatchViewModel;
    private ReceptionDataViewModel receptionDataViewModel;
    private ReceptionView receptionView;
    private List<FormulaWithVariables> formulaData;
    private DispatchView selectedContainer = null;
    private int selectedPosition = -1;
    private LinearLayout llContainerImages;
    private Uri selectedFileUri, cameraTempUri;
    private String selectedTempDispatchId = "";
    private final List<ContainerImages> containerImagesList = new ArrayList<>();
    private RecyclerViewAdapter<ContainerImages> containerImagesRecyclerViewAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reception_data_capture);
        statusBarSetting(false);
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
            LinearLayout llRoundLogs = findViewById(R.id.llRoundLogs);
            LinearLayout llSquareBlocks = findViewById(R.id.llSquareBlocks);
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
                    dispatchViewModel.getAvailableContainerList(receptionView.productTypeId).observe(this, this::bindAvailableContainerData);

                    CommonUtils.clearErrorOnTyping(etCircumference, tiCircumference);
                    CommonUtils.clearErrorOnTyping(etLength, tiLength);
                    CommonUtils.clearErrorOnTyping(etPieces, tiPieces);
                    CommonUtils.clearErrorOnTyping(etThickness, tiThickness);
                    CommonUtils.clearErrorOnTyping(etWidth, tiWidth);
                    CommonUtils.clearErrorOnTyping(etLengthSquare, tiLengthSquare);
                    CommonUtils.clearErrorOnTyping(etPiecesSquare, tiPiecesSquare);

                    mbClear.setOnClickListener(v -> clearFields(receptionView.productTypeId));

                    mbSubmit.setOnClickListener(v -> {
                        if (validateInputs(receptionView.productTypeId)) {

                            // ✅ Check container selected
                            if (selectedContainer == null) {
                                Toast.makeText(getApplicationContext(), getString(R.string.container_select_error), Toast.LENGTH_SHORT).show();
                                rvAvailableContainers.smoothScrollToPosition(0);
                                return;
                            }

                            submitMeasurementData(receptionView.productTypeId);
                        }
                    });

                    ivReceptionInfo.setOnClickListener(v -> showReceptionDetails());

                    ivReceptionData.setOnClickListener(v -> {
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                        Intent intent = new Intent(this, ReceptionDataActivity.class);
                        intent.putExtra("receptionDetails", receptionView);
                        receptionDataResultLauncher.launch(intent, options);
                    });

                    if (receptionView.productTypeId == 1 || receptionView.productTypeId == 3) {
                        llSquareBlocks.setVisibility(View.VISIBLE);
                        llRoundLogs.setVisibility(View.GONE);
                    } else {
                        llSquareBlocks.setVisibility(View.GONE);
                        llRoundLogs.setVisibility(View.VISIBLE);
                    }

                    clearFields(receptionView.productTypeId);
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

        dispatchViewRecyclerViewAdapter = new RecyclerViewAdapter<>(getApplicationContext(), new ArrayList<>(), R.layout.row_item_available_container) {
            @Override
            public void onPostBindViewHolder(ViewHolder holder, DispatchView dispatchView) {
                if (dispatchView != null) {

                    LinearLayout llAvgGirth = holder.getView(R.id.llAvgGirth);
                    LinearLayout llVolumePie = holder.getView(R.id.llVolumePie);

                    int position = holder.getBindingAdapterPosition();
                    boolean isSelected = position == selectedPosition;

                    holder.setViewText(R.id.tvContainerNumber, dispatchView.containerNumber);
                    holder.setViewText(R.id.tvShippingLine, dispatchView.shippingLine);
                    holder.setViewText(R.id.tvPieces, String.valueOf(dispatchView.totalPieces));

                    if(dispatchView.productTypeId == 1 || dispatchView.productTypeId == 3) {
                        holder.setViewText(R.id.tvVolumePie, CommonUtils.formatNumber2(dispatchView.totalVolumePie));
                        llAvgGirth.setVisibility(View.GONE);
                        llVolumePie.setVisibility(View.VISIBLE);
                    } else {
                        holder.setViewText(R.id.tvAvgGirth, CommonUtils.formatNumber2(dispatchView.avgGirth));
                        llAvgGirth.setVisibility(View.VISIBLE);
                        llVolumePie.setVisibility(View.GONE);
                    }

                    holder.setViewText(R.id.tvGrossVolume, CommonUtils.formatNumber3(dispatchView.totalGrossVolume));
                    holder.setViewText(R.id.tvNetVolume, CommonUtils.formatNumber3(dispatchView.totalNetVolume));

                    View cardBg = holder.getView(R.id.cardBackground);
                    AppCompatImageView tick = holder.getView(R.id.ivSelected);

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

                    holder.getView(R.id.ivContainerData).setOnClickListener(view -> {
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                        Intent intent = new Intent(ReceptionDataCaptureActivity.this, DispatchDataActivity.class);
                        intent.putExtra("dispatchDetails", dispatchView);
                        dispatchDataResultLauncher.launch(intent, options);
                    });

                    holder.getView(R.id.ivContainerImages).setOnClickListener(view -> {
                        selectedTempDispatchId = dispatchView.tempDispatchId;
                        showContainerImages();
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

    private boolean validateInputs(int productTypeId) {

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
                } else if (!isValidLengthForCategory(l)) {
                    tiLength.setBoxStrokeColor(errorColor);
                    String message = getCategoryValidateString();
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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

    @NonNull
    private String getCategoryValidateString() {
        String message;
        if (selectedContainer.categoryId == 1) {
            message = getString(R.string.shorts_validate);
        } else if (selectedContainer.categoryId == 2) {
            message = getString(R.string.semi_validate);
        } else if (selectedContainer.categoryId == 3) {
            message = getString(R.string.longs_validate);
        } else {
            message = getString(R.string.invalid_length);
        }
        return message;
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
                String tempReceptionDataId = "RECDATA_" + currentTimeStamp;
                String tempDispatchDataId = "DISPDATA_" + currentTimeStamp;

                ReceptionData receptionData = new ReceptionData();
                receptionData.setTempReceptionDataId(tempReceptionDataId);
                receptionData.setTempReceptionId(receptionView.tempReceptionId);
                receptionData.setReceptionId(receptionView.receptionId);
                receptionData.setReceptionDataId(null);
                receptionData.setCircumference(0);
                receptionData.setLength(l);
                receptionData.setThickness(t);
                receptionData.setWidth(w);
                receptionData.setPieces(pieces);
                receptionData.setGrossVolume(grossToSave);
                receptionData.setNetVolume(netToSave);
                receptionData.setVolumePie(pieToSave);
                receptionData.setSynced(false);
                receptionData.setDeleted(false);
                receptionData.setEdited(false);
                receptionData.setUpdatedAt(System.currentTimeMillis());
                receptionData.setCreatedAt(System.currentTimeMillis());
                receptionData.setContainerReceptionMappingId(receptionView.containerReceptionMappingId);

                ContainerData containerData = new ContainerData();
                containerData.setTempDispatchDataId(tempDispatchDataId);
                containerData.setTempReceptionDataId(tempReceptionDataId);
                containerData.setTempDispatchId(selectedContainer.tempDispatchId);
                containerData.setDispatchId(selectedContainer.dispatchId);
                containerData.setReceptionId(receptionView.receptionId);
                containerData.setTempReceptionId(receptionView.tempReceptionId);
                containerData.setReceptionDataId(null);
                containerData.setDispatchDataId(null);
                containerData.setPieces(pieces);
                containerData.setGrossVolume(grossToSave);
                containerData.setNetVolume(netToSave);
                containerData.setVolumePie(pieToSave);
                containerData.setSynced(false);
                containerData.setDeleted(false);
                containerData.setEdited(false);
                containerData.setUpdatedAt(System.currentTimeMillis());
                containerData.setCreatedAt(System.currentTimeMillis());
                containerData.setContainerReceptionMappingId(receptionView.containerReceptionMappingId);

                saveMeasurementData(receptionData, containerData);

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
                String tempReceptionDataId = "RECDATA_" + currentTimeStamp;
                String tempDispatchDataId = "DISPDATA_" + currentTimeStamp;

                ReceptionData receptionData = new ReceptionData();
                receptionData.setTempReceptionDataId(tempReceptionDataId);
                receptionData.setTempReceptionId(receptionView.tempReceptionId);
                receptionData.setReceptionId(receptionView.receptionId);
                receptionData.setReceptionDataId(null);
                receptionData.setCircumference(c);
                receptionData.setLength(l);
                receptionData.setThickness(0);
                receptionData.setWidth(0);
                receptionData.setPieces(pieces);
                receptionData.setGrossVolume(grossToSave);
                receptionData.setNetVolume(netToSave);
                receptionData.setVolumePie(0);
                receptionData.setSynced(false);
                receptionData.setDeleted(false);
                receptionData.setEdited(false);
                receptionData.setUpdatedAt(System.currentTimeMillis());
                receptionData.setCreatedAt(System.currentTimeMillis());
                receptionData.setContainerReceptionMappingId(receptionView.containerReceptionMappingId);

                ContainerData containerData = new ContainerData();
                containerData.setTempReceptionDataId(tempReceptionDataId);
                containerData.setTempDispatchDataId(tempDispatchDataId);
                containerData.setTempDispatchId(selectedContainer.tempDispatchId);
                containerData.setDispatchId(selectedContainer.dispatchId);
                containerData.setReceptionId(receptionView.receptionId);
                containerData.setTempReceptionId(receptionView.tempReceptionId);
                containerData.setReceptionDataId(null);
                containerData.setDispatchDataId(null);
                containerData.setPieces(pieces);
                containerData.setGrossVolume(grossToSave);
                containerData.setNetVolume(netToSave);
                containerData.setVolumePie(0);
                containerData.setSynced(false);
                containerData.setDeleted(false);
                containerData.setEdited(false);
                containerData.setUpdatedAt(System.currentTimeMillis());
                containerData.setCreatedAt(System.currentTimeMillis());
                containerData.setContainerReceptionMappingId(receptionView.containerReceptionMappingId);

                saveMeasurementData(receptionData, containerData);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "submitMeasurementData", e);
        }
    }

    private void saveMeasurementData(ReceptionData receptionData, ContainerData containerData) {
        receptionDataViewModel.saveMeasurementData(receptionData, containerData, success ->
                runOnUiThread(() -> {
                    if (success) {
                        resetContainerSelection();
                        clearFields(receptionView.productTypeId);
                        dispatchViewModel.getAvailableContainerList(receptionView.productTypeId);
                    } else {
                        Toast.makeText(this, getString(R.string.data_added_failed), Toast.LENGTH_SHORT).show();
                    }
                }));
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
            AppCompatTextView tvVolumePie = dialog.findViewById(R.id.tvVolumePie);
            AppCompatTextView tvGrossVolume = dialog.findViewById(R.id.tvGrossVolume);
            AppCompatTextView tvNetVolume = dialog.findViewById(R.id.tvNetVolume);
            AppCompatTextView tvContractCode = dialog.findViewById(R.id.tvContractCode);
            AppCompatTextView tvContractDesc = dialog.findViewById(R.id.tvContractDesc);
            LinearLayout llFarmContractDetails = dialog.findViewById(R.id.llFarmContractDetails);
            LinearLayout llContractDesc = dialog.findViewById(R.id.llContractDesc);
            LinearLayout llVolumePie = dialog.findViewById(R.id.llVolumePie);

            tvIca.setText(receptionView.ica);
            tvSupplier.setText(receptionView.supplierName);
            tvWood.setText(receptionView.productName);
            tvWoodType.setText(receptionView.productTypeName);
            tvMeasurement.setText(receptionView.measurementName);
            tvPieces.setText(String.valueOf(receptionView.totalPieces));

            if(receptionView.productTypeId == 1 || receptionView.productTypeId == 3) {
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

            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showDataDialog", e);
        }
    }

    private boolean isValidLengthForCategory(double length) {

        if (selectedContainer == null) {
            return true;
        }

        if (selectedContainer.categoryId == 1) {
            return length < 290;
        } else if (selectedContainer.categoryId == 2) {
            return length >= 300 && length <= 590;
        } else if (selectedContainer.categoryId == 3) {
            return length > 590;
        } else if (selectedContainer.categoryId == 4) {
            return true;
        }
        return true;
    }

    private final ActivityResultLauncher<Intent> receptionDataResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            hideKeyboard(this);
                            AppLogger.i(getClass(), "Reception Data");
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> dispatchDataResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            hideKeyboard(this);
                            AppLogger.i(getClass(), "Dispatch Data");
                        }
                    }
            );

    // CONTAINER PHOTOS
    private void showContainerImages() {
        try {
            LayoutInflater dialogInflater = LayoutInflater.from(this);
            View dialogView = dialogInflater.inflate(R.layout.dialog_container_images, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            AppCompatTextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
            AppCompatImageView imgClose = dialogView.findViewById(R.id.imgClose);
            MaterialButton btnAddContainerImage = dialogView.findViewById(R.id.btnAddContainerImage);
            rvContainerImages = dialogView.findViewById(R.id.rvContainerImages);
            llContainerImages = dialogView.findViewById(R.id.llContainerImages);
            tvNoContainerPhotoDataFound = dialogView.findViewById(R.id.tvNoDataFound);

            imgClose.setOnClickListener(view -> dialog.dismiss());
            tvDialogTitle.setText(getString(R.string.container_images));

            btnAddContainerImage.setOnClickListener(view -> showPicker());

            setupRecyclerView();
            observeData();

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showContainerImages", e);
        }
    }

    private void showPicker() {
        AlertDialog dialog;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_select_source, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        view.findViewById(R.id.llFile).setVisibility(View.GONE);

        view.findViewById(R.id.llCamera).setOnClickListener(v -> {
            dialog.dismiss();
            checkCameraPermissionAndOpen();
        });

        view.findViewById(R.id.llGallery).setOnClickListener(v -> {
            dialog.dismiss();
            openGallery();
        });

        dialog.show();
    }

    // LAUNCHER & PERMISSIONS
    private void checkCameraPermissionAndOpen() {

        if (checkSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            openCamera();
            return;
        }

        boolean askedBefore = PreferenceManager.INSTANCE.getPermissionCameraAsked();
        if (!askedBefore) {
            // 🟢 FIRST TIME → ask permission
            PreferenceManager.INSTANCE.setPermissionCameraAsked(true);
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            return;
        }

        // Permission NOT granted
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // 🟡 Denied once → explain + ask again
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.camera_permission_required))
                    .setMessage(getString(R.string.camera_access_is_required_to_take_photos_for_expense_attachments))
                    .setPositiveButton(getString(R.string.allow), (d, w) ->
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        } else {
            // 🔴 Permanently denied → settings
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.permission_required))
                    .setMessage(getString(R.string.camera_permission_is_disabled_please_enable_it_from_app_settings))
                    .setPositiveButton(getString(R.string.open_settings), (d, w) -> openAppSettings())
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    selectedFileUri = cameraTempUri;
                    processAndSaveImage(selectedFileUri, selectedTempDispatchId);
                }
            });

    ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    processAndSaveImage(selectedFileUri, selectedTempDispatchId);
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera(); // 🔥 permission granted → open camera
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show();
                }
            });

    private void openCamera() {
        try {
            File tempFile = File.createTempFile("CAM_", ".jpg", getCacheDir());
            cameraTempUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", tempFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraTempUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cameraLauncher.launch(intent);
        } catch (Exception e) {
            AppLogger.e(getClass(), "openCamera", e);
        }
    }

    private void deleteCameraTempFile() {
        try {
            if (cameraTempUri != null && "file".equals(cameraTempUri.getScheme())) {
                File temp = new File(Objects.requireNonNull(cameraTempUri.getPath()));
                if (temp.exists() && temp.delete()) {
                    AppLogger.d(getClass(), "Camera temp file deleted");
                }
            }

            if ("file".equals(selectedFileUri.getScheme())) {
                if (selectedFileUri.getPath() != null) {
                    File temp = new File(selectedFileUri.getPath());
                    if (temp.exists()) {
                        if (temp.delete()) {
                            AppLogger.d(getClass(), "Cache File Deleted");
                        }
                    }
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteCameraTempFile", e);
        }
    }

    private File compressImage(Uri uri, File outFile) throws Exception {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream boundsInput = getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(boundsInput, null, options);

        if (boundsInput != null) {
            boundsInput.close();
        }

        // Original size
        int photoW = options.outWidth;
        int photoH = options.outHeight;

        // Max size
        int maxWidth = 1280;
        int maxHeight = 1280;

        int scaleFactor = 1;

        while ((photoW / scaleFactor) > maxWidth || (photoH / scaleFactor) > maxHeight) {
            scaleFactor *= 2;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();

        decodeOptions.inSampleSize = scaleFactor;
        decodeOptions.inPreferredConfig = Bitmap.Config.RGB_565;

        InputStream bitmapInput = getContentResolver().openInputStream(uri);

        Bitmap bitmap = BitmapFactory.decodeStream(bitmapInput, null, decodeOptions);

        if (bitmapInput != null) {
            bitmapInput.close();
        }

        int quality = 90;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        do {
            bos.reset();
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            }
            quality -= 5;
        } while ((bos.size() / 1024) > 500 && quality >= 40);

        FileOutputStream fos = new FileOutputStream(outFile, false);
        fos.write(bos.toByteArray());
        fos.flush();
        fos.close();

        if (bitmap != null) {
            bitmap.recycle();
        }

        return outFile;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void processAndSaveImage(Uri uri, String tempDispatchId) {
        try {

            File mainFolder = new File(getFilesDir(), "container_images");
            if (!mainFolder.exists() && !mainFolder.mkdirs()) {
                AppLogger.e(getClass(), "processAndSaveImage", new Exception("Failed to create main folder"));
                return;
            }

            File containerFolder = new File(mainFolder, tempDispatchId);
            if (!containerFolder.exists() && !containerFolder.mkdirs()) {
                AppLogger.e(getClass(), "processAndSaveImage", new Exception("Failed to create container folder"));
                return;
            }

            String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";

            File compressedFile = new File(containerFolder, fileName);

            String imagePath = compressImage(uri, compressedFile).getAbsolutePath();

            ContainerImages image = new ContainerImages();
            image.tempDispatchId = tempDispatchId;
            image.imagePath = imagePath;
            image.createdAt = System.currentTimeMillis();
            image.isDeleted = false;
            image.isSynced = false;
            image.tempContainerImageId = "CI_" + System.currentTimeMillis();

            // INSERT ROOM DB
            new Thread(() -> {
                long insertedId = dispatchViewModel.insertContainerImage(image);
                runOnUiThread(() -> {
                    if (insertedId > 0) {
                        image.id = (int) insertedId;

                        containerImagesList.add(0, image);

                        if (containerImagesRecyclerViewAdapter != null) {
                            containerImagesRecyclerViewAdapter.notifyItemInserted(0);
                        }

                        llContainerImages.setVisibility(View.VISIBLE);
                        tvNoDataFound.setVisibility(View.GONE);

                        deleteCameraTempFile();

                        Toast.makeText(getApplicationContext(), getString(R.string.image_added), Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        } catch (Exception e) {
            AppLogger.e(getClass(), "processAndSaveImage", e);
        }
    }

    private void setupRecyclerView() {

        containerImagesRecyclerViewAdapter = new RecyclerViewAdapter<>(getApplicationContext(), containerImagesList, R.layout.row_item_container_image) {

            @Override
            public void onPostBindViewHolder(ViewHolder holder, ContainerImages containerImage) {
                if (containerImage != null) {
                    AppCompatImageView ivContainerPhoto = holder.getView(R.id.ivContainerPhoto);

                    Glide.with(getApplicationContext())
                            .load(new File(containerImage.imagePath))
                            .centerCrop()
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.stat_notify_error)
                            .into(ivContainerPhoto);

                    holder.getView(R.id.cardDelete).setOnClickListener(v -> deleteContainerImage(containerImage, holder.getBindingAdapterPosition()));
                }
            }
        };

        rvContainerImages.setLayoutManager(new GridLayoutManager(this, 3));
        rvContainerImages.setHasFixedSize(true);
        rvContainerImages.setAdapter(containerImagesRecyclerViewAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void observeData() {
        dispatchViewModel.getContainerImages(selectedTempDispatchId).observe(this, list -> {
            containerImagesList.clear();
            if (list != null && !list.isEmpty()) {
                containerImagesList.addAll(list);
                llContainerImages.setVisibility(View.VISIBLE);
                tvNoContainerPhotoDataFound.setVisibility(View.GONE);
            } else {
                llContainerImages.setVisibility(View.GONE);
                tvNoContainerPhotoDataFound.setVisibility(View.VISIBLE);
            }

            if (containerImagesRecyclerViewAdapter != null) {
                containerImagesRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    private void deleteContainerImage(ContainerImages containerImage, int position) {
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
                try {
                    new Thread(() -> {
                        int deletedRows;
                        // SOFT DELETE
                        if (containerImage.isSynced) {
                            deletedRows = dispatchViewModel.softDeleteImage(containerImage.tempContainerImageId);
                        } else {
                            // HARD DELETE
                            deletedRows = dispatchViewModel.hardDeleteImage(containerImage.tempContainerImageId);
                        }

                        // DELETE FILE
                        File imageFile = new File(containerImage.imagePath);
                        File parentFolder = imageFile.getParentFile();

                        if (imageFile.exists()) {
                            boolean imageDeleted = imageFile.delete();

                            if (!imageDeleted) {
                                AppLogger.e(getClass(), "deleteContainerImage", new Exception("Failed to delete image file"));
                            }
                        }

                        // DELETE EMPTY FOLDER
                        if (parentFolder != null && parentFolder.exists()) {
                            File[] files = parentFolder.listFiles();

                            if (files == null || files.length == 0) {
                                boolean folderDeleted = parentFolder.delete();

                                if (!folderDeleted) {
                                    AppLogger.e(getClass(), "deleteContainerImage", new Exception("Failed to delete empty folder"));
                                }
                            }
                        }

                        int finalDeletedRows = deletedRows;
                        runOnUiThread(() -> {
                            if (finalDeletedRows > 0) {
                                if (position >= 0 && position < containerImagesList.size()) {
                                    containerImagesList.remove(position);
                                    if (containerImagesRecyclerViewAdapter != null) {
                                        containerImagesRecyclerViewAdapter.notifyItemRemoved(position);
                                    }
                                }

                                if (containerImagesList.isEmpty()) {
                                    llContainerImages.setVisibility(View.GONE);
                                    tvNoContainerPhotoDataFound.setVisibility(View.VISIBLE);
                                }

                                Toast.makeText(getApplicationContext(), getString(R.string.photo_deleted), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();

                    dialog.dismiss();
                } catch (Exception e) {
                    AppLogger.e(getClass(), "deleteContainerImage", e);
                }
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteContainerImage", e);
        }
    }
}