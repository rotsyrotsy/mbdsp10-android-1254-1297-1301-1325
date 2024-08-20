package com.example.trocapp.ui.transaction;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.trocapp.MainActivity;
import com.example.trocapp.R;
import com.example.trocapp.service.AppHelper;
import com.example.trocapp.service.ExchangeService;
import com.example.trocapp.service.OnVolleyResponseListener;
import com.example.trocapp.service.ReceiveService;
import com.example.trocapp.ui.rating.RateUserFragment;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateTransactionFragment extends Fragment implements LocationListener, RateUserFragment.OnPositiveButtonClickListener {
    ReceiveService receiveService = new ReceiveService();
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    Double longitude;
    Double latitude;
    String url;
    JSONObject exchange;
    private ProgressBar loading;
    ImageView checkIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_create_transaction, container, false);
        loading = root.findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        checkIcon = root.findViewById(R.id.imageView12);

        ExchangeService exchangeService = new ExchangeService();
        //get from QR Code
        String idExchange = getArguments().getString("idExchange");
        url = AppHelper.apiUrl() + "/exchanges/"+idExchange+"/receive";


        exchangeService.getExchange(root.getContext(), idExchange, new OnVolleyResponseListener() {
            @Override
            public void onSuccess(Object data) {
                exchange = (JSONObject) data;

                Integer ownerId = null;
                try {
                    ownerId = exchange.getJSONObject("owner_proposition").getInt("user_id");
                    Integer currentUserId = getContext().getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE).getInt("userId",-1);
                    if(ownerId.equals(currentUserId)){
                        getCurrentLocation();
                    }else{
                        Toast.makeText(getContext(), "You are not the owner of the products.", Toast.LENGTH_SHORT).show();
                        NavController navController = Navigation.findNavController(root);
                        navController.navigate(R.id.action_nav_add_transaction_to_nav_exchanges);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
            @Override
            public void onFailure(String message) {
                Toast.makeText(root.getContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        return root;
    }
    private void receive(String url, Double longitude, Double latitude){
        receiveService.receiveExchange(getContext(),url, longitude,latitude, new OnVolleyResponseListener() {
            @Override
            public void onSuccess(Object message) {
                loading.setVisibility(View.GONE);
                checkIcon.setVisibility(View.VISIBLE);
                Snackbar.make(getView(), String.valueOf(message), Snackbar.LENGTH_LONG).show();
                RateUserFragment rateUserFragment = new RateUserFragment(exchange, CreateTransactionFragment.this);
                rateUserFragment.show(getParentFragmentManager(), "rateUserDialog");
            }
            @Override
            public void onFailure(String message) {
                loading.setVisibility(View.GONE);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void getCurrentLocation() {
        if (isLocationPermissionGranted()) {
            // Initialize the LocationManager
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            try {
                // Request location updates
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0, // Minimum time interval between updates in milliseconds
                        0, // Minimum distance between updates in meters
                        this
                );
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }
    private boolean isLocationPermissionGranted() {
        if (ActivityCompat.checkSelfPermission(
                getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions
            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return false;
        } else {
            return true;
        }
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        double reslatitude = location.getLatitude();
        double reslongitude = location.getLongitude();

        // Use the latitude and longitude values as needed
        longitude = reslongitude;
        latitude = reslatitude;

        if (getContext() != null && !url.isEmpty() && longitude != null && latitude != null) {

            receive(url, longitude,latitude);

        }
        // Optionally, stop location updates if only one update is needed
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // This method is deprecated and not required to implement.
    }
    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Toast.makeText(requireContext(), "GPS Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(requireContext(), "GPS Disabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get the location
                getCurrentLocation();
            } else {
                // Permission denied, show an explanation or disable the functionality
                Toast.makeText(requireContext(), "Location permission is required to get your current location.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onPositiveButtonClick() {
        try {
            String idExchange = String.valueOf(exchange.getInt("id"));
            Bundle bundle = new Bundle();
            bundle.putString("idExchange", idExchange);
            NavController navController = Navigation.findNavController(getView());
            navController.navigate(R.id.action_nav_add_transaction_to_nav_exchange_details, bundle);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


    }
}