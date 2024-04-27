package backend.proj5.service;

import backend.proj5.bean.TaskBean;
import backend.proj5.bean.UserBean;
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

@Path("/tasks")
public class TaskService {

    @Inject
    UserBean userBean;
    @Inject
    TaskBean taskBean;

    private static final Logger logger = LogManager.getLogger(TaskBean.class);

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasks(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Getting all tasks. Request from IP: {}", ipAddress);

        Response response;

        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            ArrayList<Task> allTasks = taskBean.getAllTasks(token);
            response = Response.status(Response.Status.OK).entity(allTasks).build();
            logger.info("All tasks were returned");
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;
    }

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasksFromUser(@HeaderParam("token") String token, @PathParam("username") String username, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Getting all tasks from user {}. Request from IP: {}", username, ipAddress);

        Response response;

        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.thisTokenIsFromThisUsername(token, username) || userBean.userIsProductOwner(token) || userBean.userIsScrumMaster(token)){
                ArrayList<Task> userTasks = taskBean.getAllTasksFromUser(username, token);
                response = Response.status(Response.Status.OK).entity(userTasks).build();
                logger.info("All tasks from user {} were returned", username);
            } else {
                response = Response.status(406).entity("You don't have permission for this request").build();
                logger.error("User doesn't have permission for this request");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newTask(@HeaderParam("token") String token, @HeaderParam("username") String username, Task task, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Creating a new task. Request from IP: {}", ipAddress);

        Response response;

        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.thisTokenIsFromThisUsername(token, username)) {
                logger.info("Username and token match");
                try {
                    boolean added = taskBean.newTask(task, token);
                    if (added) {
                        response = Response.status(201).entity("Task created successfully").build();
                        logger.info("Task created successfully");
                    } else {
                        response = Response.status(404).entity("Impossible to create task. Verify all fields").build();
                        logger.error("Impossible to create task. Verify all fields");
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. A new category was not created.").build();
                    logger.error("Something went wrong. A new category was not created");
                }
            } else {
                response = Response.status(Response.Status.BAD_REQUEST).entity("Username and token don't match").build();
                logger.error("Username and token don't match");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }

        return response;
    }


    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTask(@HeaderParam("token") String token, @PathParam("id") String id, Task task, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Updating task. Request from IP: {}", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.userIsTaskOwner(token, id) || userBean.userIsScrumMaster(token) || userBean.userIsProductOwner(token)) {
                logger.info("User is task owner or scrum master or product owner");
                boolean updated = taskBean.updateTask(task, id);
                if (updated) {
                    response = Response.status(200).entity("Task updated successfully").build();
                    logger.info("Task updated successfully");
                } else {
                    response = Response.status(404).entity("Impossible to update task. Verify all fields").build();
                    logger.error("Impossible to update task. Verify all fields");
                }
            } else {
                response = Response.status(403).entity("You don't have permission to update this task").build();
                logger.error("User doesn't have permission to update this task");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;
    }


    @PUT
    @Path("/{taskId}/{newStateId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTaskStatus(@HeaderParam("token") String token, @PathParam("taskId") String taskId, @PathParam("newStateId") int stateId, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Updating task status. Request from IP: {}", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            boolean updated = taskBean.updateTaskStatus(taskId, stateId);
            if (updated) {
                response = Response.status(200).entity("Task status updated successfully").build();
                logger.info("Task status updated successfully");
            } else {
                response = Response.status(404).entity("Impossible to update task status. Task not found or invalid status").build();
                logger.error("Impossible to update task status. Task not found or invalid status");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;
    }

    @PUT
    @Path("/erase-restore/{taskId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response eraseTask(@HeaderParam("token") String token, @PathParam("taskId") String id, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Erasing task. Request from IP: {}", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.userIsScrumMaster(token) || userBean.userIsProductOwner(token)) {
                logger.info("User is scrum master or product owner");
                try {
                    boolean switched = taskBean.switchErasedTaskStatus(id);
                    if (switched) {
                        response = Response.status(200).entity("Task erased status switched successfully").build();
                        logger.info("Task erased status switched successfully");
                    } else {
                        response = Response.status(404).entity("Task with this id is not found").build();
                        logger.error("Task with this id is not found");
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The task erased status was switched.").build();
                    logger.error("Something went wrong. The task erased status was switched");
                }
            } else {
                response = Response.status(403).entity("You don't have permission to switch the erased status of a task").build();
                logger.error("User doesn't have permission to switch the erased status of a task");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;
    }

    @PUT
    @Path("/erase-all")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response eraseAllTasks(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Erasing all tasks. Request from IP: {}", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.userIsProductOwner(token)) {
                logger.info("User is product owner");
                try {
                    boolean erased = taskBean.eraseAllNotErasedTasks();
                    if (erased) {
                        response = Response.status(200).entity("All tasks were erased successfully").build();
                        logger.info("All tasks were erased successfully");
                    } else {
                        response = Response.status(404).entity("Impossible to erase tasks").build();
                        logger.error("Impossible to erase tasks");
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The tasks were not erased.").build();
                    logger.error("Something went wrong. The tasks were not erased");
                }
            } else {
                response = Response.status(403).entity("You don't have permission to erase these tasks").build();
                logger.error("User doesn't have permission to erase these tasks");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;
    }

    @PUT
    @Path("/erase-all/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response eraseAllTasksFromUser(@HeaderParam("token") String token, @PathParam("username") String username, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Erasing all tasks from user {}. Request from IP: {}", username, ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.userIsProductOwner(token)) {
                logger.info("User is product owner");
                try {
                    boolean erased = taskBean.eraseAllTasksFromUser(username);
                    if (erased) {
                        response = Response.status(200).entity("All tasks from " + username.toUpperCase() + " were erased successfully").build();
                        logger.info("All tasks from user {} were erased successfully", username);
                    } else {
                        response = Response.status(404).entity("Impossible to erase tasks").build();
                        logger.error("Impossible to erase tasks");
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The tasks were not erased.").build();
                    logger.error("Something went wrong. The tasks were not erased");
                }
            } else {
                response = Response.status(403).entity("You don't have permission to erase these tasks").build();
                logger.error("User doesn't have permission to erase these tasks");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;
    }


    @PUT
    @Path("/restore-all/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response restoreAllTasksFromUser(@HeaderParam("token") String token, @PathParam("username") String username, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Restoring all tasks from user {}. Request from IP: {}", username, ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsProductOwner(token)) {
                try {
                    boolean erased = taskBean.restoreAllTasksFromUser(username);
                    if (erased) {
                        response = Response.status(200).entity("All tasks from " + username.toUpperCase() + " were restored successfully.").build();
                        logger.info("All tasks from user {} were restored successfully", username);
                    } else {
                        response = Response.status(404).entity("Impossible to restore tasks").build();
                        logger.error("Impossible to restore tasks");
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The tasks were not restored.").build();
                    logger.error("Something went wrong. The tasks were not restored");
                }
            } else {
                response = Response.status(403).entity("You don't have permission to restore these tasks").build();
                logger.error("User doesn't have permission to restore these tasks");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }

        return response;
    }

    @PUT
    @Path("/restore-all")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response restoreAllErasedTasks(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Restoring all tasks. Request from IP: {}", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.userIsProductOwner(token)) {
                logger.info("User is product owner");
                try {
                    boolean erased = taskBean.restoreAllErasedTasks();
                    if (erased) {
                        response = Response.status(200).entity("All tasks were restored successfully.").build();
                        logger.info("All tasks were restored successfully");
                    } else {
                        response = Response.status(404).entity("Impossible to restore tasks").build();
                        logger.error("Impossible to restore tasks");
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The tasks were not restored.").build();
                    logger.error("Something went wrong. The tasks were not restored");
                }
            } else {
                response = Response.status(403).entity("You don't have permission to restore these tasks").build();
                logger.error("User doesn't have permission to restore these tasks");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }

        return response;
    }


    @DELETE
    @Path("/delete/{taskId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteTask(@HeaderParam("token") String token, @PathParam("taskId") String id, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Deleting task. Request from IP: {}", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.userIsProductOwner(token)) {
                logger.info("User is product owner");
                try {
                    boolean deleted = taskBean.permanentlyDeleteTask(id);
                    if (deleted) {
                        response = Response.status(200).entity("Task removed successfully").build();
                        logger.info("Task removed successfully");
                    } else {
                        response = Response.status(404).entity("Task with this id is not found").build();
                        logger.error("Task with this id is not found");
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The task was not removed.").build();
                    logger.error("Something went wrong. The task was not removed");
                }
            } else {
                response = Response.status(403).entity("You don't have permission to delete a task").build();
                logger.error("User doesn't have permission to delete a task");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;
    }


    @DELETE
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteAllTasksFromUser(@HeaderParam("token") String token, @PathParam("username") String username, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Deleting all tasks from user {}. Request from IP: {}", username, ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.userIsProductOwner(token)) {
                logger.info("User is product owner");
                try {
                    boolean deleted = taskBean.deleteAllErasedTasksFromUser(username);
                    if (deleted) {
                        response = Response.status(200).entity("All tasks were removed successfully").build();
                        logger.info("All tasks were removed successfully");
                    } else {
                        response = Response.status(404).entity("Impossible to remove tasks").build();
                        logger.error("Impossible to remove tasks");
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The tasks were not removed.").build();
                    logger.error("Something went wrong. The tasks were not removed");
                }
            } else {
                response = Response.status(403).entity("You don't have permission to delete these tasks").build();
                logger.error("User doesn't have permission to delete these tasks");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;

    }

    @DELETE
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteAllErased(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Deleting all tasks. Request from IP: {}", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.userIsProductOwner(token)) {
                logger.info("User is product owner");
                try {
                    boolean deleted = taskBean.deleteAllErasedTasks();
                    if (deleted) {
                        response = Response.status(200).entity("All tasks were deleted successfully").build();
                        logger.info("All tasks were deleted successfully");
                    } else {
                        response = Response.status(404).entity("Impossible to delete tasks").build();
                        logger.error("Impossible to delete tasks");
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The tasks were not deleted.").build();
                    logger.error("Something went wrong. The tasks were not deleted");
                }
            } else {
                response = Response.status(403).entity("You don't have permission to delete these tasks").build();
                logger.error("User doesn't have permission to delete these tasks");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;

    }


    @GET
    @Path("/not-erased")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNotErasedTasks(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Getting not erased tasks. Request from IP: {}", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.userIsScrumMaster(token) || userBean.userIsProductOwner(token)) {
                ArrayList<Task> notErasedTasks = taskBean.getNotErasedTasks();
                response = Response.status(200).entity(notErasedTasks).build();
                logger.info("Not erased tasks were returned");
            } else {
                response = Response.status(403).entity("You don't have permission for this request").build();
                logger.error("User doesn't have permission for this request");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;
    }

    @GET
    @Path("/erased")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getErasedTasks(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Getting erased tasks. Request from IP: {}", ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            if (userBean.userIsScrumMaster(token) || userBean.userIsProductOwner(token)) {
                ArrayList<Task> erasedTasks = taskBean.getErasedTasks();
                response = Response.status(200).entity(erasedTasks).build();
                logger.info("Erased tasks were returned");
            } else {
                response = Response.status(403).entity("You don't have permission for this request").build();
                logger.error("User doesn't have permission for this request");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;
    }

    @GET
    @Path("/{username}/atributed")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAtributedTasks(@HeaderParam("token") String token, @PathParam("username") String username, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Getting atributed tasks from user {}. Request from IP: {}", username, ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            int atributedTasks = taskBean.numberOfTasksFromUser(username);
            response = Response.status(200).entity(atributedTasks).build();
            logger.info("Atributed tasks from user {} were returned", username);
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;
    }

    @GET
    @Path("/{username}/{stateId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAtributedTasksByState(@HeaderParam("token") String token, @PathParam("username") String username, @PathParam("stateId") int stateId, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Getting atributed tasks from user {} by state {}. Request from IP: {}", username, stateId, ipAddress);

        Response response;
        if (userBean.isAuthenticated(token)) {
            logger.info("User is authenticated");
            int atributedTasks = taskBean.numberOfTasksFromUserByState(username, stateId);
            response = Response.status(200).entity(atributedTasks).build();
            logger.info("Atributed tasks from user {} by state {} were returned", username, stateId);
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;
    }

    @GET
    @Path("/average")
    @Produces(MediaType.APPLICATION_JSON)
    public Response averageTimeToFinishTask(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Calculating average time to finish a task. Request from IP: {}", ipAddress);

        Response response;

        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token)) {
            logger.info("User is authenticated and is product owner");
            try {
                double average = taskBean.averageTimeToFinishTask();
                response = Response.status(200).entity(average).build();
                logger.info("Average time to finish a task was calculated");
            } catch (Exception e) {
                response = Response.status(404).entity("Something went wrong. The average time was not calculated.").build();
                logger.error("Something went wrong. The average time was not calculated");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;
    }

    @GET
    @Path("/done-by-date")
    @Produces(MediaType.APPLICATION_JSON)
    public Response totalTasksDoneByEachDay(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        logger.info("Calculating total tasks done by each day. Request from IP: {}", ipAddress);

        Response response;

        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token)) {
            logger.info("User is authenticated and is product owner");
            try {
                List<Object[]> totalTasksDoneByEachDay = taskBean.totalTasksDoneByEachDay();
                response = Response.status(200).entity(totalTasksDoneByEachDay).build();
                logger.info("Total tasks done by each day were calculated");
            } catch (Exception e) {
                response = Response.status(404).entity("Something went wrong. The total tasks done by each day was not calculated.").build();
                logger.error("Something went wrong. The total tasks done by each day was not calculated");
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
            logger.error("Invalid credentials");
        }
        return response;
    }

}