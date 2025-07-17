package com.example.appsmiths;

import android.content.Context;
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

import com.example.appsmiths.adapter.ListAdapter;
import com.example.appsmiths.remote.RecycleItem;
import com.example.appsmiths.model.RecyclableItem;
import com.example.appsmiths.model.DeleteResponse;
import com.example.appsmiths.model.User;
import com.example.appsmiths.remote.ApiUtils;
import com.example.appsmiths.sharedpref.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListItemActivity extends BaseActivity {

    private RecycleItem recycleItem;
    private RecyclerView rvItemList;
    private ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        View view = LayoutInflater.from(this).inflate(R.layout.activity_list_item, null);
        ((FrameLayout) findViewById(R.id.base_container)).addView(view);

        setupToolbar();

        // get reference to the RecyclerView bookList
        rvItemList = findViewById(R.id.rvItemList);

        //register for context menu
        registerForContextMenu(rvItemList);

        // fetch and update book list
        updateRecyclerView();

        ImageView profileIcon = findViewById(R.id.profileIcon);

        profileIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(ListItemActivity.this, v);
            popup.getMenu().add("Logout").setOnMenuItemClickListener(menuItem -> {
                // Perform logout
                SharedPrefManager shared = new SharedPrefManager(getApplicationContext());
                shared.logout();

                Toast.makeText(getApplicationContext(), "You have successfully logged out.", Toast.LENGTH_SHORT).show();

                // Navigate to LoginActivity and finish AdminActivity
                startActivity(new Intent(ListItemActivity.this, LoginActivity.class));
                finish();

                return true;
            });
            popup.show();
        });
    }

    private void updateRecyclerView() {
        // get user info from SharedPreferences to get token value
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();

        // get book service instance
        recycleItem = ApiUtils.getRecycleItem();

        // execute the call. send the user token when sending the query
        recycleItem.getAllItems(token).enqueue(new Callback<List<RecyclableItem>>() {
            @Override
            public void onResponse(Call<List<RecyclableItem>> call, Response<List<RecyclableItem>> response) {
                // for debug purpose
                Log.d("MyApp:", "Response: " + response.raw().toString());

                if (response.code() == 200) {
                    // Get list of book object from response
                    List<RecyclableItem> items = response.body();

                    // initialize adapter
                    adapter = new ListAdapter(getApplicationContext(), items);

                    // set adapter to the RecyclerView
                    rvItemList.setAdapter(adapter);

                    // set layout to recycler view
                    rvItemList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                    // add separator between item in the list
                    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvItemList.getContext(),
                            DividerItemDecoration.VERTICAL);
                    rvItemList.addItemDecoration(dividerItemDecoration);
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
            public void onFailure(Call<List<RecyclableItem>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error connecting to the server", Toast.LENGTH_LONG).show();
                Log.e("MyApp:", t.toString());
            }
        });
    }

    /**
     * Delete book record. Called by contextual menu "Delete"
     * @param selectedItem - book selected by user
     */
    private void doDeleteItem(RecyclableItem selectedItem) {
        // get user info from SharedPreferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        // prepare REST API call
        RecycleItem item = ApiUtils.getRecycleItem();
        Call<DeleteResponse> call = item.deleteBook(user.getToken(), selectedItem.getId());

        // execute the call
        call.enqueue(new Callback<DeleteResponse>() {
            @Override
            public void onResponse(Call<DeleteResponse> call, Response<DeleteResponse> response) {
                if (response.code() == 200) {
                    // 200 means OK
                    displayAlert("Item successfully deleted");
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

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        RecyclableItem selectedItem = adapter.getSelectedItem();
        Log.d("MyApp", "selected "+ selectedItem.toString());    // debug purpose

        if (item.getItemId() == R.id.menu_delete) {
            // user clicked the delete contextual menu
            doDeleteItem(selectedItem);
        }
        else if (item.getItemId() == R.id.menu_update) {
            // user clicked the update contextual menu
            doUpdateItems(selectedItem);
        }

        return super.onContextItemSelected(item);
    }

    private void doUpdateItems(RecyclableItem selectedItem) {
        Log.d("MyApp:", "updating book: " + selectedItem.toString());
        // forward user to UpdateBookActivity, passing the selected book id
        Intent intent = new Intent(getApplicationContext(), UpdateRecycleItemActivity.class);
        intent.putExtra("item_id", selectedItem.getId());
        startActivityForResult(intent, 100); // 100 = request code
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            updateRecyclerView(); // refresh list after update
        }
    }
}