package com.example.myapp;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class edit_Activity extends Fragment {
    private static final String ARG_PRODUCT = "product";
    private productobject product;
    private database db;
    private Spinner categorySpinner;
    private String selectedCategory;

    private static final String[] CATEGORIES = {
        "Fruits", "Vegetables", "Dairy", "Bread and baked goods",
        "Meat and fish", "Meat alternatives", "Cans and jars",
        "Pasta, rice, and cereals", "Sauces and condiments", "Herbs and spices",
        "Frozen foods", "Snacks", "Drinks", "Household and cleaning",
        "Personal care", "Pet care", "Baby products"
    };

    public static edit_Activity newInstance(productobject product) {
        edit_Activity fragment = new edit_Activity();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = getArguments().getParcelable(ARG_PRODUCT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_product, container, false);

        EditText name = view.findViewById(R.id.editname);
        EditText price = view.findViewById(R.id.editprice);
        EditText quantity = view.findViewById(R.id.editquantity);
        Button btnSave = view.findViewById(R.id.btneditItem);
        ImageView backbtn = view.findViewById(R.id.edit_to_inventory);
        categorySpinner = view.findViewById(R.id.categorySpinner);

        // Set up category spinner with custom adapter
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

        // Set current product values
        if (product != null) {
            name.setText(product.getName());
            price.setText(product.getPrice());
            quantity.setText(product.getQuantity());
            
            // Set the category spinner selection
            String currentCategory = product.getCategory();
            if (currentCategory != null) {
                for (int i = 0; i < CATEGORIES.length; i++) {
                    if (CATEGORIES[i].equals(currentCategory)) {
                        categorySpinner.setSelection(i);
                        selectedCategory = currentCategory;
                        break;
                    }
                }
            }
        }

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = CATEGORIES[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = product != null ? product.getCategory() : CATEGORIES[0];
            }
        });

        btnSave.setOnClickListener(v -> {
            if (product != null) {
                // Update product with new values
                product.setName(name.getText().toString());
                product.setPrice(price.getText().toString());
                product.setQuantity(quantity.getText().toString());
                product.setCategory(selectedCategory);

                // Save changes to database
                db = new database(getContext());
                db.update_Product(product);

                productobject checkoutproduct = getcheckoutItem(product.getId());

                if (checkoutproduct != null) {
                    int updatetotalprice = Integer.parseInt(checkoutproduct.getQuantity()) * Integer.parseInt(product.getPrice());
                    db.update_checkout_from_edit(product);
                    db.updatecheckout(checkoutproduct.getId(), checkoutproduct.getQuantity(), String.valueOf(updatetotalprice));
                }
                
                Toast.makeText(getContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Check if the context is an Activity that can handle fragments
                    if (getContext() instanceof AppCompatActivity) {
                        AppCompatActivity activity = (AppCompatActivity) getContext();

                        // Check if there are fragments in the back stack
                        if (activity.getSupportFragmentManager().getBackStackEntryCount() > 0) {
                            activity.getSupportFragmentManager().popBackStack();
                        } else {
                            // If no fragments in back stack, just finish the activity
                            activity.finish();
                        }
                    } else {
                        // If context is not an Activity, try to finish it if it is one
                        if (getContext() instanceof Activity) {
                            ((Activity) getContext()).finish();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }

    public productobject getcheckoutItem(String id) {
        Cursor cursor = db.getCheckoutItems();
        if (cursor.getCount() == 0) {
            Toast.makeText(getContext(), "No Data!", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                if (cursor.getString(1).equals(id)) {
                    String productid = cursor.getString(0);
                    String name = cursor.getString(2);
                    String price = cursor.getString(3);
                    String quantity = cursor.getString(4);
                    String total_price = cursor.getString(5);

                    // Get the image blob from cursor
                    byte[] imageBytes = cursor.getBlob(6); // Assuming image is at index 6
                    Bitmap productImage = null;

                    String category = cursor.getString(7);

                    if (imageBytes != null) {
                        productImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    }

                    productobject product = new productobject(productid, name, price, total_price, quantity, quantity, productImage, "",category);
                    return product;
                }
            }
        }
        cursor.close();
        return null;
    }
}