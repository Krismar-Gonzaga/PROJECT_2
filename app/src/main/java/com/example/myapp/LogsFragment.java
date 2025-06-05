package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LogsFragment extends Fragment {
    private OnIndecatorUpdate UpdateIndecator;
    private RecyclerView recyclerView;
    private Button btnExportLogs;
    private LogsAdapter logsAdapter;

    public LogsFragment(OnIndecatorUpdate updateIndecator) {
        // Required empty public constructor
        this.UpdateIndecator = updateIndecator;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logs, container, false);
        UpdateIndecator.Update_indecator();
        recyclerView = view.findViewById(R.id.recycler_view_logs);
        btnExportLogs = view.findViewById(R.id.btn_export_logs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initialize adapter once
        logsAdapter = new LogsAdapter(LogManager.getInstance(getContext()).getLogs());
        recyclerView.setAdapter(logsAdapter);

        btnExportLogs.setText("Refresh Logs");
        btnExportLogs.setOnClickListener(v -> {
            // Update the existing adapter's data instead of creating a new instance
            logsAdapter.updateLogs(LogManager.getInstance(getContext()).getLogs());
            logsAdapter.notifyDataSetChanged();
        });

        return view;
    }
}
