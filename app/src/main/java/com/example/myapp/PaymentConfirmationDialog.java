package com.example.myapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class PaymentConfirmationDialog extends DialogFragment {

    private final ArrayList<productobject> cartItems;
    private final float totalAmount;
    private final OnPaymentConfirmedListener listener;

    public interface OnPaymentConfirmedListener {
        void onPaymentConfirmed();
    }

    public PaymentConfirmationDialog(ArrayList<productobject> cartItems, float totalAmount, OnPaymentConfirmedListener listener) {
        this.cartItems = cartItems;
        this.totalAmount = totalAmount;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.payment_confirmation_dialog, null);

        // Initialize views
        TextView totalItemsView = view.findViewById(R.id.total_items);
        TextView totalAmountView = view.findViewById(R.id.total_amount);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);

        // Set values
        totalItemsView.setText(String.valueOf(cartItems.size()));
        totalAmountView.setText(String.format("₱ %.2f", totalAmount));

        // Set click listeners
        btnCancel.setOnClickListener(v -> dismiss());
        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentConfirmed();
            }
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
} 