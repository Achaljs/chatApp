package com.example.chatApp.model;

import com.google.firebase.Timestamp;

public class ChatMessageModel {
    private String message;
    private String senderId;
    private String fileUrl; // Add this field to store the download URL of the file
    private Timestamp timestamp;

    public ChatMessageModel() {
    }

    public ChatMessageModel(String message, String senderId, String fileUrl, Timestamp timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.fileUrl = fileUrl;
        this.timestamp = timestamp;
    }

    // Include getters and setters for fileUrl

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}