package com.example.appsmiths;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

import com.example.appsmiths.model.RecyclableItem;
import com.example.appsmiths.model.User;
import com.example.appsmiths.remote.ApiUtils;
import com.example.appsmiths.remote.RecycleItem;
import com.example.appsmiths.sharedpref.SharedPrefManager;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateRecycleItemActivity extends BaseActivity {

    // form fields
    private EditText txtItemName;
    private EditText txtPrice;

    private RecyclableItem recyclableItem;  // current book to be updated


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Inflate the admin layout and add it into the base container
        View view = LayoutInflater.from(this).inflate(R.layout.activity_update_item, null);
        FrameLayout container = findViewById(R.id.base_container);
        container.addView(view);

        // Optional: set up toolbar if your base layout includes one
        setupToolbar();

        // retrieve book id from intent
        // get book id sent by BookListActivity, -1 if not found
        Intent intent = getIntent();
        int id = intent.getIntExtra("item_id", -1);

        txtItemName = findViewById(R.id.etItemName);
        txtPrice = findViewById(R.id.etPrice);

        // retrieve book info from database using the book id
        // get user info from SharedPreferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        // get book service instance
        RecycleItem recycleItem = ApiUtils.getRecycleItem();

        // execute the API query. send the token and book id
        recycleItem.getItem(user.getToken(), id).enqueue(new Callback<RecyclableItem>() {
            @Override
            public void onResponse(Call<RecyclableItem> call, Response<RecyclableItem> response) {
                // for debug purpose
                Log.d("MyApp:", "Update Form Populate Response: " + response.raw().toString());

                if (response.code() == 200) {
                    // server return success
                    // get book object from response
                    recyclableItem = response.body();

                    // set values into forms
                    txtItemName.setText(recyclableItem.getItemName());
                    DecimalFormat df = new DecimalFormat("#.00");
                    txtPrice.setText(df.format(recyclableItem.getPrice_per_kg()));

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
            public void onFailure(Call<RecyclableItem> call, Throwable t) {
                Toast.makeText(null, "Error connecting", Toast.LENGTH_LONG).show();
            }
        });

        ImageView profileIcon = findViewById(R.id.profileIcon);

        profileIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(UpdateRecycleItemActivity.this, v);
            popup.getMenu().add("Logout").setOnMenuItemClickListener(menuItem -> {
                // Perform logout
                SharedPrefManager shared = new SharedPrefManager(getApplicationContext());
                shared.logout();

                Toast.makeText(getApplicationContext(), "You have successfully logged out.", Toast.LENGTH_SHORT).show();

                // Navigate to LoginActivity and finish AdminActivity
                startActivity(new Intent(UpdateRecycleItemActivity.this, LoginActivity.class));
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

        // terminate this MainActivity
        finish();

        // forward to Login Page
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

    }

    /**
     * Update book info in database when the user click Update Book button
     * @param view
     */
    public void updateItem(View view) {
        // get values in form
        String itemName = txtItemName.getText().toString().trim();
        String priceStr = txtPrice.getText().toString().trim();

        // Validate inputs
        if (itemName.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;

        try {
            // Convert price string to double
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            // Handle the error if the input is not a valid number
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return; // Stop further execution
        }

        // Debug old data
        Log.d("MyApp:", "Old Item info: " + recyclableItem.toString());

        // Set updated values
        recyclableItem.setItem_name(itemName);
        recyclableItem.setPrice_per_kg(price);


        Log.d("MyApp:", "New Item info: " + recyclableItem.toString());

        // get user info from SharedPreferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        // send request to update the book record to the REST API
        RecycleItem bookService = ApiUtils.getRecycleItem();
        Call<RecyclableItem> call = bookService.updateItem(user.getToken(), recyclableItem.getId(), recyclableItem.getItem_name(),
                recyclableItem.getPrice_per_kg());

        // execute
        call.enqueue(new Callback<RecyclableItem>() {
            @Override
            public void onResponse(Call<RecyclableItem> call, Response<RecyclableItem> response) {

                // for debug purpose
                Log.d("MyApp:", "Update Request Response: " + response.raw().toString());

                if (response.code() == 200) {
                    Toast.makeText(UpdateRecycleItemActivity.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // <-- This tells ListItemActivity that update succeeded
                    finish();
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
            public void onFailure(Call<RecyclableItem> call, Throwable t) {
                displayAlert("Error [" + t.getMessage() + "]");
                // for debug purpose
                Log.d("MyApp:", "Error: " + t.getCause().getMessage());
            }
        });
    }

    /**
     * Displaying an alert dialog with a single button
     * @param message - message to be displayed
     */
    public void displayUpdateSuccess(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // end this activity and go back to previous activity, BookListActivity
                        setResult(RESULT_OK);
                        finish();

                        finish();

                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Displaying an alert dialog with a single button
     * @param message - message to be displayed
     */
    public void displayAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}