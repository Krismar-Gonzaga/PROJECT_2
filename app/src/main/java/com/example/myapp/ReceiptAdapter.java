package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder> {
    private final ArrayList<productobject> receiptItems;
    private final Context context;

    public ReceiptAdapter(ArrayList<productobject> receiptItems, Context context) {
        this.receiptItems = receiptItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ReceiptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receipt_item, parent, false);
        return new ReceiptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptViewHolder holder, int position) {
        productobject item = receiptItems.get(position);
        holder.itemName.setText(item.getName());
        holder.itemQuantity.setText(item.getQuantity());
        holder.itemPrice.setText(String.format("₱%.2f", Float.parseFloat(item.getPrice())));
        holder.itemTotal.setText(String.format("₱%.2f", Float.parseFloat(item.getTotal_price())));
    }

    @Override
    public int getItemCount() {
        return receiptItems.size();
    }

    public static class ReceiptViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemQuantity, itemPrice, itemTotal;

        public ReceiptViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.receipt_item_name);
            itemQuantity = itemView.findViewById(R.id.receipt_item_quantity);
            itemPrice = itemView.findViewById(R.id.receipt_item_price);
            itemTotal = itemView.findViewById(R.id.receipt_item_total);
        }
    }
}