package backend.proj5.dao;

import backend.proj5.entity.MessageEntity;
import jakarta.ejb.Stateless;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class MessageDao extends AbstractDao<MessageEntity> {

    private static final long serialVersionUID = 1L;

    public MessageDao() {
        super(MessageEntity.class);
    }

    public MessageEntity findMessageById(String id) {
        try {
            return (MessageEntity) em.createNamedQuery("Message.findMessageById").setParameter("id", id)
                    .getSingleResult();

        } catch (Exception e) {
            return null;
        }
    }

    public List<MessageEntity> findMessagesBetweenUsers(String sender, String receiver) {
        try {
            return em.createNamedQuery("Message.findMessagesBetweenUsers")
                    .setParameter("sender", sender).setParameter("receiver", receiver).getResultList();

        } catch (Exception e) {
            return null;
        }
    }
}