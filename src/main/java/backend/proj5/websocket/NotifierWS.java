package backend.proj5.websocket;

import backend.proj5.bean.NotificationBean;
import backend.proj5.bean.UserBean;
import backend.proj5.dto.Notification;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

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

            try {
                session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                System.out.println("Something went wrong!");
            }
        }
    }
    @OnOpen
    public void toDoOnOpen(Session session, @PathParam("token") String token){

        if (sessions.containsKey(token)) {
            return;
        }

        if(userbean.isAuthenticated(token)) {
            System.out.println("A new WebSocket session is opened for client with token: " + token);

            sessions.put(token, session);

            String username = userbean.findUserByToken(token).getUsername();
            ArrayList<Notification> notifications = notificationBean.findUnreadNotificationsForUser(username);

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
    public void toDoOnMessage(Session session, String msg) {
        System.out.println("A new message is received: " + msg);
        String receiverToken = session.getPathParameters().get("token");
        String receiver = userbean.findUserByToken(receiverToken).getUsername();
        String sender = "";

        try {
            JsonObject jsonObject = Json.createReader(new StringReader(msg)).readObject();
            sender = jsonObject.getString("sender");
        } catch (JsonException e) {
            System.out.println("Invalid JSON format: " + e.getMessage());
            return;
        }

        notificationBean.markNotificationAsRead(sender, receiver);

        // Retrieve updated notifications for the user
        ArrayList<Notification> updatedNotifications = notificationBean.findUnreadNotificationsForUser(receiver);

        // Send updated notifications back to the client
        if (sessions.containsKey(receiverToken) && updatedNotifications != null) {
            send(receiverToken, new Gson().toJson(updatedNotifications));
        }
    }
}