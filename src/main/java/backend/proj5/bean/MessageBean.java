package backend.proj5.bean;

import backend.proj5.dao.MessageDao;
import backend.proj5.dao.UserDao;
import backend.proj5.dto.Message;
import backend.proj5.dto.User;
import backend.proj5.entity.MessageEntity;
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

    private static final long serialVersionUID = 1L;

    public MessageBean() {
    }

    public boolean sendMessage(String content, String sender, String receiver) {
        boolean sent = false;

        User senderUser = userBean.getUser(sender);
        User receiverUser = userBean.getUser(receiver);

        if(senderUser != null && receiverUser != null) {
            Message message = new Message(content, senderUser, receiverUser);
            messageDao.persist(convertMessageDtoToEntity(message));
            sent = true;
        } else {
            sent = false;
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
