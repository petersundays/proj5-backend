package backend.proj5.bean;

import backend.proj5.dao.UserDao;
import backend.proj5.dto.Message;
import backend.proj5.dto.Notification;
import backend.proj5.entity.MessageEntity;
import backend.proj5.entity.NotificationEntity;
import backend.proj5.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class NotificationBean {
    @Inject
    private UserDao userDao;

    private static final long serialVersionUID = 1L;

    public NotificationBean() {
    }

    public void markNotificationAsRead(Notification notification) {
        notification.setRead(true);
    }

    public NotificationEntity convertNotificationDtoToEntity(MessageEntity messageEntity) {
        return new NotificationEntity(messageEntity.getReceiver(), messageEntity.getSender(), messageEntity);
    }
}
