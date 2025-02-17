package com.example.androidtraining1;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ExchangeAdapter extends RecyclerView.Adapter<ExchangeAdapter.ViewHolder> {
    private List<String> mList = new ArrayList<>();
    private LayoutInflater mInflater;
    private CurrencyService mService;
    private String baseCurrency;
    private BigDecimal baseAmount;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView item_exchange_type;
        TextView item_exchange_value;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item_exchange_type = itemView.findViewById(R.id.item_exchange_type);
            item_exchange_value = itemView.findViewById(R.id.item_exchange_value);
        }
    }

    public ExchangeAdapter(LayoutInflater inflater, CurrencyService service){
        mService = service;
        mInflater = inflater;
        mList.clear();
        mList.addAll(service.getCurrencyList());
    }

    public void updateBaseCurrencyAndAmount(String base, BigDecimal amount){
        baseCurrency = base;
        baseAmount = amount;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_exchange, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String targetCurrency = mList.get(position);
        holder.item_exchange_type.setText(targetCurrency);
        BigDecimal value = mService.exchange(baseAmount, baseCurrency, targetCurrency);
        holder.item_exchange_value.setText(value.toString());    // * convert to String for passing the value (orig: BigDecimal) //
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
