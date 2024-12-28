package com.example.convoai;

public class Message {
    public static String SENDER = "me";
    public static String RECEIVER = "Gemini";
    private String message;
    private String sender;

    public Message() {
    }

    public Message(String message, String sender) {
        this.message = message;
        this.sender = sender;
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

}
