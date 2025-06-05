package com.example.myapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.EditorInfo;

import com.google.android.material.textfield.TextInputEditText;

public class Inventory extends Fragment implements OnEditProductClickListener, OnInventoryUpdate {

    private final OnEditProductClickListener OneditProduct;
    private OnIndecatorUpdate UpdateIndecator;
    database db;
    private ArrayList<productobject> Product = new ArrayList<>();
    private RecyclerView recyclerView;
    private Inventoryrecyclerview adapter;
    OnCartUpdateListener onCartUpdateListener;
    private Spinner categoryFilterSpinner;
    private String selectedCategory = "All Categories";
    private TextInputEditText searchEditText;
    private String currentSearchQuery = "";
    private View noProductsLayout;
    private TextView noProductsText, noProductsSubtext;

    private static final String[] CATEGORIES = {
        "All Categories",
        "Fruits", "Vegetables", "Dairy", "Bread and baked goods",
        "Meat and fish", "Meat alternatives", "Cans and jars",
        "Pasta, rice, and cereals", "Sauces and condiments", "Herbs and spices",
        "Frozen foods", "Snacks", "Drinks", "Household and cleaning",
        "Personal care", "Pet care", "Baby products"
    };

    // Activity Result Launchers for image selection and camera
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    public Inventory(OnEditProductClickListener OneditProduct, OnIndecatorUpdate updateIndecator) {
        // Required empty public constructor
        this.OneditProduct = OneditProduct;
        this.UpdateIndecator = updateIndecator;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UpdateIndecator.Update_indecator();
        // Initialize activity result launchers
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    try {
                        Uri selectedImage = result.getData().getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                            requireActivity().getContentResolver(), selectedImage);
                        handleImageResult(bitmap);
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        );

        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap bitmap = (Bitmap) extras.get("data");
                        if (bitmap != null) {
                            handleImageResult(bitmap);
                        }
                    }
                }
            }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.inventory_home, container, false);
        db = new database(this.getContext());
        Product.clear();

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewInventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize placeholder views
        noProductsLayout = view.findViewById(R.id.noProductsLayout);
        noProductsText = view.findViewById(R.id.noProductsText);
        noProductsSubtext = view.findViewById(R.id.noProductsSubtext);

        // Initialize search
        searchEditText = view.findViewById(R.id.searchEditText);
        setupSearch();

        // Set up adapter with image update listener
        adapter = new Inventoryrecyclerview(Product, getContext(), OneditProduct, this);
        adapter.setOnImageUpdateListener(position -> {
            // Refresh the data after image update if needed
            getdata();
            adapter.notifyDataSetChanged();
        });
        recyclerView.setAdapter(adapter);

        // Initialize and setup category filter spinner
        categoryFilterSpinner = view.findViewById(R.id.categoryFilterSpinner);
        setupCategorySpinner();

        getdata(); // Initial data load
        return view;
    }

    private void handleImageResult(Bitmap bitmap) {
        int position = adapter.getCurrentImagePosition();
        if (position != -1 && position < Product.size()) {
            productobject product = Product.get(position);
            adapter.updateProductImage(bitmap, product.getId());
        }
    }

    // Method to launch gallery intent
    public void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    // Method to launch camera intent
    public void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        }
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
        categoryFilterSpinner.setAdapter(categoryAdapter);

        categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = CATEGORIES[position];
                getdata(); // Reload data with new filter
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = CATEGORIES[0];
                updateNoProductsVisibility();
            }
        });
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
                
                // If user starts searching, reset category filter to "All Categories"
                if (!currentSearchQuery.isEmpty() && !selectedCategory.equals("All Categories")) {
                    selectedCategory = "All Categories";
                    categoryFilterSpinner.setSelection(0); // First position is "All Categories"
                }
                
                getdata();
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

    private void updateNoProductsVisibility() {
        if (Product.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noProductsLayout.setVisibility(View.VISIBLE);
            
            // Update placeholder text based on filters
            if (!currentSearchQuery.isEmpty() && !selectedCategory.equals("All Categories")) {
                noProductsText.setText("No Products Found");
                noProductsSubtext.setText("No matches for '" + currentSearchQuery + "' in " + selectedCategory);
            } else if (!currentSearchQuery.isEmpty()) {
                noProductsText.setText("No Products Found");
                noProductsSubtext.setText("No matches for '" + currentSearchQuery + "'");
            } else if (!selectedCategory.equals("All Categories")) {
                noProductsText.setText("No Products Found");
                noProductsSubtext.setText("No products in " + selectedCategory);
            } else {
                noProductsText.setText("No Products Available");
                noProductsSubtext.setText("Products will appear here once added");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noProductsLayout.setVisibility(View.GONE);
        }
    }

    public void getdata() {
        Product.clear();
        Cursor cursor;

        // Always get all products if there's a search query
        if (!currentSearchQuery.isEmpty()) {
            cursor = db.getProduct();
        } else if (selectedCategory.equals("All Categories")) {
            cursor = db.getProduct();
        } else {
            cursor = db.getProductsByCategory(selectedCategory);
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

                // Apply filters:
                // 1. If there's a search query, check if name or category contains it
                // 2. If there's a category filter (and no search query), check if category matches
                boolean shouldAdd = true;
                
                if (!currentSearchQuery.isEmpty()) {
                    // If there's a search query, check if name or category contains it
                    shouldAdd = name.toLowerCase().contains(currentSearchQuery) ||
                              category.toLowerCase().contains(currentSearchQuery);
                } else if (!selectedCategory.equals("All Categories")) {
                    // If there's no search but there's a category filter
                    shouldAdd = category.equals(selectedCategory);
                }

                if (!shouldAdd) {
                    continue;
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
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onEditProduct(productobject product) {
        // Your existing edit product implementation
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

    @Override
    public void onInventoryUpdate() {
        getdata();
    }
}
