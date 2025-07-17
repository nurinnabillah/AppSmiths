package com.example.appsmiths;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appsmiths.remote.RequestService;
import com.example.appsmiths.model.UserRequest;
import com.example.appsmiths.model.User;
import com.example.appsmiths.remote.ApiUtils;
import com.example.appsmiths.sharedpref.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRequestDetailsActivity extends BaseActivity {

    private RequestService requestService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_base);

        // Inflate the admin layout and add it into the base container
        View view = LayoutInflater.from(this).inflate(R.layout.activity_user_request_details, null);
        FrameLayout container = findViewById(R.id.userbase_container);
        container.addView(view);

        // Optional: set up toolbar if your base layout includes one
        setupToolbar();

        // get Request id sent by RequestListActivity, -1 if not found
        Intent intent = getIntent();
        int request_id = intent.getIntExtra("request_id", -1);

        // get user info from SharedPreferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();

        // get Request service instance
        requestService = ApiUtils.getRequestService();

        // execute the API query. send the token and Request id
        requestService.getRequest(token, request_id).enqueue(new Callback<UserRequest>() {

            @Override
            public void onResponse(Call<UserRequest> call, Response<UserRequest> response) {
                // for debug purpose
                Log.d("MyApp:", "Response: " + response.raw().toString());

                if (response.code() == 200) {
                    // server return success

                    // get Request object from response
                    UserRequest request = response.body();

                    // get references to the view elements
                    TextView tvItemId = findViewById(R.id.tvItemId);
                    TextView tvAddress = findViewById(R.id.tvAddress);
                    TextView tvRequestDate = findViewById(R.id.tvRequestDate);
                    TextView tvStatus = findViewById(R.id.tvStatus);
                    TextView tvWeight = findViewById(R.id.tvWeight);
                    TextView tvPrice = findViewById(R.id.tvPrice);
                    TextView tvNotes = findViewById(R.id.tvNotes);

                    if (request.getItem() != null) {
                        tvItemId.setText("Item: " + request.getItem().getItem_name());
                    } else {
                        tvItemId.setText("Item: Unknown");
                    }

                    tvAddress.setText("Address: " + request.getAddress());

                    String rawDate = request.getRequest_date();
                    String onlyDate = rawDate;

                    if (rawDate != null) {
                        if (rawDate.contains("T")) {
                            onlyDate = rawDate.split("T")[0];
                        } else if (rawDate.contains(" ")) {
                            onlyDate = rawDate.split(" ")[0];
                        }
                    }

                    tvRequestDate.setText(onlyDate);

                    tvStatus.setText(request.getStatus());

                    if (request.getWeight() != null) {
                        tvWeight.setText(request.getWeight() + " kg");
                    } else {
                        tvWeight.setText("Awaiting collection");
                    }

                    if (request.getTotal_price() != null) {
                        tvPrice.setText("RM " + request.getTotal_price());
                    } else {
                        tvPrice.setText("To be calculated");
                    }

                    if (request.getNotes() != null) {
                        tvNotes.setText(request.getNotes());
                    } else {
                        tvNotes.setText("None");
                    }
                }
                else if (response.code() == 401) {
                    // unauthorized error. invalid token, ask user to relogin
                    Toast.makeText(getApplicationContext(), "Invalid session. Please login again", Toast.LENGTH_LONG).show();
                    clearSessionAndRedirect();
                }
                else {
                    // server return other error
                    Toast.makeText(getApplicationContext(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
                    Log.e("MyApp: ", response.toString());
                }
            }

            @Override
            public void onFailure(Call<UserRequest> call, Throwable t) {
                Toast.makeText(null, "Error connecting", Toast.LENGTH_LONG).show();
            }
        });

        ImageView profileIcon = findViewById(R.id.profileIcon);

        profileIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(UserRequestDetailsActivity.this, v);
            popup.getMenu().add("Logout").setOnMenuItemClickListener(menuItem -> {
                // Perform logout
                SharedPrefManager shared = new SharedPrefManager(getApplicationContext());
                shared.logout();

                Toast.makeText(getApplicationContext(), "You have successfully logged out.", Toast.LENGTH_SHORT).show();

                // Navigate to LoginActivity and finish AdminActivity
                startActivity(new Intent(UserRequestDetailsActivity.this, LoginActivity.class));
                finish();

                return true;
            });
            popup.show();
        });
    }

    public void clearSessionAndRedirect() {
        // clear the shared preferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();

        // terminate this activity
        finish();

        // forward to Login Page
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

    }
}