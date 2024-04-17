package backend.proj5.bean;

import backend.proj5.dao.MessageDao;
import backend.proj5.dao.NotificationDao;
import backend.proj5.dao.UserDao;
import backend.proj5.dto.Message;
import backend.proj5.dto.Notification;
import backend.proj5.dto.User;
import backend.proj5.entity.MessageEntity;
import backend.proj5.entity.NotificationEntity;
import backend.proj5.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.io.Serializable;

@Stateless
public class MessageBean implements Serializable {
    @EJB
    private UserDao userDao;
    @EJB
    private UserBean userBean;
    @EJB
    private MessageDao messageDao;
    @EJB
    private NotificationDao notificationDao;
    @EJB
    private NotificationBean notificationBean;

    private static final long serialVersionUID = 1L;

    public MessageBean() {
    }

    public boolean sendMessage(String content, String sender, String receiver, String token) {
        boolean sent = false;

        User senderUser = userBean.getUser(sender, token);
        User receiverUser = userBean.getUser(receiver, token);

        if (senderUser != null && receiverUser != null) {
            Message message = new Message(content, senderUser, receiverUser);
            MessageEntity messageEntity = convertMessageDtoToEntity(message);
            messageDao.persist(messageEntity);

            Notification notification = new Notification(receiverUser, senderUser, message);
            NotificationEntity notificationEntity = notificationBean.convertNotificationDtoToEntity(messageEntity);
            notificationDao.persist(notificationEntity);

            sent = true;
        }

        return sent;
    }

    public void markMessageAsRead(Message message) {
        message.setRead(true);
    }

    public MessageEntity convertMessageDtoToEntity(Message message) {
        UserEntity sender = userDao.findUserByUsername(message.getSender().getUsername());
        UserEntity receiver = userDao.findUserByUsername(message.getReceiver().getUsername());
        return new MessageEntity(message.getContent(), sender, receiver, message.getTimestamp());
    }
}
