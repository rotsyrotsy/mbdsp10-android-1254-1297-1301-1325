package com.example.trocapp.ui.home;

import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trocapp.MyApplication;
import com.example.trocapp.R;
import com.example.trocapp.databinding.FragmentHomeBinding;
import com.example.trocapp.service.OnVolleyResponseListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    private JSONArray productList;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getProducts(new OnVolleyResponseListener() {
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
                HomeProductAdapter adapter = new HomeProductAdapter(root.getContext(), list);
                GridLayout layout = root.findViewById(R.id.listProducts);

                for(int i =0; i<adapter.getCount(); i++){
                    View item = adapter.getView(i,null,layout);
                    layout.addView(item);

                    item.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            TextView productName = view.findViewById(R.id.productName);
                            System.out.println(productName.getText()+" ====== "+view.getId());

                            Bundle bundle = new Bundle();
                            bundle.putString("idProduct", String.valueOf(view.getId()));
                            NavController navController = Navigation.findNavController(view);
                            navController.navigate(R.id.action_fragment_home_to_fragment_product_details,bundle);
                        }
                    });
                }


            }
            @Override
            public void onFailure(String message) {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
        /*RelativeLayout cardProduct = (RelativeLayout) root.findViewById(R.id.cardProduct);
        cardProduct.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_fragment_home_to_fragment_product_details);
            }
        });*/

        FloatingActionButton myFab = (FloatingActionButton) root.findViewById(R.id.buttonDetailsProduct);
        myFab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_fragment_home_to_fragment_create_product);
            }
        });



        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void getProducts(final OnVolleyResponseListener listener){

        String api = ((MyApplication) (getActivity()).getApplication()).getApiUrl();
        String url = api+"/products";
        RequestQueue queue= Volley.newRequestQueue(getActivity().getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject respObj = new JSONObject(response);
                    String message = respObj.getString("message");
                    productList = respObj.getJSONArray("data");
                    listener.onSuccess(message);
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
                params.put("x-auth-token", getContext().getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE).getString("token",null));
                return params;
            }
        };
        queue.add(stringRequest);
    }
}