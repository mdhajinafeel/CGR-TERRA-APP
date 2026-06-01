package com.cgr.codrinterraerp.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.Beneficiaries;

import java.util.ArrayList;
import java.util.List;

public class BeneficiariesAdapter extends ArrayAdapter<Beneficiaries> implements Filterable {

    private final List<Beneficiaries> originalList;
    private final List<Beneficiaries> filteredList;
    private final LayoutInflater inflater;

    public BeneficiariesAdapter(@NonNull Context context,  @NonNull List<Beneficiaries> beneficiaries) {
        super(context, 0, beneficiaries); // OK now
        inflater = LayoutInflater.from(context);
        originalList = new ArrayList<>(beneficiaries);
        filteredList = new ArrayList<>(beneficiaries);
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public Beneficiaries getItem(int position) {
        return filteredList.get(position);
    }

    // 🔥 IMPORTANT: Override getView
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    // 🔥 IMPORTANT: Override getDropDownView
    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.row_item_beneficiary, parent, false);
        }

        AppCompatTextView tvName = view.findViewById(R.id.tvName);

        tvName.setSelected(true);

        Beneficiaries beneficiary = getItem(position);
        if (beneficiary != null) {
            tvName.setText(String.format("%s --- %s", beneficiary.getBeneficiaryName(), beneficiary.getBeneficiaryIdentification()));
        }

        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue instanceof Beneficiaries) {
                    return ((Beneficiaries) resultValue)
                            .getBeneficiaryName();
                }
                return "";
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Beneficiaries> suggestions =  new ArrayList<>();
                if (constraint == null) {
                    suggestions.addAll(originalList);
                } else {
                    String query = constraint.toString().trim().toLowerCase();
                    if (query.isEmpty()) {
                        suggestions.addAll(originalList);
                    } else {
                        for (Beneficiaries item : originalList) {
                            String name = item.getBeneficiaryName().toLowerCase();
                            String id = item.getBeneficiaryIdentification().toLowerCase();
                            if (name.contains(query) || id.contains(query)) {
                                suggestions.add(item);
                            }
                        }
                    }
                }

                results.values = suggestions;
                results.count = suggestions.size();
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                if (results.values != null) {
                    filteredList.addAll((List<Beneficiaries>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }
}