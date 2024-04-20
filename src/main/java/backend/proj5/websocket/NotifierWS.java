package backend.proj5.websocket;

import backend.proj5.bean.NotificationBean;
import backend.proj5.bean.UserBean;
import backend.proj5.dto.Notification;
import com.google.gson.Gson;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Singleton
@ServerEndpoint("/websocket/notifier/{token}")
public class NotifierWS {
    @EJB
    private UserBean userbean;
    @EJB
    NotificationBean notificationBean;

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

        if(userbean.isAuthenticated(token)) {
            System.out.println("A new WebSocket session is opened for client with token: " + token);
            if (userbean.isAuthenticated(token)) {
                sessions.put(token, session);
            }

            String username = userbean.findUserByToken(token).getUsername();
            ArrayList<Notification> notifications = notificationBean.findNotificationsForUser(username);

            if (notifications != null) {
                
                for (Notification notification : notifications) {
                    send(token, new Gson().toJson(notification));
                }
            }
        }
    }

    @OnClose
    public void toDoOnClose(Session session, CloseReason reason) {
        System.out.println("Websocket session is closed with CloseCode: " + reason.getCloseCode() + ": " + reason.getReasonPhrase());
        sessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

    @OnMessage
    public void toDoOnMessage(Session session, String msg){
        System.out.println("A new message is received: "+ msg);
        try {
            session.getBasicRemote().sendText("ack");
        } catch (IOException e) {
            System.out.println("Something went wrong!");
        }
    }
}