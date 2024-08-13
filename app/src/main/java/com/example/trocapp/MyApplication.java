package com.example.trocapp;

import android.app.Application;

public class MyApplication extends Application {
    private String apiUrl ;
    public String getApiUrl() {
        return "https://troctpt-app-ptkun.ondigitalocean.app/api";
    }
}
