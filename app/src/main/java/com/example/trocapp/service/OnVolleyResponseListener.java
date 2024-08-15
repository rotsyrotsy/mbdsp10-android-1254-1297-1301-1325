package com.example.trocapp.service;

public interface OnVolleyResponseListener {
    void onSuccess(Object data);
    void onFailure(String message);
}
