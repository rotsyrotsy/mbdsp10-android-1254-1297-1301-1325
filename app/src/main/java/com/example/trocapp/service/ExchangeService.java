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
import java.util.List;
import java.util.Map;

public class ExchangeService {

    public void createExchange(Context context, String deliveryAddress, List<Integer> ownerProducts, List<Integer> takerProducts, Integer ownerId, Integer takerId, final OnVolleyResponseListener listener){
        String url = GlobalVariables.apiUrl() + "/exchanges";
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject respObj = new JSONObject(response);
                    listener.onSuccess(respObj.getString("message"));
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
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("delivery_address", deliveryAddress);
                params.put("owner_products", new JSONArray(ownerProducts).toString()); // error must be an array
                params.put("taker_products", new JSONArray(takerProducts).toString()); // error must be an array
                params.put("owner_id", String.valueOf(ownerId));
                params.put("taker_id", String.valueOf(takerId));
                return params;
            }
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
}
