package com.cgr.codrinterraerp.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.ApiLogs;
import com.cgr.codrinterraerp.model.LogCount;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.viewmodel.AppStatusViewModel;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AppStatusActivity extends BaseActivity {

    private AppCompatTextView filterDropDown, toolsDropDown, tvApiCount, tvNetworkCount, tvDatabaseCount, tvErrorCount, tvCrashCount;
    private LinearLayout llNoData;
    private RecyclerView rvAppStatusList;
    private RecyclerViewAdapter<ApiLogs> apiLogsRecyclerViewAdapter;
    private int expandedPosition = -1;
    private String formattedError = "", formattedResponse = "";
    private Drawable apiDrawable, internetDrawable, crashDrawable, errorDrawable, databaseDrawable;
    private int colorInternet, colorCrash, colorError, colorDb, colorPalePink, colorHoneyDew, colorSindoorRed, colorDarkGreen, colorApi;
    private AppStatusViewModel appStatusViewModel;
    private String selectedFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_status);
        statusBarSetting(false);
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            AppCompatImageView imgBack = findViewById(R.id.imgBack);
            AppCompatTextView txtTitle = findViewById(R.id.txtTitle);
            filterDropDown = findViewById(R.id.filterDropDown);
            toolsDropDown = findViewById(R.id.toolsDropDown);
            rvAppStatusList = findViewById(R.id.rvAppStatusList);
            llNoData = findViewById(R.id.llNoData);

            tvApiCount = findViewById(R.id.tvApiCount);
            tvNetworkCount = findViewById(R.id.tvNetworkCount);
            tvDatabaseCount = findViewById(R.id.tvDatabaseCount);
            tvErrorCount = findViewById(R.id.tvErrorCount);
            tvCrashCount = findViewById(R.id.tvCrashCount);

            txtTitle.setText(getString(R.string.app_status));
            imgBack.setOnClickListener(view -> finish());

            appStatusViewModel = new ViewModelProvider(this).get(AppStatusViewModel.class);

            rvAppStatusList.setLayoutManager(new LinearLayoutManager(this));
            initializeAdapter();

            appStatusViewModel.getApiLogsList().observe(this, this::bindAppStatusData);
            appStatusViewModel.setFilter("ALL");
            appStatusViewModel.getLogCounts().observe(this, this::updateSummaryCounts);

            apiDrawable = ContextCompat.getDrawable(this, R.drawable.ic_api);
            internetDrawable = ContextCompat.getDrawable(this, R.drawable.ic_internet);
            crashDrawable = ContextCompat.getDrawable(this, R.drawable.ic_crash);
            errorDrawable = ContextCompat.getDrawable(this, R.drawable.ic_error);
            databaseDrawable = ContextCompat.getDrawable(this, R.drawable.ic_database_error);

            colorInternet = ContextCompat.getColor(this, R.color.colorInternet);
            colorCrash = ContextCompat.getColor(this, R.color.colorSindoorRed);
            colorError = ContextCompat.getColor(this, R.color.colorError);
            colorDb = ContextCompat.getColor(this, R.color.colorDBError);
            colorPalePink = ContextCompat.getColor(this, R.color.colorPalePink);
            colorHoneyDew = ContextCompat.getColor(this, R.color.colorHoneyDew);
            colorSindoorRed = ContextCompat.getColor(this, R.color.colorSindoorRed);
            colorDarkGreen = ContextCompat.getColor(this, R.color.colorDarkGreen);
            colorApi = ContextCompat.getColor(this, R.color.colorApi);

            bindFilterOptions();
            bindToolsOptions();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void initializeAdapter() {

        apiLogsRecyclerViewAdapter = new RecyclerViewAdapter<>(getApplicationContext(), new ArrayList<>(), R.layout.row_item_app_status) {
            @Override
            public void onPostBindViewHolder(ViewHolder holder, ApiLogs apiLog) {
                if (apiLog != null) {

                    View vStatusBar = holder.getView(R.id.vStatusBar);
                    LinearLayout expandableContainer = (LinearLayout) holder.getView(R.id.expandableContainer);

                    if (apiLog.type.equalsIgnoreCase("api")) {

                        String cleanEndpoint = getEndpoint(apiLog);
                        holder.setViewText(R.id.tvTitle, cleanEndpoint);
                    } else {
                        holder.setViewText(R.id.tvTitle, apiLog.exceptionType);
                    }

                    if (!TextUtils.isEmpty(apiLog.tag) && !TextUtils.isEmpty(apiLog.methodName)) {
                        holder.setViewText(R.id.tvActivity, apiLog.tag + "." + apiLog.methodName);
                        holder.setViewVisibility(R.id.tvActivity, View.VISIBLE);
                    } else if (!TextUtils.isEmpty(apiLog.tag)) {
                        holder.setViewText(R.id.tvActivity, apiLog.tag);
                        holder.setViewVisibility(R.id.tvActivity, View.VISIBLE);
                    } else if (!TextUtils.isEmpty(apiLog.methodName)) {
                        holder.setViewText(R.id.tvActivity, apiLog.methodName);
                        holder.setViewVisibility(R.id.tvActivity, View.VISIBLE);
                    } else {
                        holder.setViewVisibility(R.id.tvActivity, View.GONE);
                    }

                    holder.setViewText(R.id.tvLogType, apiLog.type);

                    String type = apiLog.type.toUpperCase();
                    switch (type) {
                        case "API":
                            holder.setViewImageDrawable(R.id.tvIcon, apiDrawable);
                            vStatusBar.setBackgroundColor(colorApi);
                            holder.setViewTextColor(R.id.tvLogType, colorApi);
                            break;
                        case "NETWORK":
                            holder.setViewImageDrawable(R.id.tvIcon, internetDrawable);
                            vStatusBar.setBackgroundColor(colorInternet);
                            holder.setViewTextColor(R.id.tvLogType, colorInternet);
                            break;
                        case "CRASH":
                            holder.setViewImageDrawable(R.id.tvIcon, crashDrawable);
                            vStatusBar.setBackgroundColor(colorCrash);
                            holder.setViewTextColor(R.id.tvLogType, colorCrash);
                            break;
                        case "ERROR":
                            holder.setViewImageDrawable(R.id.tvIcon, errorDrawable);
                            vStatusBar.setBackgroundColor(colorError);
                            holder.setViewTextColor(R.id.tvLogType, colorError);
                            break;
                        case "DATABASE":
                            holder.setViewImageDrawable(R.id.tvIcon, databaseDrawable);
                            vStatusBar.setBackgroundColor(colorDb);
                            holder.setViewTextColor(R.id.tvLogType, colorDb);
                            break;
                    }

                    holder.setViewText(R.id.tvTime, CommonUtils.convertTimeStampToDate(apiLog.createdAt, "dd/MM/yyyy hh:mm:ss a", getApplicationContext()));

                    holder.setViewVisibility(R.id.ivExpandLogs, View.VISIBLE);

                    boolean isExpanded = holder.getAbsoluteAdapterPosition() == expandedPosition;

                    expandableContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

                    AppCompatTextView tvStackTrace = (AppCompatTextView) holder.getView(R.id.tvStackTrace);
                    AppCompatTextView tvErrorMessage = (AppCompatTextView) holder.getView(R.id.tvErrorMessage);
                    View ivExpand = holder.getView(R.id.ivExpandLogs);

                    Runnable[] toggleRunnable = new Runnable[1];
                    Runnable[] toggleRunnable1 = new Runnable[1];

                    ivExpand.animate().rotation(isExpanded ? 180f : 0f).setDuration(200).start();

                    AppCompatTextView tvStackTraceTitle = (AppCompatTextView) holder.getView(R.id.tvStackTraceTitle);
                    AppCompatTextView tvErrorMessageTitle = (AppCompatTextView) holder.getView(R.id.tvErrorMessageTitle);
                    if (apiLog.type.equalsIgnoreCase("api")) {
                        tvStackTraceTitle.setText(getString(R.string.request));
                        tvErrorMessageTitle.setText(getString(R.string.response));

                        formattedResponse = !TextUtils.isEmpty(apiLog.requestBody) ? formatLogText(apiLog.requestBody) : "";
                        formattedError = !TextUtils.isEmpty(apiLog.responseBody) ? formatLogText(apiLog.responseBody) : "";

                        if (TextUtils.isEmpty(formattedResponse)) {
                            tvStackTraceTitle.setVisibility(View.GONE);
                            tvStackTrace.setVisibility(View.GONE);
                        } else {
                            tvStackTraceTitle.setVisibility(View.VISIBLE);
                            tvStackTrace.setVisibility(View.VISIBLE);
                        }

                        if (apiLog.statusCode == 200) {
                            tvErrorMessage.setTextColor(colorDarkGreen);
                            tvErrorMessage.setBackgroundColor(colorHoneyDew);
                        } else {
                            tvErrorMessage.setTextColor(colorSindoorRed);
                            tvErrorMessage.setBackgroundColor(colorPalePink);
                        }
                    } else {

                        tvErrorMessage.setTextColor(colorSindoorRed);
                        tvErrorMessage.setBackgroundColor(colorPalePink);

                        tvStackTraceTitle.setText(getString(R.string.stack_trace));
                        tvErrorMessageTitle.setText(getString(R.string.error_message));

                        formattedResponse = !TextUtils.isEmpty(apiLog.responseBody) ? formatLogText(apiLog.responseBody) : "";
                        formattedError = !TextUtils.isEmpty(apiLog.errorMessage) ? formatLogText(apiLog.errorMessage) : "";
                    }

                    toggleRunnable[0] = () -> {

                        boolean isShowingFull = tvStackTrace.getText().toString().contains(getString(R.string.show_less));

                        if (isShowingFull) {
                            tvStackTrace.setText(getTrimmedText(formattedResponse, 5, toggleRunnable[0]));
                        } else {
                            tvStackTrace.setText(getTrimmedText(formattedResponse, -1, toggleRunnable[0]));
                        }

                        tvStackTrace.setMovementMethod(LinkMovementMethod.getInstance());
                    };

                    tvStackTrace.setText(getTrimmedText(formattedResponse, 5, toggleRunnable[0]));
                    tvStackTrace.setMovementMethod(LinkMovementMethod.getInstance());

                    toggleRunnable1[0] = () -> {

                        boolean isShowingFullError = tvErrorMessage.getText().toString().contains(getString(R.string.show_less));

                        if (isShowingFullError) {
                            tvErrorMessage.setText(getTrimmedText(formattedError, 3, toggleRunnable1[0]));
                        } else {
                            tvErrorMessage.setText(getTrimmedText(formattedError, -1, toggleRunnable1[0]));
                        }

                        tvErrorMessage.setMovementMethod(LinkMovementMethod.getInstance());
                    };

                    tvErrorMessage.setText(getTrimmedText(formattedError, 3, toggleRunnable1[0]));
                    tvErrorMessage.setMovementMethod(LinkMovementMethod.getInstance());

                    ivExpand.setOnClickListener(view -> {

                        int currentPosition = holder.getAbsoluteAdapterPosition();
                        int previousExpandedPosition = expandedPosition;

                        if (expandedPosition == currentPosition) {
                            expandedPosition = -1;
                            view.setRotation(isExpanded ? 180f : 0f);
                        } else {
                            expandedPosition = currentPosition;
                            view.setRotation(isExpanded ? 180f : 0f);
                        }

                        if (previousExpandedPosition != -1) {
                            apiLogsRecyclerViewAdapter.notifyItemChanged(previousExpandedPosition);
                        }

                        apiLogsRecyclerViewAdapter.notifyItemChanged(currentPosition);
                    });

                    holder.getView(R.id.btnShare).setOnClickListener(view -> shareLog(apiLog));

                    holder.getView(R.id.btnDelete).setOnClickListener(view -> deleteLog(apiLog));
                }
            }
        };

        rvAppStatusList.setAdapter(apiLogsRecyclerViewAdapter);
        rvAppStatusList.setHasFixedSize(true);

        RecyclerView.ItemAnimator animator = rvAppStatusList.getItemAnimator();

        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }

    @Nullable
    private static String getEndpoint(ApiLogs apiLog) {
        String cleanEndpoint = apiLog.endpoint;
        if (cleanEndpoint != null) {
            // Remove query params
            int queryIndex = cleanEndpoint.indexOf("?");
            if (queryIndex != -1) {
                cleanEndpoint = cleanEndpoint.substring(0, queryIndex);
            }
            // Remove slashes and convert to upper case
            cleanEndpoint = cleanEndpoint.replace("/", "").toUpperCase();
        }
        return cleanEndpoint;
    }

    private void bindAppStatusData(List<ApiLogs> list) {
        try {

            if (list != null) {

                Iterator<ApiLogs> iterator = list.iterator();
                while (iterator.hasNext()) {
                    ApiLogs apiLog = iterator.next();
                    String endpoint = apiLog.endpoint;
                    if (!TextUtils.isEmpty(endpoint)) {
                        endpoint = endpoint.toLowerCase();
                        boolean shouldSkip = endpoint.contains("/login")
                                || endpoint.contains("/logout")
                                || endpoint.contains("refresh")
                                || endpoint.contains("origins");

                        if (shouldSkip) {
                            iterator.remove();
                        }
                    }
                }
            }

            if (list != null && !list.isEmpty()) {
                apiLogsRecyclerViewAdapter.setItems(list);
                rvAppStatusList.setVisibility(View.VISIBLE);
                llNoData.setVisibility(View.GONE);
            } else {
                llNoData.setVisibility(View.VISIBLE);
                rvAppStatusList.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindAppStatusData", e);
        }
    }

    private void updateSummaryCounts(List<LogCount> counts) {

        int apiCount = 0;
        int networkCount = 0;
        int databaseCount = 0;
        int errorCount = 0;
        int crashCount = 0;

        if (counts != null) {
            for (LogCount logCount : counts) {
                switch (logCount.type.toUpperCase()) {
                    case "API":
                        apiCount = logCount.count;
                        break;
                    case "NETWORK":
                        networkCount = logCount.count;
                        break;
                    case "DATABASE":
                        databaseCount = logCount.count;
                        break;
                    case "ERROR":
                        errorCount = logCount.count;
                        break;
                    case "CRASH":
                        crashCount = logCount.count;
                        break;
                }
            }
        }

        tvApiCount.setText(String.valueOf(apiCount));
        tvNetworkCount.setText(String.valueOf(networkCount));
        tvDatabaseCount.setText(String.valueOf(databaseCount));
        tvErrorCount.setText(String.valueOf(errorCount));
        tvCrashCount.setText(String.valueOf(crashCount));
    }

    private SpannableString getTrimmedText(String text, int maxLines, Runnable onToggleClick) {

        if (TextUtils.isEmpty(text)) {
            return new SpannableString("");
        }

        String toggleText;

        // FULL TEXT MODE
        if (maxLines == -1) {
            toggleText = getString(R.string.show_less);
            return getSpannableString(text, onToggleClick, toggleText);
        }

        // COLLAPSED MODE
        String[] lines = text.split("\n");

        if (lines.length <= maxLines) {
            return new SpannableString(text);
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < maxLines; i++) {
            builder.append(lines[i]).append("");
        }

        toggleText = getString(R.string.show_more);
        builder.append("\n").append(toggleText);
        return getSpannableString(onToggleClick, builder, toggleText);
    }

    @NonNull
    private static SpannableString getSpannableString(String text, Runnable onToggleClick, String toggleText) {
        String fullText = text.replace("\n", "") + "\n" + toggleText;

        SpannableString spannableString = new SpannableString(fullText);

        int start = fullText.indexOf(toggleText);
        int end = start + toggleText.length();

        spannableString.setSpan(new android.text.style.ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                onToggleClick.run();
            }
        }, start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    @NonNull
    private static SpannableString getSpannableString(Runnable onToggleClick, StringBuilder builder, String toggleText) {
        SpannableString spannableString = new SpannableString(builder.toString());

        int start = builder.toString().indexOf(toggleText);
        int end = start + toggleText.length();

        spannableString.setSpan(new android.text.style.ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                onToggleClick.run();
            }
        }, start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    private String formatLogText(String text) {

        if (TextUtils.isEmpty(text)) {
            return "";
        }

        return text.replace("{", "{\n").replace("}", "\n}").replace("[", "[\n").replace("]", "\n]").replace(",", ",\n");
    }

    private void shareLog(ApiLogs apiLog) {
        try {

            StringBuilder shareText = new StringBuilder();

            shareText.append("APP STATUS REPORT\n\n");

            shareText.append("====================================\n");
            shareText.append("TYPE : ").append(apiLog.type).append("\n");
            shareText.append("TIME : ").append(CommonUtils.convertTimeStampToDate(apiLog.createdAt, "dd/MM/yyyy hh:mm:ss a", getApplicationContext())).append("\n");
            shareText.append("====================================\n\n");

            if (apiLog.type.equalsIgnoreCase("api")) {

                // API SHARE FORMAT

                shareText.append("ENDPOINT : ").append(apiLog.endpoint).append("\n\n");
                shareText.append("METHOD : ").append(apiLog.method).append("\n\n");
                shareText.append("DURATION : ").append(apiLog.durationMs).append(" ms").append("\n\n");
                shareText.append("STATUS CODE : ").append(apiLog.statusCode).append("\n\n");

                if (!TextUtils.isEmpty(apiLog.requestBody)) {
                    shareText.append("REQUEST :\n").append(apiLog.requestBody).append("\n\n");
                }

                if (!TextUtils.isEmpty(apiLog.responseBody)) {
                    shareText.append("RESPONSE :\n").append(apiLog.responseBody).append("\n\n");
                }

            } else {

                // CRASH / ERROR / DATABASE SHARE FORMAT

                if (!TextUtils.isEmpty(apiLog.exceptionType)) {
                    shareText.append("TITLE : ").append(apiLog.exceptionType).append("\n\n");
                }

                if (!TextUtils.isEmpty(apiLog.tag)) {
                    shareText.append("CLASS : ").append(apiLog.tag).append("\n");
                }

                if (!TextUtils.isEmpty(apiLog.methodName)) {
                    shareText.append("METHOD : ").append(apiLog.methodName).append("\n\n");
                }

                if (!TextUtils.isEmpty(apiLog.errorMessage)) {
                    shareText.append("ERROR MESSAGE :\n").append(apiLog.errorMessage).append("\n\n");
                }

                if (!TextUtils.isEmpty(apiLog.responseBody)) {
                    shareText.append("STACK TRACE :\n").append(apiLog.responseBody).append("\n\n");
                }
            }

            // FILE NAME
            String fileName = "log_" + CommonUtils.convertTimeStampToDate(System.currentTimeMillis(), "dd-MM-yyy-hh-mm-ss-a", getApplicationContext()) + ".txt";

            // CREATE FILE IN CACHE
            File file = new File(getCacheDir(), fileName);

            FileWriter writer = new FileWriter(file);
            writer.append(shareText.toString());
            writer.flush();
            writer.close();

            // GET URI
            Uri uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".fileprovider", file);

            // SHARE
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, getString(R.string.share_log)));

        } catch (Exception e) {
            AppLogger.e(getClass(), "shareLog", e);
        }
    }

    private void deleteLog(ApiLogs apiLog) {
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
            dialogHeader.setText(R.string.delete_log);
            dialogBody.setText(R.string.clear_log_confirmation);

            btnCancel.setOnClickListener(v -> {
                expandedPosition = -1;
                dialog.dismiss();
            });

            btnOk.setOnClickListener(v -> {
                dialog.dismiss();
                expandedPosition = -1;
                appStatusViewModel.clearLog(apiLog.id, apiLog.createdAt);
                Toast.makeText(getApplicationContext(), getString(R.string.deleted_log), Toast.LENGTH_SHORT).show();
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteLog", e);
        }
    }

    private void bindFilterOptions() {
        try {
            filterDropDown.setOnClickListener(v -> {

                Context wrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);
                PopupMenu popupMenu = new PopupMenu(wrapper, filterDropDown);
                popupMenu.getMenuInflater().inflate(R.menu.menu_filter, popupMenu.getMenu());

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

                    expandedPosition = -1;
                    if (item.getItemId() == R.id.filter_all) {
                        selectedFilter = "ALL";
                        appStatusViewModel.setFilter("ALL");
                        return true;
                    } else if (item.getItemId() == R.id.filter_api) {
                        selectedFilter = "API";
                        appStatusViewModel.setFilter("API");
                        return true;
                    } else if (item.getItemId() == R.id.filter_network) {
                        selectedFilter = "NETWORK";
                        appStatusViewModel.setFilter("NETWORK");
                        return true;
                    } else if (item.getItemId() == R.id.filter_error) {
                        selectedFilter = "ERROR";
                        appStatusViewModel.setFilter("ERROR");
                        return true;
                    } else if (item.getItemId() == R.id.filter_crash) {
                        selectedFilter = "CRASH";
                        appStatusViewModel.setFilter("CRASH");
                        return true;
                    } else if (item.getItemId() == R.id.filter_database) {
                        selectedFilter = "DATABASE";
                        appStatusViewModel.setFilter("DATABASE");
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
                popupMenu.getMenuInflater().inflate(R.menu.menu_tools, popupMenu.getMenu());

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

                    expandedPosition = -1;
                    if (item.getItemId() == R.id.tool_export) {
                        exportAllLogs();
                        return true;
                    } else if (item.getItemId() == R.id.tool_clear) {
                        showClearLogsDialog();
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

    private void exportAllLogs() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("APP STATUS REPORT\n\n");
            List<ApiLogs> logs = apiLogsRecyclerViewAdapter.getItems();

            if (!logs.isEmpty()) {

                for (ApiLogs apiLog : logs) {

                    builder.append("====================================\n");
                    builder.append("TYPE : ").append(apiLog.type).append("\n");
                    builder.append("TIME : ").append(CommonUtils.convertTimeStampToDate(apiLog.createdAt, "dd/MM/yyyy hh:mm:ss a", getApplicationContext())).append("\n");
                    builder.append("====================================\n\n");

                    // API LOG FORMAT
                    if ("API".equalsIgnoreCase(apiLog.type)) {

                        builder.append("ENDPOINT : ").append(apiLog.endpoint).append("\n\n");

                        builder.append("METHOD : ").append(apiLog.method).append("\n\n");

                        builder.append("STATUS CODE : ").append(apiLog.statusCode).append("\n\n");

                        builder.append("DURATION : ").append(apiLog.durationMs).append(" ms\n\n");

                        if (!TextUtils.isEmpty(apiLog.requestBody)) {

                            builder.append("REQUEST :\n");
                            builder.append(apiLog.requestBody);
                            builder.append("\n\n");
                        }

                        if (!TextUtils.isEmpty(apiLog.responseBody)) {

                            builder.append("RESPONSE :\n");
                            builder.append(apiLog.responseBody);
                            builder.append("\n\n");
                        }

                    } else {

                        // ERROR / CRASH / DATABASE FORMAT

                        if (!TextUtils.isEmpty(apiLog.exceptionType)) {

                            builder.append("TITLE : ").append(apiLog.exceptionType).append("\n\n");
                        }

                        if (!TextUtils.isEmpty(apiLog.tag)) {

                            builder.append("CLASS : ").append(apiLog.tag).append("\n");
                        }

                        if (!TextUtils.isEmpty(apiLog.methodName)) {

                            builder.append("METHOD : ").append(apiLog.methodName).append("\n\n");
                        }

                        if (!TextUtils.isEmpty(apiLog.errorMessage)) {

                            builder.append("ERROR MESSAGE :\n");
                            builder.append(apiLog.errorMessage);
                            builder.append("\n\n");
                        }

                        if (!TextUtils.isEmpty(apiLog.responseBody)) {

                            builder.append("STACK TRACE :\n");
                            builder.append(apiLog.responseBody);
                            builder.append("\n\n");
                        }
                    }

                    builder.append("\n--------------------------------------------------\n\n");
                }

                File file = new File(getCacheDir(), "all_logs_" + CommonUtils.convertTimeStampToDate(System.currentTimeMillis(), "dd-MM-yyy-hh-mm-ss-a", getApplicationContext()) + ".txt");
                FileWriter writer = new FileWriter(file);
                writer.append(builder.toString());
                writer.flush();
                writer.close();
                Uri uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".fileprovider", file);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, getString(R.string.export_logs)));
            } else {
                Toast.makeText(getApplicationContext(), "No logs available to export", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "exportAllLogs", e);
        }
    }

    private void showClearLogsDialog() {
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
            dialogHeader.setText(getString(R.string.clear_logs));
            dialogBody.setText(R.string.clear_logs_confirmation);

            btnCancel.setOnClickListener(v -> {
                expandedPosition = -1;
                dialog.dismiss();
            });

            btnOk.setOnClickListener(v -> {
                dialog.dismiss();
                expandedPosition = -1;
                appStatusViewModel.clearLogs(selectedFilter);
                Toast.makeText(getApplicationContext(), CommonUtils.capitalize(selectedFilter) + getString(R.string.logs_cleared), Toast.LENGTH_SHORT).show();
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showClearLogsDialog", e);
        }
    }
}