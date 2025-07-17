package com.example.appsmiths;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

import com.example.appsmiths.model.RecyclableItem;
import com.example.appsmiths.model.User;
import com.example.appsmiths.model.UserRequest;
import com.example.appsmiths.remote.ApiUtils;
import com.example.appsmiths.remote.RecycleItem;
import com.example.appsmiths.remote.RequestService;
import com.example.appsmiths.sharedpref.SharedPrefManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestsActivity extends BaseActivity {

    private Spinner spinnerItemType;
    private EditText etAddress;
    private EditText etNotes;
    private static TextView tvRequestDate;
    private static Date requestDate;

    private List<RecyclableItem> recyclableItemList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            requestDate = new GregorianCalendar(year, month, day).getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
            tvRequestDate.setText(sdf.format(requestDate));
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_base);

        // Inflate the admin layout and add it into the base container
        View view = LayoutInflater.from(this).inflate(R.layout.activity_submit_request, null);
        FrameLayout container = findViewById(R.id.userbase_container);
        container.addView(view);

        // Optional: set up toolbar if your base layout includes one
        setupToolbar();

        spinnerItemType = findViewById(R.id.spinnerItemType);
        etAddress = findViewById(R.id.etAddress);
        etNotes = findViewById(R.id.etNotes);
        tvRequestDate = findViewById(R.id.tvRequestDate);

        requestDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        tvRequestDate.setText(sdf.format(requestDate));

        // 🔄 Load data from database into spinner
        loadRecyclableItems();

        ImageView profileIcon = findViewById(R.id.profileIcon);

        profileIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(RequestsActivity.this, v);
            popup.getMenu().add("Logout").setOnMenuItemClickListener(menuItem -> {
                // Perform logout
                SharedPrefManager shared = new SharedPrefManager(getApplicationContext());
                shared.logout();

                Toast.makeText(getApplicationContext(), "You have successfully logged out.", Toast.LENGTH_SHORT).show();

                // Navigate to LoginActivity and finish AdminActivity
                startActivity(new Intent(RequestsActivity.this, LoginActivity.class));
                finish();

                return true;
            });
            popup.show();
        });
    }

    private void loadRecyclableItems() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();

        RecycleItem api = ApiUtils.getRecycleItem();

        api.getAllItems(token).enqueue(new Callback<List<RecyclableItem>>() {

            public void onResponse(Call<List<RecyclableItem>> call, Response<List<RecyclableItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recyclableItemList = response.body();

                    List<String> itemNames = new ArrayList<>();
                    for (RecyclableItem item : recyclableItemList) {
                        itemNames.add(item.getItem_name());
                    }

                    spinnerAdapter = new ArrayAdapter<>(RequestsActivity.this,
                            android.R.layout.simple_spinner_item, itemNames);
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerItemType.setAdapter(spinnerAdapter);
                } else {
                    try {
                        Log.e("SPINNER_FAIL", "Code: " + response.code());
                        Log.e("SPINNER_FAIL", "Error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(RequestsActivity.this, "Failed to load recyclable items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RecyclableItem>> call, Throwable t) {
                Toast.makeText(RequestsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("SPINNER_LOAD_ERROR", t.toString());
            }
        });
    }


    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void submitRequest(View v) {
        int selectedPosition = spinnerItemType.getSelectedItemPosition();
        if (selectedPosition < 0 || recyclableItemList.isEmpty()) {
            Toast.makeText(this, "Please select a valid item", Toast.LENGTH_SHORT).show();
            return;
        }

        RecyclableItem selectedItem = recyclableItemList.get(selectedPosition);
        String itemID = String.valueOf(selectedItem.getItem_id());
        String address = etAddress.getText().toString();
        String notes = etNotes.getText().toString();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String request_date = sdf.format(requestDate);

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        int userID = user.getId();

        RequestService requestService = ApiUtils.getRequestService();
        Call<UserRequest> call = requestService.submitRequest(user.getToken(),userID, itemID, address, request_date, notes);

        call.enqueue(new Callback<UserRequest>() {
            @Override
            public void onResponse(Call<UserRequest> call, Response<UserRequest> response) {

                // for debug purpose
                Log.d("MyApp:", "Response: " + response.raw().toString());

                if (response.code() == 201) {
                    // book added successfully
                    UserRequest addedRequest = response.body();
                    // display message
                    Toast.makeText(getApplicationContext(),
                            addedRequest.getItem().getItem_name() + " added successfully.",
                            Toast.LENGTH_LONG).show();

                    // end this activity and forward user to BookListActivity
                    Intent intent = new Intent(getApplicationContext(), RequestsListActivity.class);
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
            public void onFailure(Call<UserRequest> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Error [" + t.getMessage() + "]",
                        Toast.LENGTH_LONG).show();
                // safer logging
                Log.d("MyApp:", "Error: " + (t.getCause() != null ? t.getCause().getMessage() : t.getMessage()));
            }
        });
    }

    public void logoutClicked(View view) {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();
        Toast.makeText(getApplicationContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void clearSessionAndRedirect() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();
        finish();
        startActivity(new Intent(this, RequestsActivity.class));
    }

    public void requestsListClicked(View view) {
        // forward user to reqListActivity
        Intent intent = new Intent(getApplicationContext(), RequestsListActivity.class);
        startActivity(intent);
    }
}
