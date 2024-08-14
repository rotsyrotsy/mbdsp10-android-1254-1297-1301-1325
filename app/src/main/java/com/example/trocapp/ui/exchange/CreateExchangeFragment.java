package com.example.trocapp.ui.exchange;

import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trocapp.MyApplication;
import com.example.trocapp.R;
import com.example.trocapp.auth.ui.login.LoginActivity;
import com.example.trocapp.auth.ui.register.RegisterActivity;
import com.example.trocapp.service.OnVolleyResponseListener;
import com.example.trocapp.ui.product.ProductCheckboxAdapter;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateExchangeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateExchangeFragment extends Fragment {
    private JSONArray productList;
    private ArrayList<Integer> takerProducts;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_exchange, container, false);
        List<Integer> ownerProducts = getArguments().getIntegerArrayList("ownerProducts");
        Integer ownerId = getArguments().getInt("ownerId");

        Integer userId = 1;
        getUserProducts(userId, new OnVolleyResponseListener() {
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
                                takerProducts.add(compoundButton.getId());
                            }
                        }
                    });
                }

                Button buttonSaveExchange = (Button) root.findViewById(R.id.buttonSaveExchange);
                buttonSaveExchange.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        EditText deliveryAddress = v.findViewById(R.id.deliveryAddress);
                        createExchange(deliveryAddress.getText().toString(), ownerProducts, takerProducts, ownerId, userId, new OnVolleyResponseListener() {
                            @Override
                            public void onSuccess(String message) {
                                Snackbar.make(v, message, Snackbar.LENGTH_LONG).show();
                                NavController navController = Navigation.findNavController(v);
                                navController.navigate(R.id.action_fragment_create_exchange_to_fragment_exchanges);
                            }
                            @Override
                            public void onFailure(String message) {
                                Toast.makeText(v.getContext(), message, Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                });

            }
            @Override
            public void onFailure(String message) {

            }
        });
        return root;
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
    private void createExchange(String deliveryAddress, List<Integer> ownerProducts, List<Integer> takerProducts, Integer ownerId, Integer takerId, final OnVolleyResponseListener listener){
        String url = ((MyApplication) getActivity().getApplication()).getApiUrl() + "/exchanges";
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
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
                params.put("owner_products", ownerProducts.toString());
                params.put("taker_products", takerProducts.toString());
                params.put("owner_id", String.valueOf(ownerId));
                params.put("taker_id", String.valueOf(takerId));
                return params;
            }
        };
        queue.add(stringRequest);
    }

}