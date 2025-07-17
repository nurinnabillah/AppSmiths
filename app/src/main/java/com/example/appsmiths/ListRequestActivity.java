package com.example.appsmiths;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appsmiths.adapter.RequestAdapter;
import com.example.appsmiths.model.User;
import com.example.appsmiths.model.UserRequest;
import com.example.appsmiths.remote.ApiUtils;
import com.example.appsmiths.remote.RequestItem;
import com.example.appsmiths.sharedpref.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListRequestActivity extends BaseActivity {

    private RecyclerView rvRequestList;
    private RequestAdapter adapter;
    private RequestItem requestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        View view = LayoutInflater.from(this).inflate(R.layout.activity_list_request, null);
        ((FrameLayout) findViewById(R.id.base_container)).addView(view);

        setupToolbar();

        rvRequestList = findViewById(R.id.rvRequestList);
        rvRequestList.setLayoutManager(new LinearLayoutManager(this));

        fetchRequests();

        ImageView profileIcon = findViewById(R.id.profileIcon);

        profileIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(ListRequestActivity.this, v);
            popup.getMenu().add("Logout").setOnMenuItemClickListener(menuItem -> {
                // Perform logout
                SharedPrefManager shared = new SharedPrefManager(getApplicationContext());
                shared.logout();

                Toast.makeText(getApplicationContext(), "You have successfully logged out.", Toast.LENGTH_SHORT).show();

                // Navigate to LoginActivity and finish AdminActivity
                startActivity(new Intent(ListRequestActivity.this, LoginActivity.class));
                finish();

                return true;
            });
            popup.show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchRequests();
    }

    private void fetchRequests() {
        SharedPrefManager spm = new SharedPrefManager(ListRequestActivity.this);
        User user = spm.getUser();
        String token = user.getToken();

        requestList = ApiUtils.getRequestUser();

        requestList.getAllRequests(token).enqueue(new Callback<List<UserRequest>>() {
            @Override
            public void onResponse(Call<List<UserRequest>> call, Response<List<UserRequest>> response) {
                Log.d("MyApp:", "Response: " + response.raw().toString());

                if (response.code() == 200) {
                    List<UserRequest> requests = response.body();
                    adapter = new RequestAdapter(ListRequestActivity.this, requests);
                    rvRequestList.setAdapter(adapter);
                    rvRequestList.setLayoutManager(new LinearLayoutManager(ListRequestActivity.this));
                    rvRequestList.addItemDecoration(new DividerItemDecoration(rvRequestList.getContext(), DividerItemDecoration.VERTICAL));

                } else if (response.code() == 401) {
                    Toast.makeText(ListRequestActivity.this, "Invalid session. Please login again", Toast.LENGTH_LONG).show();
                    clearSessionAndRedirect();

                } else {
                    Toast.makeText(ListRequestActivity.this, "Error: " + response.message(), Toast.LENGTH_LONG).show();
                    Log.e("MyApp: ", response.toString());
                }
            }

            @Override
            public void onFailure(Call<List<UserRequest>> call, Throwable t) {
                Toast.makeText(ListRequestActivity.this, "Error connecting to the server", Toast.LENGTH_LONG).show();
                Log.e("MyApp:", t.toString());
            }
        });
    }

    public void displayAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void clearSessionAndRedirect() {
        SharedPrefManager spm = new SharedPrefManager(this);
        spm.logout();

        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
