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

public class dashboardrecycleview extends RecyclerView.Adapter<dashboardrecycleview.ViewHolder> {

    private ArrayList<productobject> productObjects;
    Context context;
    public dashboardrecycleview(ArrayList<productobject> productObjects, Context context) {
        this.productObjects = productObjects;
        this.context = context;
    }


    @NonNull
    @Override
    public dashboardrecycleview.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull dashboardrecycleview.ViewHolder holder, int position) {
        holder.quantity.setText("Total Quantity: " + productObjects.get(position).getQuantity());
        String totalprice = productObjects.get(position).getTotal_price();
        holder.total_price.setText("Total Price: " + totalprice);
        holder.productname.setText(productObjects.get(position).getName());
        Bitmap image = productObjects.get(position).getImage();
        if (image != null) {
            holder.productImage.setImageBitmap(image);
            holder.productImage.setVisibility(View.VISIBLE);
        } else {
            holder.productImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return productObjects.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView productname, quantity, total_price;
        ImageView productImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.dash_productImage);
            productname = itemView.findViewById(R.id.dash_productName);
            total_price = itemView.findViewById(R.id.dash_total_Price_sold);
            quantity = itemView.findViewById(R.id.sold_quantity);

        }
    }
}
