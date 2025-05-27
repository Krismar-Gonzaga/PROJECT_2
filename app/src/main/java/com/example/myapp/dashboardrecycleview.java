package com.example.myapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class dashboardrecycleview extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_PRODUCT = 1;

    private List<DashboardActivity.CheckoutGroup> checkoutGroups;
    private List<Object> displayItems = new ArrayList<>(); // Mix of String (transactionId) and productobject
    Context context;

    public dashboardrecycleview(List<DashboardActivity.CheckoutGroup> checkoutGroups, Context context) {
        this.checkoutGroups = checkoutGroups;
        this.context = context;
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            String transactionId = (String) displayItems.get(position);
            ((HeaderViewHolder) holder).headerText.setText("Checkout: " + transactionId);
        } else {
            productobject product = (productobject) displayItems.get(position);
            ProductViewHolder productHolder = (ProductViewHolder) holder;
            productHolder.quantity.setText("Total Quantity: " + product.getQuantity());
            String totalprice = product.getTotal_price();
            productHolder.total_price.setText("Total Price: " + totalprice);
            productHolder.productname.setText(product.getName());
            Bitmap image = product.getImage();
            if (image != null) {
                productHolder.productImage.setImageBitmap(image);
                productHolder.productImage.setVisibility(View.VISIBLE);
            } else {
                productHolder.productImage.setVisibility(View.GONE);
            }
        }
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
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.dash_productImage);
            productname = itemView.findViewById(R.id.dash_productName);
            total_price = itemView.findViewById(R.id.dash_total_Price_sold);
            quantity = itemView.findViewById(R.id.sold_quantity);
        }
    }
}
