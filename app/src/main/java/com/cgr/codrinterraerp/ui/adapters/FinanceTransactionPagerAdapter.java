package com.cgr.codrinterraerp.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.cgr.codrinterraerp.ui.fragments.FinanceCreditFragment;
import com.cgr.codrinterraerp.ui.fragments.FinanceDebitFragment;
import com.cgr.codrinterraerp.ui.fragments.FinanceTransactionCreditFragment;
import com.cgr.codrinterraerp.ui.fragments.FinanceTransactionDebitFragment;

public class FinanceTransactionPagerAdapter extends FragmentStateAdapter {

    public FinanceTransactionPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new FinanceTransactionDebitFragment();
        } else {
            return new FinanceTransactionCreditFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    // ✅ Prevent fragment reuse issues
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean containsItem(long itemId) {
        return itemId == 0 || itemId == 1;
    }
}