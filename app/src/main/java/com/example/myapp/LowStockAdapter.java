package com.example.myapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LowStockAdapter extends RecyclerView.Adapter<LowStockAdapter.LowStockViewHolder> {
    private Context context;
    private Cursor cursor;

    public LowStockAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
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

        String name = cursor.getString(cursor.getColumnIndexOrThrow(database.COL_PRODUCT_NAME));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(database.COL_QUANTITY));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(database.COL_PRICE));

        holder.productName.setText(name);
        holder.productQuantity.setText(String.format("Quantity: %d", quantity));
        holder.productPrice.setText(String.format("Price: $%.2f", price));
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }

    public static class LowStockViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productQuantity, productPrice;

        public LowStockViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.low_stock_product_name);
            productQuantity = itemView.findViewById(R.id.low_stock_quantity);
            productPrice = itemView.findViewById(R.id.low_stock_price);
        }
    }
}