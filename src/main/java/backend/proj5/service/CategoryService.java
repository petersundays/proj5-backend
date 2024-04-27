package backend.proj5.service;

import backend.proj5.bean.CategoryBean;
import backend.proj5.bean.TaskBean;
import backend.proj5.bean.UserBean;
import backend.proj5.dto.Category;
import backend.proj5.dto.Task;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger logger = LogManager.getLogger(TaskBean.class);



    @GET
    @Path("/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasksByCategory(@HeaderParam("token") String token, @PathParam("category") String category, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Getting tasks by category from IP: " + ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.userIsScrumMaster(token) || userBean.userIsProductOwner(token)) {
                ArrayList<Task> tasksByCategory = taskBean.getTasksByCategory(category);
                response = Response.status(200).entity(tasksByCategory).build();
                logger.info("Tasks by category were found");
            } else {
                response = Response.status(403).entity("You don't have permission for this request").build();
                logger.info("User doesn't have permission for this request");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.info("User is not authenticated");
        }
        return response;
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newCategory(@HeaderParam("token") String token, Category category, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Creating a new category from IP: " + ipAddress);

        Response response;

        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.userIsProductOwner(token)) {
                if (categoryBean.categoryExists(category.getName())) {
                    response = Response.status(409).entity("Category with this name already exists").build();
                    logger.info("Category with this name already exists");
                } else {
                    try {
                        boolean added = categoryBean.newCategory(category.getName());
                        if (added) {
                            response = Response.status(201).entity("Category created successfully").build();
                            logger.info("Category created successfully");
                        } else {
                            response = Response.status(404).entity("Impossible to create category. Verify all fields").build();
                            logger.info("Impossible to create category. Verify all fields");
                        }
                    } catch (Exception e) {
                        response = Response.status(404).entity("Something went wrong. A new category was not created.").build();
                        logger.error("Something went wrong. A new category was not created.");
                    }
                }
            } else {
                response = Response.status(403).entity("You don't have permission to create a category").build();
                logger.info("User doesn't have permission to create a category");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.info("User is not authenticated");
        }
        return response;
    }


    @PUT
    @Path("{categoryName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editCategory(@HeaderParam("token") String token, @PathParam("categoryName") String categoryName, @HeaderParam("newCategoryName") String newCategoryName, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Editing a category from IP: " + ipAddress);

        Response response;

        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.userIsProductOwner(token)) {
                try {
                    boolean edited = categoryBean.editCategory(categoryName, newCategoryName);
                    if (edited) {
                        response = Response.status(200).entity("Category edited successfully").build();
                        logger.info("Category edited successfully");
                    } else {
                        response = Response.status(404).entity("Category with this name is not found").build();
                        logger.info("Category with this name is not found");
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The category was not edited.").build();
                    logger.error("Something went wrong. The category was not edited.");
                }
            } else {
                response = Response.status(403).entity("You don't have permission to edit a category").build();
                logger.info("User doesn't have permission to edit a category");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.info("User is not authenticated");
        }
        return response;
    }


    @DELETE
    @Path("/{categoryName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCategory(@HeaderParam("token") String token, @PathParam("categoryName") String categoryName, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Deleting a category from IP: " + ipAddress);

        Response response;

        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.userIsProductOwner(token)) {
                try {
                    boolean deleted = categoryBean.deleteCategory(categoryName);
                    if (deleted) {
                        response = Response.status(200).entity("Category removed successfully").build();
                        logger.info("Category removed successfully");
                    } else {
                        response = Response.status(400).entity("Category with this name can't be deleted").build();
                        logger.info("Category with this name can't be deleted");
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The category was not removed.").build();
                    logger.error("Something went wrong. The category was not removed.");
                }
            } else {
                response = Response.status(403).entity("You don't have permission to delete a category").build();
                logger.info("User doesn't have permission to delete a category");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.info("User is not authenticated");
        }
        return response;
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Getting all categories from IP: " + ipAddress);

        Response response;

        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            try {
                List<Category> allCategories = categoryBean.findAllCategories();
                response = Response.status(200).entity(allCategories).build();
                logger.info("All categories were found");
            } catch (Exception e) {
                response = Response.status(404).entity("Something went wrong. The categories were not found.").build();
                logger.error("Something went wrong. The categories were not found.");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.info("User is not authenticated");
        }
        return response;
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listCategoriesByNumberOfTasks(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Listing categories by number of tasks from IP: " + ipAddress);

        Response response;

        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token)) {
            logger.info("User is authenticated");
            try {
                ArrayList<String> categories = categoryBean.listCategoriesByNumberOfTasks();
                response = Response.status(200).entity(categories).build();
                logger.info("Categories were found");
            } catch (Exception e) {
                response = Response.status(404).entity("Something went wrong. The categories were not found.").build();
                logger.error("Something went wrong. The categories were not found.");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.info("User is not authenticated");
        }
        return response;
    }
}