package com.ostaxi.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.ostaxi.app.Model.PayModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import com.ostaxi.app.R;

public class PayoutAdapter  extends RecyclerView.Adapter<PayoutAdapter.viewHolders> {

    private List<PayModel> itemArrayList;

    public PayoutAdapter(List<PayModel> itemArrayList, Context context) {
        this.itemArrayList = itemArrayList;
    }

    @NotNull
    @Override
    public PayoutAdapter.viewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payout, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);


        return new viewHolders(layoutView);
    }


    @Override
    public void onBindViewHolder(final @NonNull viewHolders holder, int position) {

        holder.mDate.setText(String.valueOf(itemArrayList.get(position).getDate()));

        holder.mAmount.setText(itemArrayList.get(position).getAmount());
    }


    @Override
    public int getItemCount() {
        return this.itemArrayList.size();
    }


    static class viewHolders extends RecyclerView.ViewHolder {

        TextView  mAmount, mDate;
        viewHolders(View itemView) {
            super(itemView);
            mAmount = itemView.findViewById(R.id.amount_text);
            mDate = itemView.findViewById(R.id.date_text);
        }
    }
}