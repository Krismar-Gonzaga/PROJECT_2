package com.example.myapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class dashboardrecycleview extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_PRODUCT = 1;

    private List<DashboardActivity.CheckoutGroup> checkoutGroups;
    private List<Object> displayItems = new ArrayList<>();
    Context context;
    private database db;

    public dashboardrecycleview(List<DashboardActivity.CheckoutGroup> checkoutGroups, Context context) {
        this.checkoutGroups = checkoutGroups;
        this.context = context;
        this.db = new database(context);
        buildDisplayItems();
    }

    private void buildDisplayItems() {
        displayItems.clear();
        for (DashboardActivity.CheckoutGroup group : checkoutGroups) {
            displayItems.add(group.transactionId);
            displayItems.addAll(group.products);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (displayItems.get(position) instanceof String) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_PRODUCT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_product, parent, false);
            return new ProductViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            String transactionId = (String) displayItems.get(position);
            ((HeaderViewHolder) holder).headerText.setText("Checkout: " + transactionId);
        } else {
            productobject product = (productobject) displayItems.get(position);
            ProductViewHolder productHolder = (ProductViewHolder) holder;
            productHolder.quantity.setText("Total Quantity: " + product.getQuantity());
            productHolder.total_price.setText("Total Price: " + product.getTotal_price());
            productHolder.productname.setText(product.getName());

            productHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Delete from database first
                    boolean deleted = db.delete_sold_product(product.getId());
                    if (deleted) {
                        // Find the header position for this product
                        int headerPosition = findHeaderPosition(position);

                        // Remove the product from displayItems
                        displayItems.remove(position);

                        // Check if this was the last product under this header
                        if (isHeaderEmpty(headerPosition)) {
                            // Remove the header too
                            displayItems.remove(headerPosition);
                        }

                        notifyDataSetChanged();
                        Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to Delete Sold Product!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            Bitmap image = product.getImage();
            if (image != null) {
                productHolder.productImage.setImageBitmap(image);
                productHolder.productImage.setVisibility(View.VISIBLE);
            } else {
                productHolder.productImage.setVisibility(View.GONE);
            }
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
        TextView productname, quantity, total_price;
        ImageView productImage;
        Button delete;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            delete = itemView.findViewById(R.id.delete_sold_product);
            productImage = itemView.findViewById(R.id.dash_productImage);
            productname = itemView.findViewById(R.id.dash_productName);
            total_price = itemView.findViewById(R.id.dash_total_Price_sold);
            quantity = itemView.findViewById(R.id.sold_quantity);
        }
    }
}