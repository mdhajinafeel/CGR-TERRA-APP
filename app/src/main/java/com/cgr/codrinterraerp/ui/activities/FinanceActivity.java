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
    private final Fragment dashboardFragment = new FinanceDashboardFragment();
    private final Fragment reportFragment = new FinanceReportFragment();
    private final Fragment transactionFragment = new FinanceTransactionFragment();
    private Fragment activeFragment = dashboardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);
        statusBarSetting(false);
        hideKeyboard(this);
        initComponents(savedInstanceState);
    }

    private void initComponents(Bundle savedInstanceState) {
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

            if (savedInstanceState == null) {

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.fragmentContainer, transactionFragment, "TRANSACTIONS")
                        .hide(transactionFragment)
                        .commit();

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.fragmentContainer, reportFragment, "REPORTS")
                        .hide(reportFragment)
                        .commit();

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.fragmentContainer, dashboardFragment, "DASHBOARD")
                        .commit();

                activeFragment = dashboardFragment;
            }

            bottomNav.setOnItemSelectedListener(item -> {

                View itemView = bottomNav.findViewById(item.getItemId());

                if (itemView != null) {
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.nav_item_animation);
                    itemView.startAnimation(animation);
                }

                int id = item.getItemId();

                if (id == R.id.nav_dashboard) {
                    switchFragment(dashboardFragment);
                    return true;
                }

                if (id == R.id.nav_report) {
                    switchFragment(reportFragment);
                    return true;
                }

                if (id == R.id.nav_transactions) {
                    switchFragment(transactionFragment);
                    return true;
                }

                return false;
            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            if (!(activeFragment instanceof FinanceDashboardFragment)) {
                                bottomNav.setSelectedItemId(R.id.nav_dashboard);
                                switchFragment(dashboardFragment);
                                return;
                            }
                            finish();
                        }
                    });
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void switchFragment(Fragment target) {

        if (activeFragment == target) {
            return;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.fade_fast_in,
                        R.anim.fade_fast_out
                )
                .hide(activeFragment)
                .show(target)
                .commit();

        activeFragment = target;
    }

    public void openTransactionsFragment() {
        bottomNav.setSelectedItemId(R.id.nav_transactions);
    }
}