package backend.proj5.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name="notification")

@NamedQuery(name="Notification.findNotificationById", query="SELECT n FROM NotificationEntity n WHERE n.id = :id")
@NamedQuery(name="Notification.findNotificationsForUser", query="SELECT n FROM NotificationEntity n WHERE n.receiver.username = :username")


public class NotificationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name="id", nullable = false, unique = true, updatable = false)
    private int id;

    @OneToOne
    @JoinColumn(name = "message_id", referencedColumnName = "id")
    private MessageEntity message;

    @ManyToOne
    @JoinColumn(name = "receiver_id", referencedColumnName = "username")
    private UserEntity receiver;

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "username")
    private UserEntity sender;

    @Column (name="timestamp", nullable = false, unique = false, updatable = false)
    private Timestamp timestamp;

    @Column (name="'read'", nullable = false, unique = false)
    private boolean read;

    public NotificationEntity() {
    }

    public NotificationEntity(UserEntity receiver, UserEntity sender, MessageEntity message) {
        this.receiver = receiver;
        this.sender = sender;
        this.read = false;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MessageEntity getMessage() {
        return message;
    }

    public void setMessage(MessageEntity message) {
        this.message = message;
    }

    public UserEntity getReceiver() {
        return receiver;
    }

    public void setReceiver(UserEntity receiver) {
        this.receiver = receiver;
    }

    public UserEntity getSender() {
        return sender;
    }

    public void setSender(UserEntity sender) {
        this.sender = sender;
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