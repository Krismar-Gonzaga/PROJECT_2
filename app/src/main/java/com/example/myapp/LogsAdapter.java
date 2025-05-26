package com.example.myapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {
    private List<String> logs;

    public LogsAdapter(List<String> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        holder.logText.setText(logs.get(position));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView logText;
        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            logText = itemView.findViewById(android.R.id.text1);
        }
    }
}