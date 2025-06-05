package com.example.myapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class checkoutrecyclerview extends RecyclerView.Adapter<checkoutrecyclerview.ViewHolder> {

    Context context;
    ArrayList<productobject> cart_product;
    database checkoutdb;

    OnCartUpdateListener cartUpdateListener;

    OntotalCartUpdated Oncartupdate;

    public checkoutrecyclerview( ArrayList<productobject> cart_product,Context context, OnCartUpdateListener listener, OntotalCartUpdated Oncartupdate) {
        this.cart_product = cart_product;
        this.context = context;
        this.checkoutdb = new database(context);
        this.cartUpdateListener = listener;
        this.Oncartupdate = Oncartupdate;

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
        holder.total_price.setText("Total: ₱ " + cart_product.get(position).getTotal_price());
        holder.total_quantity.setText(cart_product.get(position).getQuantity());
        String  availablequantity = cart_product.get(position).getOverquantity();
        holder.availableQuantity.setText("Available: " + availablequantity);

        Bitmap image = cart_product.get(position).getImage();
        if (image != null) {
            holder.productImage.setImageBitmap(image);
            holder.productImage.setVisibility(View.VISIBLE);
        }

        // Add click listener for quantity
        holder.total_quantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuantityEditDialog(position, holder);
            }
        });


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
                            holder.total_price.setText("Total: ₱ " + updated_total);
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
                            holder.total_price.setText("Total: ₱ " + updated_total);
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
                showDeleteConfirmationDialog(position,cart_product.get(position).getId());
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
            Oncartupdate.OntotalCartUpdate();
            cartUpdateListener.onCartUpdated();

            Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
        }
    }


    private void showQuantityEditDialog(int position, ViewHolder holder) {
        // Create custom dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_quantity, null);

// Initialize views
        TextInputLayout quantityInputLayout = dialogView.findViewById(R.id.quantityInputLayout);
        TextInputEditText quantityEditText = dialogView.findViewById(R.id.quantityEditText);
        TextView stockInfo = dialogView.findViewById(R.id.stockInfo);

// Set current quantity and stock info
        quantityEditText.setText(cart_product.get(position).getQuantity());
        productobject currentProduct = getcurrentProduct(cart_product.get(position));
        int maxQuantity = currentProduct != null ? Integer.parseInt(currentProduct.getQuantity()) : 0;
        stockInfo.setText(context.getString(R.string.max_quantity_available, maxQuantity));

// Create the dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                context,
                R.style.ThemeOverlay_MaterialComponents_Dialog
        )
                .setTitle("Edit Quantity")
                .setView(dialogView)
                .setPositiveButton("Update", null) // Set to null to override default behavior
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String newQuantityStr = quantityEditText.getText().toString().trim();

                // Validation
                if (newQuantityStr.isEmpty()) {
                    quantityInputLayout.setError("Quantity cannot be empty");
                    return;
                }

                try {
                    int newQuantity = Integer.parseInt(newQuantityStr);

                    if (newQuantity <= 0) {
                        quantityInputLayout.setError("Quantity must be greater than 0");
                        return;
                    }

                    if (newQuantity > maxQuantity) {
                        quantityInputLayout.setError("Exceeds available stock");
                        return;
                    }

                    // Update quantity
                    productobject product = cart_product.get(position);
                    float pricePerUnit = Float.parseFloat(product.getPrice());
                    float newTotal = pricePerUnit * newQuantity;

                    product.setQuantity(String.valueOf(newQuantity));
                    product.setTotal_price(String.valueOf(newTotal));

                    holder.total_quantity.setText(String.valueOf(newQuantity));
                    holder.total_price.setText(String.format("₱ %.2f", newTotal));


                    checkoutdb.updatecheckout(product.getId(), String.valueOf(newQuantity), String.valueOf(newTotal));
                    cartUpdateListener.onCartUpdated();
                    dialog.dismiss();

                    Toast.makeText(context, "Quantity updated", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    quantityInputLayout.setError("Invalid quantity");
                }
            });
        });

// Clear error when typing
        quantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                quantityInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dialog.show();
    }


    @Override
    public int getItemCount() {

        return cart_product.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView productImage;
        TextView productname, productprice, total_price, total_quantity, availableQuantity;
        ImageView increaceItem, addButton, decreaceItem, deleteButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            availableQuantity = itemView.findViewById(R.id.AvailableQuantity);
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
                        null,
                        "",
                        ""
                );
            }
        }
        return null;
    }

    private void showDeleteConfirmationDialog(int position, String productId) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Delete", (dialog, which) -> deleteItem(position, productId))
                .setNegativeButton("Cancel", null)
                .show();
    }


}
