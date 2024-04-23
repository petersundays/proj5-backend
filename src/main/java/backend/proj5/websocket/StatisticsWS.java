package backend.proj5.websocket;

import backend.proj5.bean.CategoryBean;
import backend.proj5.bean.MessageBean;
import backend.proj5.bean.TaskBean;
import backend.proj5.bean.UserBean;
import backend.proj5.dto.Statistics;
import com.google.gson.Gson;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;

import java.util.HashMap;

@Singleton
@ServerEndpoint("/websocket/stats/{token}")

public class StatisticsWS {
    @EJB
    private MessageBean messageBean;
    @EJB
    private UserBean userBean;
    @EJB
    private TaskBean taskBean;
    @EJB
    private CategoryBean categoryBean;

    private final HashMap<String, Session> sessions = new HashMap<String, Session>();
    public void send(String token, String msg){
        Session session = sessions.get(token);
        if (session != null){
            try {
                System.out.println("Sending message to client : " + msg);
                session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                System.out.println("Something went wrong!");
            }
        }
    }
    @OnOpen
    public void toDoOnOpen(Session session, @PathParam("token") String token){
        System.out.println("A new WebSocket Statistics session is opened for client with token: " + token);

        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token)) {
            sessions.put(token, session);

            Statistics statistics;

            try {
                statistics = userBean.getAllStatistics();

                send(token, new Gson().toJson(statistics));

            } catch (Exception e) {
                System.out.println("Alguma coisa went wrong!");
            }

        } else {
            System.out.println("User not authenticated or not a product owner");
        }
    }

    @OnClose
    public void toDoOnClose(Session session, CloseReason reason) {
        System.out.println("Websocket session is closed with CloseCode: " + reason.getCloseCode() + ": " + reason.getReasonPhrase());
        sessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

    @OnMessage
    public void toDoOnMessage(Session session, String msg){

        Statistics statistics = null;
        String token = session.getPathParameters().get("token");

        try {
            statistics = userBean.getAllStatistics();

        } catch (Exception e) {
            System.out.println("Something went wrong!");
        }

        if (statistics != null) {
            Statistics finalStatistics = statistics;
            sessions.forEach((key, value) -> send(key, new Gson().toJson(finalStatistics)));

        } else {
            System.out.println("No statistics found");
        }

    }



}