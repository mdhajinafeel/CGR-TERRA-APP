package com.cgr.codrinterraerp.ui.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.IncomeData;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.viewmodel.FinanceViewModel;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FinanceCreditFragment extends Fragment {

    private RecyclerView rvCreditTransactionLists;
    private LinearLayout llNoData;
    private RecyclerViewAdapter<IncomeData> incomeDataRecyclerViewAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finance_credit, container, false);
        try {

            rvCreditTransactionLists = view.findViewById(R.id.rvCreditTransactionLists);
            llNoData = view.findViewById(R.id.llNoData);

            FinanceViewModel financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

            // ✅ Setup RecyclerView
            rvCreditTransactionLists.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCreditTransactionLists.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect,
                                           @NonNull View view,
                                           @NonNull RecyclerView parent,
                                           @NonNull RecyclerView.State state) {
                    int position = parent.getChildAdapterPosition(view);
                    if (position == Objects.requireNonNull(parent.getAdapter()).getItemCount() - 1) {
                        outRect.bottom = getResources().getDimensionPixelSize(R.dimen.height_80);
                    }
                }
            });

            // ✅ Initialize adapter ONCE
            initializeAdapter();

            // ✅ Observe data (auto updates)
            financeViewModel.getRecentIncomeData().observe(getViewLifecycleOwner(), this::bindIncomeData);
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void initializeAdapter() {

        incomeDataRecyclerViewAdapter = new RecyclerViewAdapter<>(getContext(), new ArrayList<>(), R.layout.row_item_finance_transaction) {
            @Override
            public void onPostBindViewHolder(ViewHolder holder, IncomeData incomeData) {
                if (incomeData != null) {
                    ShapeableImageView imgIcon = holder.getView(R.id.imgAccountHead);

                    holder.setViewVisibility(R.id.tvBeneficiaryName, View.GONE);
                    holder.setViewVisibility(R.id.ivMenu, View.GONE);


                    imgIcon.setImageResource(R.drawable.ic_income_list);
                    holder.setViewText(R.id.tvAccountHeadName, CommonUtils.capitalizeWords(incomeData.getConceptGeneral()));
                    holder.setViewText(R.id.tvExpenseDate, incomeData.getTransactionDate());
                    holder.setViewText(R.id.tvAmount, CommonUtils.currencyFormat(incomeData.getAmount()));
                }
            }
        };

        rvCreditTransactionLists.setAdapter(incomeDataRecyclerViewAdapter);
        rvCreditTransactionLists.setHasFixedSize(true);
    }

    private void bindIncomeData(List<IncomeData> list) {
        try {
            if (list != null && !list.isEmpty()) {
                incomeDataRecyclerViewAdapter.setItems(list);
                rvCreditTransactionLists.setVisibility(View.VISIBLE);
                llNoData.setVisibility(View.GONE);
            } else {
                llNoData.setVisibility(View.VISIBLE);
                rvCreditTransactionLists.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindDispatchData", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}