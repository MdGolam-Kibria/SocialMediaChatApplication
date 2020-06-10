package com.example.chatapplication.modelAll;

import com.google.firebase.database.PropertyName;

public class ModelChat {
    String message, receiver, sender;
    //            ,timestamp;
    boolean isSeen;

    public ModelChat() {
    }

    public ModelChat(String message, String receiver, String sender, boolean isSeen) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.isSeen = isSeen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @PropertyName("isSeen")
    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    @Override
    public String toString() {
        return "ModelChat{" +
                "message='" + message + '\'' +
                ", receiver='" + receiver + '\'' +
                ", sender='" + sender + '\'' +
                ", isSeen=" + isSeen +
                '}';
    }
}
