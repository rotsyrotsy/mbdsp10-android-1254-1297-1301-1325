package com.example.trocapp.service;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trocapp.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
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
}
