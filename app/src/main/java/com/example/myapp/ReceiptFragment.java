package com.example.myapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReceiptFragment extends DialogFragment {
    private final ArrayList<productobject> soldItems;
    private final String transactionId;
    private final float totalAmount;
    private final String storeName;
    private final Context context;

    private final OnSuccessfulCheckoutListener backhome;

    public ReceiptFragment(ArrayList<productobject> soldItems, String transactionId,
                           float totalAmount, String storeName, Context context, OnSuccessfulCheckoutListener backhome) {
        this.soldItems = soldItems;
        this.transactionId = transactionId;
        this.totalAmount = totalAmount;
        this.storeName = storeName;
        this.context = context;
        this.backhome = backhome;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.receipt_layout, null);

        // Set up views
        TextView storeNameView = view.findViewById(R.id.receipt_store_name);
        TextView transactionIdView = view.findViewById(R.id.receipt_transaction_id);
        TextView dateView = view.findViewById(R.id.receipt_date);
        TextView totalView = view.findViewById(R.id.receipt_total);
        RecyclerView itemsList = view.findViewById(R.id.receipt_items_list);
        Button closeButton = view.findViewById(R.id.btn_close_receipt);

        // Set values
        storeNameView.setText(storeName);
        transactionIdView.setText("Transaction #: " + transactionId);
        dateView.setText(new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(new Date()));
        totalView.setText(String.format(Locale.getDefault(), "TOTAL: ₱%.2f", totalAmount));

        // Set up recycler view
        ReceiptAdapter adapter = new ReceiptAdapter(soldItems, context);
        itemsList.setLayoutManager(new LinearLayoutManager(context));
        itemsList.setAdapter(adapter);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                backhome.BackHome();
            }
        }

        );

        builder.setView(view);
        return builder.create();
    }
}