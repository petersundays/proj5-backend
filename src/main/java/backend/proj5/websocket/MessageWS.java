package backend.proj5.websocket;

import backend.proj5.bean.MessageBean;
import backend.proj5.bean.UserBean;
import backend.proj5.dto.Message;
import backend.proj5.dto.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.json.Json;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

@Singleton
@ServerEndpoint("/websocket/messages/{token}/{receiver}")
public class MessageWS {
    //@EJB
    //private MessageBean messageBean;
    //@EJB
    //private UserBean userBean;

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
    public void toDoOnOpen(Session session, @PathParam("token") String token,@PathParam("receiver") String receiver){
        System.out.println("***************** open MessageWS");

       // if (userBean.isAuthenticated(token)) {
            sessions.put(token, session);
      /*      ArrayList<Message> messages = messageBean.getMessages(token, receiver);
            User user = userBean.getUserByToken(token);
            for (Message message : messages) {
                if (message.getReceiver().equals(user.getUsername()) && !message.isRead()) {
                    messageBean.markMessageAsRead(user.getUsername());
                }
                send(token, new Gson().toJson(message));
            }
        }*/
    }

    @OnClose
    public void toDoOnClose(Session session, CloseReason reason) {
        System.out.println("Websocket session is closed with CloseCode: " + reason.getCloseCode() + ": " + reason.getReasonPhrase());
        sessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

    @OnMessage
    public void toDoOnMessage(Session session, String msg){

        String token = session.getPathParameters().get("token");
    //    User userSender = userBean.getUserByToken(token);
    //    User userReceiver = userBean.getUserByUsername(session.getPathParameters().get("receiver"));
     //   String sender = userSender.getUsername();
      //  String receiver = userReceiver.getUsername();
        String content;

        Gson gson = new Gson();
        try {
            JsonObject jsonObject = gson.fromJson(msg, JsonObject.class);
            content = jsonObject.get("content").getAsString();
        } catch (JsonSyntaxException e) {
            System.out.println("Invalid JSON format: " + e.getMessage());
            return;
        }

     //   User user = userBean.getUserByUsername(receiver);
       /* String receiverToken = user.getToken();
        boolean receiverOnline = false;

        if (receiverToken != null) {
            for (String key : sessions.keySet()) {
                if (key.equals(receiverToken)) {
                    Session receiverSession = sessions.get(key);
                    String receiverUsername = receiverSession.getPathParameters().get("receiver");
                    if (receiverUsername.equals(sender)) {
                        receiverOnline = true;
                    }
                }
            }
        }

        if (messageBean.sendMessage(content, userSender, userReceiver, token, receiverOnline)) {

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
        }*/
    }
}