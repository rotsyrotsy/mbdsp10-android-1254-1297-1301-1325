package com.example.trocapp.ui.product;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.trocapp.R;
import com.example.trocapp.service.AppHelper;
import com.example.trocapp.service.CategoryService;
import com.example.trocapp.service.ImageLoader;
import com.example.trocapp.service.OnVolleyResponseListener;
import com.example.trocapp.service.ProductService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;

public class UpdateProductFragment extends Fragment {

    private JSONArray categoryList;
    private ArrayList<Integer> categories;
    private static final int PICK_IMAGE_REQUEST =1 ;
    private Bitmap bitmap;
    private String filePath;
    ImageView imageView;
    private JSONObject product;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_update_product, container, false);
        CategoryService categoryservice = new CategoryService();
        ProductService productservice = new ProductService();

        String idProduct = getArguments().getString("idProduct");

        categories = new ArrayList<Integer>();
        EditText name = root.findViewById(R.id.name);
        EditText description = root.findViewById(R.id.description);
        imageView =  root.findViewById(R.id.imageView);


        productservice.getProduct(root.getContext(), idProduct, new OnVolleyResponseListener() {
            @Override
            public void onSuccess(Object data) {
                product = (JSONObject) data;
                try {
                    name.setText(product.getString("product_name"));
                    description.setText(product.getString("description"));
                    String imageURL = product.getString("product_image");
                    if (imageURL != null && !imageURL.isEmpty()) {
                        ImageLoader.setImageFromUrl(imageView, imageURL);
                    }
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
                                try {
                                    JSONArray productCategories = product.getJSONArray("Categories");
                                    for (int ii = 0; ii < productCategories.length(); ii++) {
                                        JSONObject category = productCategories.getJSONObject(ii);
                                        if(category.getInt("id")==item.getId()){
                                            checkBox.setChecked(true);
                                            categories.add(category.getInt("id"));
                                        }
                                    }
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
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


                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });



        FloatingActionButton buttonUploadFile = (FloatingActionButton) root.findViewById(R.id.buttonUploadFile);
        buttonUploadFile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        Button buttonSaveProduct = (Button) root.findViewById(R.id.buttonSaveProduct);
        buttonSaveProduct.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                productservice.updateProduct(root.getContext(), idProduct, name.getText().toString(),description.getText().toString(), categories, new OnVolleyResponseListener() {
                    @Override
                    public void onSuccess(Object data) {
                        JSONObject newProduct = (JSONObject) data;
                        Integer newProductId= null;
                        try {
                            newProductId = (Integer) newProduct.get("id");
                            // upload image
                            if(bitmap!=null){
                                Integer finalNewProductId = newProductId;
                                productservice.uploadImage(bitmap, root.getContext(), newProductId, new OnVolleyResponseListener() {
                                    @Override
                                    public void onSuccess(Object data) {
                                        // redirect to product details
                                        JSONObject updatedProduct = (JSONObject) data;
                                        try {
                                            Bundle bundle = new Bundle();
                                            bundle.putString("idProduct", String.valueOf(updatedProduct.getInt("id")));
                                            NavController navController = Navigation.findNavController(v);
                                            navController.navigate(R.id.action_nav_update_product_to_nav_product_details,bundle);
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    @Override
                                    public void onFailure(String message) {
                                        Toast.makeText(root.getContext(), message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else{
                                Bundle bundle = new Bundle();
                                bundle.putInt("idProduct", Integer.parseInt(idProduct));
                                NavController navController = Navigation.findNavController(v);
                                navController.navigate(R.id.action_nav_update_product_to_nav_product_details,bundle);
                            }
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
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri picUri = data.getData();
            filePath = AppHelper.getPath(getContext(), picUri);
            if (filePath != null) {
                try {
                    Log.d("filePath", String.valueOf(filePath));
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), picUri);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(getActivity(),"no image selected",Toast.LENGTH_LONG).show();
            }
        }

    }

}