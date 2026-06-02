package com.cgr.codrinterraerp.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FinanceTransactionCreditFragment extends Fragment {

    private RecyclerView rvCreditTransactionLists;
    private LinearLayout llNoData;
    private RecyclerViewAdapter<IncomeData> incomeDataRecyclerViewAdapter;
    private FinanceViewModel financeViewModel;
    private boolean incomeScrollAttached = false;
    private FloatingActionButton fabScrollTop;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finance_credit, container, false);
        try {

            rvCreditTransactionLists = view.findViewById(R.id.rvCreditTransactionLists);
            llNoData = view.findViewById(R.id.llNoData);
            fabScrollTop = view.findViewById(R.id.fabScrollTop);

            financeViewModel = new ViewModelProvider(requireParentFragment()).get(FinanceViewModel.class);

            // ✅ Setup RecyclerView
            rvCreditTransactionLists.setLayoutManager(new LinearLayoutManager(getContext()));

            ViewCompat.setOnApplyWindowInsetsListener(rvCreditTransactionLists, (v, insets) -> {
                int bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottom);
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(fabScrollTop, (v, insets) -> {
                int bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottom);
                return insets;
            });

            // ✅ Initialize adapter ONCE
            initializeAdapter();

            fabScrollTop.hide();
            fabScrollTop.setOnClickListener(v -> {
                if (rvCreditTransactionLists.getVisibility() == View.VISIBLE) {
                    rvCreditTransactionLists.smoothScrollToPosition(0);
                }
                fabScrollTop.hide();
            });

            financeViewModel.getFilterLiveData()
                    .observe(getViewLifecycleOwner(), filter -> {
                        financeViewModel.resetIncomePaging();
                        incomeDataRecyclerViewAdapter.clear();
                        financeViewModel.loadInitialIncomeData(
                                filter.isFilterApplied,
                                filter.transactionId
                        );
                    });
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void initializeAdapter() {

        // ✅ Observe data (auto updates)
        financeViewModel.getIncomePage().observe(getViewLifecycleOwner(), page -> {
            if (page != null && !page.isEmpty()) {
                incomeDataRecyclerViewAdapter.addAll(page);
            }

            if (incomeDataRecyclerViewAdapter.getItemCount() == 0) {
                rvCreditTransactionLists.setVisibility(View.GONE);
                llNoData.setVisibility(View.VISIBLE);
            } else {
                rvCreditTransactionLists.setVisibility(View.VISIBLE);
                llNoData.setVisibility(View.GONE);
            }
        });

        financeViewModel.loadInitialIncomeData(false, 0);

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

        attachIncomeScroll();
    }

    private void attachIncomeScroll() {

        if (incomeScrollAttached) {
            return;
        }

        incomeScrollAttached = true;

        rvCreditTransactionLists.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);

                LinearLayoutManager layout =  (LinearLayoutManager) rv.getLayoutManager();

                if (layout == null) {
                    return;
                }

                // ✅ Show FAB when user scrolls down
                if (rv.canScrollVertically(-1)) {
                    fabScrollTop.show();
                } else {
                    fabScrollTop.hide();
                }

                // ✅ Pagination
                if (layout.findLastVisibleItemPosition()  >= incomeDataRecyclerViewAdapter.getItemCount() - 3) {
                    financeViewModel.loadNextIncomePage();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        fabScrollTop.hide();
    }
}