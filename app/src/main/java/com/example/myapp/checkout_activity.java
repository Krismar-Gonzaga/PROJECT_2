package com.example.myapp;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class checkout_activity extends Fragment implements OnCartUpdateListener, OnSuccessfulCheckoutListener {

    private final OnSuccessfulCheckoutListener OnSuccessfulCheckout;
    database checkoutdb;
    TextView total_bill;
    ArrayList<productobject> Cart_Product = new ArrayList<>();
    RecyclerView recyclerView;
    checkoutrecyclerview adapter;
    Button btn_checkout;
    OntotalCartUpdated Oncartupdate;
    OnlowStockchecker checklowstock;
    private String storeName;

    public checkout_activity(OnSuccessfulCheckoutListener Backhome, OntotalCartUpdated Oncartupdate, OnlowStockchecker checklowstock) {
        this.OnSuccessfulCheckout = Backhome;
        this.Oncartupdate = Oncartupdate;
        this.checklowstock = checklowstock;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.checkout_activity, container, false);

        total_bill = view.findViewById(R.id.total_bill);
        recyclerView = view.findViewById(R.id.checkoutrecyclerView);
        btn_checkout = view.findViewById(R.id.btn_checkout);

        checkoutdb = new database(getContext());
        refreshCartData();
        loadStoreName();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new checkoutrecyclerview(Cart_Product, getContext(), this, Oncartupdate);
        recyclerView.setAdapter(adapter);
        updateTotalBill();

        btn_checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processCheckout();
            }
        });

        return view;
    }

    private void loadStoreName() {
        Cursor cursor = checkoutdb.getUsers();
        if (cursor.moveToFirst()) {
            storeName = cursor.getString(cursor.getColumnIndexOrThrow(database.COL_STORE_NAME));
        }
        cursor.close();
    }

    private void refreshCartData() {
        Cart_Product.clear();
        getcheckout();
    }

    private void processCheckout() {
        boolean isProcessed = false;
        String transactionId = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        ArrayList<productobject> inventoryItems = get_Product();
        ArrayList<productobject> cartItems = get_Checkout_item();
        float totalAmount = calculateTotal();

        for (productobject cartItem : cartItems) {
            for (productobject inventoryItem : inventoryItems) {
                if (cartItem.getId().equals(inventoryItem.getId())) {
                    try {
                        int currentQty = Integer.parseInt(inventoryItem.getQuantity());
                        int cartQty = Integer.parseInt(cartItem.getQuantity());
                        int newQty = currentQty - cartQty;
                        Bitmap image = cartItem.getImage();
                        byte[] imageBytes = null;
                        if (image != null) {
                            imageBytes = convertBitmapToByteArray(image);
                        }

                        if (newQty >= 0) {
                            inventoryItem.setQuantity(String.valueOf(newQty));
                            checkoutdb.update_checkout_Product(inventoryItem);
                            checkoutdb.add_sold_product(
                                    cartItem.getId(),
                                    cartItem.getName(),
                                    cartItem.getPrice(),
                                    cartItem.getQuantity(),
                                    cartItem.getTotal_price(),
                                    imageBytes,
                                    transactionId
                            );
                            isProcessed = true;
                        } else {
                            Toast.makeText(getContext(),
                                    "Not enough stock for " + inventoryItem.getName(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(),
                                "Invalid quantity format",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }

        if (isProcessed) {
            // Show receipt
            showReceipt(cartItems, transactionId, totalAmount);

            // Clear the cart after successful checkout
            checkoutdb.clearCheckoutTable();
            Cart_Product.clear();
            refreshCartData();
            adapter.notifyDataSetChanged();
            updateTotalBill();
            Oncartupdate.OntotalCartUpdate();
            checklowstock.checklowstock();
        } else {
            Toast.makeText(getContext(), "No items processed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showReceipt(ArrayList<productobject> soldItems, String transactionId, float totalAmount) {
        ReceiptFragment receiptFragment = new ReceiptFragment(
                soldItems,
                transactionId,
                totalAmount,
                storeName,
                getContext()
        );
        receiptFragment.show(getParentFragmentManager(), "receipt_dialog");
    }

    private float calculateTotal() {
        float total = 0;
        for (productobject product : Cart_Product) {
            try {
                total += Float.parseFloat(product.getTotal_price());
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid price format for " + product.getName(), Toast.LENGTH_SHORT).show();
            }
        }
        return total;
    }


    public ArrayList<productobject> get_Product() {
        Cursor cursor = checkoutdb.getProduct();
        ArrayList<productobject> products = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                products.add(new productobject(
                        cursor.getString(0),  // id
                        cursor.getString(2),  // name
                        cursor.getString(3),  // price
                        cursor.getString(4),
                        cursor.getString(4),  // quantity
                        cursor.getString(3),
                        null, // total_price (fixed from 3 to 5)
                        ""
                ));
            }
        }
        cursor.close();
        return products;
    }

    public ArrayList<productobject> get_Checkout_item() {
        Cursor cursor = checkoutdb.getCheckoutItems();
        ArrayList<productobject> cartItems = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                byte[] imageBytes = cursor.getBlob(6); // Assuming image is at index 6
                Bitmap productImage = null;

                if (imageBytes != null) {
                    productImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                }
                cartItems.add(new productobject(
                        cursor.getString(1),  // id
                        cursor.getString(2),  // name
                        cursor.getString(3),  // price
                        cursor.getString(5),
                        cursor.getString(4),
                        cursor.getString(4),
                        productImage,
                        ""
                ));
            }
        }
        cursor.close();
        return cartItems;
    }

    public void getcheckout() {
        Cursor cursor = checkoutdb.getCheckoutItems();
        if (cursor.getCount() == 0) {
            Toast.makeText(getContext(), "No Data!", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String name = cursor.getString(2);
                String price = cursor.getString(3);
                String quantity = cursor.getString(4);
                String total_price = cursor.getString(5);

                // Get the image blob from cursor
                byte[] imageBytes = cursor.getBlob(6); // Assuming image is at index 6
                Bitmap productImage = null;

                if (imageBytes != null) {
                    productImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                }

                productobject product = new productobject(id, name, price, total_price,quantity, quantity, productImage, "");
                Cart_Product.add(product);
            }
        }
        cursor.close();
    }

    @Override
    public void onCartUpdated() {
        updateTotalBill();
    }

    public void updateTotalBill() {
        float total = 0;
        for (productobject product : Cart_Product) {
            try {
                total += Float.parseFloat(product.getTotal_price());
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid price format for " + product.getName(), Toast.LENGTH_SHORT).show();
            }
        }
        total_bill.setText(String.format("TOTAL BILL: ₱ %.2f", total));
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    public void BackHome() {

    }
}