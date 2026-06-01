package com.cgr.codrinterraerp.utils;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.NonNull;

public class CustomTypeFaceSpan extends MetricAffectingSpan {

    private final Typeface typeface;

    public CustomTypeFaceSpan(Typeface typeface) {
        this.typeface = typeface;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        apply(tp);
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint tp) {
        apply(tp);
    }

    private void apply(TextPaint paint) {
        paint.setTypeface(typeface);
        paint.setFlags(paint.getFlags() | TextPaint.SUBPIXEL_TEXT_FLAG);
    }
}