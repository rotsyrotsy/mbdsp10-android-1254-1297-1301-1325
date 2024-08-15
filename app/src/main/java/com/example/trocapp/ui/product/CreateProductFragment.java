package com.example.trocapp.ui.product;

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

import com.example.trocapp.R;
import com.example.trocapp.service.CategoryService;
import com.example.trocapp.service.OnVolleyResponseListener;
import com.example.trocapp.service.ProductService;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CreateProductFragment extends Fragment {

    private JSONArray categoryList;
    private ArrayList<Integer> categories;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_product, container, false);
        CategoryService categoryservice = new CategoryService();
        ProductService productservice = new ProductService();

        categories = new ArrayList<Integer>();
        EditText name = root.findViewById(R.id.name);
        EditText description = root.findViewById(R.id.description);


        categoryservice.getCategories(root.getContext(), new OnVolleyResponseListener() {
            @Override
            public void onSuccess(Object data) {
                categoryList = (JSONArray) data;
                ArrayList<JSONObject> list = new ArrayList<>();
                for (int i = 0; i < categoryList.length(); i++) {
                    try {
                        list.add(categoryList.getJSONObject(i));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                CategoryCheckboxAdapter adapter = new CategoryCheckboxAdapter(root.getContext(), list);
                GridLayout layout = root.findViewById(R.id.categoryList);

                for(int i =0; i<adapter.getCount(); i++){
                    View item = adapter.getView(i,null,layout);
                    layout.addView(item);
                    CheckBox checkBox = item.findViewById(R.id.category);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                            if(isChecked){
                                categories.add(item.getId());
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(String message) {

            }
        });

        Button buttonSaveProduct = (Button) root.findViewById(R.id.buttonSaveProduct);
        buttonSaveProduct.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                productservice.createProduct(root.getContext(), name.getText().toString(),description.getText().toString(), categories, new OnVolleyResponseListener() {
                    @Override
                    public void onSuccess(Object data) {
                        JSONObject newProduct = (JSONObject) data;
                        try {
                            Bundle bundle = new Bundle();
                            bundle.putString("idProduct", String.valueOf(newProduct.getInt("id")));
                            NavController navController = Navigation.findNavController(v);
                            navController.navigate(R.id.action_nav_create_product_to_nav_product_details,bundle);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(v.getContext(), message, Toast.LENGTH_LONG).show();
                    }
                });


            }
        });
        return root;
    }

}