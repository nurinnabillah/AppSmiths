package com.example.appsmiths;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.appsmiths.model.RecyclableItem;
import com.example.appsmiths.model.User;
import com.example.appsmiths.remote.ApiUtils;
import com.example.appsmiths.remote.RecycleItem;
import com.example.appsmiths.sharedpref.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddRecycleItemActivity extends BaseActivity {

    private EditText txtItemName;
    private EditText txtItemPrice;
    private EditText txtUpdateItem;
    Spinner spinnerItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        View view = LayoutInflater.from(this).inflate(R.layout.activity_recycle_item, null);
        ((FrameLayout) findViewById(R.id.base_container)).addView(view);

        setupToolbar();

        txtItemName = findViewById(R.id.etNewItemName);
        txtItemPrice = findViewById(R.id.etNewItemPrice);

        ImageView profileIcon = findViewById(R.id.profileIcon);

        profileIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(AddRecycleItemActivity.this, v);
            popup.getMenu().add("Logout").setOnMenuItemClickListener(menuItem -> {
                // Perform logout
                SharedPrefManager shared = new SharedPrefManager(getApplicationContext());
                shared.logout();

                Toast.makeText(getApplicationContext(), "You have successfully logged out.", Toast.LENGTH_SHORT).show();

                // Navigate to LoginActivity and finish AdminActivity
                startActivity(new Intent(AddRecycleItemActivity.this, LoginActivity.class));
                finish();

                return true;
            });
            popup.show();
        });
    }

    public void addNewItem(View v) {
        String itemName = txtItemName.getText().toString();
        Double itemPrice = Double.parseDouble(txtItemPrice.getText().toString());

        SharedPrefManager sharedPreferences = new SharedPrefManager(getApplicationContext());
        User user = sharedPreferences.getUser();

        RecycleItem recycleItem = ApiUtils.getRecycleItem();
        Call<RecyclableItem> call = recycleItem.addRecycleItem(user.getToken(), itemName, itemPrice);

        call.enqueue(new Callback<RecyclableItem>() {
            @Override
            public void onResponse(Call<RecyclableItem> call, Response<RecyclableItem> response) {

                // for debug purpose
                Log.d("MyApp:", "Response: " + response.raw().toString());

                if (response.code() == 201) {
                    // book added successfully
                    RecyclableItem addedItem = response.body();
                    // display message
                    Toast.makeText(getApplicationContext(),
                            addedItem.getItemName() + " added successfully.",
                            Toast.LENGTH_LONG).show();

                    // end this activity and forward user to BookListActivity
                    Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if (response.code() == 401) {
                    // invalid token, ask user to relogin
                    Toast.makeText(getApplicationContext(), "Invalid session. Please login again", Toast.LENGTH_LONG).show();
                    clearSessionAndRedirect();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
                    // server return other error
                    Log.e("MyApp: ", response.toString());
                }
            }

            @Override
            public void onFailure(Call<RecyclableItem> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Error [" + t.getMessage() + "]",
                        Toast.LENGTH_LONG).show();
                // for debug purpose
                Log.d("MyApp:", "Error: " + t.getCause().getMessage());
            }
        });
    }


    public void clearSessionAndRedirect() {
        // clear the shared preferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();

        // terminate this MainActivity
        finish();

        // forward to Login Page
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

    }
}
