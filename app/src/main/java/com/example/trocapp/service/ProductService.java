package com.example.trocapp.service;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trocapp.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductService {

    public void getProducts(Context context,Integer userId, final OnVolleyResponseListener listener) {
        String url = GlobalVariables.apiUrl() + "/products";
        if(userId!=null){
            url = url.concat("?userId=").concat(String.valueOf(userId));
        }
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject respObj = new JSONObject(response);
                        JSONArray productList = respObj.getJSONArray("data");
                        listener.onSuccess(productList);
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
                params.put("x-auth-token", context
                        .getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE)
                        .getString("token", null));
                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void getProduct(Context context, String id, final OnVolleyResponseListener listener){
        String url = GlobalVariables.apiUrl() + "/products/"+id;
        RequestQueue queue= Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject respObj = new JSONObject(response);
                    JSONObject product = respObj.getJSONObject("data");
                    listener.onSuccess(product);
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
                params.put("x-auth-token", context.getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE).getString("token",null));
                return params;
            }
        };
        queue.add(stringRequest);
    }
    public void createProduct(Context context, String name, String description, List<Integer> categories, final OnVolleyResponseListener listener){
        String url = GlobalVariables.apiUrl() + "/products";
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject respObj = new JSONObject(response);
                    JSONObject product = respObj.getJSONObject("data");
                    listener.onSuccess(product);
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
                Map<String, String> params = new HashMap<>();
                params.put("x-auth-token", context
                        .getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE)
                        .getString("token", null));
                params.put("Content-Type", "application/json");
                return params;
            }
            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("product_name", name);
                    jsonObject.put("description", description);

                    // Assuming 'categories' is a List<Integer>
                    JSONArray categoriesArray = new JSONArray(categories);
                    jsonObject.put("categories", categoriesArray);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
            }
        };
        queue.add(stringRequest);
    }
/*
    public void uploadProductImage(Context context, Integer productId, File file, final OnVolleyResponseListener listener){
        String url = GlobalVariables.apiUrl() + "/products/uploadImage/"+productId;
        RequestQueue queue = Volley.newRequestQueue(context);
        Map<String, String> params = new HashMap<>();

        Map<String, String> headers = new HashMap<>();
        headers.put("x-auth-token", context
                .getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE)
                .getString("token", null));

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                url,
                headers,
                params,
                file,
                response -> {
                    // Handle the response
                    try {
                        JSONObject respObj = new JSONObject(String.valueOf(response));
                        listener.onSuccess(respObj.getString("message"));
                    } catch (JSONException e) {
                        listener.onFailure(e.getMessage());
                    }
                },
                error -> {
                    // Handle the error
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
                });

        queue.add(multipartRequest);
    }*/
}
