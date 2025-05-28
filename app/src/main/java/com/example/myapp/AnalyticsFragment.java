package com.example.myapp;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.util.Locale;

public class AnalyticsFragment extends Fragment {

    private database dbHelper;
    private TextView totalSalesValue, totalProductsValue, avgSaleValue, bestSellingProduct;
    private TextView revenueTodayValue, revenueWeekValue, revenueMonthValue;
    private TextView totalTransactionsValue, inventoryValue;

    public AnalyticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        dbHelper = new database(getActivity());
        initializeViews(view);
        loadAnalyticsData();

        return view;
    }

    private void initializeViews(View view) {
        totalSalesValue = view.findViewById(R.id.total_sales_value);
        totalProductsValue = view.findViewById(R.id.total_products_value);
        avgSaleValue = view.findViewById(R.id.avg_sale_value);
        bestSellingProduct = view.findViewById(R.id.best_selling_product);
        revenueTodayValue = view.findViewById(R.id.revenue_today_value);
        revenueWeekValue = view.findViewById(R.id.revenue_week_value);
        revenueMonthValue = view.findViewById(R.id.revenue_month_value);
        totalTransactionsValue = view.findViewById(R.id.total_transactions_value);
        inventoryValue = view.findViewById(R.id.inventory_value);
    }

    private void loadAnalyticsData() {
        // Get total sales revenue
        float totalRevenue = getTotalRevenue();
        totalSalesValue.setText("₱ " + totalRevenue);

        // Get total products count
        int totalProducts = getTotalProducts();
        totalProductsValue.setText(String.valueOf(totalProducts));

        // Get average sale value
        float avgSale = getAverageSale();
        avgSaleValue.setText("₱ " + avgSale);

        // Get best selling product
        String bestProduct = getBestSellingProduct();
        bestSellingProduct.setText(bestProduct != null ? bestProduct : "No sales yet");

        // Get today's revenue
        float todayRevenue = getRevenueForPeriod("date('now')");
        revenueTodayValue.setText("₱ " + todayRevenue);

        // Get weekly revenue
        float weekRevenue = getRevenueForPeriod("date('now', '-7 days')");
        revenueWeekValue.setText("₱ " + weekRevenue);

        // Get monthly revenue
        float monthRevenue = getRevenueForPeriod("date('now', '-30 days')");
        revenueMonthValue.setText("₱ " + monthRevenue);

        // Get total transactions
        int totalTransactions = getTotalTransactions();
        totalTransactionsValue.setText(String.valueOf(totalTransactions));

        // Get inventory value
        float inventoryTotal = getInventoryValue();
        inventoryValue.setText("₱ " + inventoryTotal);
    }

    private float getTotalRevenue() {
        Cursor cursor = dbHelper.getSoldProducts();
        float total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                total += cursor.getFloat(cursor.getColumnIndexOrThrow(database.COL_TOTAL_PRICE));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return total;
    }

    private int getTotalProducts() {
        Cursor cursor = dbHelper.getProduct();
        int count = cursor != null ? cursor.getCount() : 0;
        if (cursor != null) cursor.close();
        return count;
    }

    private float getAverageSale() {
        Cursor cursor = dbHelper.getSoldProducts();
        if (cursor == null || cursor.getCount() == 0) return 0;

        float total = 0;
        int count = cursor.getCount();
        while (cursor.moveToNext()) {
            total += cursor.getFloat(cursor.getColumnIndexOrThrow(database.COL_TOTAL_PRICE));
        }
        cursor.close();
        return total / count;
    }

    private String getBestSellingProduct() {
        Cursor cursor = dbHelper.getSoldProducts();
        if (cursor == null || cursor.getCount() == 0) return null;

        String bestProduct = null;
        int maxQuantity = 0;

        while (cursor.moveToNext()) {
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(database.COL_QUANTITY));
            if (quantity > maxQuantity) {
                maxQuantity = quantity;
                bestProduct = cursor.getString(cursor.getColumnIndexOrThrow(database.COL_PRODUCT_NAME));
            }
        }
        cursor.close();
        return bestProduct;
    }

    private float getRevenueForPeriod(String periodCondition) {
        // This is a simplified version - you would need to implement proper date filtering
        Cursor cursor = dbHelper.getSoldProducts();
        float total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                total += cursor.getFloat(cursor.getColumnIndexOrThrow(database.COL_TOTAL_PRICE));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return total;
    }

    private int getTotalTransactions() {
        Cursor cursor = dbHelper.getSoldProducts();
        int count = cursor != null ? cursor.getCount() : 0;
        if (cursor != null) cursor.close();
        return count;
    }

    private float getInventoryValue() {
        Cursor cursor = dbHelper.getProduct();
        float total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                float price = cursor.getFloat(cursor.getColumnIndexOrThrow(database.COL_PRICE));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(database.COL_QUANTITY));
                total += (price * quantity);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return total;
    }

}