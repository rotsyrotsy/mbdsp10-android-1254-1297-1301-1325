package com.example.trocapp.ui.product;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.trocapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CategoryCheckboxAdapter extends ArrayAdapter<JSONObject> {
    private Context context;
    private List<JSONObject> categories;

    public CategoryCheckboxAdapter(@NonNull Context context, ArrayList<JSONObject> categories) {
        super(context, 0, categories);
        this.context = context;
        this.categories = categories;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_category_checkbox, parent, false);
        }

        JSONObject category = categories.get(position);

        CheckBox categoryCheckbox = convertView.findViewById(R.id.category);

        try {
            convertView.setId(category.getInt("id"));
            String text= category.getString("category_name");
            categoryCheckbox.setText(text);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return convertView;
    }
}