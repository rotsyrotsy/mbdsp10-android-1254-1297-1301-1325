package com.example.trocapp.ui.product;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.trocapp.R;
import com.example.trocapp.service.ImageLoader;
import com.google.android.material.chip.Chip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProductListAdapter extends ArrayAdapter<JSONObject> {
    private Context context;
    private List<JSONObject> products;

    public ProductListAdapter(@NonNull Context context, ArrayList<JSONObject> products) {
        super(context, 0, products);
        this.context = context;
        this.products = products;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        }

        JSONObject product = products.get(position);

        ImageView productImage = convertView.findViewById(R.id.productImage);
        TextView productName = convertView.findViewById(R.id.productName);
        TextView productCategories = convertView.findViewById(R.id.productCategories);
        TextView productCreationDate = convertView.findViewById(R.id.productCreationDate);
        Chip isExchangeable = convertView.findViewById(R.id.isExchangeable);

        try {
            convertView.setId(product.getInt("id"));

            String imageURL = product.getString("product_image");
            if (imageURL != null && !imageURL.isEmpty()) {
                ImageLoader.setImageFromUrl(productImage, imageURL);
            } else {
                productImage.setImageResource(R.drawable.placeholder_image);
            }
            productName.setText(product.getString("product_name"));
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
            productCategories.setText(categoriesStr);
            boolean isExchangeableBool = product.getBoolean("is_exchangeable");
            if(isExchangeableBool){
                isExchangeable.setText("IS EXCHANGEABLE");
                isExchangeable.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            }else{
                isExchangeable.setText("NOT EXCHANGEABLE");
                isExchangeable.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFE61111")));
            }
            productCreationDate.setText(product.getString("createdAt"));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return convertView;
    }
}
