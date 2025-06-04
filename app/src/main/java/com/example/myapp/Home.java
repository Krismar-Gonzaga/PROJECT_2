package com.example.myapp;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.EditorInfo;
import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class Home extends Fragment implements OntotalCartUpdated, OnCartUpdateListener, productrecyclerview.OnCartUpdateListener {
    RecyclerView recyclerView;
    productrecyclerview adapter;
    ArrayList<productobject> Product = new ArrayList<>();
    private Spinner categorySpinner;
    private String currentCategory = "All Categories";
    private TextInputEditText searchEditText;
    private String currentSearchQuery = "";
    private View noProductsLayout;
    private TextView noProductsText, noProductsSubtext;

    database db;

    OntotalCartUpdated Oncartupdate;

    private static final String[] CATEGORIES = {
        "All Categories", "Fruits", "Vegetables", "Dairy", "Bread and baked goods",
        "Meat and fish", "Meat alternatives", "Cans and jars",
        "Pasta, rice, and cereals", "Sauces and condiments", "Herbs and spices",
        "Frozen foods", "Snacks", "Drinks", "Household and cleaning",
        "Personal care", "Pet care", "Baby products"
    };

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

        // Initialize views
        noProductsLayout = view.findViewById(R.id.noProductsLayout);
        noProductsText = view.findViewById(R.id.noProductsText);
        noProductsSubtext = view.findViewById(R.id.noProductsSubtext);
        
        // Initialize search
        searchEditText = view.findViewById(R.id.searchEditText);
        setupSearch();

        // Initialize category spinner
        categorySpinner = view.findViewById(R.id.categorySpinner);
        setupCategorySpinner();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new productrecyclerview(Product, getContext(), Oncartupdate);
        recyclerView.setAdapter(adapter);

        getdata(); // Initial data load
        return view;
    }

    private void updateNoProductsVisibility() {
        if (Product.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noProductsLayout.setVisibility(View.VISIBLE);
            
            // Update placeholder text based on filters
            if (!currentSearchQuery.isEmpty() && !currentCategory.equals("All Categories")) {
                noProductsText.setText("No Products Found");
                noProductsSubtext.setText("No matches for '" + currentSearchQuery + "' in " + currentCategory);
            } else if (!currentSearchQuery.isEmpty()) {
                noProductsText.setText("No Products Found");
                noProductsSubtext.setText("No matches for '" + currentSearchQuery + "'");
            } else if (!currentCategory.equals("All Categories")) {
                noProductsText.setText("No Products Found");
                noProductsSubtext.setText("No products in " + currentCategory);
            } else {
                noProductsText.setText("No Products Available");
                noProductsSubtext.setText("Products will appear here once added");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noProductsLayout.setVisibility(View.GONE);
        }
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                Product.clear();
                getdata();
                adapter.notifyDataSetChanged();
                updateNoProductsVisibility();
            }
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(
            getContext(),
            android.R.layout.simple_spinner_item,
            CATEGORIES
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(android.R.color.darker_gray));
                text.setTextSize(16);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(android.R.color.black));
                text.setTextSize(16);
                int padding = (int) (16 * getResources().getDisplayMetrics().density);
                text.setPadding(padding, padding, padding, padding);
                return view;
            }
        };
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategory = CATEGORIES[position];
                Product.clear();
                getdata();
                adapter.notifyDataSetChanged();
                updateNoProductsVisibility();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentCategory = "All Categories";
                updateNoProductsVisibility();
            }
        });
    }

    public void getdata() {
        Product.clear();
        Cursor cursor;
        
        if (currentCategory.equals("All Categories")) {
            cursor = db.getProduct();
        } else {
            cursor = db.getProductsByCategory(currentCategory);
        }

        if (cursor.getCount() == 0) {
            updateNoProductsVisibility();
        } else {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String name = cursor.getString(2);
                String price = cursor.getString(3);
                String quantity = cursor.getString(4);
                String overquantity = cursor.getString(5);
                String category = cursor.getString(6);

                // Apply search filter
                if (!currentSearchQuery.isEmpty() &&
                    !name.toLowerCase().contains(currentSearchQuery) &&
                    !category.toLowerCase().contains(currentSearchQuery)) {
                    continue; // Skip this item if it doesn't match the search
                }

                byte[] imageBytes = cursor.getBlob(7);
                Bitmap productImage = null;

                if (imageBytes != null) {
                    productImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                }

                productobject product = new productobject(id, name, price, price, quantity, overquantity, productImage, "", category);
                Product.add(product);
            }
        }
        cursor.close();
        updateNoProductsVisibility();
    }

    @Override
    public void onCartUpdated() {

    }

    @Override
    public void OntotalCartUpdate() {

    }
}