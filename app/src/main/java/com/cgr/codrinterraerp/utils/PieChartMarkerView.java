package com.cgr.codrinterraerp.utils;

import android.content.Context;

import androidx.appcompat.widget.AppCompatTextView;

import com.cgr.codrinterraerp.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.Locale;

public class PieChartMarkerView extends MarkerView {

    private final AppCompatTextView tvLabel;
    private final AppCompatTextView tvValue;
    private final int chartType;
    private final Context context;

    public PieChartMarkerView(Context context) {
        this(context, R.layout.marker_pie, 0);
    }

    public PieChartMarkerView(Context context, int layoutResource, int chartType) {
        super(context, layoutResource);
        this.context = context;
        this.chartType = chartType;

        tvLabel = findViewById(R.id.tvLabel);
        tvValue = findViewById(R.id.tvValue);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        PieEntry pieEntry = (PieEntry) e;
        tvLabel.setText(pieEntry.getLabel());

        double amount = (double) pieEntry.getData();
        float percentage = pieEntry.getValue();

        if(chartType == 1) {
            String percentageText = String.format(
                    Locale.getDefault(),
                    "%.1f",
                    percentage
            );

            tvValue.setText(
                    context.getString(
                            R.string.amount_percentage,
                            CommonUtils.currencyFormat(amount),
                            percentageText
                    )
            );
        } else {
            tvValue.setText(CommonUtils.currencyFormat(amount));
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight());
    }
}