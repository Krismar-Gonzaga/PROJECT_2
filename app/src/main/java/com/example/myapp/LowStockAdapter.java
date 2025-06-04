package com.example.myapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LowStockAdapter extends RecyclerView.Adapter<LowStockAdapter.LowStockViewHolder> {
    private OnEditProductClickListener editproduct;
    private Context context;
    private Cursor cursor;
    private database db;

    public LowStockAdapter(Context context, Cursor cursor, OnEditProductClickListener editproduct) {
        this.context = context;
        this.cursor = cursor;
        this.editproduct = editproduct;
    }

    @NonNull
    @Override
    public LowStockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_low_stock, parent, false);
        return new LowStockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LowStockViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) {
            return;
        }

        db = new database(context.getApplicationContext());

        // Get product details
        int productId = cursor.getInt(cursor.getColumnIndexOrThrow(database.COL_PRODUCT_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(database.COL_PRODUCT_NAME));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(database.COL_PRICE));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(database.COL_QUANTITY));
//        String category = cursor.getString(6);




        // Create product object with all necessary fields
        productobject product = new productobject(
            String.valueOf(productId),
            name,
            String.valueOf(price),
            String.valueOf(price), // Original price
            String.valueOf(quantity),
            String.valueOf(quantity), // Over quantity
            null, // Image
            "", // Description
            "Drinks"
        );

        productobject producttoedit = getcurrentProduct(product);


        // Set the data to views
        holder.productName.setText(name);
        holder.productQuantity.setText(String.format("Quantity: %d", quantity));
        holder.productPrice.setText(String.format("₱%.2f", price));
        holder.date.setText("Date: " + new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(new Date()));

        // Set warning color for very low stock
        if (quantity <= 5) {
            holder.productQuantity.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.productQuantity.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        // Set click listener to edit the product
        holder.cardView.setOnClickListener(v -> {
            if (editproduct != null) {
                editproduct.onEditProduct(producttoedit);
            }
        });

        // Add ripple effect
        holder.cardView.setClickable(true);
        holder.cardView.setFocusable(true);
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        notifyDataSetChanged();
    }

    public static class LowStockViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productQuantity, productPrice, date;
        CardView cardView;

        public LowStockViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            date = itemView.findViewById(R.id.nofication_date);
            productName = itemView.findViewById(R.id.low_stock_product_name);
            productQuantity = itemView.findViewById(R.id.low_stock_quantity);
            productPrice = itemView.findViewById(R.id.low_stock_price);
        }
    }

    public productobject getcurrentProduct(productobject product){
        Cursor cursor = db.getProduct();
        while (cursor.moveToNext()){
            if (String.valueOf(cursor.getString(2)).equals(product.getName())){
                return new productobject(
                        cursor.getString(0),  // id
                        cursor.getString(2),  // name
                        cursor.getString(3),  // price
                        cursor.getString(4),  // quantity
                        cursor.getString(4),  // quantity
                        cursor.getString(5),  // overquantity
                        null,
                        "",
                        cursor.getString(6)
                );
            }
        }
        return null;
    }
}