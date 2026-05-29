package com.cgr.codrinterraerp.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.cgr.codrinterraerp.ui.fragments.DispatchFragment;
import com.cgr.codrinterraerp.ui.fragments.FinanceCreditFragment;
import com.cgr.codrinterraerp.ui.fragments.FinanceDebitFragment;
import com.cgr.codrinterraerp.ui.fragments.ReceptionFragment;

public class TabPagerFinanceAdapter extends FragmentStateAdapter {

    public TabPagerFinanceAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new FinanceCreditFragment();
        } else {
            return new FinanceDebitFragment();
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