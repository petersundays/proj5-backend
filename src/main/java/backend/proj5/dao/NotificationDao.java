package backend.proj5.dao;

import backend.proj5.entity.NotificationEntity;
import jakarta.ejb.Stateless;

@Stateless
public class NotificationDao extends AbstractDao<NotificationEntity> {

    private static final long serialVersionUID = 1L;

    public NotificationDao() {
        super(NotificationEntity.class);
    }

    public NotificationEntity findNotificationById(String id) {
        try {
            return (NotificationEntity) em.createNamedQuery("Notification.findNotificationById").setParameter("id", id)
                    .getSingleResult();

        } catch (Exception e) {
            return null;
        }
    }

    public NotificationEntity findNotificationsForUser(String username) {
        try {
            return (NotificationEntity) em.createNamedQuery("Notification.findNotificationsForUser").setParameter("username", username)
                    .getSingleResult();

        } catch (Exception e) {
            return null;
        }
    }
}