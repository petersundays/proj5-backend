package backend.proj5.dto;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.sql.Timestamp;

@XmlRootElement
public class Notification {
    private boolean read;
    private Timestamp timestamp;
    private User receiver;
    private User sender;
    private Message message;

    public Notification() {
    }

    public Notification(User receiver, User sender, Message message) {
        this.receiver = receiver;
        this.sender = sender;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.read = false;
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}