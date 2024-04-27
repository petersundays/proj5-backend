package backend.proj5.bean;

import backend.proj5.dao.CategoryDao;
import backend.proj5.dao.TaskDao;
import backend.proj5.dao.UserDao;
import backend.proj5.dto.Task;
import backend.proj5.dto.TaskToSendWS;
import backend.proj5.dto.User;
import backend.proj5.entity.TaskEntity;
import backend.proj5.entity.UserEntity;
import backend.proj5.service.LocalDateAdapter;
import backend.proj5.websocket.TaskWS;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class TaskBean implements Serializable {

    private static final Logger logger = LogManager.getLogger(TaskBean.class);

    @EJB
    private TaskDao taskDao;
    @EJB
    private CategoryDao categoryDao;
    @EJB
    private UserDao userDao;
    @EJB
    private UserBean userBean;
    @EJB
    private CategoryBean categoryBean;
    @EJB
    private TaskBean taskBean;
    @EJB
    private TaskWS taskWS;

    public boolean newTask(Task task, String token) {
        logger.debug("Attempting to create task: {} by {} with id: {}", task.getTitle(), task.getOwner().getUsername(), task.getId());
        boolean created = false;

        task.generateId();
        task.setInitialStateId();
        task.setOwner(userBean.convertUserEntitytoUserDto(userDao.findUserByToken(token)));
        task.setErased(false);
        task.setCategory(task.getCategory());
        if (validateTask(task)) {
            logger.info("Task validation successful for task: {} by {} with id: {}", task.getTitle(), task.getOwner().getUsername(), task.getId());
            taskDao.persist(convertTaskToEntity(task));

            Task createdTask = convertTaskEntityToTaskDto(taskDao.findTaskById(task.getId()));

            addOrUpdateTaskWebsocket(TaskToSendWS.ADD, createdTask);

            created = true;

            logger.info("Task created: {} by {} with id: {}", task.getTitle(), task.getOwner().getUsername(), task.getId());
        } else {
            logger.error("Task validation failed for task: {} by {} with id: {}", task.getTitle(), task.getOwner().getUsername(), task.getId());
        }

        if (!created) {
            logger.error("Failed to create task: {} by {} with id: {}", task.getTitle(), task.getOwner().getUsername(), task.getId());
        }

        return created;
    }

    public ArrayList<Task> getAllTasks(String token) {
        logger.debug("Attempting to get all tasks");
        UserEntity userEntity = userDao.findUserByToken(token);
        ArrayList<TaskEntity> entityTasks = taskDao.findAllTasks();
        ArrayList<Task> tasks = new ArrayList<>();
        if (entityTasks != null) {
            logger.info("Tasks found");
            for (TaskEntity taskEntity : entityTasks) {
                if (userEntity.getTypeOfUser() == User.DEVELOPER && !taskEntity.getErased()) {
                    tasks.add(convertTaskEntityToTaskDto(taskEntity));
                } else if (userEntity.getTypeOfUser() == User.SCRUMMASTER || userEntity.getTypeOfUser() == User.PRODUCTOWNER) {
                    tasks.add(convertTaskEntityToTaskDto(taskEntity));
                }
            }
        } else {
            logger.error("No tasks found");
        }

        logger.info("Returning tasks");

        return tasks;
    }

    public int getNumberOfTasksFromUser(String username) {
        logger.debug("Attempting to get number of tasks from user: {}", username);
        UserEntity userEntity = userDao.findUserByUsername(username);
        ArrayList<TaskEntity> entityTasks = taskDao.findTasksByUser(userEntity);
        logger.info("Number of tasks from user: {}", entityTasks.size());
        return entityTasks.size();
    }

    public ArrayList<Task> getAllTasksFromUser(String username, String token) {
        logger.debug("Attempting to get all tasks from user: {}", username);

        UserEntity loggedUser = userDao.findUserByToken(token);
        UserEntity tasksOwner = userDao.findUserByUsername(username);
        ArrayList<TaskEntity> entityUserTasks = taskDao.findTasksByUser(tasksOwner);

        ArrayList<Task> userTasks = new ArrayList<>();
        if (entityUserTasks != null) {
            logger.info("Tasks found for user: {} with id: {}", username, tasksOwner.getUsername());
            for (TaskEntity taskEntity : entityUserTasks) {
                if (loggedUser.getTypeOfUser() == User.DEVELOPER && !taskEntity.getErased()) {
                    userTasks.add(convertTaskEntityToTaskDto(taskEntity));
                } else if (loggedUser.getTypeOfUser() == User.SCRUMMASTER || loggedUser.getTypeOfUser() == User.PRODUCTOWNER) {
                    userTasks.add(convertTaskEntityToTaskDto(taskEntity));
                }
            }
        } else {
            logger.error("No tasks found for user: {} with id: {}", username, tasksOwner.getUsername());
        }

        logger.info("Returning tasks for user: {} with id: {}", username, tasksOwner.getUsername());

        return userTasks;
    }

    public boolean updateTask(Task task, String id) {
        logger.debug("Attempting to update task: {} with id: {}", task.getTitle(), id);

        TaskEntity taskEntity = taskDao.findTaskById(id);
        Task taskDto = taskBean.convertTaskEntityToTaskDto(taskEntity);
        User taskOwner = taskDto.getOwner();

        boolean edited = false;
        task.setId(id);
        task.setOwner(taskOwner);
        task.setStartDate(task.getStartDate());
        task.setLimitDate(task.getLimitDate());
        task.setCategory(categoryBean.convertCategoryEntityToCategoryDto(categoryDao.findCategoryByName(task.getCategory().getName())));

        if (taskDao.findTaskById(task.getId()) != null) {
            logger.info("Task found for update: {} with id: {}", task.getTitle(), task.getId());

            if (validateTask(task)) {
                logger.info("Task validation successful for task: {} with id: {}", task.getTitle(), task.getId());
                TaskEntity updatedTask = convertTaskToEntity(task);

                if (updatedTask.getStateId() == Task.DONE) {
                    updatedTask.setConclusionDate(LocalDate.now());
                    logger.info("Task state is DONE, setting conclusion date to today");

                } else {
                    updatedTask.setConclusionDate(null);
                    logger.info("Task state is not DONE, setting conclusion date to null");
                }
                taskDao.merge(updatedTask);

                addOrUpdateTaskWebsocket(TaskToSendWS.UPDATE, task);

                logger.info("Task updated: {} with id: {}", task.getTitle(), task.getId());

                edited = true;
            }
        }

        if (!edited) {
            logger.error("Failed to update task: {} with id: {}", task.getTitle(), task.getId());
        }

        return edited;
    }

    public boolean updateTaskStatus(String taskId, int stateId) {
        logger.debug("Attempting to update task status for task with id: {}", taskId);

        boolean updated = false;

        if (stateId != 100 && stateId != 200 && stateId != 300) {
            updated = false;
            logger.error("Invalid state id: {}", stateId);

        } else {
            logger.info("Valid state id: {}", stateId);
            TaskEntity taskEntity = taskDao.findTaskById(taskId);

            if (taskEntity != null) {
                logger.info("Task found for update: {} with id: {}", taskEntity.getTitle(), taskEntity.getId());
                taskEntity.setStateId(stateId);

                if (stateId == Task.DONE) {
                    taskEntity.setConclusionDate(LocalDate.now());
                    logger.info("Task state is DONE, setting conclusion date to today");

                } else {
                    taskEntity.setConclusionDate(null);
                    logger.info("Task state is not DONE, setting conclusion date to null");
                }
                taskDao.merge(taskEntity);

                Task task = convertTaskEntityToTaskDto(taskEntity);
                addOrUpdateTaskWebsocket(TaskToSendWS.UPDATE, task);

                logger.info("Task status updated for task with id: {}", taskId);

                updated = true;
            }
        }
        return updated;
    }


    public boolean switchErasedTaskStatus(String id) {
        logger.debug("Attempting to switch erased status for task with id: {}", id);
        boolean swithedErased = false;
        TaskEntity taskEntity = taskDao.findTaskById(id);

        if(taskEntity != null) {
            logger.info("Task found for switch erased status: {} with id: {}", taskEntity.getTitle(), taskEntity.getId());

            taskEntity.setErased(!taskEntity.getErased());
            taskDao.merge(taskEntity);

            Task task = convertTaskEntityToTaskDto(taskEntity);
            addOrUpdateTaskWebsocket(TaskToSendWS.UPDATE, task);

            logger.info("Erased status switched for task with id: {}", id);

            swithedErased = true;
        }

        if (!swithedErased) {
            logger.error("Failed to switch erased status for task with id: {}", id);
        }

        return swithedErased;
    }

    public boolean permanentlyDeleteTask(String id) {
        logger.debug("Attempting to permanently delete task with id: {}", id);

        boolean removed = false;

        TaskEntity taskEntity = taskDao.findTaskById(id);

        if (taskEntity != null && !taskEntity.getErased()) {
            logger.info("Task found for permanent delete: {} with id: {}", taskEntity.getTitle(), taskEntity.getId());

            taskDao.eraseTask(id);

            Task task = convertTaskEntityToTaskDto(taskEntity);
            addOrUpdateTaskWebsocket(TaskToSendWS.UPDATE, task);

            logger.info("Task permanently deleted with id: {}", id);

            removed = true;

        } else if (taskEntity != null && taskEntity.getErased()) {
            logger.info("Task found for permanent delete: {} with id: {}", taskEntity.getTitle(), taskEntity.getId());

            taskDao.deleteTask(id);

            deleteTaskWebsocket(id);

            logger.info("Task permanently deleted with id: {}", id);

            removed = true;
            
        }

        if (!removed) {
            logger.error("Failed to permanently delete task with id: {}", id);
        }

        return removed;
    }

    public ArrayList<Task> getTasksByCategory(String category) {
        logger.debug("Attempting to get tasks by category: {}", category);

        ArrayList<TaskEntity> entityTasks = categoryDao.findTasksByCategory(category);
        ArrayList<Task> tasks = new ArrayList<>();

        if (entityTasks != null) {
            logger.info("Tasks found for category: {}", category);
            for (TaskEntity taskEntity : entityTasks) {
                tasks.add(convertTaskEntityToTaskDto(taskEntity));
            }
        } else {
            logger.error("No tasks found for category: {}", category);
        }

        if (tasks.isEmpty()) {
            logger.error("No tasks found for category: {}", category);
        }

        return tasks;
    }

    public boolean validateTask(Task task) {
        logger.debug("Attempting to validate task: {} by {} with id: {}", task.getTitle(), task.getOwner().getUsername(), task.getId());
        boolean valid = true;

        if ((task.getStartDate() == null
                || task.getLimitDate() == null
                || task.getTitle().isBlank()
                || task.getDescription().isBlank()
                || task.getOwner() == null
                || task.getPriority() == 0
                || task.getCategory() == null
                || !categoryBean.categoryExists(task.getCategory().getName())
                || (task.getPriority() != Task.LOWPRIORITY && task.getPriority() != Task.MEDIUMPRIORITY && task.getPriority() != Task.HIGHPRIORITY)
                || (task.getStateId() != Task.TODO && task.getStateId() != Task.DOING && task.getStateId() != Task.DONE)
        )) {
            logger.error("Task validation failed for task: {} by {} with id: {}", task.getTitle(), task.getOwner().getUsername(), task.getId());
            valid = false;
        }

        if (valid) {
            logger.info("Task validation successful for task: {} by {} with id: {}", task.getTitle(), task.getOwner().getUsername(), task.getId());
        }

        return valid;
    }

    public ArrayList<Task> getNotErasedTasks() {
        logger.debug("Attempting to get not erased tasks");
        ArrayList<TaskEntity> entityTasks = taskDao.findNotErasedTasks();
        ArrayList<Task> tasks = new ArrayList<>();

        if (entityTasks != null) {
            logger.info("Not erased tasks found");
            for (TaskEntity taskEntity : entityTasks) {
                tasks.add(convertTaskEntityToTaskDto(taskEntity));
            }
        } else {
            logger.error("No not erased tasks found");
        }

        if (tasks.isEmpty()) {
            logger.error("No not erased tasks found");
        }

        return tasks;
    }

    public ArrayList<Task> getErasedTasks() {
        logger.debug("Attempting to get erased tasks");
        ArrayList<TaskEntity> entityTasks = taskDao.findErasedTasks();
        ArrayList<Task> tasks = new ArrayList<>();

        if (entityTasks != null) {
            logger.info("Erased tasks found");

            for (TaskEntity taskEntity : entityTasks) {
                tasks.add(convertTaskEntityToTaskDto(taskEntity));
            }
        } else {
            logger.error("No erased tasks found");
        }

        if (tasks.isEmpty()) {
            logger.error("No erased tasks found");
        }

        return tasks;
    }

    public boolean eraseAllTasksFromUser(String username) {
        logger.debug("Attempting to erase all tasks from user: {}", username);
        boolean erased = false;
        UserEntity userEntity = userDao.findUserByUsername(username);

        if (userEntity != null) {
            logger.info("User found for erase all tasks: {}", username);
            ArrayList<TaskEntity> userTasks = taskDao.findTasksByUser(userEntity);

            if (userTasks != null) {
                logger.info("Tasks found for erase all tasks: {}", username);
                for (TaskEntity taskEntity : userTasks) {
                    taskEntity.setErased(true);
                    taskDao.merge(taskEntity);

                    Task task = convertTaskEntityToTaskDto(taskEntity);
                    addOrUpdateTaskWebsocket(TaskToSendWS.UPDATE, task);
                }
                erased = true;
            } else {
                logger.error("No tasks found for erase all tasks: {}", username);
            }
        }

        if (!erased) {
            logger.error("Failed to erase all tasks from user: {}", username);
        }

        return erased;
    }

    public boolean eraseAllNotErasedTasks() {
        logger.debug("Attempting to erase all not erased tasks");
        boolean erased = false;

        try {
            logger.info("Erasing all not erased tasks");

            taskDao.eraseAllNotErasedTasks();
            ArrayList<TaskEntity> tasks = taskDao.findErasedTasks();

            for (TaskEntity taskEntity : tasks) {
                Task task = convertTaskEntityToTaskDto(taskEntity);
                addOrUpdateTaskWebsocket(TaskToSendWS.UPDATE, task);
            }

            erased = true;

            logger.info("All not erased tasks erased");

        } catch (Exception e) {
            erased = false;

            logger.error("Failed to erase all not erased tasks");
        }

        return erased;
    }

    public boolean restoreAllTasksFromUser(String username) {
        logger.debug("Attempting to restore all tasks from user: {}", username);
        boolean restore = false;
        UserEntity userEntity = userDao.findUserByUsername(username);

        if (userEntity != null) {
            logger.info("User found for restore all tasks: {}", username);
            ArrayList<TaskEntity> userTasks = taskDao.findTasksByUser(userEntity);

            if (userTasks != null) {
                logger.info("Tasks found for restore all tasks: {}", username);

                for (TaskEntity taskEntity : userTasks) {
                    taskEntity.setErased(false);
                    taskDao.merge(taskEntity);
                    Task task = convertTaskEntityToTaskDto(taskEntity);
                    addOrUpdateTaskWebsocket(TaskToSendWS.UPDATE, task);
                }
                restore = true;
            } else {
                logger.error("No tasks found for restore all tasks: {}", username);
            }
        } else {
            logger.error("User not found for restore all tasks: {}", username);
        }

        if (!restore) {
            logger.error("Failed to restore all tasks from user: {}", username);
        } else {
            logger.info("All tasks restored from user: {}", username);
        }

        return restore;
    }

    public boolean restoreAllErasedTasks() {
        logger.debug("Attempting to restore all erased tasks");
        boolean restored = false;

        try {
            taskDao.restoreAllErasedTasks();
            ArrayList<TaskEntity> tasks = taskDao.findNotErasedTasks();

            for (TaskEntity taskEntity : tasks) {
                Task task = convertTaskEntityToTaskDto(taskEntity);
                addOrUpdateTaskWebsocket(TaskToSendWS.UPDATE, task);
            }
            restored = true;

            logger.info("All erased tasks restored");

        } catch (Exception e) {
            restored = false;

            logger.error("Failed to restore all erased tasks");
        }

        return restored;
    }


    public boolean deleteAllErasedTasksFromUser(String username) {
        logger.debug("Attempting to delete all erased tasks from user: {}", username);
        boolean deleted = false;
        UserEntity userEntity = userDao.findUserByUsername(username);

        if (userEntity != null) {
            logger.info("User found for delete all erased tasks: {}", username);
            ArrayList<TaskEntity> userTasks = taskDao.findAllErasedTasksFromUser(userEntity);

            if (userTasks != null) {
                logger.info("Tasks found for delete all erased tasks: {}", username);

                for (TaskEntity taskEntity : userTasks) {
                    taskDao.deleteTask(taskEntity.getId());
                    deleteTaskWebsocket(taskEntity.getId());
                }
                deleted = true;

                logger.info("All erased tasks deleted from user: {}", username);

            } else {
                logger.error("No tasks found for delete all erased tasks: {}", username);
            }
        }

        return deleted;
    }

    public boolean deleteAllErasedTasks () {
        logger.debug("Attempting to delete all erased tasks");
        boolean deleted = false;
         try {
             ArrayList<TaskEntity> tasks = taskDao.findErasedTasks();
             taskDao.deleteAllErasedTasks();

             for (TaskEntity taskEntity : tasks) {
                 deleteTaskWebsocket(taskEntity.getId());
             }

             deleted = true;

             logger.info("All erased tasks deleted");

        } catch (Exception e) {
             deleted = false;

             logger.error("Failed to delete all erased tasks");
         }

        return deleted;
    }


    private TaskEntity convertTaskToEntity(Task task) {
        logger.debug("Attempting to convert task to entity");

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(task.getId());
        taskEntity.setTitle(task.getTitle());
        taskEntity.setDescription(task.getDescription());
        taskEntity.setPriority(task.getPriority());
        taskEntity.setStateId(task.getStateId());
        taskEntity.setStartDate(task.getStartDate());
        taskEntity.setLimitDate(task.getLimitDate());
        taskEntity.setCategory(categoryDao.findCategoryByName(task.getCategory().getName()));
        taskEntity.setErased(task.getErased());
        taskEntity.setOwner(userBean.convertUserDtotoUserEntity(task.getOwner()));

        return taskEntity;
    }

    public Task convertTaskEntityToTaskDto(TaskEntity taskEntity) {
        logger.debug("Attempting to convert task entity to dto");

        Task task = new Task();
        task.setId(taskEntity.getId());
        task.setTitle(taskEntity.getTitle());
        task.setDescription(taskEntity.getDescription());
        task.setPriority(taskEntity.getPriority());
        task.setStateId(taskEntity.getStateId());
        task.setStartDate(taskEntity.getStartDate());
        task.setLimitDate(taskEntity.getLimitDate());
        task.setCategory(categoryBean.convertCategoryEntityToCategoryDto(taskEntity.getCategory()));
        task.setErased(taskEntity.getErased());
        task.setOwner(userBean.convertUserEntitytoUserDto(taskEntity.getOwner()));

        return task;
    }


    public int numberOfTasksFromUser(String username) {
        logger.debug("Attempting to get number of tasks from user: {}", username);

        UserEntity userEntity = userDao.findUserByUsername(username);

        return taskDao.numberOfTasksFromUser(userEntity);
    }

    public int numberOfTasksFromUserByState(String username, int stateId) {
        logger.debug("Attempting to get number of tasks from user: {} by state: {}", username, stateId);

        UserEntity userEntity = userDao.findUserByUsername(username);

        return taskDao.numberOfTasksFromUserByState(userEntity, stateId);
    }

    public double averageNumberOfTasksPerUser(int totalVisibleUsers) {
        logger.debug("Attempting to get average number of tasks per user");

        double average = 0;

        int totalNotErasedTasks = taskDao.countNumberOfNotErasedTasks();

        if (totalVisibleUsers != 0) {
            average = (double) totalNotErasedTasks / totalVisibleUsers;
            average = Math.round(average * 100.0) / 100.0; // Round to two decimal places
        } else {
            logger.error("No visible users found");
        }

        logger.info("Average number of tasks per user: {}", average);

        return average;

    }
    public int numberOfTasksByState(int stateId) {
        logger.debug("Attempting to get number of tasks by state: {}", stateId);
        return taskDao.countNumberOfTasksByState(stateId);
    }

    public double averageTimeToFinishTask() {
        logger.debug("Attempting to get average time to finish task");

        double average = 0;
        ArrayList<TaskEntity> tasksDone = taskDao.findTasksByStateId(Task.DONE);
        int totalTasksDone = tasksDone.size();
        int totalDays = 0;

        for (TaskEntity task : tasksDone) {
            if (task.getConclusionDate().isBefore(task.getStartDate())) {
                totalDays += (int) task.getCreationDate().toLocalDateTime().toLocalDate().until(task.getConclusionDate(), ChronoUnit.DAYS);
            } else {
                totalDays += task.getStartDate().until(task.getConclusionDate()).getDays();
            }
        }

        if (totalTasksDone != 0) {
            average = (double) totalDays / totalTasksDone;
            average = Math.round(average * 100.0) / 100.0; // Round to two decimal places
        } else {
            logger.error("No tasks done found");
        }

        logger.info("Average time to finish task: {}", average);

        return average;
    }

    public List<Object[]> totalTasksDoneByEachDay() {
        logger.debug("Attempting to get total tasks done by each day");
        return taskDao.totalTasksDoneByEachDay();
    }

    public void deleteTaskWebsocket(String id) {
        logger.debug("Attempting to send websocket notification to delete task with id: {} through websocket", id);

        TaskToSendWS taskToSendWS = new TaskToSendWS(TaskToSendWS.DELETE, id);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();

        String json = gson.toJson(taskToSendWS);

        taskWS.send(json);
    }

    public void addOrUpdateTaskWebsocket(String actionToDo, Task task) {
        logger.debug("Attempting to send websocket notification to {} task with id: {} through websocket", actionToDo, task.getId());

        TaskToSendWS taskToSendWS = new TaskToSendWS(actionToDo, task);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();

        String json = gson.toJson(taskToSendWS);

        taskWS.send(json);
    }
}
