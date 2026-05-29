package com.cgr.codrinterraerp.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.viewpager2.widget.ViewPager2;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.ui.adapters.TabPagerAdapter;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WarehouseActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warehouse);
        statusBarSetting(false);
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            AppCompatImageView imgBack = findViewById(R.id.imgBack);
            AppCompatTextView txtTitle = findViewById(R.id.txtTitle);
            TabLayout tabLayout = findViewById(R.id.tabLayout);
            ViewPager2 viewPager = findViewById(R.id.viewPager);

            txtTitle.setText(getString(R.string.warehouse));
            imgBack.setOnClickListener(v -> finish());

            // ✅ Adapter
            TabPagerAdapter adapter = new TabPagerAdapter(this);
            viewPager.setAdapter(adapter);

            // ✅ IMPORTANT FIX (prevents overlap)
            viewPager.setOffscreenPageLimit(1);

            // ✅ Attach TabLayout
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                if (position == 0) {
                    tab.setText(getString(R.string.reception));
                    tab.setIcon(R.drawable.ic_warehouse);
                } else {
                    tab.setText(getString(R.string.dispatch));
                    tab.setIcon(R.drawable.ic_dispatch_unselected);
                }
            }).attach();

            // ✅ Tab icon handling
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

                @Override
                public void onTabSelected(TabLayout.Tab tab) {

                    if (tab.getPosition() == 0) {
                        tab.setIcon(R.drawable.ic_warehouse);
                    } else {
                        tab.setIcon(R.drawable.ic_dispatch);
                    }

                    // 🔥 Animation
                    View tabView = ((ViewGroup) tabLayout.getChildAt(0))
                            .getChildAt(tab.getPosition());

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
                        tab.setIcon(R.drawable.ic_warehouse_unselected);
                    } else {
                        tab.setIcon(R.drawable.ic_dispatch_unselected);
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });

            viewPager.setPageTransformer(null);
            viewPager.setOffscreenPageLimit(1);
            viewPager.setUserInputEnabled(true);

        } catch (Exception e) {
            AppLogger.e(getClass(), "Error in initComponents", e);
        }
    }
}