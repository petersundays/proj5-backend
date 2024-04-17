package backend.proj5.service;

import backend.proj5.bean.MessageBean;
import backend.proj5.bean.UserBean;
import backend.proj5.dto.User;
import jakarta.inject.Inject;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/messages")
public class MessageService {

    @Inject
    private MessageBean messageBean;
    @Inject
    private UserBean userBean;


    @POST
    @Path("/{receiver}")
    public Response sendMessage(@HeaderParam("token") String token, @PathParam("receiver") String receiver, String content) {

        Response response;

        if(userBean.isAuthenticated(token)) {
            if (messageBean.sendMessage(content, userBean.getUserByToken(token).getUsername(), receiver, token)) {
                response = Response.status(Response.Status.OK).build();
            } else {
                response = Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            response = Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return response;
    }

}
