package backend.proj5.service;

import backend.proj5.bean.CategoryBean;
import backend.proj5.bean.TaskBean;
import backend.proj5.bean.UserBean;
import backend.proj5.dto.Category;
import backend.proj5.dto.Task;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Path("/categories")
public class CategoryService {

    @Inject
    UserBean userBean;
    @Inject
    TaskBean taskBean;
    @Inject
    CategoryBean categoryBean;


    @GET
    @Path("/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasksByCategory(@HeaderParam("token") String token, @PathParam("category") String category) {

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsScrumMaster(token) || userBean.userIsProductOwner(token)) {
                ArrayList<Task> tasksByCategory = taskBean.getTasksByCategory(category);
                response = Response.status(200).entity(tasksByCategory).build();
            } else {
                response = Response.status(403).entity("You don't have permission for this request").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newCategory(@HeaderParam("token") String token, Category category) {

        Response response;

        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsProductOwner(token)) {
                if (categoryBean.categoryExists(category.getName())) {
                    response = Response.status(409).entity("Category with this name already exists").build();
                } else {
                    try {
                        boolean added = categoryBean.newCategory(category.getName());
                        if (added) {
                            response = Response.status(201).entity("Category created successfully").build();
                        } else {
                            response = Response.status(404).entity("Impossible to create category. Verify all fields").build();
                        }
                    } catch (Exception e) {
                        response = Response.status(404).entity("Something went wrong. A new category was not created.").build();
                    }
                }
            } else {
                response = Response.status(403).entity("You don't have permission to create a category").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }


    @PUT
    @Path("{categoryName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editCategory(@HeaderParam("token") String token, @PathParam("categoryName") String categoryName, @HeaderParam("newCategoryName") String newCategoryName) {

        Response response;

        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsProductOwner(token)) {
                try {
                    boolean edited = categoryBean.editCategory(categoryName, newCategoryName);
                    if (edited) {
                        response = Response.status(200).entity("Category edited successfully").build();
                    } else {
                        response = Response.status(404).entity("Category with this name is not found").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The category was not edited.").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to edit a category").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }


    @DELETE
    @Path("/{categoryName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCategory(@HeaderParam("token") String token, @PathParam("categoryName") String categoryName) {
        Response response;

        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsProductOwner(token)) {
                try {
                    boolean deleted = categoryBean.deleteCategory(categoryName);
                    if (deleted) {
                        response = Response.status(200).entity("Category removed successfully").build();
                    } else {
                        response = Response.status(400).entity("Category with this name can't be deleted").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The category was not removed.").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to delete a category").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories(@HeaderParam("token") String token) {

        Response response;

        if (userBean.isAuthenticated(token)) {
            try {
                List<Category> allCategories = categoryBean.findAllCategories();
                response = Response.status(200).entity(allCategories).build();
            } catch (Exception e) {
                response = Response.status(404).entity("Something went wrong. The categories were not found.").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

}