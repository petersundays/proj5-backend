package backend.proj5.websocket;

import backend.proj5.bean.UserBean;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;

@Singleton
@ServerEndpoint("/websocket/messages/{token}")
public class TaskWS {
    @EJB
    private UserBean userBean;

    private final HashMap<String, Session> sessions = new HashMap<String, Session>();
    public void send(String msg){
        try {
            for (Session session : sessions.values()) {
                if (session.isOpen())
                    session.getBasicRemote().sendText(msg);
            }
        } catch (IOException e) {
            System.out.println("Something went wrong!");
        }

    }
    @OnOpen
    public void toDoOnOpen(Session session, @PathParam("token") String token,@PathParam("receiver") String receiver){

        if (userBean.isAuthenticated(token)) {
            sessions.put(token, session);
        }
    }

    @OnClose
    public void toDoOnClose(Session session, CloseReason reason) {
        System.out.println("Websocket tasks is closed with CloseCode: " + reason.getCloseCode() + ": " + reason.getReasonPhrase());
        sessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

    @OnMessage
    public void toDoOnMessage(Session session, String msg){
        System.out.println("Message received: " + msg);
    }
}