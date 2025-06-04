package com.example.myapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class productrecyclerview extends RecyclerView.Adapter<productrecyclerview.ViewHolder> implements OntotalCartUpdated{

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

        // Set product image if available
        Bitmap image = product.getImage();
        if (image != null) {
            holder.productImage.setImageBitmap(image);
            holder.productImage.setVisibility(View.VISIBLE);
        }

        holder.addButton.setOnClickListener(v -> {
            // Convert image to byte array if available
            byte[] imageBytes = null;
            if (image != null) {
                imageBytes = convertBitmapToByteArray(image);
            }

            // Check if product is already in cart and if so, get its quantity
            int cartQty = 0;
            Cursor cartCursor = checkoutdb.getCheckoutItems();
            if (cartCursor != null) {
                while (cartCursor.moveToNext()) {
                    String cartProductId = cartCursor.getString(1); // product_id
                    if (cartProductId.equals(product.getId())) {
                        cartQty = Integer.parseInt(cartCursor.getString(4)); // quantity in cart
                        break;
                    }
                }
                cartCursor.close();
            }
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
                    product.getPrice(), // Total price (price * quantity)
                    imageBytes
            );

            if (isAdded) {
                Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();
                holder.addButton.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_light));

                if (onCartUpdateListener != null) {
                    onCartUpdateListener.OntotalCartUpdate();
                }
            } else {
                Toast.makeText(context, "Product already in cart!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productObjects.size();
    }

    @Override
    public void OntotalCartUpdate() {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice ,quantity;
        ImageView addButton, productImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imgIcon);
            tvName = itemView.findViewById(R.id.tvName);
            quantity = itemView.findViewById(R.id.quantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            addButton = itemView.findViewById(R.id.btnAdd);
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