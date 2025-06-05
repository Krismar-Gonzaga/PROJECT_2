package com.example.myapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class productrecyclerview extends RecyclerView.Adapter<productrecyclerview.ViewHolder> implements OntotalCartUpdated {

    private List<productobject> productObjects;
    private Context context;
    private OntotalCartUpdated onCartUpdateListener;
    private database checkoutdb;

    public productrecyclerview(ArrayList<productobject> productObjects, Context context,
                               OntotalCartUpdated onCartUpdateListener) {
        this.productObjects = productObjects != null ? productObjects : new ArrayList<>();
        this.context = context;
        this.onCartUpdateListener = onCartUpdateListener;
        this.checkoutdb = new database(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        productobject product = productObjects.get(position);

        holder.tvName.setText(product.getName());
        holder.quantity.setText("Total Quantity: " + product.getQuantity());
        holder.tvPrice.setText("₱ " + product.getPrice());
        holder.addButton.setImageResource(R.drawable.ic_add);

        // Set product image if available
        Bitmap image = product.getImage();
        if (image != null) {
            holder.productImage.setImageBitmap(image);
            holder.productImage.setVisibility(View.VISIBLE);
        }

        // Check if product is already in cart

        int cartQuantity = getCartQuantity(product.getId());

        // Update UI based on cart status
        updateCartIndicator(holder, product.getId(), cartQuantity);

        holder.addButton.setOnClickListener(v -> {
            // Check if product is already in cart
            boolean isCart = isProductInCart(product.getId());
            if (isCart) {
                // Remove from cart
                boolean removed = checkoutdb.delete_checkout_Product_from_store(product.getId());
                if (removed) {
                    Toast.makeText(context, "Removed from cart", Toast.LENGTH_SHORT).show();
                    updateCartIndicator(holder, product.getId(), 0);
                    if (onCartUpdateListener != null) {
                        onCartUpdateListener.OntotalCartUpdate();
                    }
                }
            }else {
                // Convert image to byte array if available
                byte[] imageBytes = null;
                if (image != null) {
                    imageBytes = convertBitmapToByteArray(image);
                }

                // Check if product is already in cart and if so, get its quantity
                int cartQty = getCartQuantity(product.getId());
                int stockQty = 0;
                try {
                    stockQty = Integer.parseInt(product.getQuantity());
                } catch (Exception e) {
                    stockQty = 0;
                }
                if (cartQty >= stockQty) {
                    Toast.makeText(context, "Cannot add more than available stock!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add to checkout with quantity 1
                boolean isAdded = checkoutdb.add_checkoutproduct(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        "1", // Default quantity
                        product.getQuantity(),
                        product.getPrice(), // Total price (price * quantity)
                        imageBytes
                );

                if (isAdded) {
                    Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();

                    if (onCartUpdateListener != null) {
                        onCartUpdateListener.OntotalCartUpdate();
                    }
                    notifyDataSetChanged(); // Refresh the entire list to update all buttons
                } else {
                    Toast.makeText(context, "Product already in cart!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        updateCartIndicator(holder, product.getId(), cartQuantity + 1);
    }

    private boolean isProductInCart(String productId) {
        Cursor cartCursor = checkoutdb.getCheckoutItems();
        boolean isInCart = false;
        if (cartCursor != null) {
            while (cartCursor.moveToNext()) {
                String cartProductId = cartCursor.getString(1); // product_id
                if (cartProductId.equals(productId)) {
                    isInCart = true;
                    break;
                }
            }
            cartCursor.close();
        }
        return isInCart;
    }

    private int getCartQuantity(String productId) {
        Cursor cursor = checkoutdb.getCheckoutItems();
        int cartQty = 0;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String cartProductId = cursor.getString(1); // product_id
                if (cartProductId.equals(productId)) {
                    cartQty = Integer.parseInt(cursor.getString(4)); // quantity in cart
                    break;
                }
            }
            cursor.close();
        }
        return cartQty;
    }

    private void updateCartIndicator(ViewHolder holder, String productid, int cartQuantity) {
        // Update the "in cart" indicator
        boolean isInCart = isProductInCart(productid);
        holder.inCartIndicator.setVisibility(isInCart ? View.VISIBLE : View.GONE);

        // Update the add button
        if (isInCart) {
            holder.addButton.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.green)));
            holder.addButton.setImageResource(R.drawable.ic_check); // Change icon to checkmark
            holder.addButton.setContentDescription("Remove from cart");

            // Show quantity in cart if more than 1
            if (cartQuantity > 1) {
                holder.cartQuantityBadge.setVisibility(View.VISIBLE);
                holder.cartQuantityBadge.setText(String.valueOf(cartQuantity));
            } else {
                holder.cartQuantityBadge.setVisibility(View.GONE);
            }
        } else {
            holder.addButton.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.red)));
            holder.addButton.setImageResource(R.drawable.ic_add);
            holder.addButton.setContentDescription("Add to cart");
            holder.cartQuantityBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return productObjects.size();
    }

    @Override
    public void OntotalCartUpdate() {
        notifyDataSetChanged(); // Refresh the entire list when cart is updated
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, quantity, cartQuantityBadge;
        ImageView addButton, productImage;
        View inCartIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imgIcon);
            tvName = itemView.findViewById(R.id.tvName);
            quantity = itemView.findViewById(R.id.quantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            addButton = itemView.findViewById(R.id.btnAdd);
            inCartIndicator = itemView.findViewById(R.id.inCartIndicator);

            // Add a badge for showing quantity in cart
            cartQuantityBadge = itemView.findViewById(R.id.cartQuantityBadge);
        }
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public interface OnCartUpdateListener {
        void onCartUpdated();
    }
}