package com.example.myapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {
    private List<LogEntry> logs;
    private final SimpleDateFormat dateFormat;

    public LogsAdapter() {
        this(new ArrayList<>());
    }

    public LogsAdapter(List<LogEntry> logs) {
        this.logs = logs != null ? logs : new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lo, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        if (position < 0 || position >= logs.size()) return;
        
        LogEntry log = logs.get(position);
        if (log == null) return;

        holder.logText.setText(log.getMessage() != null ? log.getMessage() : "");
        
        try {
            String formattedDate = dateFormat.format(new Date(log.getTimestamp()));
            holder.timestamp.setText(formattedDate);
        } catch (Exception e) {
            holder.timestamp.setText("Invalid date");
        }
    }

    @Override
    public int getItemCount() {
        return logs != null ? logs.size() : 0;
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        final TextView logText;
        final TextView timestamp;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            logText = itemView.findViewById(R.id.tvLogText);
            timestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }

    public void updateLogs(List<LogEntry> newLogs) {
        this.logs = newLogs != null ? newLogs : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addLog(LogEntry log) {
        if (log != null) {
            logs.add(log);
            notifyItemInserted(logs.size() - 1);
        }
    }

    public void clearLogs() {
        int size = logs.size();
        logs.clear();
        notifyItemRangeRemoved(0, size);
    }
}