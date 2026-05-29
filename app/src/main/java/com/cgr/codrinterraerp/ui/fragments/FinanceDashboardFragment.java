package com.cgr.codrinterraerp.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FinanceDashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finance_dashboard, container, false);
        try {
            AppCompatImageView imgBack = view.findViewById(R.id.imgBack);
            AppCompatTextView txtTitle = view.findViewById(R.id.txtTitle);
            FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);

            txtTitle.setText(R.string.finance);
            imgBack.setOnClickListener(v -> requireActivity().finish());

            fabAdd.setOnClickListener(v -> v.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(120)
                    .withEndAction(() ->
                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(120)
                                    .start())
                    .start());
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }
}