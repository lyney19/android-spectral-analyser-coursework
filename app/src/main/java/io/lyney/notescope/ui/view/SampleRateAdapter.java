package io.lyney.notescope.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.lyney.notescope.R;

import java.util.List;

public class SampleRateAdapter extends ArrayAdapter<Integer> {

    public SampleRateAdapter(
            @NonNull Context context,
            @NonNull List<Integer> items
    ) {
        super(context, R.layout.dropdown_item, items);
    }

    @NonNull
    @Override
    public View getView(
            int position,
            @Nullable View convertView,
            @NonNull ViewGroup parent
    ) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(
            int position, @Nullable
            View convertView, @NonNull
            ViewGroup parent
    ) {
        return createView(position, convertView, parent);
    }

    @SuppressLint("SetTextI18n")
    private View createView(int position, View convertView, ViewGroup parent) {
        TextView textView;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                            R.layout.dropdown_item,
                            parent,
                            false
                    );
        }

        textView = (TextView) convertView;
        Integer value = getItem(position);
        textView.setText(value + " Hz");

        return textView;
    }
}
