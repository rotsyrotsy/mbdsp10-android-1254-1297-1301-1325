package com.example.trocapp.ui.exchange;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.trocapp.R;

public class ExchangeFragment extends Fragment {

    private ExchangeViewModel mViewModel;

    public static ExchangeFragment newInstance() {
        return new ExchangeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_exchanges, container, false);

        ImageButton buttonScanQRCode = (ImageButton) root.findViewById(R.id.buttonScanQRCode);
        buttonScanQRCode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_nav_exchanges_to_nav_qr_code_exchange);
            }
        });
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ExchangeViewModel.class);
        // TODO: Use the ViewModel
    }

}