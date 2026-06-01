package com.cgr.codrinterraerp.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.ui.fragments.FinanceDashboardFragment;
import com.cgr.codrinterraerp.ui.fragments.FinanceReportFragment;
import com.cgr.codrinterraerp.ui.fragments.FinanceTransactionFragment;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FinanceActivity extends BaseActivity {

    private BottomNavigationView bottomNav;
    private FinanceDashboardFragment dashboardFragment;
    private FinanceReportFragment reportFragment;
    private FinanceTransactionFragment transactionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);
        statusBarSetting(false);
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {

            View fragmentContainer = findViewById(R.id.fragmentContainer);
            bottomNav = findViewById(R.id.bottomNav);
            ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, systemBars.top, 0, 0); // Only top padding
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, 0, 0, systemBars.bottom);
                return insets;
            });

            dashboardFragment = new FinanceDashboardFragment();
            reportFragment = new FinanceReportFragment();
            transactionFragment = new FinanceTransactionFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, dashboardFragment, "DASHBOARD")
                    .add(R.id.fragmentContainer, reportFragment, "REPORT")
                    .hide(reportFragment)
                    .add(R.id.fragmentContainer, transactionFragment, "TRANSACTION")
                    .hide(transactionFragment)
                    .commit();

            bottomNav.setOnItemSelectedListener(item -> {

                View itemView = bottomNav.findViewById(item.getItemId());

                if (itemView != null) {
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.nav_item_animation);
                    itemView.startAnimation(animation);
                }

                int id = item.getItemId();

                if (id == R.id.nav_dashboard) {
                    showFragment(dashboardFragment);
                    return true;
                }

                if (id == R.id.nav_report) {
                    showFragment(reportFragment);
                    return true;
                }

                if (id == R.id.nav_transactions) {
                    showFragment(transactionFragment);
                    return true;
                }

                return false;
            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    int selectedItem = bottomNav.getSelectedItemId();
                    if (selectedItem != R.id.nav_dashboard) {
                        bottomNav.setSelectedItemId(R.id.nav_dashboard);
                    } else {
                        finish();
                    }
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void showFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_fast_in, R.anim.fade_fast_out)
                .hide(dashboardFragment)
                .hide(reportFragment)
                .hide(transactionFragment)
                .show(fragment)
                .commit();
    }
}