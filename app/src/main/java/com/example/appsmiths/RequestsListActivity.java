package com.example.appsmiths;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appsmiths.adapter.UserRequestAdapter;
import com.example.appsmiths.model.DeleteResponse;
import com.example.appsmiths.model.User;
import com.example.appsmiths.model.UserRequest;
import com.example.appsmiths.remote.ApiUtils;
import com.example.appsmiths.remote.RequestService;
import com.example.appsmiths.sharedpref.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestsListActivity extends BaseActivity {

    private RequestService requestService;
    private RecyclerView rvRequestList;
    private UserRequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_base);

        // Inflate the admin layout and add it into the base container
        View view = LayoutInflater.from(this).inflate(R.layout.activity_requests_list, null);
        FrameLayout container = findViewById(R.id.userbase_container);
        container.addView(view);

        // Optional: set up toolbar if your base layout includes one
        setupToolbar();

        // get reference to the RecyclerView RequestList
        rvRequestList = findViewById(R.id.rvRequestList);

        //register for context menu
        registerForContextMenu(rvRequestList);

        // fetch and update Request list
        updateRecyclerView();

        ImageView profileIcon = findViewById(R.id.profileIcon);

        profileIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(RequestsListActivity.this, v);
            popup.getMenu().add("Logout").setOnMenuItemClickListener(menuItem -> {
                // Perform logout
                SharedPrefManager shared = new SharedPrefManager(getApplicationContext());
                shared.logout();

                Toast.makeText(getApplicationContext(), "You have successfully logged out.", Toast.LENGTH_SHORT).show();

                // Navigate to LoginActivity and finish AdminActivity
                startActivity(new Intent(RequestsListActivity.this, LoginActivity.class));
                finish();

                return true;
            });
            popup.show();
        });
    }

    private void updateRecyclerView() {
        // Get user info from SharedPreferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();
        int user_id = user.getId(); // get logged-in user ID

        // get Request service instance
        requestService = ApiUtils.getRequestService();

        // get requests by user_id
        requestService.getAllRequests(token).enqueue(new Callback<List<UserRequest>>() {
            @Override
            public void onResponse(Call<List<UserRequest>> call, Response<List<UserRequest>> response) {
                if (response.isSuccessful()) {
                    List<UserRequest> allRequests = response.body();
                    List<UserRequest> userRequests = new ArrayList<>();

                    int currentUserId = user.getId(); // logged-in user's ID

                    for (UserRequest r : allRequests) {
                        if (r.getUser_id() == currentUserId) {
                            userRequests.add(r);
                        }
                    }

                    adapter = new UserRequestAdapter(getApplicationContext(), userRequests);
                    rvRequestList.setAdapter(adapter);
                    rvRequestList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                } else {
                    Toast.makeText(getApplicationContext(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserRequest>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error connecting to the server", Toast.LENGTH_LONG).show();
                Log.e("MyApp:", t.toString());
            }
        });
    }


    /**
     * Delete Request record. Called by contextual menu "Delete"
     * @param selectedRequest - Request selected by user
     */
    private void doDeleteRequest(UserRequest selectedRequest) {
        // get user info from SharedPreferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        // prepare REST API call
        RequestService requestService = ApiUtils.getRequestService();
        Call<DeleteResponse> call = requestService.deleteRequest(user.getToken(), selectedRequest.getRequest_id());

        // execute the call
        call.enqueue(new Callback<DeleteResponse>() {
            @Override
            public void onResponse(Call<DeleteResponse> call, Response<DeleteResponse> response) {
                if (response.code() == 200) {
                    // 200 means OK
                    displayAlert("Request successfully deleted");
                    // update data in list view
                    updateRecyclerView();
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
            public void onFailure(Call<DeleteResponse> call, Throwable t) {
                displayAlert("Error [" + t.getMessage() + "]");
                Log.e("MyApp:", t.getMessage());
            }
        });
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        UserRequest selectedRequest = adapter.getSelectedItem();
        if (selectedRequest == null) return;

//        menu.setHeaderTitle("Choose Action");

        // Always add "Details"
        menu.add(0, R.id.menu_details, 0, "Details");

        // Only add "Cancel" if status is Pending
        if ("Pending".equalsIgnoreCase(selectedRequest.getStatus())) {
            menu.add(0, R.id.menu_delete, 1, "Cancel");
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        UserRequest selectedRequest = adapter.getSelectedItem();
        Log.d("MyApp", "selected "+selectedRequest.toString());    // debug purpose

        if (item.getItemId() == R.id.menu_details) {
            // user clicked details contextual menu
            doViewDetails(selectedRequest);
        }
        else if (item.getItemId() == R.id.menu_delete) {
            // user clicked the delete contextual menu
            doDeleteRequest(selectedRequest);
        }

        return super.onContextItemSelected(item);
    }

    private void doViewDetails(UserRequest selectedRequest) {
        Log.d("MyApp:", "viewing details: " + selectedRequest.toString());
        // forward user to RequestDetailsActivity, passing the selected Request id
        Intent intent = new Intent(getApplicationContext(), UserRequestDetailsActivity.class);
        intent.putExtra("request_id", selectedRequest.getRequest_id());
        startActivity(intent);
    }

    /**
     * Action handler for Add Request floating action button
     * @param view
     */
    public void floatingAddRequestClicked(View view) {
        // forward user to NewRequestActivity
        Intent intent = new Intent(getApplicationContext(), RequestsActivity.class);
        startActivity(intent);
    }
}