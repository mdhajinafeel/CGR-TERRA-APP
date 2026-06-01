package com.cgr.codrinterraerp.ui.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.AccountHeads;
import com.cgr.codrinterraerp.db.entities.Beneficiaries;
import com.cgr.codrinterraerp.db.entities.ExpenseData;
import com.cgr.codrinterraerp.db.entities.IncomeData;
import com.cgr.codrinterraerp.ui.adapters.BeneficiariesAdapter;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.utils.DividerItemDecoration;
import com.cgr.codrinterraerp.viewmodel.FinanceViewModel;
import com.cgr.codrinterraerp.viewmodel.MasterViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExpenseActivity extends BaseActivity {

    private TextInputLayout tiConceptGeneral, tiAccountHead, tiBeneficiary, tiIdentification, tiExpenseDate, tiAmount;
    private AppCompatEditText etConceptGeneral, etAccountHead, etIdentification, etExpenseDate, etAmount;
    private MaterialAutoCompleteTextView etBeneficiary;
    private AppCompatImageView ivFilePreview, ivRemoveFile;
    private MaterialButton btnSubmit;
    private List<AccountHeads> accountHeadsList;
    private List<IncomeData> incomeDataList;
    private List<Beneficiaries> beneficiariesList;
    private RecyclerViewAdapter<AccountHeads> accountHeadsRecyclerViewAdapter;
    private RecyclerViewAdapter<IncomeData> incomeDataRecyclerViewAdapter;
    private RecyclerViewAdapter<Beneficiaries> beneficiariesRecyclerViewAdapter;
    private FrameLayout progressBar;
    private AppCompatTextView tvNoDataFound;
    private boolean isExpenseEdit = false;
    private ExpenseData existingExpenseData;
    private FinanceViewModel financeViewModel;
    private MasterViewModel masterViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
        statusBarSetting(false);
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            AppCompatImageView imgBack = findViewById(R.id.imgBack);
            AppCompatTextView txtTitle = findViewById(R.id.txtTitle);

            tiConceptGeneral = findViewById(R.id.tiConceptGeneral);
            tiAccountHead = findViewById(R.id.tiAccountHead);
            tiBeneficiary = findViewById(R.id.tiBeneficiary);
            tiIdentification = findViewById(R.id.tiIdentification);
            tiExpenseDate = findViewById(R.id.tiExpenseDate);
            tiAmount = findViewById(R.id.tiAmount);
            etConceptGeneral = findViewById(R.id.etConceptGeneral);
            etAccountHead = findViewById(R.id.etAccountHead);
            etBeneficiary = findViewById(R.id.etBeneficiary);
            etIdentification = findViewById(R.id.etIdentification);
            etExpenseDate = findViewById(R.id.etExpenseDate);
            etAmount = findViewById(R.id.etAmount);
            ivFilePreview = findViewById(R.id.ivFilePreview);
            ivRemoveFile = findViewById(R.id.ivRemoveFile);
            btnSubmit = findViewById(R.id.btnSubmit);
            progressBar = findViewById(R.id.progressBar);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
                masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);

                txtTitle.setText(isExpenseEdit ? getString(R.string.edit_expense) : getString(R.string.add_expense));
                imgBack.setOnClickListener(v -> finish());

                CommonUtils.clearErrorOnTyping(etConceptGeneral, tiConceptGeneral);
                CommonUtils.clearErrorOnTyping(etAccountHead, tiAccountHead);
                CommonUtils.clearErrorOnTyping(etBeneficiary, tiBeneficiary);
                CommonUtils.clearErrorOnTyping(etIdentification, tiIdentification);
                CommonUtils.clearErrorOnTyping(etExpenseDate, tiExpenseDate);
                CommonUtils.clearErrorOnTyping(etAmount, tiAmount);

                financeViewModel.getProgressState().observe(this, aBoolean -> {
                    if (aBoolean) {
                        showProgress(progressBar);
                    } else {
                        hideProgress(progressBar);
                    }
                });

                if (isExpenseEdit) {
                    //existingExpenseData = farmViewModel.fetchFarmDetailById(farmView.tempFarmId);
                    fetchData(true, existingExpenseData);
                } else {
                    fetchData(false, null);
                }

                actionListeners();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.common_error), Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void fetchData(boolean isEdit, ExpenseData expenseData) {
        try {
            incomeDataList = masterViewModel.fetchIncomeData();
            accountHeadsList = masterViewModel.fetchAccountHeads();
            beneficiariesList = masterViewModel.fetchBeneficiaries();

            if (isEdit) {

                // Concept General
                for (IncomeData i : incomeDataList) {
                    if (i.getCreditTransactionId() == expenseData.getCreditTransactionId()) {
                        etConceptGeneral.setText(i.getConceptGeneral());
                        etConceptGeneral.setTag(i.getCreditTransactionId());
                        break;
                    }
                }

                // Account Head
                for (AccountHeads a : accountHeadsList) {
                    if (a.getAccountHeadId() == expenseData.getAccountHeadId()) {
                        etAccountHead.setText(a.getAccountHeadName());
                        etAccountHead.setTag(a.getAccountHeadId());
                        break;
                    }
                }

                etBeneficiary.setText(expenseData.getBeneficiaryName());
                etIdentification.setText(expenseData.getBeneficiaryIdentification());
                etExpenseDate.setText(expenseData.getExpenseDate());
                etAmount.setText(String.valueOf(expenseData.getAmount()));
            }

            List<Beneficiaries> beneficiaries = new ArrayList<>(beneficiariesList);
            if (!beneficiaries.isEmpty()) {
                BeneficiariesAdapter beneficiariesAdapter = new BeneficiariesAdapter(this, beneficiaries);
                etBeneficiary.setAdapter(beneficiariesAdapter);

                // Always show dropdown when clicked
                etBeneficiary.setOnClickListener(v -> etBeneficiary.dismissDropDown());

                // Show dropdown when focused
                etBeneficiary.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus
                            && etBeneficiary.getAdapter() != null
                            && etBeneficiary.getAdapter().getCount() > 0) {
                        etBeneficiary.showDropDown();
                    }
                });

                // Handle selection
                etBeneficiary.setOnItemClickListener((parent, view, position, id) -> {
                    Beneficiaries selected = (Beneficiaries) parent.getItemAtPosition(position);
                    etBeneficiary.setText(selected.getBeneficiaryName(), false); // ← prevents filtering issue
                    etIdentification.setText(selected.getBeneficiaryIdentification());

                    // ✅ Move cursor to end
                    etBeneficiary.setSelection(etBeneficiary.getText().length());
                });
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchData", e);
        }
    }

    private void actionListeners() {
        try {
            etConceptGeneral.setKeyListener(null);
            etAccountHead.setKeyListener(null);

            etConceptGeneral.setOnClickListener(v -> showDataDialog("Supplier"));
            etAccountHead.setOnClickListener(v -> showDataDialog("Product"));

            tiConceptGeneral.setEndIconOnClickListener(v -> showDataDialog("Supplier"));
            tiAccountHead.setEndIconOnClickListener(v -> showDataDialog("Product"));

            btnSubmit.setOnClickListener(v -> {
                btnSubmit.setEnabled(false);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "clickListeners", e);
        }
    }

    private void showDataDialog(String tag) {
        try {
            hideKeyboard(this);
            Dialog dialog = new Dialog(this, R.style.DialogTheme);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            dialog.getWindow().setDimAmount(0.6f);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);
            layoutParams.gravity = Gravity.CENTER;
            dialog.getWindow().setAttributes(layoutParams);
            dialog.setContentView(R.layout.list_dialog);

            AppCompatTextView dialogTitle = dialog.findViewById(R.id.tvDialogTitle);
            tvNoDataFound = dialog.findViewById(R.id.tvNoDataFound);
            AppCompatImageView closeDialog = dialog.findViewById(R.id.imgClose);
            AppCompatEditText etSearch = dialog.findViewById(R.id.etSearch);
            RecyclerView rvList = dialog.findViewById(R.id.rvList);
            closeDialog.setOnClickListener(v -> dialog.dismiss());
            tvNoDataFound.setVisibility(View.GONE);

            rvList.setLayoutManager(new LinearLayoutManager(this));
            rvList.addItemDecoration(new DividerItemDecoration(this));
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showDataDialog", e);
        }
    }
}