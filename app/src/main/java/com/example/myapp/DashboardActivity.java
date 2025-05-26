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

import java.util.ArrayList;

public class DashboardActivity extends Fragment {

    dashboardrecycleview adapter;

    database productdb;

    TextView total_price_sold;
    RecyclerView recyclerView;
    ArrayList<productobject> Product = new ArrayList<>();


    public DashboardActivity(){

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
        Product.clear();
        getproducts();

        total_price_sold = view.findViewById(R.id.Sales_Profit);
        recyclerView = view.findViewById(R.id.recyclerSold);
        String total_sold = "₱ " + String.valueOf(getTotalProfit());
        total_price_sold.setText(total_sold);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new dashboardrecycleview(Product,getContext());
        recyclerView.setAdapter(adapter);

        return view;
    }

    private int getTotalProfit() {
        int totalSold = 0;
        for (productobject product : Product) {
            try {
                if (product.getTotal_price() != null) {
                    totalSold += Integer.parseInt(product.getTotal_price());
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return totalSold;
    }


    public void getproducts(){
        Cursor cursor = productdb.getSoldProducts();
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
                byte[] imageBytes = cursor.getBlob(6);
                Bitmap productImage = null;

                if (imageBytes != null) {
                    productImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                }

                productobject product = new productobject(id, name, price, total_price,quantity, quantity, productImage);
                Product.add(product);
            }
        }
        cursor.close();
    }
}
