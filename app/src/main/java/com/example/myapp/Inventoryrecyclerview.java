package com.example.myapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class Inventoryrecyclerview extends RecyclerView.Adapter<Inventoryrecyclerview.ViewHolder> {
    private ArrayList<productobject> products;
    private Context context;
    private database db;
    private OnEditProductClickListener editClickListener;

    public Inventoryrecyclerview(ArrayList<productobject> products, Context context, OnEditProductClickListener listener) {
        this.products = products;
        this.context = context;
        this.db = new database(context);
        this.editClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        productobject product = products.get(position);

        holder.productId.setText(product.getId());
        holder.productName.setText(product.getName());
        String quantity = product.getQuantity() + " / " + product.getOverquantity();
        holder.quantity.setText(quantity);

        Bitmap image = product.getImage();
        if (image != null) {
            holder.productImage.setImageBitmap(image);
            holder.productImage.setVisibility(View.VISIBLE);
        } else {
            holder.productImage.setVisibility(View.GONE);
        }

        holder.icon_edit.setOnClickListener(v -> {
            editClickListener.onEditProduct(product);
        });

        holder.delete_Product.setOnClickListener(v -> {
            showDeleteConfirmationDialog(position, product.getId());
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView productImage;
        TextView quantity, productId, productName;
        ImageView icon_edit, delete_Product;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.image_product);
            delete_Product = itemView.findViewById(R.id.delete_product);
            icon_edit = itemView.findViewById(R.id.icon_edit);
            productId = itemView.findViewById(R.id.text_product_id);
            productName = itemView.findViewById(R.id.text_product_name);
            quantity = itemView.findViewById(R.id.text_stock);
        }
    }

    private void showDeleteConfirmationDialog(int position, String productId) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Delete", (dialog, which) -> deleteItem(position, productId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteItem(int position, String productId) {
        // Validate position and productId
        if (position < 0 || position >= products.size() || productId == null) {
            Log.e("DELETE_ERROR", "Invalid position or productId");
            return;
        }

        // Perform database deletion
        boolean deleted = db.deleteProduct(productId);

        if (deleted) {
            // 2. Remove from local list
            products.remove(position);

            // 3. Notify adapter
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, products.size());

            Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to delete product", Toast.LENGTH_SHORT).show();
            Log.e("DELETE_ERROR", "Database deletion failed for product ID: " + productId);
        }
    }

}