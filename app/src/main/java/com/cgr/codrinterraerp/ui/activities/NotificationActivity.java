package com.cgr.codrinterraerp.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.PushNotifications;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.viewmodel.NotificationViewModel;
import com.google.android.material.button.MaterialButton;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationActivity extends BaseActivity {

    private AppCompatTextView filterDropDown, toolsDropDown;
    private LinearLayout llNoData;
    private RecyclerView rvNotificationsList;
    private RecyclerViewAdapter<PushNotifications> notificationsRecyclerViewAdapter;
    private String selectedFilter = "ALL";
    private NotificationViewModel notificationViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        statusBarSetting(false);
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            AppCompatImageView imgBack = findViewById(R.id.imgBack);
            AppCompatTextView txtTitle = findViewById(R.id.txtTitle);

            txtTitle.setText(getString(R.string.notifications));
            imgBack.setOnClickListener(view -> finish());

            filterDropDown = findViewById(R.id.filterDropDown);
            toolsDropDown = findViewById(R.id.toolsDropDown);
            rvNotificationsList = findViewById(R.id.rvNotificationsList);
            llNoData = findViewById(R.id.llNoData);

            notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

            rvNotificationsList.setLayoutManager(new LinearLayoutManager(this));
            initializeAdapter();

            notificationViewModel.getNotificationList().observe(this, this::bindNotificationData);
            notificationViewModel.setFilter("ALL");

            bindFilterOptions();
            bindToolsOptions();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void initializeAdapter() {

        notificationsRecyclerViewAdapter = new RecyclerViewAdapter<>(getApplicationContext(), new ArrayList<>(), R.layout.row_item_notification) {
            @Override
            public void onPostBindViewHolder(ViewHolder holder, PushNotifications pushNotification) {
                if (pushNotification != null) {

                    holder.setViewText(R.id.tvTitle, CommonUtils.getLocalizedString(getApplicationContext(), pushNotification.title));
                    holder.setViewText(R.id.tvMessage, CommonUtils.getLocalizedString(getApplicationContext(), pushNotification.message));
                    holder.setViewText(R.id.tvTimestamp, CommonUtils.convertTimeStampToDate(pushNotification.createdAt, "MMM dd, hh:mm a", getApplicationContext()));

                    AppCompatImageView ivTypeIcon = holder.getView(R.id.ivTypeIcon);
                    AppCompatTextView tvType = holder.getView(R.id.tvType);
                    AppCompatTextView tvStatus = holder.getView(R.id.tvStatus);

                    tvType.setText(pushNotification.type);
                    tvStatus.setText(pushNotification.status);

                    if(pushNotification.status.equalsIgnoreCase("SUCCESS")) {
                        tvStatus.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.chip_bg_success));
                        ivTypeIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.shape_circle_success));
                        tvStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorSuccess));
                    } else if(pushNotification.status.equalsIgnoreCase("CRITICAL")) {
                        tvStatus.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.chip_bg_error));
                        ivTypeIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.shape_circle_error));
                        tvStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorError));
                    } else if(pushNotification.status.equalsIgnoreCase("INFO")) {
                        tvStatus.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.chip_bg));
                        ivTypeIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.shape_circle_info));
                        tvStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorInfo));
                    } else if(pushNotification.status.equalsIgnoreCase("WARNING")) {
                        tvStatus.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.chip_bg_warning));
                        ivTypeIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.shape_circle_warning));
                        tvStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWarning));
                    }

                    if(pushNotification.type.equalsIgnoreCase("SYNC")) {
                        tvType.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.chip_bg_sync));
                        tvType.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorSync));
                    } else  if(pushNotification.type.equalsIgnoreCase("REMINDER")) {
                        tvType.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.chip_bg_reminder));
                        tvType.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorReminder));
                    }

                    holder.getView(R.id.btnDelete).setOnClickListener(view -> deleteNotification(pushNotification));
                }
            }
        };

        rvNotificationsList.setAdapter(notificationsRecyclerViewAdapter);
        rvNotificationsList.setHasFixedSize(true);

        RecyclerView.ItemAnimator animator = rvNotificationsList.getItemAnimator();

        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }

    private void bindNotificationData(List<PushNotifications> list) {
        try {

            if (list != null && !list.isEmpty()) {
                notificationsRecyclerViewAdapter.setItems(list);
                rvNotificationsList.setVisibility(View.VISIBLE);
                llNoData.setVisibility(View.GONE);
            } else {
                llNoData.setVisibility(View.VISIBLE);
                rvNotificationsList.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindNotificationData", e);
        }
    }

    private void bindFilterOptions() {
        try {
            filterDropDown.setOnClickListener(v -> {

                Context wrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);
                PopupMenu popupMenu = new PopupMenu(wrapper, filterDropDown);
                popupMenu.getMenuInflater().inflate(R.menu.menu_filter_notify_type, popupMenu.getMenu());

                // FORCE SHOW ICONS
                try {

                    Field field = popupMenu.getClass().getDeclaredField("mPopup");
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(Objects.requireNonNull(menuPopupHelper).getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);

                } catch (Exception e) {
                    AppLogger.e(getClass(), "bindFilterOptions", e);
                }

                popupMenu.setOnMenuItemClickListener(item -> {

                    String selected = Objects.requireNonNull(item.getTitle()).toString();
                    filterDropDown.setText(selected);

                    if (item.getItemId() == R.id.filter_all) {
                        selectedFilter = "ALL";
                        notificationViewModel.setFilter("ALL");
                        return true;
                    } else if (item.getItemId() == R.id.filter_sync) {
                        selectedFilter = "SYNC";
                        notificationViewModel.setFilter("SYNC");
                        return true;
                    } else if (item.getItemId() == R.id.filter_reminder) {
                        selectedFilter = "REMINDER";
                        notificationViewModel.setFilter("REMINDER");
                        return true;
                    }

                    return false;
                });

                popupMenu.show();
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindFilterOptions", e);
        }
    }

    private void bindToolsOptions() {
        try {
            toolsDropDown.setOnClickListener(v -> {

                Context wrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);
                PopupMenu popupMenu = new PopupMenu(wrapper, toolsDropDown);
                popupMenu.getMenuInflater().inflate(R.menu.menu_tools_notify, popupMenu.getMenu());

                // FORCE SHOW ICONS
                try {

                    Field field = popupMenu.getClass().getDeclaredField("mPopup");
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(Objects.requireNonNull(menuPopupHelper).getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);

                } catch (Exception e) {
                    AppLogger.e(getClass(), "bindToolsOptions", e);
                }

                popupMenu.setOnMenuItemClickListener(item -> {

                    if (item.getItemId() == R.id.tool_clear) {
                        showClearNotificationsDialog();
                        return true;
                    }

                    return false;
                });

                popupMenu.show();
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindToolsOptions", e);
        }
    }

    private void showClearNotificationsDialog() {
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

            btnOk.setText(getString(R.string.clear));
            dialogHeader.setText(getString(R.string.clear_notifications));
            dialogBody.setText(R.string.clear_notifications_confirmation);

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnOk.setOnClickListener(v -> {
                dialog.dismiss();
                notificationViewModel.clearNotification(selectedFilter);
                Toast.makeText(getApplicationContext(), CommonUtils.capitalize(selectedFilter) + getString(R.string.notifications_cleared), Toast.LENGTH_SHORT).show();
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showClearNotificationsDialog", e);
        }
    }

    private void deleteNotification(PushNotifications pushNotification) {
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
            dialogHeader.setText(R.string.delete_notification);
            dialogBody.setText(R.string.clear_notification_confirmation);

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnOk.setOnClickListener(v -> {
                dialog.dismiss();
                notificationViewModel.clearNotification(pushNotification.id, pushNotification.createdAt);
                Toast.makeText(getApplicationContext(), getString(R.string.deleted_notification), Toast.LENGTH_SHORT).show();
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteNotification", e);
        }
    }
}