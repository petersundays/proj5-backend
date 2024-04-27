package backend.proj5.service;


import backend.proj5.bean.EmailBean;
import backend.proj5.bean.TaskBean;
import backend.proj5.bean.UserBean;
import backend.proj5.dto.*;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Path("/users")
public class UserService {

    @Inject
    UserBean userBean;
    @Inject
    EmailBean emailBean;

    private static final Logger logger = LogManager.getLogger(TaskBean.class);

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Login login, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to login", ipAddress);

        User userLogged = userBean.login(login);
        Response response;

        if (userLogged != null) {
            response = Response.status(200).entity(userLogged).build();
            logger.info("User with IP address {} logged in successfully", ipAddress);
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.info("User with IP address {} failed to login", ipAddress);
        }

        return response;
    }

    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to logout", ipAddress);

        if (userBean.logout(token)){
            logger.info("User with IP address {} logged out successfully", ipAddress);
            return Response.status(200).entity("Logout Successful!").build();
        }

        logger.info("User with IP address {} failed to logout", ipAddress);
        return Response.status(401).entity("Invalid Token!").build();
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(User user, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to register", ipAddress);

        Response response;

        boolean isUsernameAvailable = userBean.isUsernameAvailable(user);
        boolean isEmailValid = userBean.isEmailValid(user);
        boolean isPhoneNumberValid = userBean.isPhoneNumberValid(user);
        userBean.validatePhotoUrl(user);
        boolean isFieldEmpty = userBean.isAnyFieldEmpty(user);

        if (isFieldEmpty) {
            response = Response.status(422).entity("There's an empty field. All fields must be filled in").build();
            logger.info("User with IP address {} failed to register", ipAddress);
        } else if (!isEmailValid) {
            response = Response.status(422).entity("Invalid email").build();
            logger.info("User with IP address {} failed to register", ipAddress);
        } else if (!isUsernameAvailable) {
            response = Response.status(Response.Status.CONFLICT).entity("Username already in use").build(); //status code 409
            logger.info("User with IP address {} failed to register", ipAddress);
        } else if (!isPhoneNumberValid) {
            response = Response.status(422).entity("Invalid phone number").build();
            logger.info("User with IP address {} failed to register", ipAddress);
        } else if (userBean.register(user)) {
            response = Response.status(Response.Status.CREATED).entity("User registered").build(); //status code 201
            logger.info("User with IP address {} registered successfully", ipAddress);
        } else {
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("User not registered").build(); //status code 500
            logger.info("User with IP address {} failed to register", ipAddress);
        }
        return response;
    }

    @GET
    @Path("/getUsernameFromEmail")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsernameFromEmail(@HeaderParam("email") String email, @Context HttpServletRequest request) {

        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to get username from email", ipAddress);

        Response response;

        User user = userBean.convertEntityByEmail(email);

        if (user == null) {
            response = Response.status(404).entity("User not found").build();
            logger.info("User with IP address {} failed to get username from email", ipAddress);
        } else {
            response = Response.status(200).entity(user).build();
            logger.info("User with IP address {} got username from email successfully", ipAddress);
        }

        return response;
    }

    @PUT
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("username") String username, @HeaderParam("token") String token, User user, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to update user", ipAddress);

        Response response;

        User userUpdate = userBean.getUser(username, token);

        if (userUpdate==null){
            response = Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            logger.info("User with IP address {} failed to update user", ipAddress);
            return response;
        }

        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token) || userBean.thisTokenIsFromThisUsername(token,username)) {
            logger.info("User with IP address {} is trying to update user {}", ipAddress, username);
            if (!userBean.isEmailUpdatedValid(user) && user.getEmail() != null) {
                response = Response.status(422).entity("Invalid email").build();
                logger.info("User with IP address {} failed to update user", ipAddress);
            } else if (!userBean.isImageUrlUpdatedValid(user.getPhotoURL()) && user.getPhotoURL() != null) {
                response = Response.status(422).entity("Image URL invalid").build();
                logger.info("User with IP address {} failed to update user", ipAddress);

            } else if (!userBean.isPhoneNumberUpdatedValid(user) && user.getPhone() != null) {
                response = Response.status(422).entity("Invalid phone number").build();
                logger.info("User with IP address {} failed to update user", ipAddress);

            } else {
                boolean updatedUser = userBean.updateUser(user, username);
                response = Response.status(Response.Status.OK).entity(updatedUser).build(); //status code 200
                logger.info("User with IP address {} updated user {}", ipAddress, username);
            }
        }else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.info("User with IP address {} failed to update user", ipAddress);
        }
    return response;
    }


    @PUT
    @Path("/{username}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePassword(@PathParam("username") String username,
                                   @HeaderParam("token") String token,
                                   @HeaderParam("oldpassword") String oldPassword,
                                   @HeaderParam("newpassword") String newPassword,
                                   @Context HttpServletRequest request) {

        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to update password", ipAddress);

        if (userBean.isAuthenticated(token)){
            logger.info("User with IP address {} is trying to update password", ipAddress);

            boolean isOldPasswordValid = userBean.verifyOldPassword(username, oldPassword);
            if (!isOldPasswordValid) {
                logger.info("User with IP address {} failed to update password", ipAddress);
                return Response.status(401).entity("Current password is incorrect").build();
            }
            boolean updated = userBean.updatePassword(username, newPassword);
            if (!updated) {
                logger.info("User with IP address {} failed to update password", ipAddress);
                return Response.status(400).entity("User with this username is not found").build();
            } else {
                logger.info("User with IP address {} updated password", ipAddress);
                return Response.status(200).entity("Password updated").build();
            }
        } else
            logger.info("User with IP address {} failed to update password", ipAddress);
            return Response.status(401).entity("User is not logged in").build();
    }


    @PUT
    @Path("/set/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setPassword(@HeaderParam("validationToken") String token, @HeaderParam("password") String password, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to set password", ipAddress);

        Response response;

        boolean passwordSet = userBean.setPassword(token, password);
            if (!passwordSet) {
                response = Response.status(400).entity(false).build();
                logger.info("User with IP address {} failed to set password", ipAddress);
            } else {
                response = Response.status(200).entity(true).build();
                logger.info("User with IP address {} set password", ipAddress);
            }
            return response;
    }


    @PUT
    @Path("{username}/visibility")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateVisibility(@PathParam("username") String username, @HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to update visibility", ipAddress);

        Response response;

        User user = userBean.getUser(username, token);

        if (user==null){
            response = Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            logger.info("User with IP address {} failed to update visibility", ipAddress);
            return response;
        }

        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token)) {

            userBean.updateUserEntityVisibility(username);
            response = Response.status(Response.Status.OK).entity(username + " visibility: " + !user.isVisible()).build(); //status code 200
            logger.info("User with IP address {} updated visibility", ipAddress);

        }else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.info("User with IP address {} failed to update visibility", ipAddress);
        }

        return response;
    }


    @DELETE
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUser(@HeaderParam("token") String token, @PathParam("username") String username, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to remove user", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User with IP address {} is trying to remove user {}", ipAddress, username);

            boolean removed = userBean.delete(username);
            if (removed) {
                response = Response.status(200).entity("User removed successfully").build();
                logger.info("User with IP address {} removed user {}", ipAddress, username);
            } else {
                response = Response.status(404).entity("User is not found").build();
                logger.info("User with IP address {} failed to remove user", ipAddress);
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.info("User with IP address {} failed to remove user", ipAddress);
        }
        return response;
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to get all users", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            List<User> allUsers = userBean.getUsers();
            response = Response.status(200).entity(allUsers).build();
            logger.info("User with IP address {} got all users", ipAddress);

        } else {
            response = Response.status(401).entity("You don't have permission").build();
            logger.info("User with IP address {} failed to get all users", ipAddress);

        }
        return response;
    }

    //PATH está desta forma e não 'visibility/{visible}', pq o pedido "getUser" nem sempre funcionava
    // dava erro "multiple resources" com este pedido e com o"getUsers"
    @GET
    @Path("/visibility/{visible}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByVisibility(@HeaderParam("token") String token, @PathParam("visible") boolean visible, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to get users by visibility", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token) && !userBean.userIsDeveloper(token)) {
            logger.info("User with IP address {} is trying to get users by visibility", ipAddress);

            List<User> users = userBean.getUsersByVisibility(visible);
            response = Response.status(200).entity(users).build();
            logger.info("User with IP address {} got users by visibility", ipAddress);
        } else {
            response = Response.status(401).entity("You don't have permission").build();
            logger.info("User with IP address {} failed to get users by visibility", ipAddress);
        }
        return response;
    }

    //PATH está desta forma e não '{type}', pq o pedido "getUser" nem sempre funcionava
    // dava erro "multiple resources" com este pedido e com o"getUsersByVisibility"
    @GET
    @Path("/role/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@HeaderParam("token") String token, @PathParam("type") int typeOfUser, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to get users by role", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token) && !userBean.userIsDeveloper(token)) {
            logger.info("User with IP address {} is trying to get users by role", ipAddress);
            List<User> users = userBean.getUsersByType(typeOfUser);
            response = Response.status(200).entity(users).build();
            logger.info("User with IP address {} got users by role", ipAddress);

        } else {
            response = Response.status(401).entity("You don't have permission").build();
            logger.info("User with IP address {} failed to get users by role", ipAddress);

        }
        return response;
    }

    @GET
    @Path("{type}/{visible}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@HeaderParam("token") String token, @PathParam("type") int typeOfUser, @PathParam("visible") boolean visible, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to get users by role and visibility", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token) && !userBean.userIsDeveloper(token)) {
            logger.info("User with IP address {} is trying to get users by role and visibility", ipAddress);

            List<User> users = userBean.getUsersByTypeAndVisibility(typeOfUser,visible);
            response = Response.status(200).entity(users).build();
            logger.info("User with IP address {} got users by role and visibility", ipAddress);

        } else {
            response = Response.status(401).entity("You don't have permission").build();
            logger.info("User with IP address {} failed to get users by role and visibility", ipAddress);

        }
        return response;
    }

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("username") String username, @HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to get user {}", ipAddress, username);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User with IP address {} is trying to get user {}", ipAddress, username);
            User user = userBean.getUser(username, token);
            if (user != null) {
                response = Response.status(200).entity(user).build();
                logger.info("User with IP address {} got user {}", ipAddress, username);
            } else {
                response = Response.status(404).entity("User not found").build();
                logger.info("User with IP address {} failed to get user {}", ipAddress, username);
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.info("User with IP address {} failed to get user {}", ipAddress, username);
        }
        return response;
    }

    @PUT
    @Path("/email/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmUser(@HeaderParam("validationToken") String validationToken, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to confirm user", ipAddress);

