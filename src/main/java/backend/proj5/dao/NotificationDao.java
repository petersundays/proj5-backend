package backend.proj5.dao;

import backend.proj5.entity.NotificationEntity;
import jakarta.ejb.Stateless;

import java.util.ArrayList;

@Stateless
public class NotificationDao extends AbstractDao<NotificationEntity> {

    private static final long serialVersionUID = 1L;

    public NotificationDao() {
        super(NotificationEntity.class);
    }

    public ArrayList<NotificationEntity> findNotificationById(String id) {
        try {

            return (ArrayList<NotificationEntity>) em.createNamedQuery("Notification.findNotificationById").setParameter("id", id)
                    .getResultList();

        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<NotificationEntity> findNotificationsForUser(String username) {
        try {
            return (ArrayList<NotificationEntity>) em.createNamedQuery("Notification.findNotificationsForUser").setParameter("username", username)
                    .getResultList();

        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<NotificationEntity> findNotificationBySenderAndReceiver(String sender, String receiver) {
        try {
            return (ArrayList<NotificationEntity>) em.createNamedQuery("Notification.findNotificationBySenderAndReceiver")
                    .setParameter("sender", sender)
                    .setParameter("receiver", receiver)
                    .getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public NotificationEntity findLatestNotificationForSender(String username) {
        try {
            return (NotificationEntity) em.createNamedQuery("Notification.findLatestNotificationForSender")
                    .setParameter("username", username)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<NotificationEntity> findUnreadNotificationsForUser(String username) {
        try {
            return (ArrayList<NotificationEntity>) em.createNamedQuery("Notification.findUnreadNotificationsForUser")
                    .setParameter("username", username)
                    .getResultList();
        } catch (Exception e) {
            return null;
        }
    }
}