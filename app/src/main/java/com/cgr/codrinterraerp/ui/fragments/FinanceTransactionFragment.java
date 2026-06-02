package com.cgr.codrinterraerp.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.AccountHeads;
import com.cgr.codrinterraerp.db.entities.IncomeData;
import com.cgr.codrinterraerp.ui.adapters.AccountHeadsAdapter;
import com.cgr.codrinterraerp.ui.adapters.FinanceTransactionPagerAdapter;
import com.cgr.codrinterraerp.ui.adapters.IncomeDataAdapter;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.utils.SimpleTextWatcher;
import com.cgr.codrinterraerp.viewmodel.FinanceViewModel;
import com.cgr.codrinterraerp.viewmodel.MasterViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FinanceTransactionFragment extends Fragment {

    private AppCompatTextView tvBalanceAmount, tvCreditAmount, tvDebitAmount;
    private MaterialAutoCompleteTextView etAccountHead, etConceptGeneral;
    private FinanceViewModel financeViewModel;
    private MasterViewModel masterViewModel;
    private double creditAmount = 0, debitAmount = 0;
    private int filterTransactionId = 0, filterAccountHeadId = 0;
    private String selectedStartDate = null, selectedEndDate = null;
    private boolean isFilterApplied = false;
    private Long selectedStartDateMillis = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finance_transaction, container, false);
        try {
            TabLayout tabLayoutFinance = view.findViewById(R.id.tabLayoutFinance);
            ViewPager2 viewPagerFinance = view.findViewById(R.id.viewPagerFinance);
            tvBalanceAmount = view.findViewById(R.id.tvBalanceAmount);
            tvCreditAmount = view.findViewById(R.id.tvCreditAmount);
            tvDebitAmount = view.findViewById(R.id.tvDebitAmount);
            AppCompatImageView ivFilter = view.findViewById(R.id.ivFilter);

            financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
            masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);

            // ✅ Adapter
            FinanceTransactionPagerAdapter adapter = new FinanceTransactionPagerAdapter(this);
            viewPagerFinance.setAdapter(adapter);

            // ✅ IMPORTANT FIX (prevents overlap)
            viewPagerFinance.setOffscreenPageLimit(1);

            // ✅ Attach TabLayout
            new TabLayoutMediator(tabLayoutFinance, viewPagerFinance, (tab, position) -> {
                if (position == 0) {
                    tab.setText(getString(R.string.debit));
                    tab.setIcon(R.drawable.ic_expense);
                } else {
                    tab.setText(getString(R.string.credit));
                    tab.setIcon(R.drawable.ic_income_unselected);
                }
            }).attach();

            // ✅ Tab icon handling
            tabLayoutFinance.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

                @Override
                public void onTabSelected(TabLayout.Tab tab) {

                    if (tab.getPosition() == 0) {
                        tab.setIcon(R.drawable.ic_expense);
                    } else {
                        tab.setIcon(R.drawable.ic_income);
                    }

                    // 🔥 Animation
                    View tabView = ((ViewGroup) tabLayoutFinance.getChildAt(0)).getChildAt(tab.getPosition());

                    tabView.animate()
                            .scaleX(1.1f)
                            .scaleY(1.1f)
                            .setDuration(150)
                            .withEndAction(() -> tabView.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(150)
                                    .start())
                            .start();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {
                        tab.setIcon(R.drawable.ic_expense_unselected);
                    } else {
                        tab.setIcon(R.drawable.ic_income_unselected);
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });

            viewPagerFinance.setPageTransformer(null);
            viewPagerFinance.setOffscreenPageLimit(1);
            viewPagerFinance.setUserInputEnabled(true);

            fetchAmountTransactions();

            masterViewModel.getAccountHeadsLiveData().observe(getViewLifecycleOwner(), accountHeadEntities -> {
                List<AccountHeads> accountHeads = new ArrayList<>(accountHeadEntities);
                if (!accountHeads.isEmpty()) {

                    if (filterAccountHeadId > 0) {
                        for (AccountHeads entity : accountHeadEntities) {
                            if (entity.getAccountHeadId() == filterAccountHeadId) {
                                etAccountHead.setText(entity.getAccountHeadName(), false);
                                break;
                            }
                        }
                    }

                    AccountHeadsAdapter accountHeadsAdapter = new AccountHeadsAdapter(requireContext(), accountHeads);
                    etAccountHead.setAdapter(accountHeadsAdapter);

                    setupAutoComplete(etAccountHead);

                    etAccountHead.setOnItemClickListener((parent, view1, position, id) -> {
                        AccountHeads selected = (AccountHeads) parent.getItemAtPosition(position);
                        filterAccountHeadId = selected.getAccountHeadId();
                        etAccountHead.setText(selected.getAccountHeadName(), false); // ← prevents filtering issue
                    });
                }
            });

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

            ivFilter.setOnClickListener(v -> showFilterDialog());
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void fetchAmountTransactions() {
        try {

            financeViewModel.fetchTotalCreditAmount(isFilterApplied, filterTransactionId);
            financeViewModel.fetchTotalDebitAmount(isFilterApplied, filterTransactionId, filterAccountHeadId, selectedStartDate, selectedEndDate);

            financeViewModel.getTotalCreditAmountTransactions().observe(getViewLifecycleOwner(), aDouble -> {
                creditAmount = aDouble;
                tvCreditAmount.setText(CommonUtils.currencyFormat(aDouble));
                calculateBalance(creditAmount, debitAmount);
            });

            financeViewModel.getTotalDebitAmountTransactions().observe(getViewLifecycleOwner(), aDouble -> {
                debitAmount = aDouble;
                tvDebitAmount.setText(CommonUtils.currencyFormat(aDouble));
                calculateBalance(creditAmount, debitAmount);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchAmountTransactions", e);
        }
    }

    private void calculateBalance(double creditAmount, double debitAmount) {
        double balanceAmount = creditAmount - debitAmount;
        tvBalanceAmount.setText(CommonUtils.currencyFormat(balanceAmount));
    }

    private void showFilterDialog() {
        try {

            AlertDialog dialog;

            View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter_transactions, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(view);

            dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            etAccountHead = view.findViewById(R.id.etAccountHead);
            etConceptGeneral = view.findViewById(R.id.etConceptGeneral);
            TextInputEditText etStartDate = view.findViewById(R.id.etStartDate);
            TextInputEditText etEndDate = view.findViewById(R.id.etEndDate);
            MaterialButton btnApply = view.findViewById(R.id.btnApply);
            MaterialButton btnReset = view.findViewById(R.id.btnReset);

            if (selectedStartDate != null && selectedEndDate != null) {
                etStartDate.setText(selectedStartDate);
                etEndDate.setText(selectedEndDate);
            }

            masterViewModel.getAllAccountHeadsLiveData();
            masterViewModel.getAllIncomeDataLiveData();

            etStartDate.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (s != null && !s.toString().isEmpty()) {
                        etEndDate.setText(s.toString());
                    }
                }
            });

            etStartDate.setOnClickListener(v -> CommonUtils.showDatePicker(requireContext(), etStartDate, null, millis -> {
                        // ✅ Save start date
                        selectedStartDateMillis = millis;

                        // ✅ Format date string from millis
                        String startDateText =
                                Objects.requireNonNull(etStartDate.getText()).toString();

                        // ✅ SET END DATE TEXT = START DATE
                        etEndDate.setText(startDateText);
                        etEndDate.setError(null);
                    }
            ));

            etEndDate.setOnClickListener(v -> {

                if (selectedStartDateMillis == null) {
                    etStartDate.setError(getString(R.string.select_start_date_first));
                    return;
                } else {
                    etStartDate.setError(null);
                }

                CommonUtils.showDatePicker(requireContext(), etEndDate, selectedStartDateMillis, null);
            });

            btnApply.setOnClickListener(v -> {

                isFilterApplied = true;

                financeViewModel.applyFilter(
                        true,
                        filterTransactionId,
                        filterAccountHeadId,
                        selectedStartDate,
                        selectedEndDate
                );

                fetchAmountTransactions();
                dialog.dismiss();
            });

            btnReset.setOnClickListener(v -> {
                resetFilters();

                financeViewModel.applyFilter(
                        false,
                        0,
                        0,
                        null,
                        null
                );

                fetchAmountTransactions();
                dialog.dismiss();
            });

            dialog.show();

        } catch (Exception e) {
            AppLogger.e(getClass(), "showFilterDialog", e);
        }
    }

    private void resetFilters() {
        isFilterApplied = false;
        filterTransactionId = 0;
        filterAccountHeadId = 0;
        selectedStartDate = null;
        selectedEndDate = null;
        selectedStartDateMillis = null;
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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}