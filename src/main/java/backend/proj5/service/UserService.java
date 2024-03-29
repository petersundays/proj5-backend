package backend.proj5.service;

import backend.proj5.bean.CategoryBean;
import backend.proj5.bean.TaskBean;
import backend.proj5.bean.UserBean;
import backend.proj5.dto.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Path("/users")
public class UserService {

    @Inject
    UserBean userBean;
    @Inject
    TaskBean taskBean;
    @Inject
    CategoryBean categoryBean;


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
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(User user) {
        Response response;

        boolean isUsernameAvailable = userBean.isUsernameAvailable(user);
        boolean isEmailValid = userBean.isEmailValid(user);
        boolean isFieldEmpty = userBean.isAnyFieldEmpty(user);
        boolean isPhoneNumberValid = userBean.isPhoneNumberValid(user);
        boolean isImageValid = userBean.isImageUrlValid(user.getPhotoURL());

        if (isFieldEmpty) {
            response = Response.status(422).entity("There's an empty field. All fields must be filled in").build();
        } else if (!isEmailValid) {
            response = Response.status(422).entity("Invalid email").build();
        } else if (!isUsernameAvailable) {
            response = Response.status(Response.Status.CONFLICT).entity("Username already in use").build(); //status code 409
        } else if (!isImageValid) {
            response = Response.status(422).entity("Image URL invalid").build(); //400
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
    @Path("/getFirstName")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFirstName(@HeaderParam("token") String token) {
        Response response;

        User currentUser = userBean.convertEntityByToken(token);

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(currentUser.getFirstName()).build();
        }
        return response;
    }

    //Retorna o url da foto do token enviado
    @GET
    @Path("/getPhotoUrl")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImage(@HeaderParam("token") String token) {
        Response response;

        User currentUser = userBean.convertEntityByToken(token);

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(currentUser.getPhotoURL()).build();
        }
        return response;
    }

    //Retorna username do token enviado
    @GET
    @Path("/getUsername")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsername(@HeaderParam("token") String token) {
        Response response;

        User currentUser = userBean.convertEntityByToken(token);

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(currentUser.getUsername()).build();
        }
        return response;
    }


    //Retorna tipo de user do token enviado
    @GET
    @Path("/getTypeOfUser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTypeOfUser(@HeaderParam("token") String token) {
        Response response;

        User currentUser = userBean.convertEntityByToken(token);

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(currentUser.getTypeOfUser()).build();
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

    //Atualizar um user
    @PUT
    @Path("/update-profile/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("username") String username, @HeaderParam("token") String token, User user) {
        System.out.println("****************** USER " + user);
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
    @Path("/update/{username}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePassword(@PathParam("username") String username,
                                   @HeaderParam("token") String token,
                                   @HeaderParam("oldpassword") String oldPassword,
                                   @HeaderParam("newpassword") String newPassword) {

        //Verica se user está autentificado
        if (userBean.isAuthenticated(token)){
            // Verificar password antiga
            boolean isOldPasswordValid = userBean.verifyOldPassword(username, oldPassword);
            if (!isOldPasswordValid) {
                return Response.status(401).entity("Current password is incorrect").build();
            }
            // Se a password antiga é válida, update a password
            boolean updated = userBean.updatePassword(username, newPassword);
            if (!updated) {
                return Response.status(400).entity("User with this username is not found").build();
            }else return Response.status(200).entity("Password updated").build();
        }else
            return Response.status(401).entity("User is not logged in").build();
    }

    @PUT
    @Path("/update/{username}/visibility")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateVisibility(@PathParam("username") String username, @HeaderParam("token") String token) {
        Response response;

        User user = userBean.getUser(username);

        //Verifica se o username existe na base de dados
        if (user==null){
            response = Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            return response;
        }

        //Verifica se token de quem consulta existe e se é Product Owner
        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token)) {

            userBean.updateUserEntityVisibility(username);
            response = Response.status(Response.Status.OK).entity(username + " visibility: " + !user.isVisible()).build(); //status code 200

        }else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    //Atualizar tipo de user
    @PUT
    @Path("/update/{username}/role")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateRole(@PathParam("username") String username, @HeaderParam("token") String token, @HeaderParam("typeOfUser") int typeOfUser) {
        Response response;

        User user = userBean.getUser(username);

        //Verifica se o username existe na base de dados
        if (user==null){
            response = Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            return response;
        }

        //Verifica se token existe de quem consulta e se é Product Owner
        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token)) {

            if (typeOfUser == 100 || typeOfUser == 200 || typeOfUser == 300) {

                boolean updatedRole = userBean.updateUserEntityRole(username, typeOfUser);
                response = Response.status(Response.Status.OK).entity("Role updated with success").build(); //status code 200
            }else response = Response.status(401).entity("Invalid type of User").build();

        }else {
            response = Response.status(401).entity("Invalid credentials").build();
        }

        return response;
    }

    //Apagar um user
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
    @Path("/all")
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
    @Path("/all/visible/{visible}")
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
    @Path("/all/{type}")
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
    @Path("/all/{type}/{visible}")
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

        User userSearched = userBean.getUser(username);

        //Verifica se o username existe na base de dados
        if (userSearched==null){
            response = Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            return response;
        }

        //Verifica se o token é igual ao username pesquisado
        if (userBean.thisTokenIsFromThisUsername(token,username)) {

            response = Response.ok().entity(userSearched).build();

        }else {
            //Verifica se token existe de quem consulta
            if (userBean.isAuthenticated(token)) {
                if (userBean.userIsScrumMaster(token) || userBean.userIsProductOwner(token)) {

                    response = Response.ok().entity(userSearched).build();
                } else {
                    response = Response.status(Response.Status.BAD_REQUEST).entity("Invalid username on path").build();
                }
            } else {
                response = Response.status(401).entity("Invalid credentials").build();
            }
        }
        return response;
    }

}
