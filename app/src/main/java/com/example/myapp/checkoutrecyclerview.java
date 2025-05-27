package com.example.myapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;

public class checkoutrecyclerview extends RecyclerView.Adapter<checkoutrecyclerview.ViewHolder> {

    Context context;
    ArrayList<productobject> cart_product;
    database checkoutdb;

    OnCartUpdateListener cartUpdateListener;

    public checkoutrecyclerview( ArrayList<productobject> cart_product,Context context, OnCartUpdateListener listener) {
        this.cart_product = cart_product;
        this.context = context;
        this.checkoutdb = new database(context);
        this.cartUpdateListener = listener;

    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context ).inflate(R.layout.checkout_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.productname.setText(cart_product.get(position).getName());
        holder.productprice.setText("₱ " + cart_product.get(position).getPrice());
        holder.total_price.setText("₱ " + cart_product.get(position).getTotal_price());
        holder.total_quantity.setText(cart_product.get(position).getQuantity());

        Bitmap image = cart_product.get(position).getImage();
        if (image != null) {
            holder.productImage.setImageBitmap(image);
            holder.productImage.setVisibility(View.VISIBLE);
        } else {
            holder.productImage.setVisibility(View.GONE);
        }
//        currentQuantity = checkoutdb.
//        checkoutdb = new database(context);
//        productobject currentproduct = getcurrentProduct(cart_product.get(position)); // Fixed missing parenthesis

        holder.increaceItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (productobject product: cart_product) {
                    if (product.getName().equals(holder.productname.getText().toString())) {
                        productobject currentProduct = getcurrentProduct(cart_product.get(position));

                        if (currentProduct == null) {
                            Toast.makeText(context, "Product not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Toast.makeText(context, "Current Quantity: " + currentProduct.getQuantity(), Toast.LENGTH_SHORT).show();

                        int cartQty = Integer.parseInt(cart_product.get(position).getQuantity());

                        if (cartQty != Integer.parseInt(currentProduct.getQuantity())) {
                            String updated_quantity = String.valueOf(cartQty + 1);
                            product.setQuantity(updated_quantity);
                            holder.total_quantity.setText(updated_quantity);
                            float result = Float.parseFloat(product.getTotal_price()) + Float.parseFloat(product.getPrice());
                            String updated_total = String.valueOf(result);
                            product.setTotal_price(updated_total);
                            holder.total_price.setText("₱ " + updated_total);
                            checkoutdb.updatecheckout(product.getId(), updated_quantity, updated_total);
                            cartUpdateListener.onCartUpdated();
                        }
                         else {
                            Toast.makeText(context, "Quantity exceeds overquantity", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }
        });



        holder.decreaceItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (productobject product: cart_product) {
                    if (Integer.parseInt(product.getQuantity()) > 0){
                        if (product.getName().equals(holder.productname.getText().toString())){
                            float result = Float.parseFloat(product.getTotal_price()) - Float.parseFloat(product.getPrice());
                            String updated_total =  String.valueOf(result);

                            product.setTotal_price(updated_total);
                            holder.total_price.setText("₱ " + updated_total);
                            String updated_quantity = String.valueOf(Integer.parseInt(product.getQuantity()) - 1);
                            product.setQuantity(updated_quantity);
                            holder.total_quantity.setText(updated_quantity);
                            checkoutdb.updatecheckout(product.getId(),updated_quantity, updated_total);
                            cartUpdateListener.onCartUpdated();
                        }

                    }
                }
            }
        });



        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(position,cart_product.get(position).getId());
            }
        });

    }

    private void deleteItem(int position, String productId) {
        // 1. Delete from database
        boolean deleted = checkoutdb.delete_checkout_Product(productId);

        if (deleted) {
            // 2. Remove from local list
            cart_product.remove(position);

            // 3. Notify adapter
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cart_product.size());

            // 4. Update total
            cartUpdateListener.onCartUpdated();

            Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public int getItemCount() {

        return cart_product.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView productImage;
        TextView productname, productprice, total_price, total_quantity;
        ImageView increaceItem, addButton, decreaceItem, deleteButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.checkout_image);
            productname = itemView.findViewById(R.id.product_name);
            productprice = itemView.findViewById(R.id.price);
            increaceItem = itemView.findViewById(R.id.increace);
            total_price = itemView.findViewById(R.id.total_price);
            decreaceItem = itemView.findViewById(R.id.decrease);
            deleteButton = itemView.findViewById(R.id.delete);
            total_quantity = itemView.findViewById(R.id.total_quantity);
            addButton = itemView.findViewById(R.id.btnAdd);

        }
    }

    public productobject getcurrentProduct(productobject product){
        Cursor cursor = checkoutdb.getProduct();
        while (cursor.moveToNext()){
            if (String.valueOf(cursor.getString(2)).equals(product.getName())){
                return new productobject(
                        cursor.getString(0),  // id
                        cursor.getString(2),  // name
                        cursor.getString(3),  // price
                        cursor.getString(4),  // quantity
                        cursor.getString(4),  // quantity
                        cursor.getString(5),  // overquantity
                        null
                );
            }
        }
        return null;
    }


}
