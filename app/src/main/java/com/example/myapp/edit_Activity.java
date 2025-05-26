package com.example.myapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class edit_Activity extends Fragment {
    private static final String ARG_PRODUCT = "product";
    private productobject product;




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

        // Set current product values
        if (product != null) {
            name.setText(product.getName());
            price.setText(product.getPrice());
            quantity.setText(product.getQuantity());
        }

        btnSave.setOnClickListener(v -> {
            if (product != null) {
                // Update product with new values
                product.setName(name.getText().toString());
                product.setPrice(price.getText().toString());
                product.setQuantity(quantity.getText().toString());

                // Save changes to database
                database db = new database(getContext());
                db.update_Product(product);

                Toast.makeText(getContext(), "Product updated", Toast.LENGTH_SHORT).show();

                // Go back to previous fragment
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
}