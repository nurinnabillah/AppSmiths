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

import com.example.appsmiths.model.RecyclableItem;
import com.example.appsmiths.model.UpdatedStatus;
import com.example.appsmiths.model.User;
import com.example.appsmiths.model.UserRequest;
import com.example.appsmiths.remote.ApiUtils;
import com.example.appsmiths.remote.RecycleItem;
import com.example.appsmiths.remote.RequestItem;
import com.example.appsmiths.remote.UserService;
import com.example.appsmiths.sharedpref.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the base layout
        setContentView(R.layout.activity_base);

        // Inflate the admin layout and add it into the base container
        View view = LayoutInflater.from(this).inflate(R.layout.activity_admin, null);
        FrameLayout container = findViewById(R.id.base_container);
        container.addView(view);

        // Optional: set up toolbar if your base layout includes one
        setupToolbar();

        TextView tvTotalUsers = findViewById(R.id.tvTotalUser);

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();

        UserService api = ApiUtils.getUserService();

        // Call API to get list of all users
        Call<List<User>> call = api.getAllUsers(token);

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int totalUsers = response.body().size(); // Count of users
                    String formatted = NumberFormat.getInstance().format(totalUsers);
                    tvTotalUsers.setText(formatted);
                } else {
                    tvTotalUsers.setText("0");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(AdminActivity.this, "Error loading user stats", Toast.LENGTH_SHORT).show();
            }
        });

        TextView tvPickup = findViewById(R.id.tvPickup);

        RequestItem item = ApiUtils.getRequestUser();

        // Call API to get list of all users
        Call<List<UserRequest>> recycle = item.getAllStatus(token, "Pending");

        recycle.enqueue(new Callback<List<UserRequest>>() {
            @Override
            public void onResponse(Call<List<UserRequest>> call, Response<List<UserRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int totalPending = response.body().size(); // Total Pending items
                    String formatted = NumberFormat.getInstance().format(totalPending);
                    tvPickup.setText(formatted);
                } else {
                    tvPickup.setText("0");
                }
            }

            @Override
            public void onFailure(Call<List<UserRequest>> call, Throwable t) {
                Toast.makeText(AdminActivity.this, "Error loading user stats", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up click listeners
        findViewById(R.id.listItem).setOnClickListener(v -> {
            startActivity(new Intent(this, ListItemActivity.class));
        });


        findViewById(R.id.pickupRequest).setOnClickListener(v -> {
            startActivity(new Intent(this, ListRequestActivity.class));
        });

        findViewById(R.id.recycleItem).setOnClickListener(v -> {
            startActivity(new Intent(this, AddRecycleItemActivity.class));
        });

        // Inside onCreate() after you get your inflated view or content view
        ImageView profileIcon = findViewById(R.id.profileIcon);

        profileIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(AdminActivity.this, v);
            popup.getMenu().add("Logout").setOnMenuItemClickListener(menuItem -> {
                // Perform logout
                SharedPrefManager shared = new SharedPrefManager(getApplicationContext());
                shared.logout();

                Toast.makeText(getApplicationContext(), "You have successfully logged out.", Toast.LENGTH_SHORT).show();

                // Navigate to LoginActivity and finish AdminActivity
                startActivity(new Intent(AdminActivity.this, LoginActivity.class));
                finish();

                return true;
            });
            popup.show();
        });
    }
}
