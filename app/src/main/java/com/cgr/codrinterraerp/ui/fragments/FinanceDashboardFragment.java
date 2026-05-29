package com.cgr.codrinterraerp.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.ui.adapters.TabPagerFinanceAdapter;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FinanceDashboardFragment extends Fragment {

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

            txtTitle.setText(R.string.finance);
            imgBack.setOnClickListener(v -> requireActivity().finish());

            // ✅ Adapter
            TabPagerFinanceAdapter adapter = new TabPagerFinanceAdapter(this);
            viewPagerFinance.setAdapter(adapter);

            // ✅ IMPORTANT FIX (prevents overlap)
            viewPagerFinance.setOffscreenPageLimit(1);

            // ✅ Attach TabLayout
            new TabLayoutMediator(tabLayoutFinance, viewPagerFinance, (tab, position) -> {
                if (position == 0) {
                    tab.setText(getString(R.string.credit));
                    tab.setIcon(R.drawable.ic_income);
                } else {
                    tab.setText(getString(R.string.debit));
                    tab.setIcon(R.drawable.ic_expense_unselected);
                }
            }).attach();

            // ✅ Tab icon handling
            tabLayoutFinance.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

                @Override
                public void onTabSelected(TabLayout.Tab tab) {

                    if (tab.getPosition() == 0) {
                        tab.setIcon(R.drawable.ic_income);
                    } else {
                        tab.setIcon(R.drawable.ic_expense);
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
                        tab.setIcon(R.drawable.ic_income_unselected);
                    } else {
                        tab.setIcon(R.drawable.ic_expense_unselected);
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
            fabAdd.setOnClickListener(v -> v.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(120)
                    .withEndAction(() ->
                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(120)
                                    .start())
                    .start());
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }
}