package com.cgr.codrinterraerp.ui.adapters;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.caverock.androidsvg.SVG;
import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.AccountHeads;
import com.cgr.codrinterraerp.utils.AppLogger;

import java.util.List;

public class AccountHeadsAdapter extends ArrayAdapter<AccountHeads> {

    private final LayoutInflater inflater;

    public AccountHeadsAdapter(@NonNull Context context, @NonNull List<AccountHeads> categories) {
        super(context, 0, categories);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.row_item_category_dropdown, parent, false);
        }

        AppCompatImageView imgIcon = view.findViewById(R.id.imgIcon);
        AppCompatTextView tvName = view.findViewById(R.id.tvName);
        tvName.setSelected(true);

        AccountHeads accountHead = getItem(position);
        if (accountHead != null) {

            if (accountHead.getIcon() != null) {
                try {
                    SVG svg = SVG.getFromString(accountHead.getIcon());
                    PictureDrawable drawable = new PictureDrawable(svg.renderToPicture());
                    imgIcon.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    imgIcon.setImageDrawable(drawable);
                } catch (Exception e) {
                    AppLogger.e(getClass(), "createView", e);
                    // fallback image if decoding fails
                    imgIcon.setImageResource(R.drawable.ic_default_accounthead);
                }
            } else {
                imgIcon.setImageResource(R.drawable.ic_default_accounthead);
            }

            tvName.setText(accountHead.getAccountHeadName());
        }

        return view;
    }
}