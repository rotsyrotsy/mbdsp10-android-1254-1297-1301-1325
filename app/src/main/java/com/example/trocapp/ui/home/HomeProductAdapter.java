package com.example.trocapp.ui.home;

import android.content.Context;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeProductAdapter extends ArrayAdapter<JSONObject> {
    private Context context;
    private List<JSONObject> products;

    public HomeProductAdapter(@NonNull Context context, ArrayList<JSONObject> products) {
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

        // Set the image and text here (Assuming Product has getImage() and getName() methods)
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
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return convertView;
    }
}
