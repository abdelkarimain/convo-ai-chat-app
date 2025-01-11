package io.github.abdelkarimain.auth;

public class Message {
    public static String SENDER = "me";
    public static String RECEIVER = "Gemini";
    private String message;
    private String sender;
    private String chatId;
    private long timestamp;

    public Message() {
        this.timestamp = System.currentTimeMillis();
    }

    public Message(String message, String sender) {
        this.message = message;
        this.sender = sender;
        this.timestamp = System.currentTimeMillis();
    }

    public Message(String message, String sender, String chatId) {
        this.message = message;
        this.sender = sender;
        this.chatId = chatId;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}