package com.example.trocapp.ui.exchange;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.trocapp.MainActivity;
import com.example.trocapp.R;
import com.example.trocapp.service.ExchangeService;
import com.example.trocapp.service.OnVolleyResponseListener;
import com.example.trocapp.ui.transaction.CreateTransactionFragment;
import com.google.android.material.chip.Chip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExchangeDetailsFragment extends Fragment {
    JSONObject exchange;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ExchangeService exchangeService = new ExchangeService();

        String idExchange = getArguments().getString("idExchange");

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_exchange_details, container, false);
        TextView createdAt = root.findViewById(R.id.createdAt);
        TextView owner = root.findViewById(R.id.owner);
        TextView taker = root.findViewById(R.id.taker);
        TextView ownerProducts = root.findViewById(R.id.ownerProducts);
        TextView takerProducts = root.findViewById(R.id.takerProducts);
        TextView deliveryAddress = root.findViewById(R.id.deliveryAddress);
        Chip status = root.findViewById(R.id.status);
        Button buttonReceive = root.findViewById(R.id.buttonReceiveExchange);
        ProgressBar loading = root.findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        exchangeService.getExchange(root.getContext(), idExchange, new OnVolleyResponseListener() {
            @Override
            public void onSuccess(Object data) {
                loading.setVisibility(View.GONE);
                exchange = (JSONObject) data;
                try {
                    JSONObject takerProposition = exchange.getJSONObject("taker_proposition");
                    JSONObject ownerProposition = exchange.getJSONObject("owner_proposition");

                    createdAt.setText(exchange.getString("createdAt"));

                    JSONObject ownerUser = ownerProposition.getJSONObject("user");
                    owner.setText(ownerUser.getString("username"));
                    JSONObject takerUser = takerProposition.getJSONObject("user");
                    taker.setText(takerUser.getString("username"));

                    deliveryAddress.setText(exchange.getString("delivery_address"));
                    String statusStr = exchange.getString("status");
                    status.setText(statusStr);

                    JSONArray ownerProductsJson = ownerProposition.getJSONArray("Products");
                    String ownerProductsStr = "";
                    for (int i = 0; i < ownerProductsJson.length(); i++) {
                        JSONObject product = ownerProductsJson.getJSONObject(i);
                        String productName = product.getString("product_name");
                        ownerProductsStr = ownerProductsStr.concat(productName);
                        if (i < ownerProductsJson.length() - 1) {
                            ownerProductsStr = ownerProductsStr.concat(", ");
                        }
                    }
                    ownerProducts.setText(ownerProductsStr);

                    JSONArray takerProductsJson = takerProposition.getJSONArray("Products");
                    String takerProductsStr = "";
                    for (int i = 0; i < takerProductsJson.length(); i++) {
                        JSONObject product = takerProductsJson.getJSONObject(i);
                        String productName = product.getString("product_name");
                        takerProductsStr = takerProductsStr.concat(productName);
                        if (i < takerProductsJson.length() - 1) {
                            takerProductsStr = takerProductsStr.concat(", ");
                        }
                    }
                    takerProducts.setText(takerProductsStr);

                    if(statusStr.compareTo("ACCEPTED")==0){
                        buttonReceive.setVisibility(View.VISIBLE);
                        buttonReceive.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                Bundle bundle = new Bundle();
                                bundle.putString("idExchange", idExchange);
                                NavController navController = Navigation.findNavController(v);
                                navController.navigate(R.id.action_nav_exchange_details_to_nav_add_transaction, bundle);
                            }
                        });
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(String message) {
                loading.setVisibility(View.GONE);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
        return root;
    }
}