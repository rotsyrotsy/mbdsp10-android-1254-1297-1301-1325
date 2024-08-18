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
import android.widget.Button;
import android.widget.Toast;

import com.example.trocapp.R;
import com.example.trocapp.service.OnVolleyResponseListener;
import com.example.trocapp.service.ReceiveService;
import com.google.android.material.snackbar.Snackbar;

public class CreateTransactionFragment extends Fragment implements LocationListener {
    ReceiveService receiveService = new ReceiveService();
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    Double longitude;
    Double latitude;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_create_transaction, container, false);
        Button buttonAcceptTransaction = (Button) root.findViewById(R.id.buttonAcceptTransaction);
        Button buttonRejectTransaction = (Button) root.findViewById(R.id.buttonRejectTransaction);

        Integer exchangeId = 5;
        getCurrentLocation();

        buttonAcceptTransaction.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                receive(exchangeId,true, longitude,latitude);
            }
        });
        buttonRejectTransaction.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                receive(exchangeId,false, longitude,latitude);
            }
        });

        return root;
    }
    private void receive(Integer exchangeId,Boolean accept, Double longitude, Double latitude){
        receiveService.receiveExchange(getContext(),exchangeId,accept, longitude,latitude, new OnVolleyResponseListener() {
            @Override
            public void onSuccess(Object message) {
                Snackbar.make(getView(), String.valueOf(message), Snackbar.LENGTH_LONG).show();
                NavController navController = Navigation.findNavController(getView());
                navController.navigate(R.id.action_nav_add_transaction_to_nav_rating);
            }
            @Override
            public void onFailure(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void getCurrentLocation() {
        if (isLocationPermissionGranted()) {
            // Initialize the LocationManager
            locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
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
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
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
        Toast.makeText(getContext(), "Latitude: " + reslatitude + ", Longitude: " + reslongitude, Toast.LENGTH_LONG).show();
        longitude = reslongitude;
        latitude = reslatitude;
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


}