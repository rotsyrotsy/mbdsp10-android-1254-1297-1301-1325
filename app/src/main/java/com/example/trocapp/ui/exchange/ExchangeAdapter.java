package com.example.trocapp.ui.exchange;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.trocapp.R;
import com.example.trocapp.service.ImageLoader;
import com.google.android.material.chip.Chip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExchangeAdapter extends ArrayAdapter<JSONObject> {
    private Context context;
    private List<JSONObject> exchanges;

    public ExchangeAdapter(@NonNull Context context, ArrayList<JSONObject> exchanges) {
        super(context, 0, exchanges);
        this.context = context;
        this.exchanges = exchanges;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_exchange, parent, false);
        }
        Integer currentUserId = getContext().getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE).getInt("userId",-1);

        JSONObject exchange = exchanges.get(position);

        TextView createdAt = convertView.findViewById(R.id.createdAt);
        TextView owner = convertView.findViewById(R.id.owner);
        TextView taker = convertView.findViewById(R.id.taker);
        TextView ownerProducts = convertView.findViewById(R.id.ownerProducts);
        TextView takerProducts = convertView.findViewById(R.id.takerProducts);
        TextView deliveryAddress = convertView.findViewById(R.id.deliveryAddress);
        Chip status = convertView.findViewById(R.id.status);
        ImageButton buttonAcceptExchange = convertView.findViewById(R.id.buttonAcceptExchange);
        ImageButton buttonRejectExchange = convertView.findViewById(R.id.buttonRejectExchange);
        ImageButton buttonScanQRCode = convertView.findViewById(R.id.buttonScanQRCode);

        // Set the image and text here (Assuming Product has getImage() and getName() methods)
        try {
            JSONObject takerProposition = exchange.getJSONObject("taker_proposition");
            JSONObject ownerProposition = exchange.getJSONObject("owner_proposition");

            convertView.setId(exchange.getInt("id"));

            createdAt.setText(exchange.getString("createdAt"));

            JSONObject ownerUser = ownerProposition.getJSONObject("user");
            owner.setText(ownerUser.getString("username"));
            JSONObject takerUser = takerProposition.getJSONObject("user");
            taker.setText(takerUser.getString("username"));

            deliveryAddress.setText(exchange.getString("delivery_address"));
            String statusStr = exchange.getString("status");
            status.setText(statusStr);
            String bgColor="#F2663C";
            if(statusStr.compareTo("ACCEPTED")==0 ){
                bgColor = "#4CAF50";
                buttonScanQRCode.setVisibility(View.VISIBLE);
                buttonAcceptExchange.setVisibility(View.GONE);
                buttonRejectExchange.setVisibility(View.GONE);
            } else if (statusStr.compareTo("CANCELLED")==0 || statusStr.compareTo("RECEIVED")==0) {
                bgColor="#FFE61111";
                if(statusStr.compareTo("RECEIVED")==0){
                    bgColor="#3C3C3C";
                }
                buttonScanQRCode.setVisibility(View.GONE);
                buttonAcceptExchange.setVisibility(View.GONE);
                buttonRejectExchange.setVisibility(View.GONE);
            }else{
                bgColor ="#F2663C";
                if(statusStr.compareTo("BLOCKED")!=0){
                    buttonAcceptExchange.setVisibility(View.VISIBLE);
                    buttonRejectExchange.setVisibility(View.VISIBLE);
                }
                buttonScanQRCode.setVisibility(View.GONE);
            }
            status.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(bgColor)));

            if(!currentUserId.equals(ownerProposition.getInt("user_id"))){
                buttonAcceptExchange.setVisibility(View.GONE);
                buttonRejectExchange.setVisibility(View.GONE);
            }

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
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return convertView;
    }
}