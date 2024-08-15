package com.example.trocapp.ui.product;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trocapp.MainActivity;
import com.example.trocapp.MyApplication;
import com.example.trocapp.R;
import com.example.trocapp.auth.ui.login.LoginActivity;
import com.example.trocapp.auth.ui.register.RegisterActivity;
import com.example.trocapp.service.OnVolleyResponseListener;
import com.example.trocapp.service.ProductService;
import com.example.trocapp.ui.home.HomeProductAdapter;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProductFragment extends Fragment {
    private JSONArray productList;
    public static ProductFragment newInstance() {
        return new ProductFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ProductService productService = new ProductService();
        View view = inflater.inflate(R.layout.fragment_products, container, false);
        Integer userId = getContext().getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE).getInt("userId",-1);
        productService.getProducts(view.getContext(), userId, new OnVolleyResponseListener() {
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
                ProductListAdapter adapter = new ProductListAdapter(view.getContext(), list);
                GridLayout layout = view.findViewById(R.id.listProducts);

                for(int i =0; i<adapter.getCount(); i++){
                    View item = adapter.getView(i,null,layout);
                    layout.addView(item);

                    ImageButton showBtn = item.findViewById(R.id.buttonShowProduct);
                    showBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putString("idProduct", String.valueOf(item.getId()));
                            NavController navController = Navigation.findNavController(v);
                            navController.navigate(R.id.action_nav_products_to_nav_product_details,bundle);
                        }
                    });
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }


}