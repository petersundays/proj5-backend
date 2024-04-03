package backend.proj5.service;


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
            response = Response.status(Response.Status.CREATED).entity("User registered successfully").build(); //status code 201
        } else {
            response = Response.status(Response.Status.BAD_REQUEST).entity("Something went wrong").build(); //status code 400
        }
        return response;
    }

    @GET
    @Path("/getUsernameFromEmail")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsernameFromEmail(@HeaderParam("email") String email, @HeaderParam("token") String token) {
        Response response;

        User user = userBean.convertEntityByEmail(email);

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(user.getUsername()).build();
        }
        return response;
    }

    @PUT
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("username") String username, @HeaderParam("token") String token, User user) {

        Response response;

        User userUpdate = userBean.getUser(username);

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
    @Path("{username}/visibility")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateVisibility(@PathParam("username") String username, @HeaderParam("token") String token) {
        Response response;

        User user = userBean.getUser(username);

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

    @GET
    @Path("{visible}")
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

    @GET
    @Path("{type}")
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
            User user = userBean.getUser(username);
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
    public Response confirmUser(@HeaderParam("email") String email) {

/* O PATH ESTÁ DESTA FORMA EM VEZ DE 'confirm-registration', PQ NO FRONTEND DAVA SEMPRE ERRO APESAR DE FUNCIONAR NO POSTMAN */

        Response response;

        boolean confirmed = userBean.updateUserEntityConfirmation(email);

        if (confirmed) {
            response = Response.status(200).entity("User confirmed").build();
        } else {
            response = Response.status(404).entity("User not found").build();
        }
        return response;
    }
}
