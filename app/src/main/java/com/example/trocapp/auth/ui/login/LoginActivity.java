package com.example.trocapp.auth.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trocapp.MainActivity;
import com.example.trocapp.R;
import com.example.trocapp.auth.ui.register.RegisterActivity;
import com.example.trocapp.service.AppHelper;
import com.example.trocapp.service.OnVolleyResponseListener;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private ProgressBar loading;
    private EditText email, password;
    private Button buttonLogin, buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // initializing our views
        loading = findViewById(R.id.loading);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();  // Finish the activity
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (email.getText().toString().isEmpty() && password.getText().toString().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Email and password are required", Toast.LENGTH_SHORT).show();
                    return;
                }
                login(email.getText().toString(), password.getText().toString(), new OnVolleyResponseListener() {
                    @Override
                    public void onSuccess(Object message) {
                        Snackbar.make(v, String.valueOf(message), Snackbar.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        LoginActivity.this.finish();  // Finish the activity
                    }
                    @Override
                    public void onFailure(String message) {
                        Snackbar.make(v, message, Snackbar.LENGTH_LONG).show();
                    }
                });

            }
        });
    }
    private void login(String email, String password, final OnVolleyResponseListener listener){
        loading.setVisibility(View.VISIBLE);
        String url = AppHelper.apiUrl() + "/auth/login";
        RequestQueue queue= Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loading.setVisibility(View.GONE);
                try{
                    JSONObject respObj = new JSONObject(response);
                    String message = respObj.getString("message");
                    JSONObject data = respObj.getJSONObject("data");

                    if(data.has("access_token")){
                        SharedPreferences sp = getSharedPreferences("TokenPrefs",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("token",data.getString("access_token"));
                        editor.putInt("userId",data.getJSONObject("user").getInt("id"));
                        editor.apply();
                        listener.onSuccess(message);
                    }else{
                        listener.onFailure("Something went wrong.");
                    }
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
                loading.setVisibility(View.GONE);
                listener.onFailure(message);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };
        queue.add(stringRequest);
    }
}