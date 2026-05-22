package com.cgr.codrinterraerp.ui.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.constants.IAPIConstants;
import com.cgr.codrinterraerp.model.BackupModel;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.ui.common.BaseActivity;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DataBackupActivity extends BaseActivity {

    private RecyclerView rvDataBackupList;
    private final List<BackupModel> backupModelList = new ArrayList<>();
    private LinearLayout llNoData;
    private RecyclerViewAdapter<BackupModel> backupModelRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_backup);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            AppCompatImageView imgBack = findViewById(R.id.imgBack);
            AppCompatTextView txtTitle = findViewById(R.id.txtTitle);
            MaterialButton btnCreateBackup = findViewById(R.id.btnCreateBackup);
            rvDataBackupList = findViewById(R.id.rvDataBackupList);
            llNoData = findViewById(R.id.llNoData);

            txtTitle.setText(getString(R.string.data_backup));
            imgBack.setOnClickListener(view -> finish());

            btnCreateBackup.setOnClickListener(view -> generateBackup());

            if (!checkPermission()) {
                requestPermission();
            }

            loadBackups();
            bindBackupData();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void loadBackups() {
        try {
            backupModelList.clear();

            File backupDir = new File(Environment.getExternalStorageDirectory(), "Codrin Group");
            if (!backupDir.exists()) {
                return;
            }

            File[] files = backupDir.listFiles();
            if (files == null || files.length == 0) {
                return;
            }

            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

            for (File file : files) {
                BackupModel model = new BackupModel();
                model.file = file;
                model.name = file.getName().replace(".zip", "");
                model.size = Formatter.formatShortFileSize(getApplicationContext(), file.length());
                model.date = DateFormat.format("dd MMM yyyy hh:mm a", file.lastModified()).toString();
                model.uploadCount = getZipFileCount(file);
                backupModelList.add(model);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "loadBackups", e);
        }
    }

    private void bindBackupData() {
        try {

            backupModelRecyclerViewAdapter = new RecyclerViewAdapter<>(getApplicationContext(), backupModelList, R.layout.row_item_backup) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, BackupModel backupModel) {
                    holder.setViewText(R.id.txtName, backupModel.name);
                    holder.setViewText(R.id.txtDate, backupModel.date);
                    holder.setViewText(R.id.txtSize, backupModel.size);
                    holder.setViewText(R.id.txtUploadCount, String.valueOf(backupModel.uploadCount));

                    holder.itemView.findViewById(R.id.ivShare).setOnClickListener(v -> shareBackup(backupModel.file));

                    holder.itemView.findViewById(R.id.ivDelete).setOnClickListener(v -> showDeleteBackupDialog(holder.getAbsoluteAdapterPosition(), backupModel));
                }
            };

            rvDataBackupList.setLayoutManager(new LinearLayoutManager(this));
            rvDataBackupList.setHasFixedSize(true);
            rvDataBackupList.setAdapter(backupModelRecyclerViewAdapter);

            updateEmptyView();

        } catch (Exception e) {
            AppLogger.e(getClass(), "bindBackupData", e);
        }
    }

    private void updateEmptyView() {

        if (backupModelList.isEmpty()) {
            llNoData.setVisibility(View.VISIBLE);
            rvDataBackupList.setVisibility(View.GONE);
        } else {
            llNoData.setVisibility(View.GONE);
            rvDataBackupList.setVisibility(View.VISIBLE);
        }
    }

    private void generateBackup() {
        try {
            if (checkPermission()) {
                exportBackup();
            } else {
                requestPermission();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "generateBackup", e);
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 30) {
            return Environment.isExternalStorageManager();
        }
        return ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.READ_EXTERNAL_STORAGE") == 0
                && ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.WRITE_EXTERNAL_STORAGE") == 0;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 30) {
            try {
                Intent intent = new Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse("package:" + getPackageName()));
                manageAllFilesAccessPermissionLauncher.launch(intent);
            } catch (Exception unused) {
                Intent intent2 = new Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION");
                manageAllFilesAccessPermissionLauncher.launch(intent2);
            }
        } else {
            ActivityCompat.requestPermissions(DataBackupActivity.this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 101);
        }
    }

    private final ActivityResultLauncher<Intent> manageAllFilesAccessPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (Build.VERSION.SDK_INT >= 30 && Environment.isExternalStorageManager()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.permission_granted), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                        }

                        loadBackups();
                    });

    private void exportBackup() {
        try {

            if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
                startActivity(new Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION"));
            }

            File dbFile = getDatabasePath(IAPIConstants.DBNAME);
            File exportDir = new File(Environment.getExternalStorageDirectory(), "Codrin Group");

            if (!exportDir.exists() && !exportDir.mkdirs()) {
                AppLogger.e(getClass(), "Failed to create directory: " + exportDir.getAbsolutePath());
                return;
            }

            String fileName = "CGR_BACKUP_" + CommonUtils.convertTimeStampToDate(CommonUtils.getCurrentLocalDateTimeStamp(),
                    getString(R.string.date_format), getApplicationContext()) + ".zip";
            File zipFile = new File(exportDir, fileName);

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {

                // 📦 Database files
                addFileToZip(dbFile, zos, "database/" + dbFile.getName());
                addFileToZip(new File(dbFile.getPath() + "-wal"), zos, "database/" + dbFile.getName() + "-wal");
                addFileToZip(new File(dbFile.getPath() + "-shm"), zos, "database/" + dbFile.getName() + "-shm");

                // 📁 Uploads folder
                File uploadsDir = new File(getFilesDir(), "container_images");
                addFolderToZip(uploadsDir, zos, "container_images");
            }

            Toast.makeText(getApplicationContext(), getString(R.string.database_exported_successfully), Toast.LENGTH_SHORT).show();

            // Add newly created backup to top of list
            BackupModel model = new BackupModel();
            model.file = zipFile;
            model.name = zipFile.getName().replace(".zip", "");
            model.size = Formatter.formatShortFileSize(getApplicationContext(), zipFile.length());
            model.date = DateFormat.format("dd MMM yyyy hh:mm a", zipFile.lastModified()).toString();
            model.uploadCount = getZipFileCount(zipFile);

            backupModelList.add(0, model);

            backupModelRecyclerViewAdapter.notifyItemInserted(0);
            rvDataBackupList.scrollToPosition(0);
            updateEmptyView();
        } catch (Exception e) {
            AppLogger.e(getClass(), "exportDatabaseAsZip", e);
        }
    }

    private void addFileToZip(File file, ZipOutputStream zos, String zipEntryName) {
        if (!file.exists()) return;

        try (FileInputStream fis = new FileInputStream(file)) {

            ZipEntry zipEntry = new ZipEntry(zipEntryName);
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[4096];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
        } catch (Exception e) {
            AppLogger.e(getClass(), "addFileToZip", e);
        }
    }

    private void addFolderToZip(File folder, ZipOutputStream zos, String parentPath) {

        if (folder == null || !folder.exists()) return;
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            // Preserve folder structure
            String zipPath = parentPath + "/" + file.getName();
            if (file.isDirectory()) {
                // Recursive call for subfolders
                addFolderToZip(file, zos, zipPath);
            } else {
                addFileToZip(file, zos, zipPath);
            }
        }
    }

    private int getZipFileCount(File zipFile) {
        int count = 0;
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    count++;
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "getZipFileCount", e);
        }
        return count;
    }

    private void shareBackup(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/zip");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, getString(R.string.share_backup)));
        } catch (Exception e) {
            AppLogger.e(getClass(), "shareBackup", e);
        }
    }

    private void showDeleteBackupDialog(int position, BackupModel backupModel) {
        try {
            LayoutInflater dialogInflater = LayoutInflater.from(this);
            View dialogView = dialogInflater.inflate(R.layout.custom_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            AppCompatTextView dialogHeader = dialogView.findViewById(R.id.dialogHeader);
            AppCompatTextView dialogBody = dialogView.findViewById(R.id.dialogBody);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
            MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);

            btnOk.setText(getString(R.string.yes));
            dialogHeader.setText(R.string.delete_backup);
            dialogBody.setText(R.string.delete_backup_confirmation);

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnOk.setOnClickListener(v -> {
                deleteBackup(position, backupModel);
                dialog.dismiss();
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showClearLogsDialog", e);
        }
    }

    private void deleteBackup(int position, BackupModel backupModel) {
        try {
            if (backupModel.file.exists() && backupModel.file.delete()) {
                backupModelList.remove(position);
                backupModelRecyclerViewAdapter.notifyItemRemoved(position);
                updateEmptyView();
                Toast.makeText(getApplicationContext(), getString(R.string.backup_deleted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_to_delete_backup), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteBackup", e);
        }
    }
}