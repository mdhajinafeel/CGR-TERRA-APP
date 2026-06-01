package com.cgr.codrinterraerp.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.ui.adapters.FinanceTransactionPagerAdapter;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.viewmodel.FinanceViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FinanceTransactionFragment extends Fragment {

    private AppCompatTextView tvBalanceAmount, tvCreditAmount, tvDebitAmount;
    private FinanceViewModel financeViewModel;
    private double creditAmount = 0, debitAmount = 0;
    private int filterTransactionId = 0, filterAccountHeadId = 0;
    private String selectedStartDate = "", selectedEndDate = "";
    private boolean isFilterApplied = false;

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

            financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}