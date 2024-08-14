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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProductCheckboxAdapter extends ArrayAdapter<JSONObject> {
    private Context context;
    private List<JSONObject> products;

    public ProductCheckboxAdapter(@NonNull Context context, ArrayList<JSONObject> products) {
        super(context, 0, products);
        this.context = context;
        this.products = products;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_product_checkbox, parent, false);
        }

        JSONObject product = products.get(position);

        CheckBox productCheckBox = convertView.findViewById(R.id.nameAndCategories);

        try {
            convertView.setId(product.getInt("id"));
            String text= product.getString("product_name");

            JSONArray categories = product.getJSONArray("Categories");
            String categoriesStr = "";
            for (int i = 0; i < categories.length(); i++) {
                JSONObject category = categories.getJSONObject(i);
                String categoryName = category.getString("category_name");
                categoriesStr = categoriesStr.concat(categoryName);
                if (i < categories.length() - 1) {
                    categoriesStr = categoriesStr.concat(", ");
                }
            }
            text = text.concat(" (").concat(categoriesStr).concat(")");

            productCheckBox.setText(text);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return convertView;
    }
}