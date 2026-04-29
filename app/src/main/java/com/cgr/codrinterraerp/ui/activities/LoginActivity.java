package com.cgr.codrinterraerp.ui.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.Origins;
import com.cgr.codrinterraerp.helper.PreferenceManager;
import com.cgr.codrinterraerp.model.request.LoginRequest;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.utils.DividerItemDecoration;
import com.cgr.codrinterraerp.utils.NetworkConnectivity;
import com.cgr.codrinterraerp.utils.NetworkStatusView;
import com.cgr.codrinterraerp.viewmodel.AuthViewModel;
import com.cgr.codrinterraerp.viewmodel.MasterViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends BaseActivity {

    private TextInputLayout tiOriginLogin, tiUsernameLogin, tiPasswordLogin;
    private AppCompatEditText etOriginLogin, etUsernameLogin, etPasswordLogin;
    private AppCompatTextView tvNoDataFound;
    private NetworkStatusView networkStatusView;
    private FrameLayout progressBar;
    private MasterViewModel masterViewModel;
    private AuthViewModel authViewModel;
    private List<Origins> originsList;
    private RecyclerViewAdapter<Origins> originsRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {

            requestPermission();

            LinearLayout llLogo = findViewById(R.id.llLogo);
            tiOriginLogin = findViewById(R.id.tiOriginLogin);
            tiUsernameLogin = findViewById(R.id.tiUsernameLogin);
            tiPasswordLogin = findViewById(R.id.tiPasswordLogin);
            etOriginLogin = findViewById(R.id.etOriginLogin);
            etUsernameLogin = findViewById(R.id.etUsernameLogin);
            etPasswordLogin = findViewById(R.id.etPasswordLogin);
            MaterialButton btnLogin = findViewById(R.id.btnLogin);
            networkStatusView = findViewById(R.id.networkStatusView);
            progressBar = findViewById(R.id.progressBar);

            llLogo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));

            masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);
            authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

            CommonUtils.clearErrorOnTyping(etOriginLogin, tiOriginLogin);
            CommonUtils.clearErrorOnTyping(etUsernameLogin, tiUsernameLogin);
            CommonUtils.clearErrorOnTyping(etPasswordLogin, tiPasswordLogin);

            btnLogin.setOnClickListener(v -> submitLogin());

            if (new NetworkConnectivity(this).isNetworkAvailable()) {
                masterViewModel.getOrigins();
            } else {
                fetchOrigins();
            }

            masterViewModel.getProgressState().observe(this, aBoolean -> {
                if (aBoolean) {
                    showProgress(progressBar);
                } else {
                    hideProgress(progressBar);
                }
            });

            masterViewModel.getOriginStatus().observe(this, aBoolean -> {
                if (aBoolean) {
                    fetchOrigins();
                }
            });

            etOriginLogin.setOnClickListener(v -> showDataDialog());

            authViewModel.getLoginStatus().observe(this, s -> {
                if (!s) {
                    showCustomDialog(authViewModel.getErrorTitle(), authViewModel.getErrorMessage(), false);
                }
            });

            authViewModel.getProgressState().observe(this, isProgress -> {
                if (isProgress) {
                    showProgress(progressBar);
                } else {
                    hideProgress(progressBar);
                }
            });

            authViewModel.getLoginResult().observe(this, loginDataResponse -> {

                PreferenceManager.INSTANCE.setAccessToken(loginDataResponse.getAccessToken());
                PreferenceManager.INSTANCE.setLoginExpiry(loginDataResponse.getExpiresIn());
                PreferenceManager.INSTANCE.setRefreshToken(loginDataResponse.getRefreshToken());
                PreferenceManager.INSTANCE.setName(loginDataResponse.getFullName());
                PreferenceManager.INSTANCE.setOriginId(1);
                PreferenceManager.INSTANCE.setUserId(loginDataResponse.getUserId());
                PreferenceManager.INSTANCE.setProfilePhoto(loginDataResponse.getProfilePhoto());
                PreferenceManager.INSTANCE.setTimeZone(loginDataResponse.getDefaultTimezone());
                PreferenceManager.INSTANCE.setOriginName(loginDataResponse.getOriginName());
                PreferenceManager.INSTANCE.setOriginIcon(loginDataResponse.getOriginIcon());
                PreferenceManager.INSTANCE.setTimeZone(loginDataResponse.getDefaultTimezone());
                PreferenceManager.INSTANCE.setCurrencyName(loginDataResponse.getCurrencyName());
                PreferenceManager.INSTANCE.setCurrencyFormat(loginDataResponse.getCurrencyFormat());
                PreferenceManager.INSTANCE.setCurrencyExcelFormat(loginDataResponse.getCurrencyExcelFormat());
                PreferenceManager.INSTANCE.setLoggedIn(true);
                PreferenceManager.INSTANCE.setLoginDetailId(loginDataResponse.getLoginDetailId());
                PreferenceManager.INSTANCE.setUserRoles(loginDataResponse.getRoleIds());

                startActivity(new Intent(this, MainActivity.class)
                        .putExtra("isFromLogin", true)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void fetchOrigins() {
        try {
            originsList = new ArrayList<>();
            originsList = masterViewModel.fetchOrigins();
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchOrigins", e);
        }
    }

    private void submitLogin() {
        try {
            hideKeyboard(this);
            requestPermission();

            boolean isValid1 = true, isValid2 = true, isValid3 = true;

            if (Objects.requireNonNull(etOriginLogin.getText()).toString().isEmpty()) {
                tiOriginLogin.setError(getString(R.string.required_field));
                tiOriginLogin.setErrorEnabled(true);
                isValid1 = false;
            } else {
                tiOriginLogin.setError(null);
                tiOriginLogin.setErrorEnabled(false);
            }

            if (Objects.requireNonNull(etUsernameLogin.getText()).toString().isEmpty()) {
                tiUsernameLogin.setError(getString(R.string.required_field));
                tiUsernameLogin.setErrorEnabled(true);
                isValid2 = false;
            } else {
                tiUsernameLogin.setError(null);
                tiUsernameLogin.setErrorEnabled(false);
            }

            if (Objects.requireNonNull(etPasswordLogin.getText()).toString().isEmpty()) {
                tiPasswordLogin.setError(getString(R.string.required_field));
                tiPasswordLogin.setErrorEnabled(true);
                isValid3 = false;
            } else {
                tiPasswordLogin.setError(null);
                tiPasswordLogin.setErrorEnabled(false);
            }

            if (isValid1 && isValid2 && isValid3) {
                if (new NetworkConnectivity(this).isNetworkAvailable()) {
                    networkStatusView.stop();

                    String token = PreferenceManager.INSTANCE.getFirebaseToken();
                    if (token.isEmpty()) {
                        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(newToken -> {
                            PreferenceManager.INSTANCE.setFirebaseToken(newToken);
                            callLogin(newToken);
                        });
                    } else {
                        callLogin(token);
                    }
                } else {
                    showCustomDialog(getString(R.string.information), getString(R.string.internet_not_available), false);
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "submitLogin", e);
        }
    }

    private void callLogin(String fcmToken) {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setOriginId(Integer.parseInt(etOriginLogin.getTag().toString()));
        loginRequest.setUsername(Objects.requireNonNull(etUsernameLogin.getText()).toString().trim());
        loginRequest.setPassword(Objects.requireNonNull(etPasswordLogin.getText()).toString().trim());
        loginRequest.setDeviceId(CommonUtils.getDeviceId(getApplicationContext()));
        loginRequest.setAndroidVersion(CommonUtils.getAndroidVersion());
        loginRequest.setAppVersion(CommonUtils.getVersionCode(getApplicationContext()));
        loginRequest.setFcmToken(fcmToken.trim());
        loginRequest.setDeviceModel(CommonUtils.getDeviceModel(getApplicationContext()));

        Gson gson = new Gson();
        String json = gson.toJson(loginRequest);
        AppLogger.d(getClass(), "Login Request: " + json);

        authViewModel.login(loginRequest);
    }

    private void showDataDialog() {
        try {

            Dialog dialog = new Dialog(this, R.style.DialogTheme);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            dialog.getWindow().setDimAmount(0.6f);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);
            layoutParams.gravity = Gravity.CENTER;
            dialog.getWindow().setAttributes(layoutParams);
            dialog.setContentView(R.layout.list_dialog);

            AppCompatTextView dialogTitle = dialog.findViewById(R.id.tvDialogTitle);
            tvNoDataFound = dialog.findViewById(R.id.tvNoDataFound);
            AppCompatImageView closeDialog = dialog.findViewById(R.id.imgClose);
            AppCompatEditText etSearch = dialog.findViewById(R.id.etSearch);
            RecyclerView rvList = dialog.findViewById(R.id.rvList);
            closeDialog.setOnClickListener(v -> dialog.dismiss());
            dialogTitle.setText(R.string.select_origins);

            rvList.setLayoutManager(new LinearLayoutManager(this));
            rvList.addItemDecoration(new DividerItemDecoration(this));

            originsRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(originsList), R.layout.row_dialog_list) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, Origins origins) {

                    AppCompatTextView tvName = holder.itemView.findViewById(R.id.tvName);
                    AppCompatImageView ivSelected = holder.itemView.findViewById(R.id.ivItemSelected);

                    tvName.setText(origins.getOriginName());

                    boolean isSelected = false;
                    if (etOriginLogin.getTag() != null) {
                        isSelected = Objects.equals(origins.getOriginId(), etOriginLogin.getTag());
                    }

                    ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

                    if (isSelected) {
                        holder.setViewTypeface(R.id.tvName,
                                ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_bold));
                    } else {
                        holder.setViewTypeface(R.id.tvName,
                                ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_medium));
                    }
                }
            };

            rvList.setAdapter(originsRecyclerViewAdapter);
            originsRecyclerViewAdapter.setOnItemClickListener((view, position) -> {

                Origins selected = originsRecyclerViewAdapter.getItem(position);

                etOriginLogin.setText(selected.getOriginName());
                etOriginLogin.setTag(selected.getOriginId());

                dialog.dismiss(); // optional
            });

            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    String query = s.toString().trim().toLowerCase();

                    if (query.isEmpty()) {
                        originsRecyclerViewAdapter.resetFilter();
                    } else {
                        originsRecyclerViewAdapter.filter(item ->
                                item.getOriginName() != null &&
                                        item.getOriginName().toLowerCase().contains(query)
                        );
                    }

                    // Optional: Show "No Data Found"
                    if (originsRecyclerViewAdapter.getItemCount() == 0) {
                        tvNoDataFound.setVisibility(View.VISIBLE);
                    } else {
                        tvNoDataFound.setVisibility(View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showDataDialog", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkStatusView.start();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }
}