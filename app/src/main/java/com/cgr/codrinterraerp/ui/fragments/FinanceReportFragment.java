package com.cgr.codrinterraerp.ui.fragments;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.caverock.androidsvg.SVG;
import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.IncomeData;
import com.cgr.codrinterraerp.model.AccountHeadReportData;
import com.cgr.codrinterraerp.ui.adapters.IncomeDataAdapter;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.utils.PieChartMarkerView;
import com.cgr.codrinterraerp.utils.SimpleTextWatcher;
import com.cgr.codrinterraerp.viewmodel.FinanceViewModel;
import com.cgr.codrinterraerp.viewmodel.MasterViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FinanceReportFragment extends Fragment {

    private PieChart pieChart;
    private RecyclerView rvReportLegend;
    private LinearLayout llNoData;
    private CardView cardChart;
    private MaterialAutoCompleteTextView etConceptGeneral;
    private int filterTransactionId = 0, filterChartType = 1;
    private String selectedStartDate = null, selectedEndDate = null;
    private Long selectedStartDateMillis = null;
    private FinanceViewModel financeViewModel;
    private MasterViewModel masterViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finance_report, container, false);
        try {
            pieChart = view.findViewById(R.id.pieChart);
            rvReportLegend = view.findViewById(R.id.rvReportLegend);
            cardChart = view.findViewById(R.id.cardChart);
            llNoData = view.findViewById(R.id.llNoData);
            AppCompatImageView ivFilter = view.findViewById(R.id.ivFilter);

            financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
            masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);

            ivFilter.setOnClickListener(v -> showFilterDialog());

            financeViewModel.applyFilter(
                    false,
                    0,
                    0,
                    null,
                    null
            );

            financeViewModel.getFilterLiveData().observe(
                    getViewLifecycleOwner(),
                    filter -> {

                        filterTransactionId = filter.transactionId;

                        bindReportData(
                                filter.isFilterApplied ? filterChartType : 1,
                                filter.transactionId,
                                filter.startDate,
                                filter.endDate
                        );
                    }
            );
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void showFilterDialog() {
        try {

            AlertDialog dialog;

            View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter_transactions, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(view);

            dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            etConceptGeneral = view.findViewById(R.id.etConceptGeneral);
            TextInputLayout tiAccountHead = view.findViewById(R.id.tiAccountHead);
            TextInputEditText etStartFilterExpenseDate = view.findViewById(R.id.etStartDate);
            TextInputEditText etEndFilterExpenseDate = view.findViewById(R.id.etEndDate);
            LinearLayout layoutChartType = view.findViewById(R.id.layoutChartType);
            MaterialButton btnApply = view.findViewById(R.id.btnApply);
            MaterialButton btnReset = view.findViewById(R.id.btnReset);
            RadioGroup rgChartType = view.findViewById(R.id.rgChartType);

            if (filterChartType == 1) {
                rgChartType.check(R.id.rbAccountHead);
            } else {
                rgChartType.check(R.id.rbCreditVsDebit);
            }

            // CONCEPT GENERAL
            masterViewModel.getAllIncomeDataLiveData();
            masterViewModel.getIncomeDataLiveData().observe(getViewLifecycleOwner(), incomeDataEntities -> {
                List<IncomeData> incomeData = new ArrayList<>(incomeDataEntities);
                if (!incomeData.isEmpty()) {

                    if (filterTransactionId > 0) {
                        for (IncomeData entity : incomeDataEntities) {
                            if (entity.getCreditTransactionId() == filterTransactionId) {
                                etConceptGeneral.setText(entity.getConceptGeneral(), false);
                                break;
                            }
                        }
                    }

                    IncomeDataAdapter incomeDataAdapter = new IncomeDataAdapter(requireContext(), incomeData);
                    etConceptGeneral.setAdapter(incomeDataAdapter);

                    setupAutoComplete(etConceptGeneral);

                    etConceptGeneral.setOnItemClickListener((parent, view1, position, id) -> {
                        IncomeData selected = (IncomeData) parent.getItemAtPosition(position);
                        filterTransactionId = selected.getCreditTransactionId();
                        etConceptGeneral.setText(selected.getConceptGeneral(), false); // ← prevents filtering issue
                    });
                }
            });

            tiAccountHead.setVisibility(View.GONE);
            layoutChartType.setVisibility(View.VISIBLE);

            if (selectedStartDate != null && selectedEndDate != null) {
                etStartFilterExpenseDate.setText(selectedStartDate);
                etEndFilterExpenseDate.setText(selectedEndDate);
            }

            etStartFilterExpenseDate.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (s != null && !s.toString().isEmpty()) {
                        etEndFilterExpenseDate.setText(s.toString());
                    }
                }
            });

            etStartFilterExpenseDate.setOnClickListener(v -> CommonUtils.showDatePicker(requireContext(), etStartFilterExpenseDate, null, millis -> {
                        // ✅ Save start date
                        selectedStartDateMillis = millis;

                        // ✅ Format date string from millis
                        String startDateText =
                                Objects.requireNonNull(etStartFilterExpenseDate.getText()).toString();

                        // ✅ SET END DATE TEXT = START DATE
                        etEndFilterExpenseDate.setText(startDateText);
                        etEndFilterExpenseDate.setError(null);
                    }
            ));

            etEndFilterExpenseDate.setOnClickListener(v -> {

                if (selectedStartDateMillis == null) {
                    etStartFilterExpenseDate.setError(getString(R.string.select_start_date_first));
                    return;
                } else {
                    etEndFilterExpenseDate.setError(null);
                }

                CommonUtils.showDatePicker(requireContext(), etEndFilterExpenseDate, selectedStartDateMillis, null);
            });

            btnApply.setOnClickListener(v -> {

                int selectedId = rgChartType.getCheckedRadioButtonId();

                if (selectedId == R.id.rbAccountHead) {
                    filterChartType = 1; // Account Head wise
                } else if (selectedId == R.id.rbCreditVsDebit) {
                    filterChartType = 2; // Credit vs Debit
                }

                selectedStartDate = Objects.requireNonNull(etStartFilterExpenseDate.getText()).toString();
                selectedEndDate = Objects.requireNonNull(etEndFilterExpenseDate.getText()).toString();

                bindReportData(filterChartType, filterTransactionId,
                        CommonUtils.convertDateTimeFormat(selectedStartDate, "dd/MM/yyyy", "yyyy-MM-dd", requireContext()),
                        CommonUtils.convertDateTimeFormat(selectedEndDate, "dd/MM/yyyy", "yyyy-MM-dd", requireContext()));

                dialog.dismiss();
            });

            btnReset.setOnClickListener(v -> {
                resetFilters();
                dialog.dismiss();
            });

            dialog.show();

        } catch (Exception e) {
            AppLogger.e(getClass(), "showFilterDialog", e);
        }
    }

    private void bindReportData(int chartType, int filterTransactionId, String filterStartDate, String filterEndDate) {
        try {
            if (chartType == 1) {
                List<AccountHeadReportData> accountHeadReportDataList = financeViewModel.getExpenseByAccountHead(filterTransactionId, filterStartDate, filterEndDate);
                if (!accountHeadReportDataList.isEmpty()) {
                    setupPieChart(chartType, accountHeadReportDataList, 0, 0);
                    setupLegendData(chartType, accountHeadReportDataList, 0, 0);

                    cardChart.setVisibility(View.VISIBLE);
                    rvReportLegend.setVisibility(View.VISIBLE);
                    pieChart.setVisibility(View.VISIBLE);
                    llNoData.setVisibility(View.GONE);
                } else {
                    cardChart.setVisibility(View.GONE);
                    rvReportLegend.setVisibility(View.GONE);
                    pieChart.setVisibility(View.GONE);
                    llNoData.setVisibility(View.VISIBLE);
                }
            } else {
                double totalCreditAmount = financeViewModel.getIncomeTotal(filterTransactionId);
                double totalDebitAmount = financeViewModel.getExpenseTotal(filterTransactionId, filterStartDate, filterEndDate);

                if (totalCreditAmount > 0 || totalDebitAmount > 0) {
                    setupPieChart(chartType, null, totalCreditAmount, totalDebitAmount);
                    setupLegendData(chartType, null, totalCreditAmount, totalDebitAmount);

                    cardChart.setVisibility(View.VISIBLE);
                    rvReportLegend.setVisibility(View.VISIBLE);
                    pieChart.setVisibility(View.VISIBLE);
                    llNoData.setVisibility(View.GONE);
                } else {
                    cardChart.setVisibility(View.GONE);
                    rvReportLegend.setVisibility(View.GONE);
                    pieChart.setVisibility(View.GONE);
                    llNoData.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindReportData", e);
        }
    }

    private void resetFilters() {
        filterTransactionId = 0;
        selectedStartDate = null;
        selectedEndDate = null;
        selectedStartDateMillis = null;

        financeViewModel.applyFilter(
                false,
                0,
                0,
                null,
                null
        );
    }

    private void setupAutoComplete(MaterialAutoCompleteTextView autoCompleteTextView) {

        autoCompleteTextView.setThreshold(1);

        autoCompleteTextView.setOnClickListener(v -> {
            if (!autoCompleteTextView.isPopupShowing()) {
                autoCompleteTextView.showDropDown();
            }
        });

        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteTextView.showDropDown();
            }
        });
    }

    // CHART VIEW
    private void setupPieChart(int filterChartType, List<AccountHeadReportData> accountHeadReportDataList, double totalCreditAmount, double totalDebitAmount) {
        try {
            ArrayList<PieEntry> pieEntries = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();
            if (filterChartType == 1) {

                for (AccountHeadReportData reportModel : accountHeadReportDataList) {
                    colors.add(Color.parseColor(reportModel.colorCodePrimary));
                    pieEntries.add(new PieEntry(reportModel.percentage, reportModel.accountHeadName, reportModel.totalAmount));
                }

                PieDataSet pieDataSet = getPieDataSet(pieEntries, colors);

                PieData pieData = new PieData(pieDataSet);
                pieData.setValueFormatter(new PercentFormatter());
                pieData.setValueTextSize(10f);
                pieData.setValueTextColor(Color.BLACK);
                pieData.setDrawValues(true);
                pieData.setValueTypeface(ResourcesCompat.getFont(requireContext(), R.font.exo2_semibold));

                pieChart.setData(pieData);
                pieChart.setHoleRadius(45f);
                pieChart.setTransparentCircleRadius(0f);
                pieChart.setUsePercentValues(true);
                pieChart.setDrawCenterText(false);
                pieChart.setDrawEntryLabels(false);
                pieChart.getLegend().setEnabled(false);
                pieChart.setExtraOffsets(20, 18, 20, 18);

                PieChartMarkerView marker = new PieChartMarkerView(requireContext(), R.layout.marker_pie, filterChartType);

                marker.setChartView(pieChart);
                pieChart.setMarker(marker);

                Description description = new Description();
                description.setText("");

                pieChart.setDescription(description);
            } else {

                colors.add(Color.parseColor("#00C853")); // Credit
                colors.add(Color.parseColor("#FF6D00")); // Debit

                pieEntries.add(new PieEntry((float) totalCreditAmount, getString(R.string.credit), totalCreditAmount));
                pieEntries.add(new PieEntry((float) totalDebitAmount, getString(R.string.debit), totalDebitAmount));

                PieDataSet pieDataSet = getPieDataSet(pieEntries, colors);

                PieData pieData = new PieData(pieDataSet);
                pieData.setValueFormatter(new PercentFormatter());
                pieData.setValueTextSize(10f);
                pieData.setValueTextColor(Color.BLACK);
                pieData.setDrawValues(true);
                pieData.setValueTypeface(ResourcesCompat.getFont(requireContext(), R.font.exo2_semibold));

                pieChart.setData(pieData);
                pieChart.setHoleRadius(45f);
                pieChart.setTransparentCircleRadius(0f);
                pieChart.setUsePercentValues(true);
                pieChart.setDrawCenterText(false);
                pieChart.setDrawEntryLabels(false);
                pieChart.getLegend().setEnabled(false);
                pieChart.setExtraOffsets(20, 18, 20, 18);

                PieChartMarkerView marker = new PieChartMarkerView(requireContext(), R.layout.marker_pie, filterChartType);

                marker.setChartView(pieChart);
                pieChart.setMarker(marker);

                Description description = new Description();
                description.setText("");

                pieChart.setDescription(description);
            }
            pieChart.getLegend().setEnabled(false);
            pieChart.setRotationAngle(120f);
            pieChart.invalidate();
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupPieChart", e);
        }
    }

    private void setupLegendData(int filterChartType, List<AccountHeadReportData> accountHeadReportDataList, double totalCreditAmount, double totalDebitAmount) {
        try {
            RecyclerViewAdapter<AccountHeadReportData> reportDataCommonRecyclerViewAdapter;
            if (filterChartType == 1) {
                reportDataCommonRecyclerViewAdapter = new RecyclerViewAdapter<>(requireContext(), accountHeadReportDataList, R.layout.row_item_report_legend) {
                    @Override
                    public void onPostBindViewHolder(@NonNull ViewHolder holder, @NonNull AccountHeadReportData item) {
                        holder.setViewText(R.id.tvAccountHead, item.accountHeadName);

                        float percentage = item.percentage;
                        if (percentage < 0) {
                            percentage = 0;
                        }

                        holder.setViewText(R.id.tvAmount, CommonUtils.currencyFormat(item.totalAmount) + " (" + String.format(Locale.getDefault(),
                                "%.1f", percentage) + "%)");
                        ShapeableImageView imgIcon = holder.getView(R.id.ivIcon);

                        if (item.icon != null) {
                            try {
                                SVG svg = SVG.getFromString(item.icon);
                                PictureDrawable drawable = new PictureDrawable(svg.renderToPicture());
                                imgIcon.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                                imgIcon.setImageDrawable(drawable);
                            } catch (Exception e) {
                                imgIcon.setImageResource(R.drawable.ic_default_accounthead);
                            }
                        } else {
                            imgIcon.setImageResource(R.drawable.ic_default_accounthead);
                        }
                    }
                };
            } else {

                List<AccountHeadReportData> accountHeadReportData = new ArrayList<>();

                AccountHeadReportData accountHeadData = new AccountHeadReportData();
                accountHeadData.totalAmount = totalCreditAmount;
                accountHeadData.accountHeadName = getString(R.string.credit);
                accountHeadData.accountHeadId = 1;
                accountHeadReportData.add(accountHeadData);

                accountHeadData = new AccountHeadReportData();
                accountHeadData.totalAmount = totalDebitAmount;
                accountHeadData.accountHeadName = getString(R.string.debit);
                accountHeadData.accountHeadId = 2;
                accountHeadReportData.add(accountHeadData);

                reportDataCommonRecyclerViewAdapter = new RecyclerViewAdapter<>(requireContext(), accountHeadReportData, R.layout.row_item_report_legend) {
                    @Override
                    public void onPostBindViewHolder(@NonNull ViewHolder holder, @NonNull AccountHeadReportData item) {

                        holder.setViewText(R.id.tvAccountHead, item.accountHeadName);

                        ShapeableImageView shapeableImageView = holder.getView(R.id.ivIcon);
                        shapeableImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                        if (item.accountHeadId == 1) {
                            shapeableImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_credit));
                        } else {
                            shapeableImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_debit));
                        }

                        holder.setViewText(R.id.tvAmount, CommonUtils.currencyFormat(item.totalAmount));
                    }
                };
            }

            rvReportLegend.setVisibility(View.VISIBLE);
            rvReportLegend.setAdapter(reportDataCommonRecyclerViewAdapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupLegendData", e);
        }
    }

    private PieDataSet getPieDataSet(ArrayList<PieEntry> pieEntries, ArrayList<Integer> colors) {

        PieDataSet pieDataSet = new PieDataSet(pieEntries, null);
        pieDataSet.setColors(colors);

        // 🔽 FORCE VALUES OUTSIDE
        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setValueLinePart1OffsetPercentage(80f); // ⭐ IMPORTANT
        pieDataSet.setValueLinePart1Length(0.3f); // shorter first segment
        pieDataSet.setValueLinePart2Length(0.7f); // longer second segment
        pieDataSet.setValueLineColor(Color.GRAY);
        pieDataSet.setValueLineWidth(1f);
        pieDataSet.setValueLineVariableLength(true);

        pieDataSet.setSliceSpace(2f); // more gap between slices
        pieDataSet.setSelectionShift(10f);

        return pieDataSet;
    }

    @Override
    public void onResume() {
        super.onResume();
        financeViewModel.applyFilter(
                false,
                0,
                0,
                null,
                null
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}