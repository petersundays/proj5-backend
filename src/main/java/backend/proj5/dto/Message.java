package backend.proj5.dto;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.sql.Timestamp;

@XmlRootElement
public class Message {
    private String content;
    private String sender;
    private String  receiver;
    private Timestamp timestamp;
    private boolean read;

    public Message() {
    }

    public Message(String content, String sender, String receiver) {
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.read = false;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

}
