package backend.proj5.service;

import backend.proj5.bean.TaskBean;
import backend.proj5.bean.UserBean;
import backend.proj5.dto.Task;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;

@Path("/tasks")
public class TaskService {

    @Inject
    UserBean userBean;
    @Inject
    TaskBean taskBean;

@GET
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public Response getAllTasks(@HeaderParam("token") String token) {

    Response response;

    if (userBean.isAuthenticated(token)) {
        ArrayList<Task> allTasks = taskBean.getAllTasks(token);
        response = Response.status(Response.Status.OK).entity(allTasks).build();
    } else {
        response = Response.status(401).entity("Invalid credentials").build();
    }
    return response;
}

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasksFromUser(@HeaderParam("token") String token, @PathParam("username") String username) {

        Response response;

        if (userBean.isAuthenticated(token)) {
            if (userBean.thisTokenIsFromThisUsername(token, username) || userBean.userIsProductOwner(token) || userBean.userIsScrumMaster(token)){
                ArrayList<Task> userTasks = taskBean.getAllTasksFromUser(username, token);
                response = Response.status(Response.Status.OK).entity(userTasks).build();
            } else {
                response = Response.status(406).entity("You don't have permission for this request").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newTask(@HeaderParam("token") String token, @HeaderParam("username") String username, Task task) {
        Response response;

        if (userBean.isAuthenticated(token)) {
            if (userBean.thisTokenIsFromThisUsername(token, username)) {
                try {
                    boolean added = taskBean.newTask(task, token);
                    if (added) {
                        response = Response.status(201).entity("Task created successfully").build();
                    } else {
                        response = Response.status(404).entity("Impossible to create task. Verify all fields").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. A new category was not created.").build();
                }
            } else {
                response = Response.status(Response.Status.BAD_REQUEST).entity("Username and token don't match").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }

        return response;
    }


    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTask(@HeaderParam("token") String token, @PathParam("id") String id, Task task) {

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsTaskOwner(token, id) || userBean.userIsScrumMaster(token) || userBean.userIsProductOwner(token)) {
                boolean updated = taskBean.updateTask(task, id);
                if (updated) {
                    response = Response.status(200).entity("Task updated successfully").build();
                } else {
                    response = Response.status(404).entity("Impossible to update task. Verify all fields").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to update this task").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }


    @PUT
    @Path("/{taskId}/{newStateId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTaskStatus(@HeaderParam("token") String token, @PathParam("taskId") String taskId, @PathParam("newStateId") int stateId) {

        Response response;
        if (userBean.isAuthenticated(token)) {
            boolean updated = taskBean.updateTaskStatus(taskId, stateId);
            if (updated) {
                response = Response.status(200).entity("Task status updated successfully").build();
            } else {
                response = Response.status(404).entity("Impossible to update task status. Task not found or invalid status").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @PUT
    @Path("/erase-restore/{taskId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response eraseTask(@HeaderParam("token") String token, @PathParam("taskId") String id) {

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsScrumMaster(token) || userBean.userIsProductOwner(token)) {
                try {
                    boolean switched = taskBean.switchErasedTaskStatus(id);
                    if (switched) {
                        response = Response.status(200).entity("Task erased status switched successfully").build();
                    } else {
                        response = Response.status(404).entity("Task with this id is not found").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The task erased status was switched.").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to switch the erased status of a task").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @PUT
    @Path("/erase-all")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response eraseAllTasks(@HeaderParam("token") String token) {

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsProductOwner(token)) {
                try {
                    boolean erased = taskBean.eraseAllNotErasedTasks();
                    if (erased) {
                        response = Response.status(200).entity("All tasks were erased successfully").build();
                    } else {
                        response = Response.status(404).entity("Impossible to erase tasks").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The tasks were not erased.").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to erase these tasks").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @PUT
    @Path("/erase-all/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response eraseAllTasksFromUser(@HeaderParam("token") String token, @PathParam("username") String username) {

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsProductOwner(token)) {
                try {
                    boolean erased = taskBean.eraseAllTasksFromUser(username);
                    if (erased) {
                        response = Response.status(200).entity("All tasks from " + username.toUpperCase() + " were erased successfully").build();
                    } else {
                        response = Response.status(404).entity("Impossible to erase tasks").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The tasks were not erased.").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to erase these tasks").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }


    @PUT
    @Path("/restore-all/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response restoreAllTasksFromUser(@HeaderParam("token") String token, @PathParam("username") String username) {

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsProductOwner(token)) {
                try {
                    boolean erased = taskBean.restoreAllTasksFromUser(username);
                    if (erased) {
                        response = Response.status(200).entity("All tasks from " + username.toUpperCase() + " were restored successfully.").build();
                    } else {
                        response = Response.status(404).entity("Impossible to restore tasks").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The tasks were not restored.").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to restore these tasks").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @PUT
    @Path("/restore-all")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response restoreAllErasedTasks(@HeaderParam("token") String token) {

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsProductOwner(token)) {
                try {
                    boolean erased = taskBean.restoreAllErasedTasks();
                    if (erased) {
                        response = Response.status(200).entity("All tasks were restored successfully.").build();
                    } else {
                        response = Response.status(404).entity("Impossible to restore tasks").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The tasks were not restored.").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to restore these tasks").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }


    @DELETE
    @Path("/{taskId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteTask(@HeaderParam("token") String token, @PathParam("taskId") String id) {

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsProductOwner(token)) {
                try {
                    boolean deleted = taskBean.permanentlyDeleteTask(id);
                    if (deleted) {
                        response = Response.status(200).entity("Task removed successfully").build();
                    } else {
                        response = Response.status(404).entity("Task with this id is not found").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The task was not removed.").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to delete a task").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }


    @DELETE
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteAllTasksFromUser(@HeaderParam("token") String token, @PathParam("username") String username) {

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsProductOwner(token)) {
                try {
                    boolean deleted = taskBean.deleteAllErasedTasksFromUser(username);
                    if (deleted) {
                        response = Response.status(200).entity("All tasks were removed successfully").build();
                    } else {
                        response = Response.status(404).entity("Impossible to remove tasks").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The tasks were not removed.").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to delete these tasks").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;

    }

    @DELETE
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteAllErased(@HeaderParam("token") String token) {

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsProductOwner(token)) {
                try {
                    boolean deleted = taskBean.deleteAllErasedTasks();
                    if (deleted) {
                        response = Response.status(200).entity("All tasks were deleted successfully").build();
                    } else {
                        response = Response.status(404).entity("Impossible to delete tasks").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The tasks were not deleted.").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to delete these tasks").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;

    }


    @GET
    @Path("/not-erased")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNotErasedTasks(@HeaderParam("token") String token) {
        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsScrumMaster(token) || userBean.userIsProductOwner(token)) {
                ArrayList<Task> notErasedTasks = taskBean.getNotErasedTasks();
                response = Response.status(200).entity(notErasedTasks).build();
            } else {
                response = Response.status(403).entity("You don't have permission for this request").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("/erased")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getErasedTasks(@HeaderParam("token") String token) {
        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsScrumMaster(token) || userBean.userIsProductOwner(token)) {
                ArrayList<Task> erasedTasks = taskBean.getErasedTasks();
                response = Response.status(200).entity(erasedTasks).build();
            } else {
                response = Response.status(403).entity("You don't have permission for this request").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("/{username}/atributed")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAtributedTasks(@HeaderParam("token") String token, @PathParam("username") String username) {
        Response response;
        if (userBean.isAuthenticated(token)) {
            int atributedTasks = taskBean.numberOfTasksFromUser(username);
            response = Response.status(200).entity(atributedTasks).build();
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("/{username}/{stateId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAtributedTasksByState(@HeaderParam("token") String token, @PathParam("username") String username, @PathParam("stateId") int stateId) {
        Response response;
        if (userBean.isAuthenticated(token)) {
            int atributedTasks = taskBean.numberOfTasksFromUserByState(username, stateId);
            response = Response.status(200).entity(atributedTasks).build();
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("/average")
    @Produces(MediaType.APPLICATION_JSON)
    public Response averageTimeToFinishTask(@HeaderParam("token") String token) {
        Response response;

        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token)) {
            try {
                double average = taskBean.averageTimeToFinishTask();
                response = Response.status(200).entity(average).build();
            } catch (Exception e) {
                response = Response.status(404).entity("Something went wrong. The average time was not calculated.").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

}