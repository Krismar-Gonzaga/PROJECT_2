package com.example.myapp;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class homeactivity extends AppCompatActivity implements OnlowStockchecker, OntotalCartUpdated, OnEditProductClickListener,OnCartUpdateListener,OnSuccessfulCheckoutListener,OnviewAnalytics {

    // Views
    private TextView cartBadge, PageName;
    private ImageView checkoutbtn, dashboard, backhome, back_btn, menuIcon;
    private FloatingActionButton floatingActionButton;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    // Database
    private database db;
    private database checkoutdb;

    // Current user
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        initViews();

        // Get current user from intent
        currentUser = getIntent().getParcelableExtra("CURRENT_USER");
        if (currentUser == null) {
            Toast.makeText(this, "User data missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        floatingActionButton.show();

        // Setup database
        db = new database(this);
        checkoutdb = new database(this);
        // Setup fragment listener for FAB visibility
        setupFragmentListener();

        // Set initial FAB visibility
        updateFabVisibility();

        OnCart();

        checkLowStock();

        // Setup navigation drawer
        setupNavigationDrawer();


        // Set initial fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new Home(this))
                .commit();

        // Hide 'Logs' menu item for non-admin users
        if (!currentUser.getType().equals("admin")) {
            navigationView.getMenu().findItem(R.id.nav_logs).setVisible(false);
        }



    }

    private void initViews() {
        floatingActionButton = findViewById(R.id.fab_add);
        dashboard = findViewById(R.id.dashboard);
        cartBadge = findViewById(R.id.cartBadge);
        checkoutbtn = findViewById(R.id.ic_cart);
        backhome = findViewById(R.id.backhome);
        PageName = findViewById(R.id.Pagename);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuIcon = findViewById(R.id.menuIcon);
        back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(v -> onBackPressed());

        updateFabVisibility();
    }

    private void setupNavigationDrawer() {
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.open,
                R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Handle menu icon click
        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Navigation item selection
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();


            if (id == R.id.nav_inventory) {

                if(currentUser.getType().equals("admin")) {
                    navigateToFragment(new Inventory(), "Inventory");
                }else{
                    Toast.makeText(this, "Admin Access Only!", Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.nav_user) {
                navigateToProfile();
            }
            else if (id == R.id.nav_logs) {
                if(currentUser.getType().equals("admin")) {
                    navigateToFragment(new LogsFragment(), "Logs");
                } else {
                    Toast.makeText(this, "Admin access only", Toast.LENGTH_SHORT).show();
                }
            }
            else if (id == R.id.nav_logout){
                navigateToLogout();
            }else if (id == R.id.action_low_stock){

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new LowStockFragment())
                            .addToBackStack(null)
                            .commit();

            }

            drawerLayout.closeDrawer(GravityCompat.START);
            updateFabVisibility();
            return true;
        });

        // Other click listeners
        backhome.setOnClickListener(v -> navigateToFragment(new Home(this), "Home"));

        if(currentUser.getType().equals("admin")) {
            dashboard.setOnClickListener(v -> navigateToFragment(new DashboardActivity(this), "Dashboard"));
        }else{
            Toast.makeText(this,"Admin Access Only!",Toast.LENGTH_SHORT).show();
        }
        checkoutbtn.setOnClickListener(v -> {
            navigateToFragment(new checkout_activity(this, this,this), "Checkout");
            floatingActionButton.hide();
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null && "admin".equals(currentUser.getType())) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, product.newInstance(currentUser))
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
    }

    private void navigateToFragment(Fragment fragment, String title) {
        PageName.setText(title);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToProfile() {
        PageName.setText("User Page");
        Toast.makeText(this, currentUser.getName(), Toast.LENGTH_SHORT).show();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, Profile_activity.newInstance(currentUser))
                .addToBackStack("profile_fragment")
                .commit();
    }
    private void navigateToLogout() {
        LogManager.getInstance(getApplicationContext()).log("User logged out: " + (currentUser != null ? currentUser.getEmailAddress() : "unknown"));
        SharedPreferences preferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        preferences.edit().clear().apply();

        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logout Successfully.", Toast.LENGTH_SHORT).show();
    }

    private void setupFragmentListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            updateFabVisibility();
        });
    }

    private void updateFabVisibility() {
        if (currentUser == null) {
            floatingActionButton.hide();
            return;
        }

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if ("admin".equals(currentUser.getType()) &&
                (currentFragment instanceof Inventory)) {
            floatingActionButton.show();
        } else {
            floatingActionButton.hide();
        }
    }

    @Override
    public void onEditProduct(productobject product) {
        if (product == null) {
            Log.e("FRAGMENT_DEBUG", "Product is null!");
            return;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, edit_Activity.newInstance(product))
                .addToBackStack("edit_product")
                .commit();
    }

    public int getTotalOncartProduct(){
        Cursor cursor = db.getCheckoutItems();
        int total_product = 0;
        while (cursor.moveToNext()){
            total_product += 1;
        }
        return total_product;
    }

    public void OnCart() {
        int totalProducts = getTotalOncartProduct();
        if(totalProducts > 0) {
            cartBadge.setVisibility(View.VISIBLE);
            cartBadge.setText(String.valueOf(totalProducts));
        } else {
            cartBadge.setVisibility(View.GONE);
        }
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void BackHome() {
        navigateToFragment(new Home(this), "Home");
    }

    @Override
    public void OntotalCartUpdate() {
        OnCart();
    }











    public void analyticsview(){
        FloatingActionButton fabAnalytics = findViewById(R.id.fabAnalytics);
        fabAnalytics.setOnClickListener(view -> {
            // Open AnalyticsFragment
            AnalyticsFragment analyticsFragment = new AnalyticsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, analyticsFragment) // Replace with your container ID
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public void viewAnalytics() {
        analyticsview();
    }

    @Override
    public void onCartUpdated() {


    }







    private void checkLowStock() {
        database db = new database(this);
        if (db.hasLowStockItems()) {
            int count = db.getLowStockCount();
            NotificationHelper.showLowStockNotification(this, count);

            // You might want to add a badge or indicator in your UI
            // to show there are low stock items
        }
        db.close();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_low_stock) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LowStockFragment())
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void checklowstock() {
        checkLowStock();
    }
}