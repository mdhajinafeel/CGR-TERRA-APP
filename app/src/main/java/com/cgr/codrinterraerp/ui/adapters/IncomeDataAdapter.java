package com.cgr.codrinterraerp.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.IncomeData;
import java.util.List;

public class IncomeDataAdapter extends ArrayAdapter<IncomeData> {

    private final LayoutInflater inflater;

    public IncomeDataAdapter(@NonNull Context context, @NonNull List<IncomeData> categories) {
        super(context, 0, categories);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.row_item_beneficiary, parent, false);
        }

        AppCompatTextView tvName = view.findViewById(R.id.tvName);
        tvName.setSelected(true);

        IncomeData incomeData = getItem(position);
        if (incomeData != null) {
            tvName.setText(incomeData.getConceptGeneral());
        }

        return view;
    }
}