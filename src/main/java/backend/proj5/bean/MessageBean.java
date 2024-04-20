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
import backend.proj5.websocket.NotifierWS;
import com.google.gson.Gson;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.ArrayList;

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
    @EJB
    private NotifierWS notifierWS;

    private static final long serialVersionUID = 1L;

    public MessageBean() {
    }

    public boolean sendMessage(String content, User sender, User receiver, String token, boolean receiverOnline) {
        boolean sent = false;

        if (sender != null && receiver != null) {
            Message message = new Message(content, sender.getUsername(), receiver.getUsername());
            MessageEntity messageEntity = convertMessageDtoToEntity(message);
            if (receiverOnline) {
                messageEntity.setRead(true);
            }
            messageDao.persist(messageEntity);

            if (!receiverOnline) {

                NotificationEntity notificationEntity = new NotificationEntity(messageEntity.getReceiver(), messageEntity.getSender(), messageEntity);
                notificationDao.persist(notificationEntity);
                String receiverToken = userDao.findUserByUsername(receiver.getUsername()).getToken();
                if (receiverToken != null) {
                    NotificationEntity completeNotificationEntity = notificationDao.findLatestNotificationForSender(sender.getUsername());
                    Notification notification = notificationBean.convertNotificationEntityToDto(completeNotificationEntity);
                    notifierWS.send(receiverToken, new Gson().toJson(notification));
                }
            }

            sent = true;
        }

        return sent;
    }

    public void markMessageAsRead(String username) {
        ArrayList<MessageEntity> messages = messageDao.findMessagesUnreadForUser(username);
        for (MessageEntity message : messages) {
            message.setRead(true);
            messageDao.merge(message);
        }
    }

    public MessageEntity convertMessageDtoToEntity(Message message) {
        UserEntity sender = userDao.findUserByUsername(message.getSender());
        UserEntity receiver = userDao.findUserByUsername(message.getReceiver());
        return new MessageEntity(message.getContent(), sender, receiver, message.getTimestamp());
    }

    public Message convertMessageEntityToDto(MessageEntity messageEntity) {
        return new Message(messageEntity.getContent(), messageEntity.getSender().getUsername(), messageEntity.getReceiver().getUsername());
    }

    public ArrayList<Message> getMessages(String token, String receiver) {
        ArrayList<Message> messages = new ArrayList<>();
        User user = userBean.findUserByToken(token);

        if (user != null) {
            ArrayList<MessageEntity> messageEntities = messageDao.findMessagesBetweenUsers(user.getUsername(), receiver);
            if(messageEntities!=null)
                for (MessageEntity messageEntity : messageEntities) {
                    messages.add(convertMessageEntityToDto(messageEntity));
                }
        }
        return messages;
    }
}
