package com.example.myapp;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardActivity extends Fragment implements OnTotalProfitUpdate, OnviewAnalytics {
    private static final String TAG = "DashboardActivity";

    private dashboardrecycleview adapter;
    private database productdb;
    private TextView total_price_sold;
    private TextView totalOrdersCount;
    private TextView activeProductsCount;
    private TextView profitChangeText;
    private RecyclerView recyclerView;

    private ArrayList<productobject> Product = new ArrayList<>();
    private OnviewAnalytics onviewAnalytics;
    private String total_sold;
    private User currentUser;
    private OnIndecatorUpdate updateIndecator;

    // Grouped data structure for dashboard
    public static class CheckoutGroup {
        public String transactionId;
        public List<productobject> products;
        public CheckoutGroup(String transactionId, List<productobject> products) {
            this.transactionId = transactionId;
            this.products = products;
        }
    }
    private ArrayList<CheckoutGroup> checkoutGroups = new ArrayList<>();

    public static DashboardActivity newInstance(OnviewAnalytics listener, User currentUser, OnIndecatorUpdate updated_indecator) {
        DashboardActivity fragment = new DashboardActivity();
        fragment.onviewAnalytics = listener;
        fragment.updateIndecator = updated_indecator;
        fragment.currentUser = currentUser;
        return fragment;
    }

    public DashboardActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateIndecator.Update_indecator();
        try {
            if (getContext() == null) {
                Log.e(TAG, "Context is null in onCreate");
                return;
            }
            productdb = new database(getContext());
            productdb.addCategoryColumnIfNeeded();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        try {
            view = inflater.inflate(R.layout.dashboard, container, false);

            if (view == null) {
                Log.e(TAG, "Failed to inflate dashboard layout");
                return null;
            }

            if (getContext() == null) {
                Log.e(TAG, "Context is null in onCreateView");
                return view;
            }

            // Initialize database if not already initialized
            if (productdb == null) {
                productdb = new database(getContext());
            }

            checkoutGroups.clear();

            // Load and display data
            loadDashboardData();

            // Initialize views
            if (!initializeViews(view)) {
                Log.e(TAG, "Failed to initialize views");
                Toast.makeText(getContext(), "Error initializing dashboard views", Toast.LENGTH_SHORT).show();
                return view;
            }






        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error initializing dashboard: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return view != null ? view : new View(getContext());
    }

    private boolean initializeViews(View view) {
        try {
            total_price_sold = view.findViewById(R.id.SalesProfit);
            totalOrdersCount = view.findViewById(R.id.total_Orders_Count);
            activeProductsCount = view.findViewById(R.id.active_Products_Count);
            profitChangeText = view.findViewById(R.id.Subtitle);
            recyclerView = view.findViewById(R.id.recyclerSold);


            // Update total orders
            int totalOrders = checkoutGroups.size();
            if (totalOrdersCount != null) {
                totalOrdersCount.setText(String.valueOf(totalOrders));
            }

            // Update active products
            int activeProducts = getActiveProductsCount();
            if (activeProductsCount != null) {
                activeProductsCount.setText(String.valueOf(activeProducts));
            }

            // Update profit change percentage
            calculateAndDisplayProfitChange();


            // Update total profit
            int totalProfit = getTotalProfit();
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
            String formattedProfit = currencyFormat.format(totalProfit);
            if (total_price_sold != null) {
                total_price_sold.setText(formattedProfit);
            }


            // Check if any essential views are null
            if (total_price_sold == null || recyclerView == null) {
                Log.e(TAG, "Essential views are null");
                return false;
            }

            // Setup RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new dashboardrecycleview(checkoutGroups, getContext(), this);
            recyclerView.setAdapter(adapter);

            // Setup FloatingActionButton
            FloatingActionButton fabAnalytics = view.findViewById(R.id.fabAnalytics);
            // check if the current user is Admin and can view the analytics
            if (currentUser.getType().equals("user")){
                fabAnalytics.setVisibility(View.GONE);
            }
            if (fabAnalytics != null && onviewAnalytics != null) {
                fabAnalytics.setOnClickListener(v -> {
                    if (onviewAnalytics != null) {
                        onviewAnalytics.viewAnalytics(String.valueOf(totalOrders));
                    }
                });
            }


            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            return false;
        }
    }

    private void loadDashboardData() {
        try {
            getGroupedProducts();
        } catch (Exception e) {
            Log.e(TAG, "Error loading dashboard data", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading dashboard data", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private int getActiveProductsCount() {
        Cursor cursor = null;
        try {
            cursor = productdb.getAvailableProducts();
            return cursor != null ? cursor.getCount() : 0;
        } catch (Exception e) {
            Log.e(TAG, "Error getting active products count", e);
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void calculateAndDisplayProfitChange() {
        try {
            // This is a placeholder. Implement actual calculation based on your requirements
            double changePercentage = 12.5; // Example value
            String changeText = String.format("%s%.1f%% from last month",
                    changePercentage >= 0 ? "+" : "",
                    changePercentage);
            if (profitChangeText != null) {
                profitChangeText.setText(changeText);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating profit change", e);
        }
    }

    private int getTotalProfit() {
        int totalSold = 0;
        try {
            for (CheckoutGroup group : checkoutGroups) {
                for (productobject product : group.products) {
                    if (product.getTotal_price() != null) {
                        totalSold += Integer.parseInt(product.getTotal_price());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating total profit", e);
        }
        return totalSold;
    }

    private void getGroupedProducts() {
        Cursor cursor = null;
        try {
            if (productdb == null) {
                Log.e(TAG, "Database is null");
                return;
            }

            cursor = productdb.getSoldProducts();
            Log.d(TAG, "Cursor obtained. Count: " + (cursor != null ? cursor.getCount() : 0));

            Map<String, List<productobject>> groupMap = new HashMap<>();

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    try {
                        String id = cursor.getString(0);
                        String name = cursor.getString(1);
                        String price = cursor.getString(2);
                        String quantity = cursor.getString(3);
                        String total_price = cursor.getString(4);
                        String transactionId = cursor.getString(5);
                        byte[] imageBytes = cursor.getBlob(6);
                        String date = cursor.getString(7);
                        String category = "Uncategorized"; // Default category if not found

                        Log.d(TAG, "Processing product: " + name + ", transactionId: " + transactionId);

                        Bitmap productImage = null;
                        if (imageBytes != null) {
                            productImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        }

                        productobject product = new productobject(id, name, price, total_price, quantity, quantity, productImage, date, category);

                        if (transactionId != null) {
                            if (!groupMap.containsKey(transactionId)) {
                                groupMap.put(transactionId, new ArrayList<>());
                            }
                            groupMap.get(transactionId).add(product);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing cursor row: " + e.getMessage());
                    }
                }

                // Convert map to list of groups
                checkoutGroups.clear();
                for (Map.Entry<String, List<productobject>> entry : groupMap.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        checkoutGroups.add(new CheckoutGroup(entry.getKey(), entry.getValue()));
                    }
                }

                Log.d(TAG, "Number of checkout groups: " + checkoutGroups.size());

                // Sort checkoutGroups by transactionId (date string) descending
                if (!checkoutGroups.isEmpty()) {
                    Collections.sort(checkoutGroups, (o1, o2) -> {
                        if (o1 == null || o2 == null || o1.transactionId == null || o2.transactionId == null) {
                            return 0;
                        }
                        return o2.transactionId.compareTo(o1.transactionId);
                    });
                }

                // Notify adapter of data change
                if (adapter != null) {
                    adapter.updateData(checkoutGroups);
                } else {
                    Log.e(TAG, "Adapter is null when trying to update data");
                }
            } else {
                if (getContext() != null) {
                    Log.d(TAG, "No sold products found in database");
                    // Don't show toast for empty data, just log it
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting grouped products: " + e.getMessage());
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    @Override
    public void Update_total_profit() {
        try {
            loadDashboardData();
            if (adapter != null) {
                adapter.updateData(checkoutGroups);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating total profit", e);
        }
    }

    @Override
    public void viewAnalytics(String tolal) {
        // Implement analytics view
        if (getContext() != null) {
            Toast.makeText(getContext(), "Opening Analytics", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (productdb != null) {
                productdb.close();
                productdb = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage());
        }
    }
}
