package com.example.myapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogManager {
    private static LogManager instance;
    private final List<String> logs = new ArrayList<>();

    private LogManager() {}

    public static synchronized LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    public void log(String message) {
        logs.add(message);
        if (logs.size() > 1000) { // Limit log size
            logs.remove(0);
        }
    }

    public List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    public void clearLogs() {
        logs.clear();
    }
}