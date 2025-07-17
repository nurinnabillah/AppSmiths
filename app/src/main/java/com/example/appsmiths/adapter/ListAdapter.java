package com.example.appsmiths.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appsmiths.R;
import com.example.appsmiths.model.RecyclableItem;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    /**
     * Create ViewHolder class to bind list item view
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public TextView tvID;
        public TextView tvItemName;
        public TextView tvPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            tvID = itemView.findViewById(R.id.tvID);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvPrice = itemView.findViewById(R.id.tvPrice);

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            currentPos = getAdapterPosition();
            return false;
        }

        @Override
        public boolean onLongClickUseDefaultHapticFeedback(@NonNull View v) {
            return View.OnLongClickListener.super.onLongClickUseDefaultHapticFeedback(v);
        }
    } // close ViewHolder class

    /// ///////////////////////////////////////////////////////////////////
    // adapter class definitions

    private List<RecyclableItem> itemListData;   // list of book objects
    private Context mContext;       // activity context
    private int currentPos;

    public ListAdapter(Context context, List<RecyclableItem> listData) {
        itemListData = listData;
        mContext = context;
    }

    private Context getmContext() {
        return mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate layout using the single item layout
        View view = inflater.inflate(R.layout.item_list, parent, false);
        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @NonNull

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // bind data to the view holder instance
        RecyclableItem m = itemListData.get(position);
        holder.tvID.setText(String.valueOf(m.getItem_id()));
        holder.tvItemName.setText(m.getItem_name());
        holder.tvPrice.setText("RM " + String.format("%.2f", m.getPrice_per_kg()));
    }

    @Override
    public int getItemCount() {
        return itemListData.size();
    }

    /**
     * return book object for currently selected book (index already set by long press in viewholder)
     *
     * @return
     */
    public RecyclableItem getSelectedItem() {
        // return the book record if the current selected position/index is valid
        if (currentPos >= 0 && itemListData != null && currentPos < itemListData.size()) {
            return itemListData.get(currentPos);
        }
        return null;
    }
}