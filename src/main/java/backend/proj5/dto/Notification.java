package backend.proj5.dto;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.sql.Timestamp;

@XmlRootElement
public class Notification {
    private boolean read;
    private Timestamp timestamp;
    private String receiver;
    private String sender;
    private Message message;

    public Notification() {
    }

    public Notification(String receiver, String sender, Message message) {
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

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

}