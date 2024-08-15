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

import java.nio.charset.StandardCharsets;
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
                    jsonObject.put("delivery_address", deliveryAddress);
                    jsonObject.put("owner_id", ownerId);
                    jsonObject.put("taker_id", takerId);

                    JSONArray ownerProductsArray = new JSONArray(ownerProducts);
                    jsonObject.put("owner_products", ownerProductsArray);
                    JSONArray takerProductsArray = new JSONArray(takerProducts);
                    jsonObject.put("taker_products", takerProductsArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
            }
        };
        queue.add(stringRequest);
    }
}
