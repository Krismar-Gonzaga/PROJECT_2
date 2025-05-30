package com.example.myapp;

import android.annotation.SuppressLint;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class Home extends Fragment implements OntotalCartUpdated, OnCartUpdateListener, productrecyclerview.OnCartUpdateListener {
    RecyclerView recyclerView;
    productrecyclerview adapter;
    ArrayList<productobject> Product = new ArrayList<>();

    database db;

    OntotalCartUpdated Oncartupdate;


    homeactivity homeactivity = new homeactivity();
    public Home(OntotalCartUpdated Oncartupdate) {
        // Required empty public constructor
        this.Oncartupdate = Oncartupdate;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        db = new database(getContext());
        Product.clear();
        getdata();


        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()  ));
        adapter = new productrecyclerview(Product, getContext(), Oncartupdate);
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

                productobject product = new productobject(id, name, price, price, quantity, overquantity, productImage , "");
                Product.add(product);
            }
        }
        cursor.close();
    }



    @Override
    public void onCartUpdated() {

    }


    @Override
    public void OntotalCartUpdate() {

    }
}