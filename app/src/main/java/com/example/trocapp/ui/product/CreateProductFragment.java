package com.example.trocapp.ui.product;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.trocapp.R;
import com.example.trocapp.service.AppHelper;
import com.example.trocapp.service.CategoryService;
import com.example.trocapp.service.GlobalVariables;
import com.example.trocapp.service.OnVolleyResponseListener;
import com.example.trocapp.service.ProductService;
import com.example.trocapp.service.VolleyMultipartRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateProductFragment extends Fragment {

    private JSONArray categoryList;
    private ArrayList<Integer> categories;
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int PICK_IMAGE_REQUEST =1 ;
    private Bitmap bitmap;
    private String filePath;
    ImageView imageView;
    private Uri imageUri;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_product, container, false);
        CategoryService categoryservice = new CategoryService();
        ProductService productservice = new ProductService();

        categories = new ArrayList<Integer>();
        EditText name = root.findViewById(R.id.name);
        EditText description = root.findViewById(R.id.description);
        imageView =  root.findViewById(R.id.imageView);


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

                productservice.createProduct(root.getContext(), name.getText().toString(),description.getText().toString(), categories, new OnVolleyResponseListener() {
                    @Override
                    public void onSuccess(Object data) {
                        JSONObject newProduct = (JSONObject) data;
                        try {
                            // upload image
                            //uploadBitmap(bitmap, root.getContext(),16 );

                            // redirect to product details
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
            filePath = getPath(picUri);
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
    public String getPath(Uri uri) {
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContext().getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();

        String[] projection = {MediaStore.Images.Media.DATA};
        cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

            // Check if columnIndex is valid
            if (columnIndex != -1 && cursor.moveToFirst()) {
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                return filePath;
            } else {
                cursor.close();
                // Handle the case where columnIndex is invalid
                Toast.makeText(getContext(), "Unable to retrieve the image path.", Toast.LENGTH_SHORT).show();
            }
        }
        return null;
    }
    private void uploadBitmap(final Bitmap bitmap, Context context, Integer productId) {
        String url = GlobalVariables.apiUrl() + "/products/uploadImage/"+productId;
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = "An error occurred";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String jsonString = new String(error.networkResponse.data);
                                JSONObject errorObj = new JSONObject(jsonString);
                                message = jsonString;
                            } catch (JSONException e) {
                                e.printStackTrace();
                                message = new String(error.networkResponse.data);
                            }
                        }
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    }
                }) {
                    @Override
                    protected Map<String, DataPart> getByteData() {
                        Map<String, DataPart> params = new HashMap<>();
                        long imagename = System.currentTimeMillis();
                        params.put("file", new DataPart(imagename + ".png",AppHelper.getFileDataFromDrawable(getContext(), bitmap),"image/png"));
                        return params;
                    }
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("x-auth-token", context
                                .getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE)
                                .getString("token", null));
                        params.put("Content-Type", "application/json");
                        return params;
                    }
        };
        //adding the request to volley
        Volley.newRequestQueue(context).add(volleyMultipartRequest);
    }


}