package backend.proj5.websocket;

import backend.proj5.bean.MessageBean;
import backend.proj5.dao.UserDao;
import backend.proj5.dto.Message;
import backend.proj5.entity.UserEntity;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

@Singleton
@ServerEndpoint("/websocket/messages/{token}")
public class MessageWS {
    @EJB
    private MessageBean messageBean;
    @EJB
    private UserDao userDao;

    private final HashMap<String, Session> sessions = new HashMap<String, Session>();
    public void send(String token, String msg){
        Session session = sessions.get(token);
        if (session != null){
            System.out.println("sending.......... "+msg);
            try {
                session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                System.out.println("Something went wrong!");
            }
        }
    }
    @OnOpen
    public void toDoOnOpen(Session session, @PathParam("token") String token){
        System.out.println("A new WebSocket session is opened for client with token: "+ token);
        sessions.put(token,session);
    }

    @OnClose
    public void toDoOnClose(Session session, CloseReason reason) {
        System.out.println("Websocket session is closed with CloseCode: " + reason.getCloseCode() + ": " + reason.getReasonPhrase());
        for (String key : sessions.keySet()) {
            if (sessions.get(key) == session) {
                sessions.remove(key);
            }
        }
    }

    @OnMessage
    public void toDoOnMessage(Session session, String msg){
        System.out.println("Received message: "+msg);

        String token = session.getPathParameters().get("token");
        String sender;
        String receiver;
        String content;

        try {
            JsonObject jsonObject = Json.createReader(new StringReader(msg)).readObject();
            sender = jsonObject.getString("sender");
            receiver = jsonObject.getString("receiver");
            content = jsonObject.getString("content");
        } catch (JsonSyntaxException e) {
            System.out.println("Invalid JSON format" + e.getMessage());
            return;
        }

        UserEntity receiverEntity = userDao.findUserByUsername(receiver);
        String receiverToken = receiverEntity.getToken();
        boolean receiverOnline = false;

        if (receiverToken != null) {
            for (String key : sessions.keySet()) {
                if (key.equals(receiverToken)) {
                    receiverOnline = true;
                }
            }
        }

        if (messageBean.sendMessage(content, sender, receiver, token, receiverOnline)) {
            System.out.println("Message sent successfully");

            if (receiverOnline) {
                send(receiverToken, new Gson().toJson(new Message(content, sender, receiver)));
            }

            for (String key : sessions.keySet()) {
                if (key.equals(token)) {
                    send(token, new Gson().toJson(new Message(content, sender, receiver)));
                }
            }

        } else {
            System.out.println("Message not sent");
        }
    }
}