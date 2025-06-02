package com.example.myapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class dashboardrecycleview extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_PRODUCT = 1;
    private static final String TAG = "DashboardAdapter";

    private List<DashboardActivity.CheckoutGroup> checkoutGroups;
    private List<Object> displayItems = new ArrayList<>();
    Context context;
    private database db;

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
            for (DashboardActivity.CheckoutGroup group : checkoutGroups) {
                if (group != null && group.transactionId != null) {
                    displayItems.add(group.transactionId);
                    if (group.products != null) {
                        displayItems.addAll(group.products);
                    }
                }
            }
        }
        Log.d(TAG, "Built display items: " + displayItems.size() + " items");
    }

    @Override
    public int getItemViewType(int position) {
        Object item = displayItems.get(position);
        return (item instanceof String) ? VIEW_TYPE_HEADER : VIEW_TYPE_PRODUCT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_HEADER) {
            View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_dashboard_product, parent, false);
            return new ProductViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        try {
            if (getItemViewType(position) == VIEW_TYPE_HEADER) {
                String transactionId = (String) displayItems.get(position);
                if (transactionId != null) {
                    ((HeaderViewHolder) holder).headerText.setText("Transaction ID: " + transactionId);
                }
            } else {
                productobject currentProduct = (productobject) displayItems.get(position);
                if (currentProduct == null) {
                    Log.e(TAG, "Product at position " + position + " is null");
                    return;
                }

                ProductViewHolder productHolder = (ProductViewHolder) holder;
                
                // Set text with null checks and proper formatting
                productHolder.quantity.setText("Total Quantity: " + 
                    (currentProduct.getQuantity() != null ? currentProduct.getQuantity() : "0"));
                
                // Format total price with currency
                String totalPrice = currentProduct.getTotal_price() != null ? 
                    String.format("₱%.2f", Float.parseFloat(currentProduct.getTotal_price())) : "₱0.00";
                productHolder.total_price.setText("Total Price: " + totalPrice);
                
                productHolder.productname.setText(currentProduct.getName() != null ? 
                    currentProduct.getName() : "Unknown Product");
                
                productHolder.date.setText("Date: " + 
                    (currentProduct.getDate() != null ? currentProduct.getDate() : "No Date"));

                // Handle delete action safely
                productHolder.delete.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete Sold Product")
                           .setMessage("Are you sure you want to delete this sold product?")
                           .setPositiveButton("Delete", (dialog, which) -> {
                                if (db != null && currentProduct.getId() != null) {
                                    boolean deleted = db.delete_sold_product(currentProduct.getId());
                                    if (deleted && TotalProfitUpdate != null) {
                                        TotalProfitUpdate.Update_total_profit();
                                        Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Failed to delete sold product!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                           })
                           .setNegativeButton("Cancel", null)
                           .show();
                });

                // Handle image safely
                Bitmap image = currentProduct.getImage();
                if (image != null && !image.isRecycled()) {
                    productHolder.productImage.setImageBitmap(image);
                    productHolder.productImage.setVisibility(View.VISIBLE);
                } else {
                    productHolder.productImage.setImageResource(R.drawable.logo);
                    productHolder.productImage.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder: " + e.getMessage());
        }
    }

    // Helper method to find the header position for a given product position
    private int findHeaderPosition(int productPosition) {
        for (int i = productPosition; i >= 0; i--) {
            if (getItemViewType(i) == VIEW_TYPE_HEADER) {
                return i;
            }
        }
        return -1;
    }

    // Helper method to check if a header has no products under it
    private boolean isHeaderEmpty(int headerPosition) {
        // If header is the last item, it's empty
        if (headerPosition == displayItems.size() - 1) return true;

        // Check if the next item is another header (meaning no products under this one)
        return getItemViewType(headerPosition + 1) == VIEW_TYPE_HEADER;
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }
    

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerText = itemView.findViewById(android.R.id.text1);
        }
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productname, quantity, total_price, date;
        ImageView productImage;
        Button delete;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            delete = itemView.findViewById(R.id.delete_sold_product);
            productImage = itemView.findViewById(R.id.dash_productImage);
            productname = itemView.findViewById(R.id.dash_productName);
            total_price = itemView.findViewById(R.id.dash_total_Price_sold);
            quantity = itemView.findViewById(R.id.sold_quantity);
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