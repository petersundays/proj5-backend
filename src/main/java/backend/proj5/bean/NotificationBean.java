package backend.proj5.bean;

import backend.proj5.dao.NotificationDao;
import backend.proj5.dto.Message;
import backend.proj5.dto.Notification;
import backend.proj5.dto.User;
import backend.proj5.entity.MessageEntity;
import backend.proj5.entity.NotificationEntity;
import backend.proj5.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.io.Serializable;
import java.util.ArrayList;

@Stateless
public class NotificationBean implements Serializable {
    @EJB
    private UserBean userBean;
    @EJB
    private MessageBean messageBean;
    @EJB
    NotificationDao notificationDao;


    private static final long serialVersionUID = 1L;

    public NotificationBean() {
    }

    public void markNotificationAsRead(String sender, String receiver) {
        ArrayList<NotificationEntity> notifications = notificationDao.findNotificationBySenderAndReceiver(sender, receiver);
        for (NotificationEntity notification : notifications) {
            notification.setRead(true);
            notificationDao.merge(notification);
        }
    }

    public Notification convertNotificationEntityToDto(NotificationEntity notificationEntity) {
        User receiver = userBean.getUserByUsername(notificationEntity.getReceiver().getUsername());
        User sender = userBean.getUserByUsername(notificationEntity.getSender().getUsername());
        Message message = messageBean.convertMessageEntityToDto(notificationEntity.getMessage());
        Notification notification = new Notification(receiver.getUsername(), sender.getUsername(), message);
        notification.setTimestamp(notificationEntity.getTimestamp());
        notification.setRead(notificationEntity.isRead());
        return notification;
    }

    public NotificationEntity convertNotificationDtoToEntity(Notification notification) {
        User receiverDto = userBean.getUserByUsername(notification.getReceiver());
        User senderDto = userBean.getUserByUsername(notification.getSender());
        UserEntity receiver = userBean.convertUserDtotoUserEntity(receiverDto);
        UserEntity sender = userBean.convertUserDtotoUserEntity(senderDto);
        MessageEntity message = messageBean.convertMessageDtoToEntity(notification.getMessage());
        return new NotificationEntity(receiver, sender, message);
    }

    public ArrayList<Notification> findNotificationsForUser(String username) {
        ArrayList<NotificationEntity> notificationEntities = notificationDao.findNotificationsForUser(username);
        ArrayList<Notification> notifications = new ArrayList<>();

        if (notificationEntities != null) {
            for (NotificationEntity notificationEntity : notificationEntities) {
                notifications.add(convertNotificationEntityToDto(notificationEntity));
            }
        }
        return notifications;
    }

    public ArrayList<Notification> findUnreadNotificationsForUser(String username) {
        ArrayList<NotificationEntity> notificationEntities = notificationDao.findUnreadNotificationsForUser(username);
        ArrayList<Notification> notifications = new ArrayList<>();

        if (notificationEntities != null) {
            for (NotificationEntity notificationEntity : notificationEntities) {
                notifications.add(convertNotificationEntityToDto(notificationEntity));
            }
        }
        return notifications;
    }

}
