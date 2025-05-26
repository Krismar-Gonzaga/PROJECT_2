package com.example.myapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class LogsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Button btnExportLogs;
    private LogsAdapter logsAdapter;
    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        recyclerView = findViewById(R.id.recycler_view_logs);
        btnExportLogs = findViewById(R.id.btn_export_logs);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        logsAdapter = new LogsAdapter(LogManager.getInstance().getLogs());
        recyclerView.setAdapter(logsAdapter);

        btnExportLogs.setText("Refresh Logs");
        btnExportLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logsAdapter = new LogsAdapter(LogManager.getInstance().getLogs());
                recyclerView.setAdapter(logsAdapter);
            }
        });
    }

    private void exportLogsToFile() {
        List<String> logs = LogManager.getInstance().getLogs();
        StringBuilder sb = new StringBuilder();
        for (String log : logs) {
            sb.append(log).append("\n");
        }
        String fileName = "app_logs.txt";
        File file = new File(Environment.getExternalStorageDirectory(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(sb.toString().getBytes());
            Toast.makeText(this, "Logs exported to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to export logs: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportLogsToFile();
            } else {
                Toast.makeText(this, "Permission denied to write to storage", Toast.LENGTH_SHORT).show();
            }
        }
    }
}