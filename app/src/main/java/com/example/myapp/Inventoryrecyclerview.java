package com.example.myapp;
import android.database.Cursor;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class Inventoryrecyclerview extends RecyclerView.Adapter<Inventoryrecyclerview.ViewHolder> {
    private OnInventoryUpdate InventoryUpdate;
    private ArrayList<productobject> products;
    private Context context;
    private database db;
    private OnEditProductClickListener editClickListener;
    private int currentImagePosition = -1;

    public interface OnImageUpdateListener {
        void onImageUpdated(int position);
    }

    private OnImageUpdateListener imageUpdateListener;

    public void setOnImageUpdateListener(OnImageUpdateListener listener) {
        this.imageUpdateListener = listener;
    }

    public int getCurrentImagePosition() {
        return currentImagePosition;
    }

    public Inventoryrecyclerview(ArrayList<productobject> products, Context context, OnEditProductClickListener listener, OnInventoryUpdate InventoryUpdate) {
        this.products = products;
        this.context = context;
        this.db = new database(context);
        this.editClickListener = listener;
        this.InventoryUpdate = InventoryUpdate;
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

        holder.productName.setText(product.getName());
        String quantity = product.getQuantity() + " / " + product.getOverquantity();
        holder.quantity.setText(quantity);
        holder.productPrice.setText("₱" + product.getPrice());

        Bitmap image = product.getImage();
        if (image != null) {
            holder.productImage.setImageBitmap(image);
            holder.productImage.setVisibility(View.VISIBLE);
            holder.imagePlaceholder.setVisibility(View.GONE);
        } else {
            holder.productImage.setVisibility(View.GONE);
            holder.imagePlaceholder.setVisibility(View.VISIBLE);
        }

        // Make both the image and placeholder clickable
        View.OnClickListener imageClickListener = v -> showImagePickerDialog(position);
        holder.productImage.setOnClickListener(imageClickListener);
        holder.imagePlaceholder.setOnClickListener(imageClickListener);

        holder.icon_edit.setOnClickListener(v -> {
            editClickListener.onEditProduct(product);
        });

        holder.delete_Product.setOnClickListener(v -> {
            showDeleteConfirmationDialog(position, product.getId());
        });
    }

    private void showImagePickerDialog(int position) {
        currentImagePosition = position;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Update Product Image")
                .setItems(new CharSequence[]{"Choose from Gallery"}, (dialog, which) -> {
                    if (context instanceof FragmentActivity) {
                        FragmentActivity activity = (FragmentActivity) context;
                        Fragment fragment = activity.getSupportFragmentManager()
                                .findFragmentById(R.id.fragment_container);
                        
                        if (fragment instanceof Inventory) {
                            Inventory inventoryFragment = (Inventory) fragment;
                            if (which == 0) {
                                inventoryFragment.launchGallery();
                            }
                        } else {
                            Toast.makeText(context, "Cannot update image at this time", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void updateProductImage(Bitmap newImage, String productId) {
        if (currentImagePosition != -1 && currentImagePosition < products.size()) {
            productobject product = products.get(currentImagePosition);
            
            // Scale down the image if it's too large
            Bitmap scaledImage = scaleBitmapIfNeeded(newImage);
            product.setImage(scaledImage);
            
            // Convert Bitmap to byte array and update database
            byte[] imageBytes = BitmapUtils.bitmapToByteArray(scaledImage);
            boolean success = db.updateProductImage(productId, imageBytes);
            db.updateCheckoutProductImage(productId,imageBytes);
            
            if (success) {
                notifyItemChanged(currentImagePosition);
                if (imageUpdateListener != null) {
                    imageUpdateListener.onImageUpdated(currentImagePosition);
                }
                Toast.makeText(context, "Image updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to update image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap scaleBitmapIfNeeded(Bitmap bitmap) {
        int maxDimension = 1024; // Maximum width or height
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        
        if (originalWidth > maxDimension || originalHeight > maxDimension) {
            float scale = Math.min(
                    (float) maxDimension / originalWidth,
                    (float) maxDimension / originalHeight);
            
            int newWidth = Math.round(originalWidth * scale);
            int newHeight = Math.round(originalHeight * scale);
            
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }
        
        return bitmap;
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
        if (position < 0 || position >= products.size() || productId == null) {
            Log.e("DELETE_ERROR", "Invalid position or productId");
            return;
        }
        boolean deleted = db.deleteProduct(productId);
        if (deleted) {
            products.remove(position);
            notifyDataSetChanged();
            Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show();
            InventoryUpdate.onInventoryUpdate();
        } else {
            Toast.makeText(context, "Failed to delete product", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView productImage;
        public View imagePlaceholder;
        TextView quantity, productName, productPrice;
        ImageView icon_edit, delete_Product;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.image_product);
            imagePlaceholder = itemView.findViewById(R.id.image_placeholder);
            delete_Product = itemView.findViewById(R.id.delete_product);
            icon_edit = itemView.findViewById(R.id.icon_edit);
            productName = itemView.findViewById(R.id.text_product_name);
            quantity = itemView.findViewById(R.id.text_stock);
            productPrice = itemView.findViewById(R.id.text_product_price);
        }
    }
}