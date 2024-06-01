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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.LocalDateTime;
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

    private static final Logger logger = LogManager.getLogger(TaskBean.class);

    private static final long serialVersionUID = 1L;

    public MessageBean() {
    }

    public boolean sendMessage(String content, User sender, User receiver, String token, boolean receiverOnline) {
        boolean sent = false;
        if (sender != null && receiver != null) {
            logger.info("Sending message from: {} to: {}", sender.getUsername(), receiver.getUsername());
            Message message = new Message(content, sender.getUsername(), receiver.getUsername());
            MessageEntity messageEntity = convertMessageDtoToEntity(message);
            if (receiverOnline) {
                messageEntity.setRead(true);
                logger.info("Receiver is online, message marked as read");
            }
            messageDao.persist(messageEntity);

            if (!receiverOnline) {
                logger.info("Receiver is offline, creating notification");

                NotificationEntity notificationEntity = new NotificationEntity(messageEntity.getReceiver(), messageEntity.getSender(), messageEntity);
                notificationDao.persist(notificationEntity);
                String receiverToken = userDao.findUserByUsername(receiver.getUsername()).getToken();
                if (receiverToken != null) {
                    logger.info("Sending notification to receiver");
                    NotificationEntity completeNotificationEntity = notificationDao.findLatestNotificationForSender(sender.getUsername());
                    Notification notification = notificationBean.convertNotificationEntityToDto(completeNotificationEntity);
                    notifierWS.send(receiverToken, new Gson().toJson(notification));
                } else {
                    logger.error("Receiver token not found");
                }
            }

            userBean.updateLastAccess(sender.getUsername(), LocalDateTime.now());
            sent = true;
            logger.info("Message sent from: {} to: {}", sender.getUsername(), receiver.getUsername());
        } else {
            logger.error("Message not sent");
        }

        return sent;
    }

    public void markMessageAsRead(String username) {
        logger.info("Marking messages as read for: {}", username);
        ArrayList<MessageEntity> messages = messageDao.findMessagesUnreadForUser(username);

        for (MessageEntity message : messages) {
            message.setRead(true);
            messageDao.merge(message);

            logger.info("Message {} marked as read for: {}", message.getId(), username);
        }

    }

    public MessageEntity convertMessageDtoToEntity(Message message) {
        logger.info("Converting message DTO to entity");
        UserEntity sender = userDao.findUserByUsername(message.getSender());
        UserEntity receiver = userDao.findUserByUsername(message.getReceiver());
        return new MessageEntity(message.getContent(), sender, receiver, message.getTimestamp());
    }

    public Message convertMessageEntityToDto(MessageEntity messageEntity) {
        logger.info("Converting message entity to DTO");
        return new Message(messageEntity.getContent(), messageEntity.getSender().getUsername(), messageEntity.getReceiver().getUsername(), messageEntity.getTimestamp(), messageEntity.isRead());
    }

    public ArrayList<Message> getMessages(String token, String receiver) {
        logger.info("Getting messages for: {}", receiver);
        ArrayList<Message> messages = new ArrayList<>();
        User user = userBean.findUserByToken(token);

        if (user != null) {
            logger.info("User found: {}", user.getUsername());
            ArrayList<MessageEntity> messageEntities = messageDao.findMessagesBetweenUsers(user.getUsername(), receiver);
            if(messageEntities!=null) {
                logger.info("Messages found for: {}", receiver);
                for (MessageEntity messageEntity : messageEntities) {
                    messages.add(convertMessageEntityToDto(messageEntity));
                    logger.info("Message {} added to list", messageEntity.getId());
                }
            }
        }

        logger.info("Returning messages for: {}", receiver);
        return messages;
    }
}
