package com.example.myapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

public class Inventory extends Fragment implements OnEditProductClickListener {

    database db;
    private ArrayList<productobject> Product = new ArrayList<>();
    private RecyclerView recyclerView;


    OnCartUpdateListener onCartUpdateListener;

    public Inventory() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.inventory_home, container, false);
        db = new database(this.getContext());
        Product.clear();
        getdata();


        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewInventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        // Set up adapter
        Inventoryrecyclerview adapter = new Inventoryrecyclerview(Product,getContext(), (OnEditProductClickListener) getActivity());
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void getdata() {
        Cursor cursor = db.getProduct();
        if (cursor.getCount() == 0) {
            Toast.makeText(getContext(), "No Data!", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String name = cursor.getString(2);
                String price = cursor.getString(3);
                String quantity = cursor.getString(4);
                String overquantity = cursor.getString(5);

                // Get the image blob from cursor
                byte[] imageBytes = cursor.getBlob(6); // Assuming image is at index 6
                Bitmap productImage = null;

                if (imageBytes != null) {
                    productImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                }

                productobject product = new productobject(id, name, price, quantity,quantity, overquantity, productImage, "");
                Product.add(product);
            }
        }
        cursor.close();
    }




    @Override
    public void onEditProduct(productobject product) {

    }
}
