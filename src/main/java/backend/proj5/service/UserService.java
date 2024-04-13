package backend.proj5.service;


import backend.proj5.bean.EmailBean;
import backend.proj5.bean.UserBean;
import backend.proj5.dto.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/users")
public class UserService {

    @Inject
    UserBean userBean;
    @Inject
    EmailBean emailBean;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Login login) {
        User userLogged = userBean.login(login);
        Response response;

        if (userLogged != null) {
            response = Response.status(200).entity(userLogged).build();
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@HeaderParam("token") String token) {

        if (userBean.logout(token)) return Response.status(200).entity("Logout Successful!").build();

        return Response.status(401).entity("Invalid Token!").build();
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(User user) {
        Response response;

        boolean isUsernameAvailable = userBean.isUsernameAvailable(user);
        boolean isEmailValid = userBean.isEmailValid(user);
        boolean isPhoneNumberValid = userBean.isPhoneNumberValid(user);
        userBean.validatePhotoUrl(user);
        boolean isFieldEmpty = userBean.isAnyFieldEmpty(user);

        if (isFieldEmpty) {
            response = Response.status(422).entity("There's an empty field. All fields must be filled in").build();
        } else if (!isEmailValid) {
            response = Response.status(422).entity("Invalid email").build();
        } else if (!isUsernameAvailable) {
            response = Response.status(Response.Status.CONFLICT).entity("Username already in use").build(); //status code 409
        } else if (!isPhoneNumberValid) {
            response = Response.status(422).entity("Invalid phone number").build();
        } else if (userBean.register(user)) {
            response = Response.status(Response.Status.CREATED).entity("User registered").build(); //status code 201
        } else {
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("User not registered").build(); //status code 500
        }
        return response;
    }

    @GET
    @Path("/getUsernameFromEmail")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsernameFromEmail(@HeaderParam("email") String email) {
        Response response;

        User user = userBean.convertEntityByEmail(email);

        if (user == null) {
            response = Response.status(404).entity("User not found").build();
        } else {
            response = Response.status(200).entity(user).build();
        }

        return response;
    }

    @PUT
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("username") String username, @HeaderParam("token") String token, User user) {

        Response response;

        User userUpdate = userBean.getUser(username, token);

        if (userUpdate==null){
            response = Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            return response;
        }

        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token) || userBean.thisTokenIsFromThisUsername(token,username)) {
            if (!userBean.isEmailUpdatedValid(user) && user.getEmail() != null) {
                response = Response.status(422).entity("Invalid email").build();

            } else if (!userBean.isImageUrlUpdatedValid(user.getPhotoURL()) && user.getPhotoURL() != null) {
                response = Response.status(422).entity("Image URL invalid").build();

            } else if (!userBean.isPhoneNumberUpdatedValid(user) && user.getPhone() != null) {
                response = Response.status(422).entity("Invalid phone number").build();

            } else {
                boolean updatedUser = userBean.updateUser(user, username);
                response = Response.status(Response.Status.OK).entity(updatedUser).build(); //status code 200
            }
        }else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
    return response;
    }


    @PUT
    @Path("/{username}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePassword(@PathParam("username") String username,
                                   @HeaderParam("token") String token,
                                   @HeaderParam("oldpassword") String oldPassword,
                                   @HeaderParam("newpassword") String newPassword) {

        if (userBean.isAuthenticated(token)){
            boolean isOldPasswordValid = userBean.verifyOldPassword(username, oldPassword);
            if (!isOldPasswordValid) {
                return Response.status(401).entity("Current password is incorrect").build();
            }
            boolean updated = userBean.updatePassword(username, newPassword);
            if (!updated) {
                return Response.status(400).entity("User with this username is not found").build();
            }else return Response.status(200).entity("Password updated").build();
        }else
            return Response.status(401).entity("User is not logged in").build();
    }


    @PUT
    @Path("/set/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setPassword(@HeaderParam("validationToken") String token, @HeaderParam("password") String password) {
        Response response;

        boolean passwordSet = userBean.setPassword(token, password);
            if (!passwordSet) {
                response = Response.status(400).entity(false).build();
            } else {
                response = Response.status(200).entity(true).build();
            }
            return response;
    }


    @PUT
    @Path("{username}/visibility")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateVisibility(@PathParam("username") String username, @HeaderParam("token") String token) {
        Response response;

        User user = userBean.getUser(username, token);

        if (user==null){
            response = Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            return response;
        }

        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token)) {

            userBean.updateUserEntityVisibility(username);
            response = Response.status(Response.Status.OK).entity(username + " visibility: " + !user.isVisible()).build(); //status code 200

        }else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }


    @DELETE
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUser(@HeaderParam("token") String token, @PathParam("username") String username) {

        Response response;
        if (userBean.isAuthenticated(token)) {

            boolean removed = userBean.delete(username);
            if (removed) {
                response = Response.status(200).entity("User removed successfully").build();
            } else {
                response = Response.status(404).entity("User is not found").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@HeaderParam("token") String token) {
        Response response;
        if (userBean.isAuthenticated(token) && !userBean.userIsDeveloper(token)) {
            List<User> allUsers = userBean.getUsers();
            response = Response.status(200).entity(allUsers).build();
        } else {
            response = Response.status(401).entity("You don't have permission").build();
        }
        return response;
    }

    //PATHestá desta forma e não 'visibility/{visible}', pq o pedido "getUser" nem sempre funcionava
    // dava erro "multiple resources" com este pedido e com o"getUsers"
    @GET
    @Path("/visibility/{visible}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByVisibility(@HeaderParam("token") String token, @PathParam("visible") boolean visible) {
        Response response;
        if (userBean.isAuthenticated(token) && !userBean.userIsDeveloper(token)) {
            List<User> users = userBean.getUsersByVisibility(visible);
            response = Response.status(200).entity(users).build();
        } else {
            response = Response.status(401).entity("You don't have permission").build();
        }
        return response;
    }

    //PATH está desta forma e não '{type}', pq o pedido "getUser" nem sempre funcionava
    // dava erro "multiple resources" com este pedido e com o"getUsersByVisibility"
    @GET
    @Path("/role/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@HeaderParam("token") String token, @PathParam("type") int typeOfUser) {
        Response response;
        if (userBean.isAuthenticated(token) && !userBean.userIsDeveloper(token)) {
            List<User> users = userBean.getUsersByType(typeOfUser);
            response = Response.status(200).entity(users).build();
        } else {
            response = Response.status(401).entity("You don't have permission").build();
        }
        return response;
    }

    @GET
    @Path("{type}/{visible}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@HeaderParam("token") String token, @PathParam("type") int typeOfUser, @PathParam("visible") boolean visible) {
        Response response;
        if (userBean.isAuthenticated(token) && !userBean.userIsDeveloper(token)) {
            List<User> users = userBean.getUsersByTypeAndVisibility(typeOfUser,visible);
            response = Response.status(200).entity(users).build();
        } else {
            response = Response.status(401).entity("You don't have permission").build();
        }
        return response;
    }

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("username") String username, @HeaderParam("token") String token) {
        Response response;
        if (userBean.isAuthenticated(token)) {
            User user = userBean.getUser(username, token);
            if (user != null) {
                response = Response.status(200).entity(user).build();
            } else {
                response = Response.status(404).entity("User not found").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @PUT
    @Path("/email/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmUser(@HeaderParam("validationToken") String validationToken) {

/* O PATH ESTÁ DESTA FORMA EM VEZ DE 'confirm-registration', PQ NO FRONTEND DAVA SEMPRE ERRO APESAR DE FUNCIONAR NO POSTMAN */

        Response response;

        if (validationToken == null) {
            response = Response.status(422).entity("Invalid credentials").build();
            return response;
        }

        int confirmed = userBean.updateUserEntityConfirmation(validationToken);

        if (confirmed == 0) {
            response = Response.status(404).entity("Account not found").build();
        } else if (confirmed == 1) {
            response = Response.status(200).entity("Account successfully confirmed.").build();
        } else {
            response = Response.status(400).entity("Account already confirmed.").build();
        }
        return response;
    }

    @GET
    @Path("/defined-password")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doesUserHavePasswordDefined(@HeaderParam("validationToken") String validationToken) {
        Response response;

        if (validationToken == null) {
            response = Response.status(422).entity("Invalid credentials").build();
            return response;
        }

        boolean hasPassword = userBean.doesUserHavePasswordDefined(validationToken);
        response = Response.status(200).entity(hasPassword).build();
        return response;
    }

    @POST
    @Path("/recover-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response recoverPassword(@HeaderParam("email") String email) {
        Response response;

        if (email == null || !userBean.isEmailFormatValid(email)) {
            response = Response.status(422).entity("Invalid email").build();
            return response;
        }

        int reset = userBean.sendPasswordResetEmail(email);

        if (reset == 0) {
            response = Response.status(404).entity("Account not found").build();
        } else if (reset == 1) {
            response = Response.status(400).entity("Please confirm your account first").build();
        } else {
            response = Response.status(200).entity("Email sent").build();
        }

        return response;
    }

}
