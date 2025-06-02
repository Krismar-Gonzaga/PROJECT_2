package com.example.myapp;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.io.IOException;

public class LogManager {
    private static LogManager instance;
    private static final String LOG_FILE_NAME = "logs.txt";
    private Context context;

    private LogManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized LogManager getInstance(Context context) {
        if (instance == null) {
            instance = new LogManager(context);
        }
        return instance;
    }

    public void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = "[" + timestamp + "] " + message;
        try (FileOutputStream fos = context.openFileOutput(LOG_FILE_NAME, Context.MODE_APPEND)) {
            fos.write((logEntry + "\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<LogEntry> getLogs() {
        List<LogEntry> logs = new ArrayList<>();
        try (FileInputStream fis = context.openFileInput(LOG_FILE_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Parse the timestamp from the log entry
                if (line.length() > 22 && line.startsWith("[") && line.contains("]")) {
                    String timestampStr = line.substring(1, 20); // Extract "yyyy-MM-dd HH:mm:ss"
                    String message = line.substring(line.indexOf("]") + 2); // Get message after "] "
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = sdf.parse(timestampStr);
                        logs.add(new LogEntry(message, date.getTime()));
                    } catch (Exception e) {
                        // If parsing fails, create entry with current timestamp
                        logs.add(new LogEntry(line));
                    }
                } else {
                    // If the line doesn't match expected format, create entry with current timestamp
                    logs.add(new LogEntry(line));
                }
            }
        } catch (IOException e) {
            // File may not exist yet, that's fine
        }
        return Collections.unmodifiableList(logs);
    }

    public void clearLogs() {
        try (FileOutputStream fos = context.openFileOutput(LOG_FILE_NAME, Context.MODE_PRIVATE)) {
            // Overwrite with nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}