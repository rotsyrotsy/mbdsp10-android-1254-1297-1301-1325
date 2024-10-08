package com.example.trocapp.ui.product;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trocapp.R;
import com.example.trocapp.service.ImageLoader;
import com.example.trocapp.service.OnVolleyResponseListener;
import com.example.trocapp.service.ProductService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProductDetailsFragment extends Fragment {
    private JSONObject product;
    private JSONArray productList;
    private ArrayList<Integer> ownerProducts;
    private Integer ownerId;
    private Integer productId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_product_details, container, false);
        String idProduct = getArguments().getString("idProduct");
        ownerProducts = new ArrayList<Integer>();
        ProductService productService = new ProductService();
        ProgressBar loading = root.findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        productService.getProduct(root.getContext(), idProduct, new OnVolleyResponseListener() {
            @Override
            public void onSuccess(Object data) {
                loading.setVisibility(View.GONE);
                product = (JSONObject) data;
                Integer currentUserId = getContext().getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE).getInt("userId",-1);

                Button buttonPropose = (Button) root.findViewById(R.id.buttonPropose);
                ImageView productImage = root.findViewById(R.id.productImage);
                TextView productName = root.findViewById(R.id.productName);
                TextView productCategories = root.findViewById(R.id.productCategories);
                TextView productOwner = root.findViewById(R.id.productOwner);
                TextView productDescription = root.findViewById(R.id.productDescription);
                TextView productFirstOwner = root.findViewById(R.id.productFirstOwner);
                TextView productCreationDate = root.findViewById(R.id.productCreationDate);
                TextView textProductList = root.findViewById(R.id.textProductList);
                Button buttonUpdate = (Button) root.findViewById(R.id.buttonUpdate);
                Button buttonDelete = root.findViewById(R.id.buttonDelete);

                try {
                    productId = product.getInt("id");
                    root.setId(productId);

                    String imageURL = product.getString("product_image");
                    if (imageURL != null && !imageURL.isEmpty()) {
                        ImageLoader.setImageFromUrl(productImage, imageURL);
                    } else {
                        productImage.setImageResource(R.drawable.placeholder_image);
                    }
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
                    JSONObject owner = product.getJSONObject("actual_owner");
                    ownerId = owner.getInt("id");
                    productName.setText(product.getString("product_name"));
                    productOwner.setText(owner.getString("username"));
                    productDescription.setText(product.getString("description"));
                    productFirstOwner.setText(product.getJSONObject("first_owner").getString("username"));
                    productCreationDate.setText(product.getString("createdAt"));

                    if(!currentUserId.equals(ownerId)){
                        buttonPropose.setVisibility(View.VISIBLE);
                        buttonDelete.setVisibility(View.GONE);
                        buttonUpdate.setVisibility(View.GONE);

                        textProductList.setText(owner.getString("username")+"'s exchangeable products");
                        productService.getProducts(root.getContext(), ownerId, new OnVolleyResponseListener() {
                            @Override
                            public void onSuccess(Object data) {
                                productList = (JSONArray) data;
                                ArrayList<JSONObject> list = new ArrayList<>();
                                for (int i = 0; i < productList.length(); i++) {
                                    try {
                                        list.add(productList.getJSONObject(i));
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                ProductCheckboxAdapter adapter = new ProductCheckboxAdapter(root.getContext(), list);
                                GridLayout layout = root.findViewById(R.id.userProductList);

                                for(int i =0; i<adapter.getCount(); i++){
                                    View item = adapter.getView(i,null,layout);
                                    layout.addView(item);
                                    CheckBox checkBox = item.findViewById(R.id.nameAndCategories);
                                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                                            if(isChecked){
                                                ownerProducts.add(item.getId());
                                            }
                                        }
                                    });
                                }
                            }
                            @Override
                            public void onFailure(String message) {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                            }
                        });

                        buttonPropose.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                Bundle bundle = new Bundle();
                                bundle.putIntegerArrayList("ownerProducts", ownerProducts);
                                bundle.putInt("ownerId", ownerId);
                                NavController navController = Navigation.findNavController(v);
                                navController.navigate(R.id.action_fragment_product_details_to_fragment_create_exchange, bundle);
                            }
                        });

                    }else{
                        buttonDelete.setVisibility(View.VISIBLE);
                        buttonUpdate.setVisibility(View.VISIBLE);
                        buttonPropose.setVisibility(View.GONE);

                        buttonUpdate.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                Bundle bundle = new Bundle();
                                bundle.putString("idProduct", String.valueOf(productId));
                                NavController navController = Navigation.findNavController(v);
                                navController.navigate(R.id.action_fragment_product_details_to_fragment_update_product,bundle);
                            }
                        });

                        buttonDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                productService.deleteProduct(view.getContext(),productId, new OnVolleyResponseListener() {
                                    @Override
                                    public void onSuccess(Object data) {
                                        Toast.makeText(getActivity(),(String)data, Toast.LENGTH_SHORT).show();
                                        NavController navController = Navigation.findNavController(view);
                                        navController.popBackStack();
                                    }
                                    @Override
                                    public void onFailure(String message) {
                                        Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });

                    }


                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onFailure(String message) {
                loading.setVisibility(View.GONE);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });



        return root;
    }

}