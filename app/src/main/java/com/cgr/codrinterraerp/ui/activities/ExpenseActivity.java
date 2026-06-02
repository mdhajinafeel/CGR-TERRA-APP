package com.cgr.codrinterraerp.ui.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.caverock.androidsvg.SVG;
import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.AccountHeads;
import com.cgr.codrinterraerp.db.entities.Beneficiaries;
import com.cgr.codrinterraerp.db.entities.ExpenseData;
import com.cgr.codrinterraerp.db.entities.IncomeData;
import com.cgr.codrinterraerp.db.views.ExpenseView;
import com.cgr.codrinterraerp.helper.PreferenceManager;
import com.cgr.codrinterraerp.ui.adapters.BeneficiariesAdapter;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.utils.DividerItemDecoration;
import com.cgr.codrinterraerp.viewmodel.FinanceViewModel;
import com.cgr.codrinterraerp.viewmodel.MasterViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ExpenseActivity extends BaseActivity {

    private TextInputLayout tiConceptGeneral, tiAccountHead, tiBeneficiary, tiIdentification, tiExpenseDate, tiAmount;
    private AppCompatEditText etConceptGeneral, etAccountHead, etIdentification, etExpenseDate, etAmount;
    private MaterialAutoCompleteTextView etBeneficiary;
    private AppCompatImageView ivFilePreview, ivRemoveFile;
    private MaterialButton btnAttachFile, btnSubmit;
    private List<AccountHeads> accountHeadsList;
    private List<IncomeData> incomeDataList;
    private RecyclerViewAdapter<AccountHeads> accountHeadsRecyclerViewAdapter;
    private RecyclerViewAdapter<IncomeData> incomeDataRecyclerViewAdapter;
    private FrameLayout progressBar;
    private AppCompatTextView tvNoDataFound;
    private boolean isExpenseEdit = false;
    private boolean isFileAttached = false, isForestry = false, isAttachmentRemoved = false, isNewAttachmentSelected = false;
    private Uri selectedFileUri, cameraTempUri;
    private int forestryCostType = 0;
    private String existingFileUri = "";
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
            btnAttachFile = findViewById(R.id.btnAttachFile);
            progressBar = findViewById(R.id.progressBar);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
                masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);

                isExpenseEdit = bundle.getBoolean("isEdit");

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

                    ExpenseView expenseView = (ExpenseView) bundle.getSerializable("expenseData");

                    if (expenseView != null) {
                        existingExpenseData = financeViewModel.fetchExpenseById(expenseView.tempTransactionId);
                        fetchData(true, existingExpenseData);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.common_error), Toast.LENGTH_SHORT).show();
                        finish();
                    }
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
            List<Beneficiaries> beneficiariesList = masterViewModel.fetchBeneficiaries();

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

            if (expenseData != null) {
                // ✅ LOCAL FILE (offline)
                if (!TextUtils.isEmpty(expenseData.getAttachFileUri())) {
                    selectedFileUri = Uri.fromFile(new File(expenseData.getAttachFileUri()));
                    showPreviewFromUri(selectedFileUri);
                    isFileAttached = true;
                }
                // ✅ REMOTE FILE (online)
                else if (!TextUtils.isEmpty(expenseData.getAttachFileUrl())) {
                    showPreviewFromUrl(expenseData.getAttachFileUrl());
                    isFileAttached = true;
                }
            }

            List<Beneficiaries> beneficiaries = new ArrayList<>(beneficiariesList);
            if (!beneficiaries.isEmpty()) {
                BeneficiariesAdapter beneficiariesAdapter = new BeneficiariesAdapter(this, beneficiaries);

                etBeneficiary.setAdapter(beneficiariesAdapter);
                etBeneficiary.setThreshold(1);

                etBeneficiary.setOnClickListener(v -> {
                    if (!etBeneficiary.isPopupShowing()) {
                        etBeneficiary.showDropDown();
                    }
                });

                etBeneficiary.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        etBeneficiary.showDropDown();
                    }
                });

                etBeneficiary.setOnItemClickListener((parent, view, position, id) -> {
                    Beneficiaries selected = (Beneficiaries) parent.getItemAtPosition(position);
                    etBeneficiary.setText(selected.getBeneficiaryName(), false);
                    etIdentification.setText(selected.getBeneficiaryIdentification());
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

            etConceptGeneral.setOnClickListener(v -> showDataDialog("ConceptGeneral"));
            etAccountHead.setOnClickListener(v -> showDataDialog("AccountHead"));

            tiConceptGeneral.setEndIconOnClickListener(v -> showDataDialog("ConceptGeneral"));
            tiAccountHead.setEndIconOnClickListener(v -> showDataDialog("AccountHead"));

            etExpenseDate.setOnClickListener(v -> {
                hideKeyboard(this);
                CommonUtils.showDatePicker(this, etExpenseDate);
            });

            btnAttachFile.setOnClickListener(view -> {
                hideKeyboard(this);
                showPicker();
            });

            ivRemoveFile.setOnClickListener(v -> {

                selectedFileUri = null;
                isFileAttached = false;
                isAttachmentRemoved = true;
                isNewAttachmentSelected = false;

                ivFilePreview.setImageResource(R.drawable.ic_placeholder);
                ivRemoveFile.setVisibility(View.GONE);

                if (isExpenseEdit && existingExpenseData != null) {
                    existingFileUri = existingExpenseData.getAttachFileUri();
                    existingExpenseData.setAttachFileUri(null);
                    existingExpenseData.setAttachFileUrl(null);
                }
            });

            btnSubmit.setOnClickListener(v -> {
                btnSubmit.setEnabled(false);
                saveOrUpdateExpenseDetails();
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

            if (tag.equalsIgnoreCase("ConceptGeneral")) {
                dialogTitle.setText(R.string.select_concept_general);

                if (incomeDataList != null && incomeDataList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);

                    incomeDataRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(incomeDataList), R.layout.row_dialog_list) {
                        @Override
                        public void onPostBindViewHolder(ViewHolder holder, IncomeData incomeData) {

                            AppCompatTextView tvName = holder.itemView.findViewById(R.id.tvName);
                            AppCompatImageView ivSelected = holder.itemView.findViewById(R.id.ivItemSelected);

                            tvName.setText(incomeData.getConceptGeneral());

                            boolean isSelected = false;
                            if (etConceptGeneral.getTag() != null) {
                                isSelected = Objects.equals(incomeData.getCreditTransactionId(), etConceptGeneral.getTag());
                            }

                            ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

                            if (isSelected) {
                                holder.setViewTypeface(R.id.tvName,
                                        ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_bold));
                            } else {
                                holder.setViewTypeface(R.id.tvName,
                                        ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_medium));
                            }
                        }
                    };

                    rvList.setAdapter(incomeDataRecyclerViewAdapter);
                    incomeDataRecyclerViewAdapter.setOnItemClickListener((view, position) -> {

                        IncomeData selected = incomeDataRecyclerViewAdapter.getItem(position);

                        etConceptGeneral.setText(selected.getConceptGeneral());
                        etConceptGeneral.setTag(selected.getCreditTransactionId());

                        dialog.dismiss();
                    });
                }
            } else if (tag.equalsIgnoreCase("AccountHead")) {

                dialogTitle.setText(R.string.select_concept);

                if (accountHeadsList.isEmpty()) {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    tvNoDataFound.setVisibility(View.GONE);
                }

                accountHeadsRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(accountHeadsList), R.layout.row_dialog_list_icon) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, AccountHeads accountHead) {

                        AppCompatTextView tvName = holder.itemView.findViewById(R.id.tvName);
                        AppCompatImageView ivSelected = holder.itemView.findViewById(R.id.ivItemSelected);
                        AppCompatImageView imgIcon = holder.itemView.findViewById(R.id.imgIcon);

                        tvName.setText(accountHead.getAccountHeadName());
                        if (accountHead.icon != null) {
                            try {
                                SVG svg = SVG.getFromString(accountHead.icon);
                                PictureDrawable drawable = new PictureDrawable(svg.renderToPicture());
                                imgIcon.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                                imgIcon.setImageDrawable(drawable);
                            } catch (Exception e) {
                                imgIcon.setImageResource(R.drawable.ic_default_accounthead);
                            }
                        } else {
                            imgIcon.setImageResource(R.drawable.ic_default_accounthead);
                        }

                        boolean isSelected = false;
                        if (etAccountHead.getTag() != null) {
                            isSelected = Objects.equals(accountHead.getAccountHeadId(), etAccountHead.getTag());
                        }

                        ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

                        if (isSelected) {
                            holder.setViewTypeface(R.id.tvName,
                                    ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_bold));
                        } else {
                            holder.setViewTypeface(R.id.tvName,
                                    ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_medium));
                        }
                    }
                };

                rvList.setAdapter(accountHeadsRecyclerViewAdapter);
                rvList.setHasFixedSize(true);
                accountHeadsRecyclerViewAdapter.setOnItemClickListener((view, position) -> {

                    AccountHeads selected = accountHeadsRecyclerViewAdapter.getItem(position);

                    etAccountHead.setText(selected.getAccountHeadName());
                    etAccountHead.setTag(selected.getAccountHeadId());
                    isForestry = selected.isForestry();
                    forestryCostType = selected.getForestryCostType();
                    dialog.dismiss();
                });
            }

            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    String query = s.toString().trim().toLowerCase();

                    if (tag.equalsIgnoreCase("ConceptGeneral")) {
                        if (query.isEmpty()) {
                            incomeDataRecyclerViewAdapter.resetFilter();
                        } else {
                            incomeDataRecyclerViewAdapter.filter(item ->
                                    item.getConceptGeneral() != null &&
                                            item.getConceptGeneral().toLowerCase().contains(query)
                            );
                        }

                        if (incomeDataRecyclerViewAdapter.getItemCount() == 0) {
                            tvNoDataFound.setVisibility(View.VISIBLE);
                        } else {
                            tvNoDataFound.setVisibility(View.GONE);
                        }
                    } else if (tag.equalsIgnoreCase("AccountHead")) {
                        if (query.isEmpty()) {
                            accountHeadsRecyclerViewAdapter.resetFilter();
                        } else {
                            accountHeadsRecyclerViewAdapter.filter(item ->
                                    item.getAccountHeadName() != null &&
                                            item.getAccountHeadName().toLowerCase().contains(query)
                            );
                        }

                        // Optional: Show "No Data Found"
                        if (accountHeadsRecyclerViewAdapter.getItemCount() == 0) {
                            tvNoDataFound.setVisibility(View.VISIBLE);
                        } else {
                            tvNoDataFound.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showDataDialog", e);
        }
    }

    private void saveOrUpdateExpenseDetails() {
        try {
            hideKeyboard(this);

            boolean isValid = true;

            if (TextUtils.isEmpty(etConceptGeneral.getText())) {
                tiConceptGeneral.setError(getString(R.string.required_field));
                tiConceptGeneral.setErrorEnabled(true);
                isValid = false;
            } else {
                tiConceptGeneral.setErrorEnabled(false);
                tiConceptGeneral.setError(null);
            }

            if (TextUtils.isEmpty(etAccountHead.getText())) {
                tiAccountHead.setError(getString(R.string.required_field));
                tiAccountHead.setErrorEnabled(true);
                isValid = false;
            } else {
                tiAccountHead.setErrorEnabled(false);
                tiAccountHead.setError(null);
            }

            if (TextUtils.isEmpty(etBeneficiary.getText())) {
                tiBeneficiary.setError(getString(R.string.required_field));
                tiBeneficiary.setErrorEnabled(true);
                isValid = false;
            } else {
                tiBeneficiary.setErrorEnabled(false);
                tiBeneficiary.setError(null);
            }

            if (TextUtils.isEmpty(etIdentification.getText())) {
                tiIdentification.setError(getString(R.string.required_field));
                tiIdentification.setErrorEnabled(true);
                isValid = false;
            } else {
                tiIdentification.setErrorEnabled(false);
                tiIdentification.setError(null);
            }

            if (TextUtils.isEmpty(etExpenseDate.getText())) {
                tiExpenseDate.setError(getString(R.string.required_field));
                tiExpenseDate.setErrorEnabled(true);
                isValid = false;
            } else {
                tiExpenseDate.setErrorEnabled(false);
                tiExpenseDate.setError(null);
            }

            if (TextUtils.isEmpty(etAmount.getText())) {
                tiAmount.setError(getString(R.string.required_field));
                tiAmount.setErrorEnabled(true);
                isValid = false;
            } else {
                tiAmount.setErrorEnabled(false);
                tiAmount.setError(null);
            }

            if (!isValid) {
                enableSubmit();
                return;
            }

            // ================= CREATE / UPDATE OBJECT =================
            ExpenseData expenseData;
            long currentTimeStamp = CommonUtils.getCurrentLocalDateTimeStamp();
            if (isExpenseEdit && existingExpenseData != null) {
                expenseData = existingExpenseData;
            } else {
                expenseData = new ExpenseData();
            }

            // ================= COMMON FIELD SET =================
            expenseData.setCreditTransactionId(CommonUtils.getTagInt(etConceptGeneral.getTag()));
            expenseData.setAccountHeadId(CommonUtils.getTagInt(etAccountHead.getTag()));
            expenseData.setBeneficiaryName(etBeneficiary.getText().toString());
            expenseData.setBeneficiaryIdentification(etIdentification.getText().toString());
            expenseData.setExpenseDate(etExpenseDate.getText().toString().trim());
            expenseData.setAmount(Double.parseDouble(etAmount.getText().toString()));
            expenseData.setForestry(isForestry);
            expenseData.setForestryCostType(forestryCostType);

            // ================= ID HANDLING =================
            if (isExpenseEdit && existingExpenseData != null) {
                expenseData.setTempTransactionId(existingExpenseData.getTempTransactionId());
                expenseData.setTransactionId(existingExpenseData.getTransactionId());
                expenseData.setEdited(true);
            } else {
                expenseData.setTempTransactionId("EXP_" + currentTimeStamp);
                expenseData.setTransactionId(null);
                expenseData.setEdited(false);
                expenseData.setCapturedTimeStamp(System.currentTimeMillis());
            }

            // ================= ATTACHMENT HANDLING =================
            if (!isExpenseEdit) {
                // 🟢 ADD MODE → attachment mandatory
                if (!isFileAttached || selectedFileUri == null) {
                    Toast.makeText(this, getString(R.string.attach_file_error), Toast.LENGTH_SHORT).show();
                    enableSubmit();
                    return;
                }

                File attachedFile = saveFinalFile(selectedFileUri);
                expenseData.setAttachFileUri(attachedFile.getAbsolutePath());
                expenseData.setAttachFileUrl("");
                expenseData.setAttachUpdated(false);

                // 🧹 delete camera temp file
                deleteCameraTempFile();
            } else {
                // 🟡 EDIT MODE
                if (isAttachmentRemoved) {
                    // ❌ User removed attachment
                    deleteLocalFile(existingFileUri);

                    expenseData.setAttachFileUri(null);
                    expenseData.setAttachFileUrl(null);
                    expenseData.setAttachUpdated(true);
                } else if (isNewAttachmentSelected && selectedFileUri != null) {
                    // Store old attachment path
                    String oldFilePath = expenseData.getAttachFileUri();

                    // Save new attachment
                    File attachedFile = saveFinalFile(selectedFileUri);

                    expenseData.setAttachFileUri(attachedFile.getAbsolutePath());
                    expenseData.setAttachFileUrl("");
                    expenseData.setAttachUpdated(true);

                    // Delete old attachment
                    deleteLocalFile(oldFilePath);

                    // Delete camera temp file only
                    deleteCameraTempFile();
                } else {
                    // ✅ User kept existing attachment
                    expenseData.setAttachFileUri(expenseData.getAttachFileUri());
                    expenseData.setAttachFileUrl(expenseData.getAttachFileUrl());
                    expenseData.setAttachUpdated(false);
                }

                boolean hasExistingAttachment = !TextUtils.isEmpty(expenseData.getAttachFileUri()) ||
                        !TextUtils.isEmpty(expenseData.getAttachFileUrl());

                boolean hasNewAttachment = isFileAttached && selectedFileUri != null;

                if (!hasExistingAttachment && !hasNewAttachment) {
                    Toast.makeText(this, getString(R.string.attach_file_error), Toast.LENGTH_SHORT).show();
                    enableSubmit();
                    return;
                }
            }

            expenseData.setSynced(false);
            expenseData.setDeleted(false);
            expenseData.setUpdatedAt(System.currentTimeMillis());

            // ================= SAVE =================
            financeViewModel.saveExpenseDetails(expenseData);

            financeViewModel.getExpenseStatus().observe(this, new Observer<>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    financeViewModel.getExpenseStatus().removeObserver(this);

                    enableSubmit();

                    if (aBoolean) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("savedExpenseId", financeViewModel.getExpenseSavedId());
                        resultIntent.putExtra("isEdit", isExpenseEdit);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        showCustomDialog(getString(R.string.error), getString(R.string.data_added_failed), false);
                    }
                }
            });
        } catch (Exception e) {
            enableSubmit();
            AppLogger.e(getClass(), "saveOrUpdateExpenseDetails", e);
        }
    }

    private void showPicker() {
        AlertDialog dialog;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_select_source, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        view.findViewById(R.id.llCamera).setOnClickListener(v -> {
            dialog.dismiss();
            checkCameraPermissionAndOpen();
        });

        view.findViewById(R.id.llGallery).setOnClickListener(v -> {
            dialog.dismiss();
            openGallery();
        });

        view.findViewById(R.id.llFile).setOnClickListener(v -> {
            dialog.dismiss();
            openFileManager();
        });

        dialog.show();
    }

    private void openCamera() {
        try {
            File tempFile = File.createTempFile("CAM_", ".jpg", getCacheDir());

            cameraTempUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", tempFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraTempUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            cameraLauncher.launch(intent);

        } catch (Exception e) {
            AppLogger.e(getClass(), "openCamera", e);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void openFileManager() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        fileLauncher.launch(intent);
    }

    private void showPreviewFromUri(Uri uri) {

        if (uri == null) {
            ivFilePreview.setImageResource(R.drawable.ic_placeholder);
            return;
        }

        ivFilePreview.setVisibility(View.VISIBLE);
        ivRemoveFile.setVisibility(View.VISIBLE);

        String path = uri.getPath();
        if (path != null) {
            String lower = path.toLowerCase();

            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
                ivFilePreview.setImageURI(uri);
                return;
            }
        }

        String mime = getContentResolver().getType(uri);

        if (mime != null && mime.startsWith("image")) {
            ivFilePreview.setImageURI(uri);
        } else {
            ivFilePreview.setImageResource(getFileIconFromUri(uri));
        }
    }

    private int getFileIconFromUri(Uri uri) {

        String name = getFileNameFromUri(uri).toLowerCase();

        if (name.endsWith(".pdf")) return R.drawable.ic_file_pdf;
        if (name.endsWith(".doc") || name.endsWith(".docx")) return R.drawable.ic_file_doc;
        if (name.endsWith(".ppt") || name.endsWith(".pptx")) return R.drawable.ic_file_ppt;
        if (name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".csv"))
            return R.drawable.ic_file_excel;
        if (name.endsWith(".zip")) return R.drawable.ic_file_zip;
        if (name.endsWith(".rar")) return R.drawable.ic_file_rar;
        if (name.endsWith(".xml")) return R.drawable.ic_file_xml;

        return R.drawable.ic_file_generic;
    }

    private String getFileExtensionFromUrl(String url) {
        try {
            String cleanUrl = url.split("\\?")[0]; // remove query params
            return cleanUrl.substring(cleanUrl.lastIndexOf(".")).toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }

    private void showPreviewFromUrl(String fileUrl) {

        ivFilePreview.setVisibility(View.VISIBLE);
        ivRemoveFile.setVisibility(View.VISIBLE);

        String ext = getFileExtensionFromUrl(fileUrl);

        // 🖼 Image files
        if (ext.equals(".jpg") || ext.equals(".jpeg")
                || ext.equals(".png") || ext.equals(".webp")) {

            Glide.with(this)
                    .load(fileUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_file_generic)
                    .into(ivFilePreview);

        } else {
            // 📄 Document files
            ivFilePreview.setImageResource(getFileIconFromExtension(ext));
        }
    }

    private int getFileIconFromExtension(String ext) {

        return switch (ext) {
            case ".pdf" -> R.drawable.ic_file_pdf;
            case ".doc", ".docx" -> R.drawable.ic_file_doc;
            case ".ppt", ".pptx" -> R.drawable.ic_file_ppt;
            case ".xls", ".xlsx", ".csv" -> R.drawable.ic_file_excel;
            case ".zip" -> R.drawable.ic_file_zip;
            case ".rar" -> R.drawable.ic_file_rar;
            case ".xml" -> R.drawable.ic_file_xml;
            default -> R.drawable.ic_file_generic;
        };
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver()
                    .query(uri, null, null, null, null)) {

                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                AppLogger.e(getClass(), "getFileNameFromUri", e);
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
        }

        return result;
    }

    private void deleteCameraTempFile() {
        try {
            if (cameraTempUri != null) {
                File tempFile = new File(Objects.requireNonNull(cameraTempUri.getPath()));
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        AppLogger.d(getClass(), "Camera temp file deleted");
                    }
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteCameraTempFile", e);
        }
    }

    private void deleteLocalFile(String filePath) {
        try {
            if (TextUtils.isEmpty(filePath)) {
                return;
            }
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                AppLogger.d(getClass(), "Old attachment deleted: " + deleted);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteLocalFile", e);
        }
    }

    private File compressImageKeepResolution(Uri uri, File outFile) throws Exception {

        InputStream input = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        if (input != null) input.close();

        int quality = 95;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        do {
            byteArrayOutputStream.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
            quality -= 5;
        } while ((byteArrayOutputStream.size() / 1024) > 300 && quality >= 80);

        FileOutputStream fos = new FileOutputStream(outFile, false);
        fos.write(byteArrayOutputStream.toByteArray());
        fos.flush();
        fos.close();

        bitmap.recycle();
        return outFile;
    }

    private File saveFinalFile(Uri uri) throws Exception {

        File dir = new File(getFilesDir(), "uploads");
        if (!dir.exists() && !dir.mkdirs()) {
            AppLogger.e(getClass(), "Failed to create uploads dir");
        }

        String name = getFileNameFromUri(uri);
        String extension = name.contains(".")
                ? name.substring(name.lastIndexOf("."))
                : ".bin";

        File outFile = new File(
                dir,
                "FILE_" + System.currentTimeMillis() + extension
        );

        String mime = getContentResolver().getType(uri);

        if (mime != null && mime.startsWith("image")) {
            return compressImageKeepResolution(uri, outFile);
        } else {
            return copyUriToFile(uri, outFile);
        }
    }

    private File copyUriToFile(Uri uri, File outFile) throws Exception {

        InputStream in = getContentResolver().openInputStream(uri);
        OutputStream out = new FileOutputStream(outFile);

        byte[] buffer = new byte[4096];
        int read;
        if (in != null) {
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }

        Objects.requireNonNull(in).close();
        out.close();

        return outFile;
    }

    // LAUNCHER & PERMISSIONS
    private void checkCameraPermissionAndOpen() {

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
            return;
        }

        boolean askedBefore = PreferenceManager.INSTANCE.getPermissionCameraAsked();
        if (!askedBefore) {
            // 🟢 FIRST TIME → ask permission
            PreferenceManager.INSTANCE.setPermissionCameraAsked(true);
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            return;
        }

        // Permission NOT granted
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // 🟡 Denied once → explain + ask again
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.camera_permission_required))
                    .setMessage(getString(R.string.camera_access_is_required_to_take_photos_for_expense_attachments))
                    .setPositiveButton(getString(R.string.allow), (d, w) ->
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        } else {
            // 🔴 Permanently denied → settings
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.permission_required))
                    .setMessage(getString(R.string.camera_permission_is_disabled_please_enable_it_from_app_settings))
                    .setPositiveButton(getString(R.string.open_settings), (d, w) -> openAppSettings())
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    selectedFileUri = cameraTempUri;
                    showPreviewFromUri(selectedFileUri);

                    isFileAttached = true;
                    isNewAttachmentSelected = true;
                    isAttachmentRemoved = false;
                }
            });

    ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    showPreviewFromUri(selectedFileUri);

                    isFileAttached = true;
                    isNewAttachmentSelected = true;
                    isAttachmentRemoved = false;
                }
            });

    ActivityResultLauncher<Intent> fileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    showPreviewFromUri(selectedFileUri);

                    isFileAttached = true;
                    isNewAttachmentSelected = true;
                    isAttachmentRemoved = false;
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera(); // 🔥 permission granted → open camera
                } else {
                    Toast.makeText(this, getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show();
                }
            });

    private void enableSubmit() {
        btnSubmit.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}