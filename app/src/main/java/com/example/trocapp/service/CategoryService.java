package com.example.trocapp.service;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CategoryService {
    public void getCategories(Context context, final OnVolleyResponseListener listener) {
        String url = AppHelper.apiUrl() + "/categories";
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject respObj = new JSONObject(response);
                        JSONArray categories = respObj.getJSONArray("data");
                        listener.onSuccess(categories);
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

}

