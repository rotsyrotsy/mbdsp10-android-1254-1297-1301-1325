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
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trocapp.MyApplication;
import com.example.trocapp.R;
import com.example.trocapp.service.ImageLoader;
import com.example.trocapp.service.OnVolleyResponseListener;
import com.example.trocapp.ui.home.HomeProductAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailsFragment extends Fragment {
    private JSONObject product;
    private JSONArray productList;
    private ArrayList<Integer> ownerProducts;
    private Integer ownerId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_product_details, container, false);
        String idProduct = getArguments().getString("idProduct");

        getProduct(idProduct, new OnVolleyResponseListener() {
            @Override
            public void onSuccess(String message) {

                ImageView productImage = root.findViewById(R.id.productImage);
                TextView productName = root.findViewById(R.id.productName);
                TextView productCategories = root.findViewById(R.id.productCategories);
                TextView productOwner = root.findViewById(R.id.productOwner);
                TextView productDescription = root.findViewById(R.id.productDescription);
                TextView productFirstOwner = root.findViewById(R.id.productFirstOwner);
                TextView productCreationDate = root.findViewById(R.id.productCreationDate);
                TextView textProductList = root.findViewById(R.id.textProductList);

                try {
                    root.setId(product.getInt("id"));

                    String imageURL = product.getString("product_image");
                    if (imageURL != null && !imageURL.isEmpty()) {
                        ImageLoader.setImageFromUrl(productImage, imageURL);
                    } else {
                        productImage.setImageResource(R.drawable.placeholder_image);
                    }
                    /*JSONArray categories = product.getJSONArray("Categories");
                    String categoriesStr = "";
                    for (int i = 0; i < categories.length(); i++) {
                        JSONObject category = categories.getJSONObject(i);
                        String categoryName = category.getString("category_name");
                        categoriesStr = categoriesStr.concat(categoryName);
                        if (i < categories.length() - 1) {
                            categoriesStr = categoriesStr.concat(", ");
                        }
                    }
                    productCategories.setText(categoriesStr);*/
                    JSONObject owner = product.getJSONObject("actual_owner");
                    ownerId = owner.getInt("id");
                    productName.setText(product.getString("product_name"));
                    productOwner.setText(owner.getString("username"));
                    productDescription.setText(product.getString("description"));
                    productFirstOwner.setText(product.getJSONObject("first_owner").getString("username"));
                    productCreationDate.setText(product.getString("createdAt"));
                    textProductList.setText(owner.getString("username")+"'s exchangeable products");

                    getUserProducts(ownerId, new OnVolleyResponseListener() {
                        @Override
                        public void onSuccess(String message) {
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
                                            ownerProducts.add(compoundButton.getId());
                                        }
                                    }
                                });
                            }
                        }
                        @Override
                        public void onFailure(String message) {

                        }
                    });

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                Button buttonUpdate = (Button) root.findViewById(R.id.buttonUpdate);
                buttonUpdate.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        NavController navController = Navigation.findNavController(v);
                        navController.navigate(R.id.action_fragment_product_details_to_fragment_update_product);
                    }
                });
                Button buttonPropose = (Button) root.findViewById(R.id.buttonPropose);
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
            }

            @Override
            public void onFailure(String message) {

            }
        });



        return root;
    }

    private void getProduct(String id, final OnVolleyResponseListener listener){

        String api = ((MyApplication) (getActivity()).getApplication()).getApiUrl();
        String url = api+"/products/"+id;
        RequestQueue queue= Volley.newRequestQueue(getActivity().getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject respObj = new JSONObject(response);
                    String message = respObj.getString("message");
                    product = respObj.getJSONObject("data");
                    listener.onSuccess(message);
                }catch (JSONException e){
                    e.printStackTrace();
                    listener.onFailure(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = "An error occurred";
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    try {
                        String jsonString = new String(error.networkResponse.data);
                        JSONObject errorObj = new JSONObject(jsonString);
                        message = errorObj.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        message = new String(error.networkResponse.data);
                    }
                }
                listener.onFailure(message);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("x-auth-token", getContext().getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE).getString("token",null));
                return params;
            }
        };
        queue.add(stringRequest);
    }
    private void getUserProducts(Integer userId, final OnVolleyResponseListener listener){
        String url = ((MyApplication) getActivity().getApplication()).getApiUrl() + "/products";
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject respObj = new JSONObject(response);
                        productList = respObj.getJSONArray("data");
                        listener.onSuccess(respObj.getString("message"));
                    } catch (JSONException e) {
                        listener.onFailure(e.getMessage());
                    }
                },
                error -> {
                    String message = "An error occurred";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            JSONObject errorObj = new JSONObject(new String(error.networkResponse.data));
                            message = errorObj.getString("message");
                        } catch (JSONException e) {
                            message = new String(error.networkResponse.data);
                        }
                    }
                    listener.onFailure(message);
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("x-auth-token", getContext()
                                .getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE)
                                .getString("token", null));
                        return params;
                    }
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("userId", String.valueOf(userId));
                        return params;
                    }
                };
        queue.add(stringRequest);
    }

}