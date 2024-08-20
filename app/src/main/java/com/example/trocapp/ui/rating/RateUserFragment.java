package com.example.trocapp.ui.rating;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.trocapp.R;
import com.example.trocapp.service.OnVolleyResponseListener;
import com.example.trocapp.service.RatingService;

import org.json.JSONException;
import org.json.JSONObject;

public class RateUserFragment extends DialogFragment {
    JSONObject exchange;
    private OnPositiveButtonClickListener positiveButtonClickListener;

    public interface OnPositiveButtonClickListener {
        void onPositiveButtonClick();
    }

    public RateUserFragment(JSONObject exchange, OnPositiveButtonClickListener positiveButtonClickListener){
        this.exchange = exchange;
        this.positiveButtonClickListener = positiveButtonClickListener;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        RatingService ratingService = new RatingService();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_rate_user, null);

        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        ratingBar.setNumStars(5);
        LayerDrawable stars=(LayerDrawable)ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);

        EditText review = (EditText) view.findViewById(R.id.review);

        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because it's going in the dialog layout.
        builder.setView(view);
        builder.setPositiveButton("Submit", (DialogInterface.OnClickListener) (dialog, which) -> {
            try {
                String reviewStr = review.getText().toString();
                String ratingStr = String.valueOf(ratingBar.getRating());
                String concernedId = null;
                concernedId = String.valueOf(exchange.getJSONObject("taker_proposition").getInt("user_id"));

                ratingService.rateUser(view.getContext(), concernedId, reviewStr, ratingStr, new OnVolleyResponseListener() {
                    @Override
                    public void onSuccess(Object data) {
                        Toast.makeText(view.getContext(), "Thanks for your vote.", Toast.LENGTH_LONG).show();
                        if (positiveButtonClickListener != null) {
                            positiveButtonClickListener.onPositiveButtonClick();
                        }
                        dismiss();
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        });
        builder.setNegativeButton("Cancel", (DialogInterface.OnClickListener) (dialog, which) -> {
            dismiss();
        });
        return builder.create();
    }
}