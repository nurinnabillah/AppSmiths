package com.example.appsmiths.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.example.appsmiths.R;
import com.example.appsmiths.model.UserRequest;

import java.util.List;

public class UserRequestAdapter extends RecyclerView.Adapter<UserRequestAdapter.ViewHolder> {

    // Adapter-scoped fields
    private List<UserRequest> requestListData;   // list of request objects
    private Context mContext;                // activity context
    private int currentPos = -1;             // currently long-pressed item position

    // Constructor
    public UserRequestAdapter(Context context, List<UserRequest> listData) {
        this.mContext = context;
        this.requestListData = listData;
    }

    private Context getmContext() {
        return mContext;
    }

    // ViewHolder class
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        public TextView tvItemId, tvAddress, tvRequestDate, tvStatus, tvWeight, tvPrice, tvNotes;

        public ViewHolder(View itemView) {
            super(itemView);

            tvItemId = itemView.findViewById(R.id.tvItemId);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvRequestDate = itemView.findViewById(R.id.tvRequestDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvNotes = itemView.findViewById(R.id.tvNotes);

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            currentPos = getAdapterPosition();
            return false; // allow context menu to show
        }
    }

    @Override
    public UserRequestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_request_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserRequestAdapter.ViewHolder holder, int position) {
        if (requestListData == null || position >= requestListData.size()) return;

        UserRequest m = requestListData.get(position);
        if (m.getItem() != null) {
            holder.tvItemId.setText("Item: " + m.getItem().getItem_name());
        } else {
            holder.tvItemId.setText("Item: Unknown");
        }

        holder.tvAddress.setText("Address: " + m.getAddress());

        holder.tvStatus.setText("Status: " + m.getStatus());

        if ("Pending".equalsIgnoreCase(m.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#FF5722")); // red/orange
        } else if ("Completed".equalsIgnoreCase(m.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // green
        } else {
            holder.tvStatus.setTextColor(Color.BLACK); // default/fallback color
        }

        if (m.getTotal_price() != null) {
            holder.tvPrice.setText("Price: RM " + m.getTotal_price());
        } else {
            holder.tvPrice.setText("Price: To be calculated");
        }

        // Hide unused fields
        holder.tvRequestDate.setVisibility(View.GONE);
        holder.tvWeight.setVisibility(View.GONE);
        holder.tvNotes.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return (requestListData != null) ? requestListData.size() : 0;
    }

    /**
     * Return the currently selected request item (based on long press).
     */
    public UserRequest getSelectedItem() {
        if (currentPos >= 0 && requestListData != null && currentPos < requestListData.size()) {
            return requestListData.get(currentPos);
        }
        return null;
    }

    /**
     * Optional: expose current position
     */
    public int getCurrentPos() {
        return currentPos;
    }
}
