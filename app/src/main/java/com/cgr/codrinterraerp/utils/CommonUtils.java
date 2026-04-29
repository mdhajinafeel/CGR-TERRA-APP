package com.cgr.codrinterraerp.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.helper.PreferenceManager;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class CommonUtils {

    // TIME CONVERSION
    public static String convertTimeStampToDate(Long milliSeconds, String dateFormat, Context context) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, new Locale(Locale.getDefault().getLanguage()));
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTimeInMillis(milliSeconds);
        } catch (Exception e) {
            AppLogger.e(context.getClass(), "convertTimeStampToDate", e);
        }
        return simpleDateFormat.format(calendar.getTime());
    }

    public static Long getCurrentLocalDateTimeStamp() {
        return Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
    }

    // BITMAP INITIAL CREATION
    private static Bitmap createInitialsBitmap(String initials, int bgColor) {
        int size = 200; // Fixed bitmap size
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw background circle
        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(bgColor);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, circlePaint);

        // Configure text paint
        Paint textPaint = createTextPaint(initials, size);

        // ✅ Adjust baseline vertically (centering fix)
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float x = size / 2f;
        float y = (size / 2f) - (fm.ascent + fm.descent) / 2f;

        // ✅ Now draw the text (make sure initials not empty)
        if (initials != null && !initials.isEmpty()) {
            canvas.drawText(initials, x, y, textPaint);
        }

        return bitmap;
    }

    // Extracted method
    private static Paint createTextPaint(String text, int canvasSize) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);

        // Dynamically adjust text size to fit inside the circle
        float textSize = canvasSize / 2f; // Start large
        paint.setTextSize(textSize);

        float textWidth = paint.measureText(text);
        while (textWidth > canvasSize * 0.8f) { // leave 10% padding
            textSize -= 2f;
            paint.setTextSize(textSize);
            textWidth = paint.measureText(text);
        }

        return paint;
    }

    private static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            initials.append(Character.toUpperCase(part.charAt(0)));
        }
        return initials.toString();
    }

    // IMAGE CREATE FOR INITIALS
    public static void imageCreate(Context context, Activity activity, AppCompatImageView appCompatImageView) {
        new Thread(() -> {
            Bitmap avatar = createInitialsBitmap(
                    getInitials(PreferenceManager.INSTANCE.getName()),
                    ContextCompat.getColor(context, R.color.colorAccent));
            activity.runOnUiThread(() -> appCompatImageView.setImageBitmap(avatar));
        }).start();
    }

    //GREETING
    public static String getGreeting(Context context) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return context.getString(R.string.good_morning);
        } else if (hour >= 12 && hour < 17) {
            return context.getString(R.string.good_afternoon);
        } else if (hour >= 17 && hour < 21) {
            return context.getString(R.string.good_evening);
        } else {
            return context.getString(R.string.good_night);
        }
    }

    //CLEAR ERROR
    public static void clearErrorOnTyping(AppCompatEditText editText, TextInputLayout layout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
                layout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public static void clearErrorOnTyping(MaterialAutoCompleteTextView materialAutoCompleteTextView, TextInputLayout layout) {
        materialAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // DEVICE ID
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    // VERSION CODE
    public static String getVersionCode(Context context) {
        String versionName = "";
        long versionCode = 0;

        try {
            PackageInfo pInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            versionName = pInfo.versionName;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                versionCode = pInfo.getLongVersionCode();
            } else {
                versionCode = pInfo.versionCode;
            }

        } catch (Exception e) {
            AppLogger.e(context.getClass(), "Error in getVersionCode");
        }
        return versionName + " (" + versionCode + ")";
    }

    // DEVICE NAME
    public static String getDeviceModel(Context context) {
        String deviceName = "";
        try {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            deviceName = manufacturer + " " + model;
        } catch (Exception e) {
            AppLogger.e(context.getClass(), "Error in getVersionCode");
        }
        return deviceName;
    }

    public static String getAndroidVersion() {
        String version = android.os.Build.VERSION.RELEASE;
        int api = android.os.Build.VERSION.SDK_INT;
        return "Android " + version + " (API " + api + ")";
    }

    public static float dpToPx(float dp, Context context) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static void deleteFolderRecursive(File file, Context context) {
        if (file == null || !file.exists()) return;

        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                deleteFolderRecursive(child, context);
            }
        }
        boolean fileDeleted = file.delete();
        if(fileDeleted) {
            AppLogger.d(context.getClass(), "File Deleted");
        }
    }

    //DATE PICKER
    public static void showDatePicker(Context context, AppCompatEditText appCompatEditText) {

        // Today
        Calendar today = Calendar.getInstance();

        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);

        // Min date = 5 years before today
        Calendar minDateCal = Calendar.getInstance();
        minDateCal.add(Calendar.MONTH, -2);

        // Max date = 1 month after today
        Calendar maxDateCal = Calendar.getInstance();
        maxDateCal.add(Calendar.DAY_OF_MONTH, 1);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, selectedYear, selectedMonth, selectedDay) -> {
            String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
            appCompatEditText.setText(date);
        },
                year, month, day
        );

        // 🔒 Apply limits
        datePickerDialog.getDatePicker().setMinDate(minDateCal.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDateCal.getTimeInMillis());

        datePickerDialog.show();
    }

    public static double round(double value, int precision) {
        return BigDecimal.valueOf(value)
                .setScale(precision, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static String formatNumber(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        } else {
            return String.valueOf(value);
        }
    }
}