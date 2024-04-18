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
import java.util.ArrayList;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

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

    public boolean sendMessage(String content, String sender, String receiver, String token, boolean receiverOnline) {
        boolean sent = false;

        User senderUser = userBean.getUser(sender, token);
        User receiverUser = userBean.getUser(receiver, token);

        if (senderUser != null && receiverUser != null) {
            Message message = new Message(content, sender, receiver);
            MessageEntity messageEntity = convertMessageDtoToEntity(message);
            messageDao.persist(messageEntity);

            if (!receiverOnline) {

                NotificationEntity notificationEntity = notificationBean.convertNotificationDtoToEntity(messageEntity);
                notificationDao.persist(notificationEntity);
            }

            sent = true;
        }

        return sent;
    }

    public void markMessageAsRead(Message message) {
        message.setRead(true);
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
            List<MessageEntity> messageEntities = messageDao.findMessagesBetweenUsers(user.getUsername(), receiver);
            if(messageEntities!=null)
                for (MessageEntity messageEntity : messageEntities) {
                    messages.add(convertMessageEntityToDto(messageEntity));
                }
        }
        return messages;
    }
}
