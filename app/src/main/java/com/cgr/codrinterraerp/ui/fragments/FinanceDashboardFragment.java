package com.cgr.codrinterraerp.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.ui.activities.ExpenseActivity;
import com.cgr.codrinterraerp.ui.activities.FinanceActivity;
import com.cgr.codrinterraerp.ui.adapters.FinanceDashboardPagerAdapter;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.viewmodel.FinanceViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FinanceDashboardFragment extends Fragment {

    private AppCompatTextView tvBalanceAmount, tvCreditAmount, tvDebitAmount;
    private FinanceViewModel financeViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finance_dashboard, container, false);
        try {
            AppCompatImageView imgBack = view.findViewById(R.id.imgBack);
            AppCompatTextView txtTitle = view.findViewById(R.id.txtTitle);
            FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
            TabLayout tabLayoutFinance = view.findViewById(R.id.tabLayoutFinance);
            ViewPager2 viewPagerFinance = view.findViewById(R.id.viewPagerFinance);
            tvBalanceAmount = view.findViewById(R.id.tvBalanceAmount);
            tvCreditAmount = view.findViewById(R.id.tvCreditAmount);
            tvDebitAmount = view.findViewById(R.id.tvDebitAmount);
            AppCompatTextView tvSeeAll = view.findViewById(R.id.tvSeeAll);

            txtTitle.setText(R.string.finance);
            imgBack.setOnClickListener(v -> requireActivity().finish());

            financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

            // ✅ Adapter
            FinanceDashboardPagerAdapter adapter = new FinanceDashboardPagerAdapter(this);
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

            // FAB CLICK
            fabAdd.setOnClickListener(v -> {
                v.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(120)
                        .withEndAction(() ->
                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(120)
                                        .start())
                        .start();

                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
                Intent intent = new Intent(requireActivity(), ExpenseActivity.class);
                intent.putExtra("isEdit", false);
                expenseResultLauncher.launch(intent, options);
            });

            tvSeeAll.setOnClickListener(v -> {
                if (getActivity() instanceof FinanceActivity) {
                    ((FinanceActivity) getActivity()).openTransactionsFragment();
                }
            });

            fetchSummaryData();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    public void fetchSummaryData() {
        try {
            financeViewModel.getTotalCredit().observe(getViewLifecycleOwner(), credit -> tvCreditAmount.setText(CommonUtils.currencyFormat(credit)));

            financeViewModel.getTotalDebit().observe(getViewLifecycleOwner(), debit -> tvDebitAmount.setText(CommonUtils.currencyFormat(debit)));

            financeViewModel.getBalance().observe(getViewLifecycleOwner(), balance -> tvBalanceAmount.setText(CommonUtils.currencyFormat(balance)));
        }catch (Exception e) {
            AppLogger.e(getClass(), "fetchSummaryData", e);
        }
    }

    private final ActivityResultLauncher<Intent> expenseResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        int savedExpenseId = (int) data.getLongExtra("savedExpenseId", 0);
                        boolean isExpenseEdit = data.getBooleanExtra("isEdit", false);
                        if (savedExpenseId > 0) {
                            if(!isExpenseEdit) {
                                Toast.makeText(requireContext(), getString(R.string.expense_saved), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.expense_updated), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}