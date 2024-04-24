package backend.proj5.dto;

public class TaskToSendWS {
    private String actionToDo;
    private String id;
    private Task task;
    public static final String ADD = "add";
    public static final String DELETE = "delete";
    public static final String UPDATE = "update";

    public TaskToSendWS() {
    }

    public TaskToSendWS(String actionToDo, String id) {
        this.actionToDo = actionToDo;
        this.id = id;
    }

    public TaskToSendWS(String actionToDo, Task task) {
        this.actionToDo = actionToDo;
        this.task = task;
    }

    public String getActionToDo() {
        return actionToDo;
    }

    public void setActionToDo(String actionToDo) {
        this.actionToDo = actionToDo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

}
