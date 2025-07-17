package com.example.appsmiths;

import android.content.Intent;
import android.icu.text.NumberFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appsmiths.model.User;
import com.example.appsmiths.remote.ApiUtils;
import com.example.appsmiths.remote.UserService;
import com.example.appsmiths.sharedpref.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the base layout
        setContentView(R.layout.activity_user_base);

        // Inflate the admin layout and add it into the base container
        View view = LayoutInflater.from(this).inflate(R.layout.activity_user_homepage, null);
        FrameLayout container = findViewById(R.id.userbase_container);
        container.addView(view);

        // Optional: set up toolbar if your base layout includes one
        setupToolbar();


        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();

        UserService api = ApiUtils.getUserService();

        // Set up click listeners
        findViewById(R.id.btnViewListings).setOnClickListener(v -> {
            startActivity(new Intent(this, RequestsListActivity.class));
        });


        findViewById(R.id.btnRecycleNow).setOnClickListener(v -> {
            startActivity(new Intent(this, RequestsActivity.class));
        });

        // Inside onCreate() after you get your inflated view or content view
        ImageView profileIcon = findViewById(R.id.profileIcon);

       profileIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(UserActivity.this, v);
            popup.getMenu().add("Logout").setOnMenuItemClickListener(menuItem -> {
                // Perform logout
                SharedPrefManager shared = new SharedPrefManager(getApplicationContext());
                shared.logout();

                Toast.makeText(getApplicationContext(), "You have successfully logged out.", Toast.LENGTH_SHORT).show();

                // Navigate to LoginActivity and finish AdminActivity
                startActivity(new Intent(UserActivity.this, LoginActivity.class));
                finish();

                return true;
            });
            popup.show();
        });
    }
}
