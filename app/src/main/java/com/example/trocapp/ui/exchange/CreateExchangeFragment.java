package com.example.trocapp.ui.exchange;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.trocapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateExchangeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateExchangeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CreateExchangeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateExchangeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateExchangeFragment newInstance(String param1, String param2) {
        CreateExchangeFragment fragment = new CreateExchangeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_create_exchange, container, false);

        Button buttonSaveExchange = (Button) root.findViewById(R.id.buttonSaveExchange);
        buttonSaveExchange.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_fragment_create_exchange_to_fragment_exchanges);
            }
        });

        Button buttonCancelExchange = (Button) root.findViewById(R.id.buttonCancelExchange);
        buttonCancelExchange.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.popBackStack();
            }
        });
        return root;
    }
}