package com.example.myapp;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends Fragment implements OnTotalProfitUpdate, OnviewAnalytics{

    dashboardrecycleview adapter;

    database productdb;

    TextView total_price_sold;
    RecyclerView recyclerView;
    ArrayList<productobject> Product = new ArrayList<>();

    OnviewAnalytics onviewAnalytics;
    String total_sold;
    @Override
    public void viewAnalytics() {

    }



    // Grouped data structure for dashboard
    public static class CheckoutGroup {
        public String transactionId;
        public List<productobject> products;
        public CheckoutGroup(String transactionId, List<productobject> products) {
            this.transactionId = transactionId;
            this.products = products;
        }
    }
    ArrayList<CheckoutGroup> checkoutGroups = new ArrayList<>();

    public DashboardActivity(OnviewAnalytics onviewAnalytics){
        this.onviewAnalytics = onviewAnalytics;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);



    }
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstranceState){
        View view = inflater.inflate(R.layout.dashboard,container,false);
        productdb = new database(getContext());
        checkoutGroups.clear();
        getGroupedProducts();

        total_price_sold = view.findViewById(R.id.Sales_Profit);
        recyclerView = view.findViewById(R.id.recyclerSold);
        total_sold = "₱ " + String.valueOf(getTotalProfit());
        total_price_sold.setText(total_sold);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new dashboardrecycleview(checkoutGroups,getContext(),this);
        recyclerView.setAdapter(adapter);
        FloatingActionButton fabAnalytics = view.findViewById(R.id.fabAnalytics);


        fabAnalytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onviewAnalytics.viewAnalytics();
            }
        });

        return view;
    }

    private int getTotalProfit() {
        int totalSold = 0;
        for (CheckoutGroup group : checkoutGroups) {
            for (productobject product : group.products) {
                try {
                    if (product.getTotal_price() != null) {
                        totalSold += Integer.parseInt(product.getTotal_price());
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return totalSold;
    }

    // New method to group products by transaction_id
    public void getGroupedProducts(){
        Cursor cursor = productdb.getSoldProducts();
        Map<String, List<productobject>> groupMap = new HashMap<>();
        if (cursor.getCount() == 0) {
            Toast.makeText(getContext(), "No Data!", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                String price = cursor.getString(2);
                String quantity = cursor.getString(3);
                String total_price = cursor.getString(4);
                String transactionId = cursor.getString(5);
                byte[] imageBytes = cursor.getBlob(6);
                String date = cursor.getString(7);
                Bitmap productImage = null;
                if (imageBytes != null) {
                    productImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                }
                productobject product = new productobject(id, name, price, total_price, quantity, quantity, productImage, date);
                if (!groupMap.containsKey(transactionId)) {
                    groupMap.put(transactionId, new ArrayList<>());
                }
                groupMap.get(transactionId).add(product);
            }
            // Convert map to list of groups
            for (Map.Entry<String, List<productobject>> entry : groupMap.entrySet()) {
                checkoutGroups.add(new CheckoutGroup(entry.getKey(), entry.getValue()));
            }
            // Sort checkoutGroups by transactionId (date string) ascending so latest is at the bottom
            Collections.sort(checkoutGroups, new Comparator<CheckoutGroup>() {
                @Override
                public int compare(CheckoutGroup o1, CheckoutGroup o2) {
                    return o1.transactionId.compareTo(o2.transactionId);
                }
            });
        }
        cursor.close();
    }

    public void updatetotal(){
        total_price_sold.setText("₱ " + String.valueOf(getTotalProfit()));
    }


    @Override
    public void Update_total_profit() {
        updatetotal();
    }
}
