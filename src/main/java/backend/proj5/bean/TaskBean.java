package backend.proj5.bean;

import backend.proj5.dao.CategoryDao;
import backend.proj5.dao.TaskDao;
import backend.proj5.dao.UserDao;
import backend.proj5.dto.Task;
import backend.proj5.dto.User;
import backend.proj5.entity.TaskEntity;
import backend.proj5.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

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




    public boolean newTask(Task task, String token) {
        boolean created = false;

        task.generateId();
        task.setInitialStateId();
        task.setOwner(userBean.convertUserEntitytoUserDto(userDao.findUserByToken(token)));
        task.setErased(false);
        task.setCategory(task.getCategory());
        if (validateTask(task)) {
            taskDao.persist(convertTaskToEntity(task));
            created = true;

        }

        logger.info("Task created: {} by {} with id: {}", task.getTitle(), task.getOwner().getUsername(), task.getId());
        logger.debug("Sample debug message");
        logger.info("Sample info message");
        logger.warn("Sample warn message");
        logger.error("Sample error message");
        logger.fatal("Sample fatal message");

        return created;
    }

    public ArrayList<Task> getAllTasks(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        ArrayList<TaskEntity> entityTasks = taskDao.findAllTasks();
        ArrayList<Task> tasks = new ArrayList<>();
        if (entityTasks != null) {
            for (TaskEntity taskEntity : entityTasks) {
                if (userEntity.getTypeOfUser() == User.DEVELOPER && !taskEntity.getErased()) {
                    tasks.add(convertTaskEntityToTaskDto(taskEntity));
                } else if (userEntity.getTypeOfUser() == User.SCRUMMASTER || userEntity.getTypeOfUser() == User.PRODUCTOWNER) {
                    tasks.add(convertTaskEntityToTaskDto(taskEntity));
                }
            }
        }
        return tasks;
    }

    public int getNumberOfTasksFromUser(String username) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        ArrayList<TaskEntity> entityTasks = taskDao.findTasksByUser(userEntity);
        return entityTasks.size();
    }

    public ArrayList<Task> getAllTasksFromUser(String username, String token) {
        UserEntity loggedUser = userDao.findUserByToken(token);
        UserEntity tasksOwner = userDao.findUserByUsername(username);
        ArrayList<TaskEntity> entityUserTasks = taskDao.findTasksByUser(tasksOwner);

        ArrayList<Task> userTasks = new ArrayList<>();
        if (entityUserTasks != null) {
            for (TaskEntity taskEntity : entityUserTasks) {
                if (loggedUser.getTypeOfUser() == User.DEVELOPER && !taskEntity.getErased()) {
                    userTasks.add(convertTaskEntityToTaskDto(taskEntity));
                } else if (loggedUser.getTypeOfUser() == User.SCRUMMASTER || loggedUser.getTypeOfUser() == User.PRODUCTOWNER) {
                    userTasks.add(convertTaskEntityToTaskDto(taskEntity));
                }
            }
        }
        return userTasks;
    }

    public boolean updateTask(Task task, String id) {
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
            if (validateTask(task)) {
                TaskEntity updatedTask = convertTaskToEntity(task);
                if (updatedTask.getStateId() == Task.DONE) {
                    updatedTask.setConclusionDate(LocalDate.now());
                } else {
                    updatedTask.setConclusionDate(null);
                }
                taskDao.merge(updatedTask);
                edited = true;
            }
        }

        return edited;
    }

    public boolean updateTaskStatus(String taskId, int stateId) {
        boolean updated = false;
        if (stateId != 100 && stateId != 200 && stateId != 300) {
            updated = false;
        } else {
            TaskEntity taskEntity = taskDao.findTaskById(taskId);
            if (taskEntity != null) {
                taskEntity.setStateId(stateId);
                if (stateId == Task.DONE) {
                    taskEntity.setConclusionDate(LocalDate.now());
                } else {
                    taskEntity.setConclusionDate(null);
                }
                taskDao.merge(taskEntity);
                updated = true;
            }
        }
        return updated;
    }



    public boolean switchErasedTaskStatus(String id) {
        boolean swithedErased = false;
        TaskEntity taskEntity = taskDao.findTaskById(id);
        if(taskEntity != null) {
            taskEntity.setErased(!taskEntity.getErased());
            taskDao.merge(taskEntity);
            swithedErased = true;
        }
        return swithedErased;
    }

    public boolean permanentlyDeleteTask(String id) {
        boolean removed = false;
        TaskEntity taskEntity = taskDao.findTaskById(id);
        if (taskEntity != null && !taskEntity.getErased()) {
            taskDao.eraseTask(id);
            removed = true;
        } else if (taskEntity != null && taskEntity.getErased()) {
            taskDao.deleteTask(id);
            removed = true;
        }
        return removed;
    }

    public ArrayList<Task> getTasksByCategory(String category) {
        ArrayList<TaskEntity> entityTasks = categoryDao.findTasksByCategory(category);
        ArrayList<Task> tasks = new ArrayList<>();
        if (entityTasks != null) {
            for (TaskEntity taskEntity : entityTasks) {
                tasks.add(convertTaskEntityToTaskDto(taskEntity));
            }
        }
        return tasks;
    }

    public boolean validateTask(Task task) {
        boolean valid = true;
        if ((task.getStartDate() == null
                || task.getLimitDate() == null
              //  || task.getLimitDate().isBefore(task.getStartDate())
                || task.getTitle().isBlank()
                || task.getDescription().isBlank()
                || task.getOwner() == null
                || task.getPriority() == 0
                || task.getCategory() == null
                || !categoryBean.categoryExists(task.getCategory().getName())
                || (task.getPriority() != Task.LOWPRIORITY && task.getPriority() != Task.MEDIUMPRIORITY && task.getPriority() != Task.HIGHPRIORITY)
                || (task.getStateId() != Task.TODO && task.getStateId() != Task.DOING && task.getStateId() != Task.DONE)
        )) {
            valid = false;
        }
        return valid;
    }

    public ArrayList<Task> getNotErasedTasks() {
        ArrayList<TaskEntity> entityTasks = taskDao.findNotErasedTasks();
        ArrayList<Task> tasks = new ArrayList<>();
        if (entityTasks != null) {
            for (TaskEntity taskEntity : entityTasks) {
                tasks.add(convertTaskEntityToTaskDto(taskEntity));
            }
        }
        return tasks;
    }

    public ArrayList<Task> getErasedTasks() {
        ArrayList<TaskEntity> entityTasks = taskDao.findErasedTasks();
        ArrayList<Task> tasks = new ArrayList<>();
        if (entityTasks != null) {
            for (TaskEntity taskEntity : entityTasks) {
                tasks.add(convertTaskEntityToTaskDto(taskEntity));
            }
        }
        return tasks;
    }

    public boolean eraseAllTasksFromUser(String username) {
        boolean erased = false;
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {
            ArrayList<TaskEntity> userTasks = taskDao.findTasksByUser(userEntity);
            if (userTasks != null) {
                for (TaskEntity taskEntity : userTasks) {
                    taskEntity.setErased(true);
                    taskDao.merge(taskEntity);
                }
                erased = true;
            }
        }
        return erased;
    }

    public boolean eraseAllNotErasedTasks() {
        boolean erased = false;
        try {
            taskDao.eraseAllNotErasedTasks();
            erased = true;
        } catch (Exception e) {
            erased = false;
        }
        return erased;
    }

    public boolean restoreAllTasksFromUser(String username) {
        boolean restore = false;
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {
            ArrayList<TaskEntity> userTasks = taskDao.findTasksByUser(userEntity);
            if (userTasks != null) {
                for (TaskEntity taskEntity : userTasks) {
                    taskEntity.setErased(false);
                    taskDao.merge(taskEntity);
                }
                restore = true;
            }
        }
        return restore;
    }

    public boolean restoreAllErasedTasks() {
        boolean restored = false;
        try {
            taskDao.restoreAllErasedTasks();
            restored = true;
        } catch (Exception e) {
            restored = false;
        }
        return restored;
    }


    public boolean deleteAllErasedTasksFromUser(String username) {
        boolean deleted = false;
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {
            taskDao.deleteAllTasksFromUser(userEntity);
            deleted = true;
        }
        return deleted;
    }

    public boolean deleteAllErasedTasks () {
        boolean deleted = false;
         try {
            taskDao.deleteAllErasedTasks();
            deleted = true;
        } catch (Exception e) {
            deleted = false;
         }
        return deleted;
    }


    private TaskEntity convertTaskToEntity(Task task) {
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

        UserEntity userEntity = userDao.findUserByUsername(username);
        return taskDao.numberOfTasksFromUser(userEntity);
    }

    public int numberOfTasksFromUserByState(String username, int stateId) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        return taskDao.numberOfTasksFromUserByState(userEntity, stateId);
    }

    public double averageNumberOfTasksPerUser(int totalVisibleUsers) {
        double average = 0;

        int totalNotErasedTasks = taskDao.countNumberOfNotErasedTasks();

        if (totalVisibleUsers != 0) {
            average = (double) totalNotErasedTasks / totalVisibleUsers;
            average = Math.round(average * 100.0) / 100.0; // Round to two decimal places
        }

        return average;

    }
    public int numberOfTasksByState(int stateId) {
        return taskDao.countNumberOfTasksByState(stateId);
    }

    public double averageTimeToFinishTask() {
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
        }

        return average;
    }
}
