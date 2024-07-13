package backend.proj5.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name="message")
@NamedQuery(name="Message.findMessagesBetweenUsers", query="SELECT m FROM MessageEntity m WHERE m.sender.username = :sender AND m.receiver.username = :receiver OR m.sender.username = :receiver AND m.receiver.username = :sender ORDER BY m.timestamp ASC")
@NamedQuery(name="Message.findMessageById", query="SELECT m FROM MessageEntity m WHERE m.id = :id")
@NamedQuery(name="Message.findMessagesUnreadForUser", query="SELECT m FROM MessageEntity m WHERE m.receiver.username = :username AND m.read = false")

public class MessageEntity implements Serializable {

        private static final long serialVersionUID = 1L;

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column (name="id", nullable = false, unique = true, updatable = false)
        private int id;

        @Column (name="content", nullable = false, length = 20000, columnDefinition = "TEXT")
        private String content;

        @ManyToOne
        @JoinColumn(name = "sender_id", referencedColumnName = "username")
        private UserEntity sender;

        @ManyToOne
        @JoinColumn(name = "receiver_id", referencedColumnName = "username")
        private UserEntity receiver;

        @Column (name="timestamp", nullable = false, updatable = false)
        private Timestamp timestamp;

        @Column (name="'read'", nullable = false)
        private boolean read;

        @OneToOne(mappedBy = "message")
        private NotificationEntity notification;

        public MessageEntity() {
        }

        public MessageEntity(String content, UserEntity sender, UserEntity receiver, Timestamp timestamp) {
            this.content = content;
            this.sender = sender;
            this.receiver = receiver;
            this.timestamp = timestamp;
            this.read = false;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public UserEntity getSender() {
            return sender;
        }

        public void setSender(UserEntity sender) {
            this.sender = sender;
        }

        public UserEntity getReceiver() {
            return receiver;
        }

        public void setReceiver(UserEntity receiver) {
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

        public NotificationEntity getNotification() {
            return notification;
        }

        public void setNotification(NotificationEntity notification) {
            this.notification = notification;
        }

}
