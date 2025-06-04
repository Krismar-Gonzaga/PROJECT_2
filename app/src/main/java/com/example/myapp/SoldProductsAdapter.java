package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class SoldProductsAdapter extends RecyclerView.Adapter<SoldProductsAdapter.ViewHolder> {

    private List<productobject> products;
    private Context context;

    public SoldProductsAdapter(Context context, List<productobject> products) {
        this.context = context;
        this.products = products;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sold_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        productobject product = products.get(position);

        holder.productName.setText(product.getName());
        holder.productQuantity.setText("Quantity: " + product.getQuantity());
        holder.productPrice.setText(String.format("₱%.2f", Float.parseFloat(product.getTotal_price())));

        if (product.getImage() != null && !product.getImage().isRecycled()) {
            holder.productImage.setImageBitmap(product.getImage());
        } else {
            holder.productImage.setImageResource(R.drawable.logo);
        }
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView productImage;
        TextView productName, productQuantity, productPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productQuantity = itemView.findViewById(R.id.productQuantity);
            productPrice = itemView.findViewById(R.id.productPrice);
        }
    }
} 