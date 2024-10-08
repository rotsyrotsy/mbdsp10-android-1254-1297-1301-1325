package com.example.trocapp.ui.exchange;

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
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.trocapp.R;
import com.example.trocapp.service.ExchangeService;
import com.example.trocapp.service.OnVolleyResponseListener;
import com.example.trocapp.service.ProductService;
import com.example.trocapp.ui.product.ProductCheckboxAdapter;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CreateExchangeFragment extends Fragment {
    private JSONArray productList;
    private ArrayList<Integer> takerProducts;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_exchange, container, false);
        ProgressBar loading = root.findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        List<Integer> ownerProducts = getArguments().getIntegerArrayList("ownerProducts");
        Integer ownerId = getArguments().getInt("ownerId");
        takerProducts = new ArrayList<Integer>();
        EditText deliveryAddress = root.findViewById(R.id.exchangeDeliveryAddress);
        ProductService productService = new ProductService();
        ExchangeService exchangeService = new ExchangeService();
        Integer userId = getContext().getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE).getInt("userId",-1);
        Button buttonSaveExchange = (Button) root.findViewById(R.id.buttonSaveExchange);

        productService.getProducts(root.getContext(), userId, new OnVolleyResponseListener() {
            @Override
            public void onSuccess(Object data) {
                loading.setVisibility(View.GONE);
                productList = (JSONArray)data;
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
                                takerProducts.add(item.getId());
                            }
                        }
                    });
                }

            }
            @Override
            public void onFailure(String message) {
                loading.setVisibility(View.GONE);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
        buttonSaveExchange.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                loading.setVisibility(View.VISIBLE);

                exchangeService.createExchange(root.getContext(), deliveryAddress.getText().toString(), ownerProducts, takerProducts, ownerId, userId, new OnVolleyResponseListener() {
                    @Override
                    public void onSuccess(Object message) {
                        loading.setVisibility(View.GONE);
                        Snackbar.make(v, String.valueOf(message), Snackbar.LENGTH_LONG).show();
                        NavController navController = Navigation.findNavController(v);
                        navController.navigate(R.id.action_fragment_create_exchange_to_fragment_exchanges);
                    }
                    @Override
                    public void onFailure(String message) {
                        loading.setVisibility(View.GONE);
                        Toast.makeText(v.getContext(), message, Toast.LENGTH_LONG).show();
                    }
                });

            }
        });

        return root;
    }

}