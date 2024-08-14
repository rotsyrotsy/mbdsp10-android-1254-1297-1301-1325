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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProductDetailsFragment extends Fragment {
    private JSONObject product;
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
                    String ownerName = product.getJSONObject("actual_owner").getString("username");
                    productName.setText(product.getString("product_name"));
                    productOwner.setText(ownerName);
                    productDescription.setText(product.getString("description"));
                    productFirstOwner.setText(product.getJSONObject("first_owner").getString("username"));
                    productCreationDate.setText(product.getString("createdAt"));
                    textProductList.setText(ownerName+"'s exchangeable products");

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
                        NavController navController = Navigation.findNavController(v);
                        navController.navigate(R.id.action_fragment_product_details_to_fragment_create_exchange);
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
}