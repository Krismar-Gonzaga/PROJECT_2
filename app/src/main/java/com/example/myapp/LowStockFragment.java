package com.example.myapp;

import android.database.Cursor;
import android.os.Bundle;
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

public class LowStockFragment extends Fragment implements OnEditProductClickListener {
    private OnIndecatorUpdate updatedIndecator;
    private OnEditProductClickListener parentEditListener;
    private database dbHelper;
    private RecyclerView recyclerView;
    private LowStockAdapter adapter;
    private TextView emptyView;

    public LowStockFragment(OnEditProductClickListener editProduct, OnIndecatorUpdate updateIndecator){
        this.parentEditListener = editProduct;
        this.updatedIndecator = updateIndecator;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_low_stock, container, false);
        updatedIndecator.Update_indecator();
        dbHelper = new database(getActivity());
        recyclerView = view.findViewById(R.id.low_stock_recycler);
        emptyView = view.findViewById(R.id.empty_low_stock_view);

        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        Cursor cursor = dbHelper.getLowStockProducts();

        if (cursor.getCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("No low stock items");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

            adapter = new LowStockAdapter(getActivity(), cursor, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onEditProduct(productobject product) {
        if (parentEditListener != null) {
            parentEditListener.onEditProduct(product);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}