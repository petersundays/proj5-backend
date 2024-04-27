package backend.proj5.service;

import backend.proj5.bean.MessageBean;
import backend.proj5.bean.TaskBean;
import backend.proj5.bean.UserBean;
import backend.proj5.dto.Message;
import backend.proj5.dto.User;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

@Path("/messages")
public class MessageService {

    @Inject
    private MessageBean messageBean;
    @Inject
    private UserBean userBean;

    private static final Logger logger = LogManager.getLogger(TaskBean.class);



   /* @POST
    @Path("/{receiver}")
    public Response sendMessage(@HeaderParam("token") String token, @PathParam("receiver") String receiver, String content, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Request to send message from IP: " + ipAddress);

        Response response;

        if(userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (messageBean.sendMessage(content, userBean.getUserByToken(token).getUsername(), receiver, token)) {
                response = Response.status(Response.Status.OK).build();
                logger.info("Message sent");
            } else {
                response = Response.status(Response.Status.BAD_REQUEST).build();
                logger.error("Message not sent");
            }
        } else {
            response = Response.status(Response.Status.UNAUTHORIZED).build();
            logger.error("User is not authenticated");
        }
        return response;
    }*/

    @GET
    @Path("/{receiver}")
    @Produces("application/json")
    public Response getMessagesBetweenUsers(@HeaderParam("token") String token, @PathParam("receiver") String receiver, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Request to get messages between users from IP: " + ipAddress);

        Response response;

        if(userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            ArrayList<Message> messages = messageBean.getMessages(token, receiver);
            response = Response.status(Response.Status.OK).entity(messages).build();
            logger.info("Messages sent");
        } else {
            response = Response.status(Response.Status.UNAUTHORIZED).build();
            logger.error("User is not authenticated");
        }
        return response;
    }
}
