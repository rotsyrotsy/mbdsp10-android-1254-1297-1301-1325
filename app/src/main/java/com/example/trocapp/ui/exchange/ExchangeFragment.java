package com.example.trocapp.ui.exchange;

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
import android.widget.Toast;

import com.example.trocapp.R;
import com.example.trocapp.service.ExchangeService;
import com.example.trocapp.service.OnVolleyResponseListener;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ExchangeFragment extends Fragment {
    private JSONArray exchangeList;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_exchanges, container, false);
        ExchangeService exchangeService = new ExchangeService();

        exchangeService.getExchanges(root.getContext(), new OnVolleyResponseListener() {
            @Override
            public void onSuccess(Object data) {
                exchangeList = (JSONArray) data;
                ArrayList<JSONObject> list = new ArrayList<>();
                for (int i = 0; i < exchangeList.length(); i++) {
                    try {
                        list.add(exchangeList.getJSONObject(i));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                ExchangeAdapter adapter = new ExchangeAdapter(root.getContext(), list);
                GridLayout layout = root.findViewById(R.id.listExchanges);

                for(int i =0; i<adapter.getCount(); i++){
                    View item = adapter.getView(i,null,layout);
                    layout.addView(item);

                    ImageButton buttonAcceptExchange = item.findViewById(R.id.buttonAcceptExchange);
                    ImageButton buttonRejectExchange = item.findViewById(R.id.buttonRejectExchange);
                    ImageButton buttonScanQRCode = item.findViewById(R.id.buttonScanQRCode);

                    buttonAcceptExchange.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            exchangeService.updateStatusExchange(root.getContext(), item.getId(), true, new OnVolleyResponseListener() {
                                @Override
                                public void onSuccess(Object message) {
                                    Toast.makeText(view.getContext(), String.valueOf(message), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(String message) {
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    buttonRejectExchange.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            exchangeService.updateStatusExchange(root.getContext(), item.getId(), false, new OnVolleyResponseListener() {
                                @Override
                                public void onSuccess(Object message) {
                                    Toast.makeText(view.getContext(), String.valueOf(message), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(String message) {
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    buttonScanQRCode.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            NavController navController = Navigation.findNavController(v);
                            navController.navigate(R.id.action_nav_exchanges_to_nav_qr_code_exchange);
                        }
                    });
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
        return root;
    }

}