package com.cgr.codrinterraerp.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.ContainerImages;
import com.cgr.codrinterraerp.db.views.DispatchView;
import com.cgr.codrinterraerp.helper.PreferenceManager;
import com.cgr.codrinterraerp.ui.activities.DispatchActivity;
import com.cgr.codrinterraerp.ui.activities.DispatchDataActivity;
import com.cgr.codrinterraerp.ui.adapters.RecyclerViewAdapter;
import com.cgr.codrinterraerp.ui.adapters.ViewHolder;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.viewmodel.DispatchViewModel;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DispatchFragment extends Fragment {

    private AppCompatTextView filterDropDown;
    private RecyclerView rvDispatchLists, rvContainerImages;
    private LinearLayout llNoData, llContainerImages;
    private AppCompatTextView tvNoDataFound;
    private RecyclerViewAdapter<DispatchView> dispatchViewRecyclerViewAdapter;
    private DispatchViewModel dispatchViewModel;
    private Uri selectedFileUri, cameraTempUri;
    private String selectedTempDispatchId = "";
    private final List<ContainerImages> containerImagesList = new ArrayList<>();
    private RecyclerViewAdapter<ContainerImages> containerImagesRecyclerViewAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dispatch, container, false);
        try {
            MaterialButton btnAddReception = view.findViewById(R.id.btnAddDispatch);
            rvDispatchLists = view.findViewById(R.id.rvDispatchLists);
            filterDropDown = view.findViewById(R.id.filterDropDown);
            llNoData = view.findViewById(R.id.llNoData);

            dispatchViewModel = new ViewModelProvider(this).get(DispatchViewModel.class);

            btnAddReception.setOnClickListener(v -> {

                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(requireContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                Intent intent = new Intent(requireActivity(), DispatchActivity.class);
                intent.putExtra("isEdit", false);
                dispatchResultLauncher.launch(intent, options);
            });

            // ✅ Setup RecyclerView
            rvDispatchLists.setLayoutManager(new LinearLayoutManager(getContext()));

            // ✅ Initialize adapter ONCE
            initializeAdapter();

            // ✅ Observe data (auto updates)
            dispatchViewModel.getDispatchList().observe(getViewLifecycleOwner(), this::bindDispatchData);

            bindFilterOption();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void initializeAdapter() {

        dispatchViewRecyclerViewAdapter = new RecyclerViewAdapter<>(getContext(), new ArrayList<>(), R.layout.row_item_warehouse_dispatch) {
            @Override
            public void onPostBindViewHolder(ViewHolder holder, DispatchView dispatchView) {
                if (dispatchView != null) {

                    AppCompatImageButton btnInfoDispatch = holder.getView(R.id.btnInfoDispatch);
                    AppCompatImageButton btnEditDispatch = holder.getView(R.id.btnEditDispatch);

                    holder.setViewText(R.id.tvContainerNumber, dispatchView.containerNumber);
                    holder.setViewText(R.id.tvContainerCategory, dispatchView.category.toUpperCase());
                    holder.setViewText(R.id.tvShippingLine, dispatchView.shippingLine);
                    holder.setViewText(R.id.tvPieces, String.valueOf(dispatchView.totalPieces));

                    if (dispatchView.productTypeId == 1 || dispatchView.productTypeId == 3) {
                        holder.setViewImageDrawable(R.id.ivTypeIcon, ContextCompat.getDrawable(requireContext(), R.drawable.ic_square_logs));

                        holder.setViewText(R.id.tvAvgGirthTitle, getString(R.string.volume_pie));
                        holder.setViewText(R.id.tvAvgGirth, CommonUtils.formatNumber2(dispatchView.totalVolumePie));
                    } else {
                        holder.setViewImageDrawable(R.id.ivTypeIcon, ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_logs));

                        if (dispatchView.categoryId == 1) {
                            holder.setViewText(R.id.tvAvgGirthTitle, getString(R.string.cft));
                            holder.setViewText(R.id.tvAvgGirth, String.valueOf(dispatchView.cft));
                        } else {
                            holder.setViewText(R.id.tvAvgGirthTitle, getString(R.string.avg_girth));
                            holder.setViewText(R.id.tvAvgGirth, String.valueOf(dispatchView.avgGirth));
                        }
                    }

                    holder.setViewText(R.id.tvGrossVolume, CommonUtils.formatNumber3(dispatchView.totalGrossVolume));
                    holder.setViewText(R.id.tvNetVolume, CommonUtils.formatNumber3(dispatchView.totalNetVolume));
                    holder.setViewText(R.id.tvDate, dispatchView.dispatchDate);

                    holder.getView(R.id.btnEditDispatch).setTag(dispatchView);
                    holder.getView(R.id.btnDeleteDispatch).setTag(dispatchView);

                    holder.getView(R.id.btnDeleteDispatch).setOnClickListener(v -> deleteDispatch(dispatchView));

                    holder.getView(R.id.btnContainerData).setOnClickListener(view -> {
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(requireContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                        Intent intent = new Intent(requireActivity(), DispatchDataActivity.class);
                        intent.putExtra("dispatchDetails", dispatchView);
                        dispatchDataResultLauncher.launch(intent, options);
                    });

                    holder.getView(R.id.btnContainerImages).setOnClickListener(v -> {
                        selectedTempDispatchId = dispatchView.tempDispatchId;
                        showContainerImages();
                    });

                    if (dispatchView.isClosed) {
                        btnInfoDispatch.setVisibility(View.VISIBLE);
                        btnEditDispatch.setVisibility(View.GONE);

                        btnInfoDispatch.setOnClickListener(v -> showDispatchDetails(dispatchView));
                    } else {
                        btnEditDispatch.setVisibility(View.VISIBLE);
                        btnInfoDispatch.setVisibility(View.GONE);

                        holder.getView(R.id.btnEditDispatch).setOnClickListener(v -> {
                            ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(requireContext(), R.anim.fade_fast_in, R.anim.fade_fast_out);
                            Intent intent = new Intent(requireActivity(), DispatchActivity.class);
                            intent.putExtra("isEdit", true);
                            intent.putExtra("dispatchDetails", dispatchView);
                            dispatchResultLauncher.launch(intent, options);
                        });
                    }
                }
            }
        };

        rvDispatchLists.setAdapter(dispatchViewRecyclerViewAdapter);
    }

    // ✅ Bind data (only update adapter)
    private void bindDispatchData(List<DispatchView> list) {
        try {
            if (list != null && !list.isEmpty()) {
                dispatchViewRecyclerViewAdapter.setItems(list); // 🔥 only update data
                rvDispatchLists.setVisibility(View.VISIBLE);
                llNoData.setVisibility(View.GONE);
            } else {
                llNoData.setVisibility(View.VISIBLE);
                rvDispatchLists.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindDispatchData", e);
        }
    }

    // ✅ Activity result launcher
    private final ActivityResultLauncher<Intent> dispatchResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                int savedDispatchId = (int) data.getLongExtra("savedDispatchId", 0);
                                boolean isClosed = data.getBooleanExtra("isClosed", false);
                                boolean isOpened = data.getBooleanExtra("isOpened", false);
                                boolean isDispatchEdit = data.getBooleanExtra("isEdit", false);
                                if (savedDispatchId > 0) {
                                    if(isDispatchEdit) {
                                        Toast.makeText(requireContext(), getString(R.string.dispatch_updated_successfully), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(requireContext(), getString(R.string.dispatch_added_successfully), Toast.LENGTH_SHORT).show();
                                    }
                                } else if(isClosed) {
                                    Toast.makeText(requireContext(), getString(R.string.dispatch_closed_successfully), Toast.LENGTH_SHORT).show();
                                } else if(isOpened) {
                                    Toast.makeText(requireContext(), getString(R.string.dispatch_opened_successfully), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> dispatchDataResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            AppLogger.i(getClass(), "Dispatch Data");
                        }
                    }
            );

    private void deleteDispatch(DispatchView dispatchView) {
        try {
            LayoutInflater dialogInflater = LayoutInflater.from(requireContext());
            View dialogView = dialogInflater.inflate(R.layout.custom_dialog_delete, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            AppCompatTextView dialogHeader = dialogView.findViewById(R.id.dialogHeader);
            AppCompatTextView dialogBody = dialogView.findViewById(R.id.dialogBody);
            MaterialButton btnDelete = dialogView.findViewById(R.id.btnDelete);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

            dialogHeader.setText(R.string.confirmation);
            dialogBody.setText(R.string.delete_confirmation);

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnDelete.setOnClickListener(v -> new Thread(() -> {

                int deleted = dispatchViewModel.deleteDispatchDetails(dispatchView.tempDispatchId, System.currentTimeMillis());

                // Switch to main thread safely
                new Handler(Looper.getMainLooper()).post(() -> {

                    // ✅ Prevent crash if fragment is not attached
                    if (!isAdded() || getContext() == null) return;

                    if (deleted > 0) {
                        Toast.makeText(getContext(), getString(R.string.data_deleted), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.data_deleted_failed), Toast.LENGTH_SHORT).show();
                    }

                    // ✅ Safe dialog dismiss
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                });

            }).start());

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteReception", e);
        }
    }

    private void showContainerImages() {
        try {
            LayoutInflater dialogInflater = LayoutInflater.from(requireContext());
            View dialogView = dialogInflater.inflate(R.layout.dialog_container_images, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            AppCompatTextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
            AppCompatImageView imgClose = dialogView.findViewById(R.id.imgClose);
            MaterialButton btnAddContainerImage = dialogView.findViewById(R.id.btnAddContainerImage);
            rvContainerImages = dialogView.findViewById(R.id.rvContainerImages);
            llContainerImages = dialogView.findViewById(R.id.llContainerImages);
            tvNoDataFound = dialogView.findViewById(R.id.tvNoDataFound);

            imgClose.setOnClickListener(view -> dialog.dismiss());
            tvDialogTitle.setText(getString(R.string.container_images));

            btnAddContainerImage.setOnClickListener(view -> showPicker());

            setupRecyclerView();
            observeData();

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showContainerImages", e);
        }
    }

    private void showPicker() {
        AlertDialog dialog;

        View view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_select_source, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);

        dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        view.findViewById(R.id.llFile).setVisibility(View.GONE);

        view.findViewById(R.id.llCamera).setOnClickListener(v -> {
            dialog.dismiss();
            checkCameraPermissionAndOpen();
        });

        view.findViewById(R.id.llGallery).setOnClickListener(v -> {
            dialog.dismiss();
            openGallery();
        });

        dialog.show();
    }

    // LAUNCHER & PERMISSIONS
    private void checkCameraPermissionAndOpen() {

        if (requireActivity().checkSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

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
            new AlertDialog.Builder(requireActivity())
                    .setTitle(getString(R.string.camera_permission_required))
                    .setMessage(getString(R.string.camera_access_is_required_to_take_photos_for_expense_attachments))
                    .setPositiveButton(getString(R.string.allow), (d, w) ->
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        } else {
            // 🔴 Permanently denied → settings
            new AlertDialog.Builder(requireActivity())
                    .setTitle(getString(R.string.permission_required))
                    .setMessage(getString(R.string.camera_permission_is_disabled_please_enable_it_from_app_settings))
                    .setPositiveButton(getString(R.string.open_settings), (d, w) -> openAppSettings())
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + requireActivity().getPackageName()));
        startActivity(intent);
    }

    ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    selectedFileUri = cameraTempUri;
                    processAndSaveImage(selectedFileUri, selectedTempDispatchId);
                }
            });

    ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    processAndSaveImage(selectedFileUri, selectedTempDispatchId);
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera(); // 🔥 permission granted → open camera
                } else {
                    Toast.makeText(requireActivity(), getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show();
                }
            });

    private void openCamera() {
        try {
            File tempFile = File.createTempFile("CAM_", ".jpg", requireActivity().getCacheDir());
            cameraTempUri = FileProvider.getUriForFile(requireActivity(), requireActivity().getPackageName() + ".fileprovider", tempFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraTempUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cameraLauncher.launch(intent);
        } catch (Exception e) {
            AppLogger.e(getClass(), "openCamera", e);
        }
    }

    private void deleteCameraTempFile() {
        try {
            if (cameraTempUri != null && "file".equals(cameraTempUri.getScheme())) {
                File temp = new File(Objects.requireNonNull(cameraTempUri.getPath()));
                if (temp.exists() && temp.delete()) {
                    AppLogger.d(getClass(), "Camera temp file deleted");
                }
            }

            if ("file".equals(selectedFileUri.getScheme())) {
                if (selectedFileUri.getPath() != null) {
                    File temp = new File(selectedFileUri.getPath());
                    if (temp.exists()) {
                        if (temp.delete()) {
                            AppLogger.d(getClass(), "Cache File Deleted");
                        }
                    }
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteCameraTempFile", e);
        }
    }

    private File compressImage(Uri uri, File outFile) throws Exception {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream boundsInput = requireActivity().getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(boundsInput, null, options);

        if (boundsInput != null) {
            boundsInput.close();
        }

        // Original size
        int photoW = options.outWidth;
        int photoH = options.outHeight;

        // Max size
        int maxWidth = 1280;
        int maxHeight = 1280;

        int scaleFactor = 1;

        while ((photoW / scaleFactor) > maxWidth || (photoH / scaleFactor) > maxHeight) {
            scaleFactor *= 2;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();

        decodeOptions.inSampleSize = scaleFactor;
        decodeOptions.inPreferredConfig = Bitmap.Config.RGB_565;

        InputStream bitmapInput = requireActivity().getContentResolver().openInputStream(uri);

        Bitmap bitmap = BitmapFactory.decodeStream(bitmapInput, null, decodeOptions);

        if (bitmapInput != null) {
            bitmapInput.close();
        }

        int quality = 90;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        do {
            bos.reset();
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            }
            quality -= 5;
        } while ((bos.size() / 1024) > 500 && quality >= 40);

        FileOutputStream fos = new FileOutputStream(outFile, false);
        fos.write(bos.toByteArray());
        fos.flush();
        fos.close();

        if (bitmap != null) {
            bitmap.recycle();
        }

        return outFile;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void processAndSaveImage(Uri uri, String tempDispatchId) {
        try {

            File mainFolder = new File(requireContext().getFilesDir(), "container_images");
            if (!mainFolder.exists() && !mainFolder.mkdirs()) {
                AppLogger.e(getClass(), "processAndSaveImage", new Exception("Failed to create main folder"));
                return;
            }

            File containerFolder = new File(mainFolder, tempDispatchId);
            if (!containerFolder.exists() && !containerFolder.mkdirs()) {
                AppLogger.e(getClass(), "processAndSaveImage", new Exception("Failed to create container folder"));
                return;
            }

            String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";

            File compressedFile = new File(containerFolder, fileName);

            String imagePath = compressImage(uri, compressedFile).getAbsolutePath();

            ContainerImages image = new ContainerImages();
            image.tempDispatchId = tempDispatchId;
            image.imagePath = imagePath;
            image.createdAt = System.currentTimeMillis();
            image.isDeleted = false;
            image.isSynced = false;
            image.tempContainerImageId = "CI_" + System.currentTimeMillis();

            // INSERT ROOM DB
            new Thread(() -> {
                long insertedId = dispatchViewModel.insertContainerImage(image);
                requireActivity().runOnUiThread(() -> {
                    if (insertedId > 0) {
                        image.id = (int) insertedId;

                        containerImagesList.add(0, image);

                        if (containerImagesRecyclerViewAdapter != null) {
                            containerImagesRecyclerViewAdapter.notifyItemInserted(0);
                        }

                        llContainerImages.setVisibility(View.VISIBLE);
                        tvNoDataFound.setVisibility(View.GONE);

                        deleteCameraTempFile();

                        Toast.makeText(requireContext(), getString(R.string.image_added), Toast.LENGTH_SHORT).show();
                    }

                });
            }).start();
        } catch (Exception e) {
            AppLogger.e(getClass(), "processAndSaveImage", e);
        }
    }

    private void setupRecyclerView() {

        containerImagesRecyclerViewAdapter = new RecyclerViewAdapter<>(getContext(), containerImagesList, R.layout.row_item_container_image) {

            @Override
            public void onPostBindViewHolder(ViewHolder holder, ContainerImages containerImage) {
                if (containerImage != null) {
                    AppCompatImageView ivContainerPhoto = holder.getView(R.id.ivContainerPhoto);

                    Glide.with(requireContext())
                            .load(new File(containerImage.imagePath))
                            .centerCrop()
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.stat_notify_error)
                            .into(ivContainerPhoto);

                    holder.getView(R.id.cardDelete).setOnClickListener(v -> deleteContainerImage(containerImage, holder.getBindingAdapterPosition()));
                }
            }
        };

        rvContainerImages.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvContainerImages.setHasFixedSize(true);
        rvContainerImages.setAdapter(containerImagesRecyclerViewAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void observeData() {
        dispatchViewModel.getContainerImages(selectedTempDispatchId).observe(this, list -> {
            containerImagesList.clear();
            if (list != null && !list.isEmpty()) {
                containerImagesList.addAll(list);
                llContainerImages.setVisibility(View.VISIBLE);
                tvNoDataFound.setVisibility(View.GONE);
            } else {
                llContainerImages.setVisibility(View.GONE);
                tvNoDataFound.setVisibility(View.VISIBLE);
            }

            if (containerImagesRecyclerViewAdapter != null) {
                containerImagesRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    private void deleteContainerImage(ContainerImages containerImage, int position) {
        try {
            LayoutInflater dialogInflater = LayoutInflater.from(requireContext());
            View dialogView = dialogInflater.inflate(R.layout.custom_dialog_delete, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            AppCompatTextView dialogHeader = dialogView.findViewById(R.id.dialogHeader);
            AppCompatTextView dialogBody = dialogView.findViewById(R.id.dialogBody);
            MaterialButton btnDelete = dialogView.findViewById(R.id.btnDelete);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

            dialogHeader.setText(R.string.confirmation);
            dialogBody.setText(R.string.delete_confirmation);

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnDelete.setOnClickListener(v -> {
                try {
                    new Thread(() -> {
                        int deletedRows;
                        // SOFT DELETE
                        if (containerImage.isSynced) {
                            deletedRows = dispatchViewModel.softDeleteImage(containerImage.tempContainerImageId);
                        } else {
                            // HARD DELETE
                            deletedRows = dispatchViewModel.hardDeleteImage(containerImage.tempContainerImageId);
                        }

                        // DELETE FILE
                        File imageFile = new File(containerImage.imagePath);
                        File parentFolder = imageFile.getParentFile();

                        if (imageFile.exists()) {
                            boolean imageDeleted = imageFile.delete();

                            if (!imageDeleted) {
                                AppLogger.e(getClass(), "deleteContainerImage", new Exception("Failed to delete image file"));
                            }
                        }

                        // DELETE EMPTY FOLDER
                        if (parentFolder != null && parentFolder.exists()) {
                            File[] files = parentFolder.listFiles();

                            if (files == null || files.length == 0) {
                                boolean folderDeleted = parentFolder.delete();

                                if (!folderDeleted) {
                                    AppLogger.e(getClass(), "deleteContainerImage", new Exception("Failed to delete empty folder"));
                                }
                            }
                        }

                        int finalDeletedRows = deletedRows;
                        requireActivity().runOnUiThread(() -> {
                            if (finalDeletedRows > 0) {
                                if (position >= 0 && position < containerImagesList.size()) {
                                    containerImagesList.remove(position);
                                    if (containerImagesRecyclerViewAdapter != null) {
                                        containerImagesRecyclerViewAdapter.notifyItemRemoved(position);
                                    }
                                }

                                if (containerImagesList.isEmpty()) {
                                    llContainerImages.setVisibility(View.GONE);
                                    tvNoDataFound.setVisibility(View.VISIBLE);
                                }

                                Toast.makeText(requireContext(), getString(R.string.photo_deleted), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();

                    dialog.dismiss();
                } catch (Exception e) {
                    AppLogger.e(getClass(), "deleteContainerImage", e);
                }
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteContainerImage", e);
        }
    }

    private void bindFilterOption() {
        try {
            filterDropDown.setOnClickListener(v -> {

                Context wrapper = new ContextThemeWrapper(requireContext(), R.style.CustomPopupMenu);
                PopupMenu popupMenu = new PopupMenu(wrapper, filterDropDown);
                popupMenu.getMenuInflater().inflate(R.menu.menu_status, popupMenu.getMenu());

                // FORCE SHOW ICONS
                try {

                    Field field = popupMenu.getClass().getDeclaredField("mPopup");
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(Objects.requireNonNull(menuPopupHelper).getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);

                } catch (Exception e) {
                    AppLogger.e(getClass(), "bindFilterOptions", e);
                }

                popupMenu.setOnMenuItemClickListener(item -> {

                    String selected = Objects.requireNonNull(item.getTitle()).toString();
                    filterDropDown.setText(selected);

                    if (item.getItemId() == R.id.status_open) {
                        dispatchViewModel.setFilter(false);
                        return true;
                    } else if (item.getItemId() == R.id.status_close) {
                        dispatchViewModel.setFilter(true);
                        return true;
                    }

                    return false;
                });

                popupMenu.show();
            });
        }catch (Exception e) {
            AppLogger.e(getClass(), "bindFilterOptions", e);
        }
    }

    private void showDispatchDetails(DispatchView dispatchView) {
        try {
            Dialog dialog = new Dialog(requireContext(), R.style.DialogTheme);
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
            dialog.setContentView(R.layout.dialog_dispatch_details);

            AppCompatTextView dialogTitle = dialog.findViewById(R.id.tvDialogTitle);
            AppCompatImageView closeDialog = dialog.findViewById(R.id.imgClose);
            closeDialog.setOnClickListener(v -> dialog.dismiss());
            dialogTitle.setText(getString(R.string.dispatch_detail));

            AppCompatTextView tvContainer = dialog.findViewById(R.id.tvContainer);
            AppCompatTextView tvWood = dialog.findViewById(R.id.tvWood);
            AppCompatTextView tvWoodType = dialog.findViewById(R.id.tvWoodType);
            AppCompatTextView tvCategory = dialog.findViewById(R.id.tvCategory);
            AppCompatTextView tvPieces = dialog.findViewById(R.id.tvPieces);
            AppCompatTextView tvVolumePie = dialog.findViewById(R.id.tvVolumePie);
            AppCompatTextView tvGrossVolume = dialog.findViewById(R.id.tvGrossVolume);
            AppCompatTextView tvNetVolume = dialog.findViewById(R.id.tvNetVolume);
            AppCompatTextView tvClosedDate = dialog.findViewById(R.id.tvClosedDate);
            LinearLayout llClosedDate = dialog.findViewById(R.id.llClosedDate);
            LinearLayout llVolumePie = dialog.findViewById(R.id.llVolumePie);
            MaterialButton btnOpenDispatch = dialog.findViewById(R.id.btnOpenDispatch);

            tvContainer.setText(dispatchView.containerNumber);
            tvWood.setText(dispatchView.productName);
            tvWoodType.setText(dispatchView.productTypeName);
            tvCategory.setText(dispatchView.category);
            tvPieces.setText(String.valueOf(dispatchView.totalPieces));

            if (dispatchView.productTypeId == 1 || dispatchView.productTypeId == 3) {
                llVolumePie.setVisibility(View.VISIBLE);

                tvVolumePie.setText(CommonUtils.formatNumber2(dispatchView.totalVolumePie));
            } else {
                llVolumePie.setVisibility(View.GONE);
            }

            tvGrossVolume.setText(CommonUtils.formatNumber3(dispatchView.totalGrossVolume));
            tvNetVolume.setText(CommonUtils.formatNumber3(dispatchView.totalNetVolume));

            if (dispatchView.isClosed) {
                llClosedDate.setVisibility(View.VISIBLE);
                btnOpenDispatch.setVisibility(View.VISIBLE);
                tvClosedDate.setText(CommonUtils.convertTimeStampToDate(dispatchView.closedDate, "dd/MM/yyyy", requireContext()));

                btnOpenDispatch.setOnClickListener(view -> openConfirmation(dispatchView, dialog));
            } else {
                llClosedDate.setVisibility(View.GONE);
                btnOpenDispatch.setVisibility(View.GONE);
            }

            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showDispatchDetails", e);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void openConfirmation(DispatchView dispatchView, Dialog dispatchDialog) {
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

            dialogHeader.setText(getString(R.string.confirmation));

            dialogBody.setText(R.string.open_confirmation);

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnOk.setOnClickListener(v -> {
                boolean opened = dispatchViewModel.closeDispatchDetails(dispatchView.tempDispatchId, 0, 0, false);

                if (opened) {
                    dispatchViewRecyclerViewAdapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(), getString(R.string.dispatch_opened_successfully), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                dispatchDialog.dismiss();
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "openConfirmation", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}