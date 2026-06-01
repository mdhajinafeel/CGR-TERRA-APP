package com.cgr.codrinterraerp.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.caverock.androidsvg.SVG;
import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.views.ExpenseView;
import com.cgr.codrinterraerp.ui.activities.ExpenseActivity;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.utils.CustomTypeFaceSpan;
import com.cgr.codrinterraerp.viewmodel.FinanceViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FinanceTransactionDebitFragment extends Fragment {

    private RecyclerView rvDebitTransactionLists;
    private LinearLayout llNoData;
    private RecyclerViewAdapter<ExpenseView> expenseDataRecyclerViewAdapter;
    private FinanceViewModel financeViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finance_debit, container, false);
        try {
            rvDebitTransactionLists = view.findViewById(R.id.rvDebitTransactionLists);
            llNoData = view.findViewById(R.id.llNoData);

            financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);

            // ✅ Setup RecyclerView
            rvDebitTransactionLists.setLayoutManager(new LinearLayoutManager(getContext()));

            // ✅ Initialize adapter ONCE
            initializeAdapter();

            // ✅ Observe data (auto updates)
            financeViewModel.getRecentExpenseList().observe(getViewLifecycleOwner(), this::bindExpenseData);
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void initializeAdapter() {

        expenseDataRecyclerViewAdapter = new RecyclerViewAdapter<>(getContext(), new ArrayList<>(), R.layout.row_item_finance_transaction) {
            @Override
            public void onPostBindViewHolder(ViewHolder holder, ExpenseView expenseView) {
                if (expenseView != null) {
                    ShapeableImageView imgIcon = holder.getView(R.id.imgAccountHead);
                    AppCompatImageView ivMenu = holder.getView(R.id.ivMenu);

                    ivMenu.setVisibility(View.VISIBLE);
                    holder.setViewText(R.id.tvAccountHeadName, CommonUtils.capitalizeWords(expenseView.accountHeadName));
                    holder.setViewText(R.id.tvBeneficiaryName, CommonUtils.capitalizeWords(expenseView.beneficiaryName));
                    holder.setViewText(R.id.tvExpenseDate, expenseView.expenseDate);
                    holder.setViewText(R.id.tvAmount, CommonUtils.currencyFormat(expenseView.amount));

                    if(expenseView.icon != null) {
                        try {
                            SVG svg = SVG.getFromString(expenseView.icon);
                            PictureDrawable drawable = new PictureDrawable(svg.renderToPicture());
                            imgIcon.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                            imgIcon.setImageDrawable(drawable);
                        } catch (Exception e) {
                            imgIcon.setImageResource(R.drawable.ic_default_accounthead);
                        }
                    } else {
                        imgIcon.setImageResource(R.drawable.ic_default_accounthead);
                    }

                    PopupMenu popupMenu = new PopupMenu(requireContext(), ivMenu, 0, 0, R.style.MyPopupMenuStyle);
                    popupMenu.inflate(R.menu.menu_expense_item);

                    Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.exo2_semibold);
                    Menu menu = popupMenu.getMenu();

                    for (int i = 0; i < menu.size(); i++) {
                        MenuItem menuItem = menu.getItem(i);

                        SpannableString spannable = new SpannableString(menuItem.getTitle());

                        // Text size (use DIMENS)
                        int textSizePx = getResources().getDimensionPixelSize(R.dimen.text_14);

                        spannable.setSpan(new AbsoluteSizeSpan(textSizePx), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        // Font family (custom typeface)
                        if (typeface != null) {
                            spannable.setSpan(new CustomTypeFaceSpan(typeface), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        if (menuItem.getItemId() == R.id.action_delete) {
                            spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            // Text color
                            spannable.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        menuItem.setTitle(spannable);
                    }

                    try {
                        Field field = popupMenu.getClass().getDeclaredField("mPopup");
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popupMenu);

                        Class<?> classPopupHelper = null;
                        if (menuPopupHelper != null) {
                            classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        }

                        Method setForceShowIcon = null;
                        if (classPopupHelper != null) {
                            setForceShowIcon = classPopupHelper.getDeclaredMethod("setForceShowIcon", boolean.class);
                        }

                        if (setForceShowIcon != null) {
                            setForceShowIcon.invoke(menuPopupHelper, true);
                        }

                    } catch (Exception e) {
                        AppLogger.e(getClass(), "createAdapter Popupmenu", e);
                    }

                    ivMenu.setOnClickListener(v -> {
                        popupMenu.setOnMenuItemClickListener(item1 -> {
                            if (item1.getItemId() == R.id.action_edit) {
                                editExpense(expenseView);
                                return true;
                            } else if (item1.getItemId() == R.id.action_delete) {
                                confirmDelete(expenseView);
                                return true;
                            }
                            return false;
                        });

                        popupMenu.show();
                    });
                }
            }
        };

        rvDebitTransactionLists.setAdapter(expenseDataRecyclerViewAdapter);
    }

    private void bindExpenseData(List<ExpenseView> list) {
        try {
            if (list != null && !list.isEmpty()) {
                expenseDataRecyclerViewAdapter.setItems(list);
                rvDebitTransactionLists.setVisibility(View.VISIBLE);
                llNoData.setVisibility(View.GONE);
            } else {
                llNoData.setVisibility(View.VISIBLE);
                rvDebitTransactionLists.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindDispatchData", e);
        }
    }

    private void editExpense(ExpenseView expenseView) {
        try {

            ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(requireContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);

            Intent intent = new Intent(requireContext(), ExpenseActivity.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("expenseData", expenseView);
            expenseResultLauncher.launch(intent, options);
        } catch (Exception e) {
            AppLogger.e(getClass(), "editExpense", e);
        }
    }

    private void confirmDelete(ExpenseView expenseView) {
        try {
            LayoutInflater dialogInflater = LayoutInflater.from(requireContext());
            View dialogView = dialogInflater.inflate(R.layout.custom_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            AppCompatTextView dialogHeader = dialogView.findViewById(R.id.dialogHeader);
            AppCompatTextView dialogBody = dialogView.findViewById(R.id.dialogBody);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
            MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);

            btnCancel.setText(getString(R.string.cancel));
            btnOk.setText(getString(R.string.ok));
            dialogHeader.setText(getString(R.string.confirmation));
            dialogBody.setText(R.string.delete_confirmation);

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnOk.setOnClickListener(v -> {

                if (financeViewModel.deleteExpense(expenseView.capturedTimeStamp)) {
                    if (expenseView.attachFileUri != null && !Objects.equals(expenseView.attachFileUri, "")) {
                        File file = new File(expenseView.attachFileUri);
                        if (file.exists()) {
                            boolean s = file.delete();
                            AppLogger.d(getClass(), "File deleted: " + s);
                            expenseDataRecyclerViewAdapter.removeItem(expenseView);
                        }
                    }
                }

                dialog.dismiss();
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "confirmDelete", e);
        }
    }

    private final ActivityResultLauncher<Intent> expenseResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        int savedExpenseId = (int) data.getLongExtra("savedExpenseId", 0);
                        boolean isExpenseEdit = data.getBooleanExtra("isEdit", false);
                        if (savedExpenseId > 0) {
                            if(!isExpenseEdit) {
                                Toast.makeText(requireContext(), getString(R.string.expense_saved), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.expense_updated), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}