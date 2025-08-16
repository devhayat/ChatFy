package com.example.wordwave;

public class MessageModel {

    private String content;   // Message text or image URL
    private String senderId;  // UID of sender
    private long timeStamp;   // Time in millis
    private String type;      // "text" or "image"

    // Empty constructor required for Firebase
    public MessageModel() {
        this.type = "text"; // Default to text if not set
    }

    // Full constructor
    public MessageModel(String content, String senderId, long timeStamp, String type) {
        this.content = content;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
        this.type = (type != null && !type.isEmpty()) ? type : "text";
    }

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getType() {
        return (type != null && !type.isEmpty()) ? type : "text";
    }

    public void setType(String type) {
        this.type = (type != null && !type.isEmpty()) ? type : "text";
    }
}
