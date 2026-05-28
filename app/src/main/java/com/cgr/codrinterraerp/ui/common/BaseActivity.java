package com.cgr.codrinterraerp.ui.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.ui.activities.LoginActivity;
import com.google.android.material.button.MaterialButton;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void statusBarSetting(boolean isLogin) {
        Window window = getWindow();

        // Keep normal layout behavior (no overlap)
        WindowCompat.setDecorFitsSystemWindows(window, true);

        // ✅ Set ONLY status bar color
        if(isLogin) {
            window.setStatusBarColor(getColor(R.color.colorWhite));
        } else {
            window.setStatusBarColor(getColor(R.color.colorDarkGreen));
        }

        // ✅ White icons (since background is dark)
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(window, window.getDecorView());

        controller.setAppearanceLightStatusBars(!isLogin);
    }

    public void hideKeyboard(Context ctx) {
        InputMethodManager inputManager =
                (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null) return;
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void showCustomDialog(String header, String body, boolean isCancel) {

        hideKeyboard(this);

        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.custom_dialog, null);

        // Find views
        AppCompatTextView tvHeader = view.findViewById(R.id.dialogHeader);
        AppCompatTextView tvBody = view.findViewById(R.id.dialogBody);
        MaterialButton btnOk = view.findViewById(R.id.btnOk);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        // Set texts
        tvHeader.setText(header);
        tvBody.setText(body);

        if(isCancel) {
            btnCancel.setVisibility(View.VISIBLE);
        } else {
            btnCancel.setVisibility(View.GONE);
        }

        // Build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        // Button click listeners
        btnOk.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Show dialog
        dialog.show();
    }

    public void performLogout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(getApplicationContext(), getString(R.string.logged_out_successfully), Toast.LENGTH_SHORT).show();
        finish();
    }

    public void showProgress(FrameLayout progressBar) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress(FrameLayout progressBar) {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.GONE);
    }
}