package com.example.myapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class product extends Fragment {

    private static final int MAX_IMAGE_WIDTH = 1024;
    private static final int MAX_IMAGE_HEIGHT = 1024;
    private static final int IMAGE_QUALITY = 80;

    private EditText product_name;
    private EditText product_price;
    private EditText product_quantity;
    private ImageView backbtn;
    private Button uploadImage;
    private Button add_button;
    private ImageView productImageView;
    private database db;
    private Bitmap productImageBitmap;
    private Uri imageUri;
    private Spinner categorySpinner;
    private String selectedCategory;

    private static final String[] CATEGORIES = {
        "Fruits", "Vegetables", "Dairy", "Bread and baked goods",
        "Meat and fish", "Meat alternatives", "Cans and jars",
        "Pasta, rice, and cereals", "Sauces and condiments", "Herbs and spices",
        "Frozen foods", "Snacks", "Drinks", "Household and cleaning",
        "Personal care", "Pet care", "Baby products"
    };

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    try {
                        productImageBitmap = decodeSampledBitmapFromUri(imageUri, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
                        if (productImageBitmap != null) {
                            productImageView.setImageBitmap(productImageBitmap);
                            productImageView.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e("ImageError", "Error loading image", e);
                        Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    public product() {
        // Required empty public constructor
    }

    private static final String ARG_USER = "currentUser";
    private User currentUser;

    public static product newInstance(User currentUser) {
        product fragment = new product();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, currentUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUser = getArguments().getParcelable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_product, container, false);
        db = new database(requireContext());

        // Initialize views
        backbtn = view.findViewById(R.id.add_to_home);
        product_name = view.findViewById(R.id.name);
        product_price = view.findViewById(R.id.price);
        product_quantity = view.findViewById(R.id.quantity);
        add_button = view.findViewById(R.id.btnAddItem);
        uploadImage = view.findViewById(R.id.uploadImage);
        productImageView = view.findViewById(R.id.productImage);
        categorySpinner = view.findViewById(R.id.categorySpinner);

        // Set up category spinner
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

        // Initialize selectedCategory with the first category
        selectedCategory = CATEGORIES[0];

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = CATEGORIES[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = CATEGORIES[0];
            }
        });

        // Set up image upload button
        uploadImage.setOnClickListener(v -> openImageChooser());

        add_button.setOnClickListener(v -> saveProduct());

        backbtn.setOnClickListener(v -> navigateBack());

        return view;
    }

    private Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth, int reqHeight) throws IOException {
        InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);

        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        inputStream = requireActivity().getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        return bitmap;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;


            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Product Image"));
    }

    private void saveProduct() {
        String name = product_name.getText().toString().trim();
        String priceStr = product_price.getText().toString().trim();
        String quantityStr = product_quantity.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategory == null) {
            selectedCategory = CATEGORIES[0]; // Set default category if none selected
        }

        try {
            float price = Float.parseFloat(priceStr);
            int quantity = Integer.parseInt(quantityStr);

            if (price <= 0) {
                Toast.makeText(getContext(), "Price must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (quantity <= 0) {
                Toast.makeText(getContext(), "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] imageBytes = null;
            if (productImageBitmap != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                productImageBitmap.compress(Bitmap.CompressFormat.WEBP, IMAGE_QUALITY, stream);
                imageBytes = stream.toByteArray();
                stream.close();
            }

            if (currentUser == null || currentUser.getId() == null) {
                Toast.makeText(getContext(), "Error: User information not available", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean inserted = db.add_product(currentUser.getId(), name, price, quantity, imageBytes, selectedCategory);

            if (inserted) {
                Toast.makeText(getContext(), "Product Added Successfully!", Toast.LENGTH_SHORT).show();
                notifyParentAndClose();
            } else {
                Toast.makeText(getContext(), "Product with this name already exists", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers for price and quantity", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("SaveError", "Error compressing image", e);
            Toast.makeText(getContext(), "Error saving image", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("SaveError", "Error saving product: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error saving product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyParentAndClose() {
        Bundle result = new Bundle();
        result.putBoolean("refresh", true);
        getParentFragmentManager().setFragmentResult("product_update", result);
        getParentFragmentManager().popBackStack();
    }

    private void navigateBack() {
        try {
            if (getContext() instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) getContext();
                if (activity.getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    activity.getSupportFragmentManager().popBackStack();
                } else {
                    activity.finish();
                }
            } else if (getContext() instanceof Activity) {
                ((Activity) getContext()).finish();
            }
        } catch (Exception e) {
            Log.e("NavigationError", "Error navigating back", e);
        }
    }

    @Override
    public void onDestroyView() {
        if (productImageBitmap != null && !productImageBitmap.isRecycled()) {
            productImageBitmap.recycle();
            productImageBitmap = null;
        }
        if (db != null) {
            db.close();
        }
        super.onDestroyView();
    }
}