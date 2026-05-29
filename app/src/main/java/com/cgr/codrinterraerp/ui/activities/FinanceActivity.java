package com.cgr.codrinterraerp.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

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

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new FinanceDashboardFragment())
                    .commit();

            bottomNav.setOnItemSelectedListener(item -> {
                View itemView = bottomNav.findViewById(item.getItemId());
                if (itemView != null) {
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.nav_item_animation);
                    itemView.startAnimation(animation);
                }

                Fragment fragment = null;
                int id = item.getItemId();
                if (id == R.id.nav_dashboard) {
                    fragment = new FinanceDashboardFragment();
                } else if (id == R.id.nav_report) {
                    fragment = new FinanceReportFragment();
                } else if (id == R.id.nav_transactions) {
                    fragment = new FinanceTransactionFragment();
                }

                if (fragment != null) {
                    loadFragment(fragment);
                    return true;
                }
                return false;
            });

            getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                Fragment fragment = getSupportFragmentManager()
                        .findFragmentById(R.id.fragmentContainer);

                if (fragment instanceof FinanceDashboardFragment) {
                    bottomNav.setSelectedItemId(R.id.nav_dashboard);
                } else if (fragment instanceof FinanceReportFragment) {
                    bottomNav.setSelectedItemId(R.id.nav_report);
                }  else if (fragment instanceof FinanceTransactionFragment) {
                    bottomNav.setSelectedItemId(R.id.nav_transactions);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void loadFragment(Fragment fragment) {

        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        // Avoid reloading same fragment
        if (current != null && current.getClass().equals(fragment.getClass())) {
            return;
        }

        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_fast_in, R.anim.fade_fast_out, R.anim.fade_fast_in, R.anim.fade_fast_out)
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }
}