/* O PATH ESTÁ DESTA FORMA EM VEZ DE 'confirm-registration', PQ NO FRONTEND DAVA SEMPRE ERRO APESAR DE FUNCIONAR NO POSTMAN */

        Response response;

        if (validationToken == null) {
            response = Response.status(422).entity("Invalid credentials").build();
            logger.info("User with IP address {} failed to confirm user", ipAddress);

            return response;
        }

        int confirmed = userBean.updateUserEntityConfirmation(validationToken);

        if (confirmed == 0) {
            response = Response.status(404).entity("Account not found").build();
            logger.info("User with IP address {} failed to confirm user", ipAddress);

        } else if (confirmed == 1) {
            response = Response.status(200).entity("Account successfully confirmed.").build();
            logger.info("User with IP address {} confirmed user", ipAddress);

        } else {
            response = Response.status(400).entity("Account already confirmed.").build();
            logger.info("User with IP address {} failed to confirm user", ipAddress);
        }
        return response;
    }

    @GET
    @Path("/defined-password")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doesUserHavePasswordDefined(@HeaderParam("validationToken") String validationToken, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to check if user has password defined", ipAddress);

        Response response;

        if (validationToken == null) {
            response = Response.status(422).entity("Invalid credentials").build();
            logger.info("User with IP address {} failed to check if user has password defined", ipAddress);

            return response;
        }

        boolean hasPassword = userBean.doesUserHavePasswordDefined(validationToken);
        response = Response.status(200).entity(hasPassword).build();
        logger.info("User with IP address {} checked if user has password defined", ipAddress);

        return response;
    }

    @POST
    @Path("/recover-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response recoverPassword(@HeaderParam("email") String email, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to recover password", ipAddress);

        Response response;

        if (email == null || !userBean.isEmailFormatValid(email)) {
            response = Response.status(422).entity("Invalid email").build();
            logger.info("User with IP address {} failed to recover password", ipAddress);

            return response;
        }

        int reset = userBean.sendPasswordResetEmail(email);

        if (reset == 0) {
            response = Response.status(404).entity("Account not found").build();
            logger.info("User with IP address {} failed to recover password", ipAddress);

        } else if (reset == 1) {
            response = Response.status(400).entity("Please confirm your account first").build();
            logger.info("User with IP address {} failed to recover password", ipAddress);

        } else {
            response = Response.status(200).entity("Email sent").build();
            logger.info("User with IP address {} recovered password", ipAddress);

        }

        return response;
    }

    @GET
    @Path("/validate-token")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isValidationTokenValid(@HeaderParam("validationToken") String validationToken, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to validate token", ipAddress);

        Response response;
        if (validationToken == null) {
            response = Response.status(422).entity("Invalid credentials").build();
            logger.info("User with IP address {} failed to validate token", ipAddress);
            return response;
        }

        boolean isValid = userBean.isValidationTokenValid(validationToken);
        response = Response.status(200).entity(isValid).build();
        logger.info("User with IP address {} validated token", ipAddress);

        return response;
    }

    @GET
    @Path("/user-stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserStats(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to get user stats", ipAddress);

        Response response;

        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token)) {
            logger.info("User with IP address {} is trying to get user stats", ipAddress);
            try {
                Number[] userStats = userBean.userStats();
                response = Response.status(200).entity(userStats).build();
                logger.info("User with IP address {} got user stats", ipAddress);

            } catch (Exception e) {
                response = Response.status(500).entity("Error getting user stats").build();
                logger.info("User with IP address {} failed to get user stats", ipAddress);
            }

        } else {
            response = Response.status(401).entity("You don't have permission").build();
            logger.info("User with IP address {} failed to get user stats", ipAddress);

        }

        return response;
    }

    @GET
    @Path("/total-by-date")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTotalUsersRegisteredByEachDay(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to get total users registered by each day", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token)) {
            logger.info("User with IP address {} is trying to get total users registered by each day", ipAddress);

            try {
                List<Object[]> users = userBean.getTotalUsersRegisteredByEachDay();
                response = Response.status(200).entity(users).build();
                logger.info("User with IP address {} got total users registered by each day", ipAddress);

            } catch (Exception e) {
                response = Response.status(500).entity("Error getting user stats").build();
                logger.info("User with IP address {} failed to get total users registered by each day", ipAddress);

            }

        } else {
            response = Response.status(401).entity("You don't have permission").build();
            logger.info("User with IP address {} failed to get total users registered by each day", ipAddress);
        }

        return response;
    }

    @PUT
    @Path("/session/set-timeout")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSessionTimeout(@HeaderParam("token") String token, @HeaderParam("timeout") int timeout, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to update session timeout", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token)) {
            logger.info("User with IP address {} is trying to update session timeout", ipAddress);
            if (userBean.setSessionTimeout(timeout)) {
                response = Response.status(200).entity("Session timeout updated").build();
                logger.info("User with IP address {} updated session timeout", ipAddress);

            } else {
                response = Response.status(500).entity("Timeout must be between 1 and 60 minutes").build();
                logger.info("User with IP address {} failed to update session timeout", ipAddress);

            }
        } else {
            response = Response.status(401).entity("You don't have permission").build();
            logger.info("User with IP address {} failed to update session timeout", ipAddress);
        }
        return response;
    }

    @GET
    @Path("/session/get-timeout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSessionTimeout(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("User with IP address {} is trying to get session timeout", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User with IP address {} is trying to get session timeout", ipAddress);
            int timeout = userBean.getSessionTimeout();
            response = Response.status(200).entity(timeout).build();
            logger.info("User with IP address {} got session timeout", ipAddress);
        } else {
            response = Response.status(401).entity("You don't have permission").build();
            logger.info("User with IP address {} failed to get session timeout", ipAddress);
        }
        return response;
    }
}
