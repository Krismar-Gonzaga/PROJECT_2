package com.example.myapp;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class dashboardrecycleview extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int VIEW_TYPE_TRANSACTION = 0;
    private static final String TAG = "DashboardAdapter";

    private List<DashboardActivity.CheckoutGroup> checkoutGroups;
    private List<Object> displayItems = new ArrayList<>();
    Context context;
    private database db;
    private int expandedPosition = -1;

    productobject product;

    private OnTotalProfitUpdate TotalProfitUpdate;

    public dashboardrecycleview(List<DashboardActivity.CheckoutGroup> checkoutGroups, Context context, OnTotalProfitUpdate OnTotalProfitUpdate) {
        this.checkoutGroups = checkoutGroups != null ? checkoutGroups : new ArrayList<>();
        this.context = context;
        this.db = new database(context);
        this.TotalProfitUpdate = OnTotalProfitUpdate;
        buildDisplayItems();
        Log.d(TAG, "Adapter initialized with " + this.checkoutGroups.size() + " groups");
    }

    public void updateData(List<DashboardActivity.CheckoutGroup> newCheckoutGroups) {
        Log.d(TAG, "Updating data with " + (newCheckoutGroups != null ? newCheckoutGroups.size() : 0) + " groups");
        this.checkoutGroups = newCheckoutGroups != null ? newCheckoutGroups : new ArrayList<>();
        buildDisplayItems();
        notifyDataSetChanged();
    }

    private void buildDisplayItems() {
        displayItems.clear();
        if (checkoutGroups != null) {
            displayItems.addAll(checkoutGroups);
        }
        Log.d(TAG, "Built display items: " + displayItems.size() + " items");
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_TRANSACTION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_product, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        try {
            DashboardActivity.CheckoutGroup group = (DashboardActivity.CheckoutGroup) displayItems.get(position);
            TransactionViewHolder viewHolder = (TransactionViewHolder) holder;

            // Set transaction summary data
            viewHolder.transactionId.setText("Transaction ID: " + group.transactionId);
            viewHolder.date.setText("Date: " + (group.products != null && !group.products.isEmpty() ? 
                group.products.get(0).getDate() : "N/A"));

            // Calculate total amount for the transaction
            float totalAmount = 0;
            if (group.products != null) {
                for (productobject product : group.products) {
                    if (product.getTotal_price() != null) {
                        totalAmount += Float.parseFloat(product.getTotal_price());
                    }
                }
            }
            viewHolder.totalAmount.setText(String.format("Total Amount: ₱%.2f", totalAmount));

            // Handle expansion state
            boolean isExpanded = position == expandedPosition;
            viewHolder.productDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            // Animate the expand icon
            float targetRotation = isExpanded ? 180f : 0f;
            if (viewHolder.expandIcon.getRotation() != targetRotation) {
                ObjectAnimator rotation = ObjectAnimator.ofFloat(viewHolder.expandIcon, "rotation", viewHolder.expandIcon.getRotation(), targetRotation);
                rotation.setDuration(300);
                rotation.setInterpolator(new AccelerateDecelerateInterpolator());
                rotation.start();
            }

            // Set up click listener for expansion
            viewHolder.transactionSummary.setOnClickListener(v -> {
                int oldExpandedPosition = expandedPosition;
                expandedPosition = isExpanded ? -1 : position;
                
                if (oldExpandedPosition >= 0) {
                    notifyItemChanged(oldExpandedPosition);
                }
                notifyItemChanged(position);
            });

            // Set up products RecyclerView if expanded
            if (isExpanded && group.products != null && !group.products.isEmpty()) {
                viewHolder.productsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                SoldProductsAdapter productsAdapter = new SoldProductsAdapter(context, group.products);
                viewHolder.productsRecyclerView.setAdapter(productsAdapter);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView transactionId, date, totalAmount;
        ImageView expandIcon;
        LinearLayout transactionSummary, productDetails;
        RecyclerView productsRecyclerView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionId = itemView.findViewById(R.id.transactionId);
            date = itemView.findViewById(R.id.date);
            totalAmount = itemView.findViewById(R.id.totalAmount);
            expandIcon = itemView.findViewById(R.id.expandIcon);
            transactionSummary = itemView.findViewById(R.id.transactionSummary);
            productDetails = itemView.findViewById(R.id.productDetails);
            productsRecyclerView = itemView.findViewById(R.id.productsRecyclerView);
        }
    }

    private void showDeleteConfirmationDialog(Consumer<Boolean> callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Sold Product")
                    .setMessage("Are you sure you want to delete this Sold product?")
                    .setPositiveButton("Delete", (dialog, which) -> callback.accept(true))
                    .setNegativeButton("Cancel", (dialog, which) -> callback.accept(false))
                    .show();
        }
    }
}