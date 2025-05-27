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

    public List<String> getLogs() {
        List<String> logs = new ArrayList<>();
        try (FileInputStream fis = context.openFileInput(LOG_FILE_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logs.add(line);
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