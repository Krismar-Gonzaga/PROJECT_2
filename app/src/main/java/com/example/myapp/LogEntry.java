package com.example.myapp;

public class LogEntry {
    private String message;
    private long timestamp;

    public LogEntry(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public LogEntry(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}