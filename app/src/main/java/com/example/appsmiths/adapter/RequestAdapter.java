package com.example.appsmiths.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appsmiths.AdminActivity;
import com.example.appsmiths.R;
import com.example.appsmiths.model.RecyclableItem;
import com.example.appsmiths.model.UpdatedStatus;
import com.example.appsmiths.model.User;
import com.example.appsmiths.model.UserRequest;
import com.example.appsmiths.remote.ApiUtils;
import com.example.appsmiths.remote.RecycleItem;
import com.example.appsmiths.remote.RequestItem;
import com.example.appsmiths.sharedpref.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {
    private Context context;
    private List<UserRequest> requestList;

    public RequestAdapter(Context context, List<UserRequest> requestList) {
        this.context = context;
        this.requestList = requestList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvUser, tvAddress, tvStatus, tvDate, tvNotes, tvFinalWeight;
        EditText etWeightInput;
        TextView tvCalculatedTotal;
        Button btnUpdateStatus, btnCalculate;

        public ViewHolder(View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStatus = itemView.findViewById(R.id.tvRequestStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            tvFinalWeight = itemView.findViewById(R.id.tvFinalWeight);
            etWeightInput = itemView.findViewById(R.id.etWeightInput);
            tvCalculatedTotal = itemView.findViewById(R.id.tvCalculatedTotal);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
            btnCalculate = itemView.findViewById(R.id.btnCalculate);
        }
    }

    @NonNull
    @Override
    public RequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.request_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.ViewHolder holder, int position) {
        UserRequest request = requestList.get(position);

        if (request.getUser() != null) {
            holder.tvUser.setText("Name: " + request.getUser().getUsername());
        } else {
            holder.tvUser.setText("Name: Unknown");
        }
        holder.tvItemName.setText("Item: " + request.getItem().getItem_name());

        holder.tvAddress.setText("Address: " + request.getAddress());

        String rawDate = request.getRequest_date();
        String onlyDate = rawDate;

        if (rawDate != null) {
            if (rawDate.contains("T")) {
                onlyDate = rawDate.split("T")[0];
            } else if (rawDate.contains(" ")) {
                onlyDate = rawDate.split(" ")[0];
            }
        }

        holder.tvDate.setText("Date: " + onlyDate);

        holder.tvStatus.setText("Status: " + request.getStatus());

        if (request.getNotes() != null) {
            holder.tvNotes.setText("Notes: " + request.getNotes());
        } else {
            holder.tvNotes.setText("Notes: None");
        }

        String totalPriceText = (request.getTotal_price() == null || request.getTotal_price().equals("null"))
                ? "Total Price: To be calculated"
                : "Total Price: RM " + request.getTotal_price();
        holder.tvCalculatedTotal.setText(totalPriceText);

        final double[] calculatedWeight = {0};
        final double[] calculatedTotal = {0};
        final boolean[] isCalculated = {false};  // Track whether calculation has been performed

        holder.btnCalculate.setOnClickListener(v -> {
            String weightStr = holder.etWeightInput.getText().toString().trim();

            if (TextUtils.isEmpty(weightStr)) {
                Toast.makeText(context, "Please enter weight", Toast.LENGTH_SHORT).show();
                return;
            }

            double weight;
            try {
                weight = Double.parseDouble(weightStr);
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid weight format", Toast.LENGTH_SHORT).show();
                return;
            }

            int itemId = request.getItem_id();
            SharedPrefManager spm = new SharedPrefManager(context.getApplicationContext());
            User user = spm.getUser();
            String token = user.getToken();
            RecycleItem item = ApiUtils.getRecycleItem();

            item.getItemById(token, itemId).enqueue(new Callback<RecyclableItem>() {
                public void onResponse(Call<RecyclableItem> call, Response<RecyclableItem> response) {
                    if (response.isSuccessful()) {
                        RecyclableItem item = response.body();
                        double pricePerKg = item.getPrice_per_kg();
                        double total = weight * pricePerKg;

                        calculatedWeight[0] = weight;
                        calculatedTotal[0] = total;
                        isCalculated[0] = true;  // ✅ Mark as calculated

                        holder.tvCalculatedTotal.setText("Total Price: RM " + String.format("%.2f", total));
                    } else {
                        Toast.makeText(context, "Failed to fetch item price", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<RecyclableItem> call, Throwable t) {
                    Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        holder.btnUpdateStatus.setOnClickListener(v -> {
            if (!isCalculated[0]) {
                Toast.makeText(context, "Please calculate total first", Toast.LENGTH_SHORT).show();
                return;
            }

            updateStatus(request, holder, calculatedWeight[0], calculatedTotal[0]);
        });

        // If already completed, disable everything
        if ("Completed".equalsIgnoreCase(request.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#388E3C"));
            holder.btnUpdateStatus.setVisibility(View.GONE);
            holder.btnUpdateStatus.setEnabled(false);
            holder.btnCalculate.setEnabled(false);
            holder.etWeightInput.setEnabled(false);
            holder.etWeightInput.setVisibility(View.GONE);
            holder.btnCalculate.setVisibility(View.GONE);
            holder.tvFinalWeight.setVisibility(View.VISIBLE);
            holder.tvFinalWeight.setText("Weight: " + request.getWeight() + " kg");

        } else {
            // Ensure the fields are visible/enabled again if not completed
            holder.tvFinalWeight.setVisibility(View.GONE);
            holder.etWeightInput.setEnabled(true);
            holder.etWeightInput.setVisibility(View.VISIBLE);
            holder.btnCalculate.setEnabled(true);
            holder.btnUpdateStatus.setEnabled(true);
            holder.btnUpdateStatus.setText("Mark as Completed");
        }

    }


    @Override
    public int getItemCount() {
        return requestList.size();
    }

    private void updateStatus(UserRequest request, ViewHolder holder, double weight, double totalPrice) {
        SharedPrefManager spm = new SharedPrefManager(context.getApplicationContext());
        User user = spm.getUser();

        RequestItem api = ApiUtils.getRequestUser();
        UpdatedStatus updatedStatus = new UpdatedStatus("Completed", weight, totalPrice);

        Call <UserRequest> call = api.updateRequestItem( user.getToken(), request.getRequest_id(),"Completed", weight, totalPrice);

        call.enqueue(new Callback<UserRequest>() {
            @Override
            public void onResponse(Call<UserRequest> call, Response<UserRequest> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Status updated to Completed", Toast.LENGTH_SHORT).show();
                    holder.tvStatus.setText("Status: Completed");
                    holder.tvStatus.setTextColor(Color.parseColor("#388E3C"));
                    holder.btnUpdateStatus.setEnabled(false);
                    holder.btnUpdateStatus.setText("Completed");
                    request.setStatus("Completed");

                    Intent intent = new Intent(context, AdminActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // optional: clear back stack
                    context.startActivity(intent);

                } else {
                    Toast.makeText(context, "Update failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserRequest> call, Throwable t) {
                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
