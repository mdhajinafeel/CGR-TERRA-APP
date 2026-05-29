package com.cgr.codrinterraerp.ui.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.caverock.androidsvg.SVG;
import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.constants.NavigationType;
import com.cgr.codrinterraerp.helper.PreferenceManager;
import com.cgr.codrinterraerp.model.DashboardMenuModel;
import com.cgr.codrinterraerp.model.MenuModel;
import com.cgr.codrinterraerp.model.request.LogoutRequest;
import com.cgr.codrinterraerp.ui.adapters.NavigationAdapters;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.utils.NetworkConnectivity;
import com.cgr.codrinterraerp.utils.NetworkStatusView;
import com.cgr.codrinterraerp.viewmodel.AuthViewModel;
import com.cgr.codrinterraerp.viewmodel.SyncViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private AppCompatTextView tvVersion, tvGreeting, tvName, tvNameMenu, tvOriginName, tvLogout, tvNotificationBadge;
    private AppCompatImageView ivOriginIcon;
    private AppCompatImageView imgClose;
    private DrawerLayout drawerLayout;
    private Toolbar toolBar;
    private ListView lstMenu;
    private List<MenuModel> menuModels;
    private ShapeableImageView profileImage, profileImageMenu;
    private GridLayout gridDashboardMenu;
    private NetworkStatusView networkStatusView;
    private FrameLayout progressBar;
    private SyncViewModel syncViewModel;
    private AuthViewModel authViewModel;
    private long lastBackPressedTime = 0;
    private static final long EXIT_INTERVAL = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusBarSetting(false);
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            drawerLayout = findViewById(R.id.drawerLayout);
            toolBar = findViewById(R.id.toolbar);
            lstMenu = findViewById(R.id.lstMenu);
            imgClose = findViewById(R.id.imgClose);
            profileImage = findViewById(R.id.profileImage);
            profileImageMenu = findViewById(R.id.profileImageMenu);
            networkStatusView = findViewById(R.id.networkStatusView);
            tvGreeting = findViewById(R.id.tvGreeting);
            tvName = findViewById(R.id.tvName);
            tvNameMenu = findViewById(R.id.tvNameMenu);
            ivOriginIcon = findViewById(R.id.ivOriginIcon);
            tvOriginName = findViewById(R.id.tvOriginName);
            tvVersion = findViewById(R.id.tvVersion);
            tvLogout = findViewById(R.id.tvLogout);
            gridDashboardMenu = findViewById(R.id.gridDashboardMenu);
            progressBar = findViewById(R.id.progressBar);
            FrameLayout frameNotification = findViewById(R.id.frameNotification);
            tvNotificationBadge = findViewById(R.id.tvNotificationBadge);

            syncViewModel = new ViewModelProvider(this).get(SyncViewModel.class);
            authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                if (bundle.getBoolean("isFromLogin")) {
                    syncViewModel.masterDownload(true);
                }
            }

            syncViewModel.getErrorMessage().observe(this, s ->
                    showCustomDialog(syncViewModel.getErrorTitle().getValue(), syncViewModel.getErrorMessage().getValue(), false));

            syncViewModel.getProgressState().observe(this, s -> {
                if (s) {
                    showProgress(progressBar);
                } else {
                    hideProgress(progressBar);
                }
            });

            syncViewModel.getSyncStatus().observe(this, aBoolean -> {
                if (aBoolean) {
                    Toast.makeText(getApplicationContext(), getString(R.string.data_has_been_synced_successfully), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.data_sync_failed_please_try_again), Toast.LENGTH_SHORT).show();
                }
            });

            authViewModel.getProgressState().observe(this, aBoolean -> {
                if (aBoolean) {
                    showProgress(progressBar);
                } else {
                    hideProgress(progressBar);
                }
            });

            setDashboardDetails();
            setDashboardMenus();

            syncViewModel.hasUnsyncedDataLiveData().observe(this, aBoolean -> {
                if (aBoolean) {
                    showUnsyncedWarningDialog();
                } else {
                    forceLogout();
                }
            });

            frameNotification.setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, NotificationActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });

            observeNotificationCount();

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {

                    // If Home → press again to exit
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastBackPressedTime < EXIT_INTERVAL) {
                        finish(); // Exit app
                    } else {
                        lastBackPressedTime = currentTime;
                        Toast.makeText(getApplicationContext(), getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void setMenus() {
        setSupportActionBar(toolBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setupDrawerToggle();
            lstMenu.setAdapter(new NavigationAdapters(this, R.layout.row_menu_item, getMenuListItems()));
        }
    }

    public void setupDrawerToggle() {
        try {
            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolBar, R.string.app_name, R.string.app_name);
            actionBarDrawerToggle.syncState();
            actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
            actionBarDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_menu);
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            actionBarDrawerToggle.setToolbarNavigationClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupDrawerToggle", e);
        }
    }

    private List<MenuModel> getMenuListItems() {
        menuModels = new ArrayList<>();
        menuModels.add(new MenuModel(R.drawable.ic_sync, getString(R.string.sync), NavigationType.SYNCHRONIZATION));
        menuModels.add(new MenuModel(R.drawable.ic_export_data, getString(R.string.data_backup), NavigationType.DATA_BACKUP));
        menuModels.add(new MenuModel(R.drawable.ic_logs, getString(R.string.app_status), NavigationType.APP_STATUS));
        return menuModels;
    }

    private void setDashboardMenus() {
        gridDashboardMenu.removeAllViews();
        List<DashboardMenuModel> dashboardMenuModelList = new ArrayList<>();

        if (PreferenceManager.INSTANCE.hasRole("9")) {
            dashboardMenuModelList.add(new DashboardMenuModel(getString(R.string.finca), getString(R.string.farm_management),
                    R.drawable.ic_farm, R.drawable.card_bg_farm, 1));
        }

        if (PreferenceManager.INSTANCE.hasRole("7")) {
            dashboardMenuModelList.add(new DashboardMenuModel(getString(R.string.warehouse), getString(R.string.stock_movement),
                    R.drawable.ic_inventory, R.drawable.card_bg_reception, 2));
        }

        if (PreferenceManager.INSTANCE.hasRole("12")) {
            dashboardMenuModelList.add(new DashboardMenuModel(getString(R.string.sawmill), getString(R.string.processed_movement),
                    R.drawable.ic_sawmill, R.drawable.card_bg_sawmill, 3));
        }

        if (PreferenceManager.INSTANCE.hasRole("6")) {
            dashboardMenuModelList.add(new DashboardMenuModel(getString(R.string.finance), getString(R.string.expense_tracker),
                    R.drawable.ic_finance, R.drawable.card_bg_finance, 4));
        }

        if (PreferenceManager.INSTANCE.hasRole("13")) {
            dashboardMenuModelList.add(new DashboardMenuModel(getString(R.string.carbons), getString(R.string.footprints_sustainability),
                    R.drawable.ic_carbons, R.drawable.card_bg_carbons, 5));
        }

        // Add dynamically
        LayoutInflater inflater = LayoutInflater.from(this);

        for (DashboardMenuModel item : dashboardMenuModelList) {
            View view = inflater.inflate(R.layout.item_dashboard_menu, gridDashboardMenu, false);

            CardView cardMenu = view.findViewById(R.id.cardMenu);
            AppCompatImageView img = view.findViewById(R.id.imgFarm);
            AppCompatTextView title = view.findViewById(R.id.tvTitle);
            AppCompatTextView sub = view.findViewById(R.id.tvSub);
            LinearLayout bg = view.findViewById(R.id.llIconBg);

            img.setImageResource(item.getIcon());
            title.setText(item.getTitle());
            sub.setText(item.getSub());
            bg.setBackgroundResource(item.getBg());

            gridDashboardMenu.addView(view);
            applyErpClickAnimation(cardMenu);

            cardMenu.setOnClickListener(v -> {
                animateIcon(img);

                if (item.getTag() == 1) {
                    startActivity(new Intent(MainActivity.this, FarmListsActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else if (item.getTag() == 2) {
                    startActivity(new Intent(MainActivity.this, WarehouseActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else if (item.getTag() == 4) {
                    startActivity(new Intent(MainActivity.this, FinanceActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }
    }

    private void setDashboardDetails() {
        try {
            PackageInfo packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            tvVersion.setText(String.format("%s: %s.%s", getString(R.string.version), packageInfo.versionName, packageInfo.versionCode));
            tvVersion.setPaintFlags(tvVersion.getPaintFlags() | 8);

            if (!PreferenceManager.INSTANCE.getProfilePhoto().isEmpty()) {
                Glide.with(this)
                        .asBitmap()
                        .load(PreferenceManager.INSTANCE.getProfilePhoto())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource,
                                                        @Nullable Transition<? super Bitmap> transition) {
                                profileImage.setImageBitmap(resource);
                                profileImageMenu.setImageBitmap(resource);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                CommonUtils.imageCreate(getApplicationContext(), MainActivity.this, profileImage);
                                CommonUtils.imageCreate(getApplicationContext(), MainActivity.this, profileImageMenu);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                        });
            } else {
                // Create and set the image
                CommonUtils.imageCreate(getApplicationContext(), MainActivity.this, profileImage);
                CommonUtils.imageCreate(getApplicationContext(), MainActivity.this, profileImageMenu);
            }

            tvGreeting.setText(CommonUtils.getGreeting(getApplicationContext()));
            tvName.setText(PreferenceManager.INSTANCE.getName());
            tvNameMenu.setText(PreferenceManager.INSTANCE.getName());
            tvOriginName.setText(PreferenceManager.INSTANCE.getOriginName());

            String base64Svg = PreferenceManager.INSTANCE.getOriginIcon();

            if (!base64Svg.isEmpty()) {
                if (base64Svg.contains(",")) {
                    base64Svg = base64Svg.split(",")[1];
                }

                byte[] svgBytes = Base64.decode(base64Svg, Base64.DEFAULT);
                String svgString = new String(svgBytes, StandardCharsets.UTF_8);

                SVG svg = SVG.getFromString(svgString);
                PictureDrawable drawable = new PictureDrawable(svg.renderToPicture());
                ivOriginIcon.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // required
                ivOriginIcon.setImageDrawable(drawable);
            } else {
                ivOriginIcon.setVisibility(View.GONE);
            }

            setMenus();

            imgClose.setOnClickListener(v -> closeDrawers());
            lstMenu.setOnItemClickListener(this);

            tvLogout.setOnClickListener(v -> {
                closeDrawers();
                logoutConfirmation();
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setDashboardDetails", e);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        NavigationType selectedType = menuModels.get(position).getNavigationType();

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {

            DrawerLayout.SimpleDrawerListener listener = new DrawerLayout.SimpleDrawerListener() {
                @Override
                public void onDrawerClosed(@NonNull View drawerView) {
                    drawerLayout.removeDrawerListener(this);
                    handleMenuClick(selectedType);
                }
            };

            drawerLayout.addDrawerListener(listener);
            drawerLayout.closeDrawer(GravityCompat.START);

        } else {
            // 🔥 fallback (important)
            handleMenuClick(selectedType);
        }
    }

    private void handleMenuClick(NavigationType selectedType) {

        if (selectedType == NavigationType.SYNCHRONIZATION) {

            Boolean isRunning = syncViewModel.getProgressState().getValue();

            if (Boolean.TRUE.equals(isRunning)) {
                Toast.makeText(this, getString(R.string.sync_already_in_progress), Toast.LENGTH_SHORT).show();
                return;
            }

            if (new NetworkConnectivity(getApplicationContext()).isNetworkAvailable()) {
                syncViewModel.startFullSync();
            } else {
                showCustomDialog(getString(R.string.information), getString(R.string.internet_not_available), false);
            }

        } else if (selectedType == NavigationType.DATA_BACKUP) {
            startActivity(new Intent(MainActivity.this, DataBackupActivity.class));
        } else if (selectedType == NavigationType.APP_STATUS) {
            startActivity(new Intent(MainActivity.this, AppStatusActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void closeDrawers() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        }
    }

    private void applyErpClickAnimation(View view) {

        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(1.10f)
                            .scaleY(1.10f)
                            .setDuration(80)
                            .start();

                    v.setElevation(12f);
                    return true; // consume event

                case MotionEvent.ACTION_UP:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .start();

                    v.setElevation(CommonUtils.dpToPx(8, getApplicationContext()));

                    v.performClick(); // ✅ REQUIRED
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .start();

                    v.setElevation(CommonUtils.dpToPx(8, getApplicationContext()));
                    return true;
            }
            return false;
        });
    }

    private void animateIcon(View icon) {
        icon.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() ->
                        icon.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .setInterpolator(new DecelerateInterpolator())
                                .start()
                )
                .start();
    }

    private void logoutConfirmation() {
        try {
            LayoutInflater dialogInflater = LayoutInflater.from(this);
            View dialogView = dialogInflater.inflate(R.layout.custom_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            AppCompatTextView dialogHeader = dialogView.findViewById(R.id.dialogHeader);
            AppCompatTextView dialogBody = dialogView.findViewById(R.id.dialogBody);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
            MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);

            btnOk.setText(getString(R.string.yes));
            dialogHeader.setText(getString(R.string.logout));
            dialogBody.setText(R.string.logout_confirmation);

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnOk.setOnClickListener(v -> {
                dialog.dismiss();
                syncViewModel.checkUnsyncedData();
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "performLogout", e);
        }
    }

    private void showUnsyncedWarningDialog() {

        LayoutInflater dialogInflater = LayoutInflater.from(this);
        View dialogView = dialogInflater.inflate(R.layout.custom_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        AppCompatTextView dialogHeader = dialogView.findViewById(R.id.dialogHeader);
        AppCompatTextView dialogBody = dialogView.findViewById(R.id.dialogBody);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);

        btnCancel.setText(getString(R.string.logout_anyway));
        btnOk.setText(getString(R.string.sync_now));
        dialogHeader.setText(getString(R.string.sync_pending));
        dialogBody.setText(R.string.unsynced_data_warning);

        btnCancel.setOnClickListener(v -> forceLogout());

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            syncViewModel.startFullSync();
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void forceLogout() {
        if (new NetworkConnectivity(getApplicationContext()).isNetworkAvailable()) {

            LogoutRequest logoutRequest = new LogoutRequest();
            logoutRequest.setRefreshToken(PreferenceManager.INSTANCE.getRefreshToken());
            authViewModel.logout(logoutRequest);

            authViewModel.getLogoutStatus().observe(this, aBoolean -> performFullLogoutCleanup());
        } else {
            performFullLogoutCleanup();
        }
    }

    private void performFullLogoutCleanup() {
        // 1️⃣ Delete ALL ROWS from ALL TABLES
        authViewModel.clearAllTableData();

        // 2️⃣ Delete uploads folder (files only)
        File uploadsDir = new File(getFilesDir(), "uploads");
        CommonUtils.deleteFolderRecursive(uploadsDir, getApplicationContext());

        // 3️⃣ Clear shared preferences / session
        PreferenceManager.INSTANCE.clearLoginSession();

        // 4️⃣ Navigate to Login
        new Handler(Looper.getMainLooper()).postDelayed(this::performLogout, 300);
    }

    private void observeNotificationCount() {

        syncViewModel.getUnreadCount().observe(this, unreadCount -> {

            if (unreadCount == null) {
                unreadCount = 0;
            }

            updateNotificationBadge(unreadCount);
        });
    }

    private void updateNotificationBadge(int count) {

        if (count <= 0) {
            tvNotificationBadge.setVisibility(View.GONE);
            return;
        }

        tvNotificationBadge.setVisibility(View.VISIBLE);

        if (count > 9) {
            tvNotificationBadge.setText("9+");
        } else {
            tvNotificationBadge.setText(String.valueOf(count));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        networkStatusView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        networkStatusView.stop();
    }
}