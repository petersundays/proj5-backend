package backend.proj5.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Table(name="task")
@NamedQuery(name="Task.findTaskById", query="SELECT a FROM TaskEntity a WHERE a.id = :id ")
@NamedQuery(name="Task.findTasksByUser", query="SELECT a FROM TaskEntity a WHERE a.owner = :owner ORDER BY a.priority DESC, a.startDate ASC, a.limitDate ASC")
@NamedQuery(name="Task.findTasksByCategory", query="SELECT a FROM TaskEntity a WHERE a.category = :category ORDER BY a.priority DESC, a.startDate ASC, a.limitDate ASC")
@NamedQuery(name="Task.findNotErasedTasks", query="SELECT a FROM TaskEntity a WHERE a.erased = false ORDER BY a.priority DESC, a.startDate ASC, a.limitDate ASC")
@NamedQuery(name="Task.findErasedTasks", query="SELECT a FROM TaskEntity a WHERE a.erased = true ORDER BY a.priority DESC, a.startDate ASC, a.limitDate ASC")
@NamedQuery(name="Task.findAllErasedTasksFromUser", query="SELECT a FROM TaskEntity a WHERE a.owner = :owner AND a.erased = true ORDER BY a.priority DESC, a.startDate ASC, a.limitDate ASC")
@NamedQuery(name="Task.findAllTasks", query="SELECT a FROM TaskEntity a ORDER BY a.priority DESC, a.startDate ASC, a.limitDate ASC")
@NamedQuery(name="DeleteTask", query="DELETE FROM TaskEntity a WHERE a.id = :id")
@NamedQuery(name="Task.deleteAllErasedTasksFromUser", query="DELETE FROM TaskEntity a WHERE a.owner = :owner AND a.erased = true")
@NamedQuery(name="Task.deleteAllErasedTasks", query="DELETE FROM TaskEntity a WHERE a.erased = true")
@NamedQuery(name="Tasks.restoreAllErasedTasks", query="UPDATE TaskEntity a SET a.erased = false WHERE a.erased = true")
@NamedQuery(name="Task.eraseAllNotErasedTasks", query="UPDATE TaskEntity a SET a.erased = true WHERE a.erased = false")
@NamedQuery(name="Task.numberOfTasksFromUser", query="SELECT COUNT(a) FROM TaskEntity a WHERE a.owner = :owner")
@NamedQuery(name="Task.numberOfTasksFromUserByState", query="SELECT COUNT(a) FROM TaskEntity a WHERE a.owner = :owner AND a.stateId = :stateId")
@NamedQuery(name="Task.countNumberOfNotErasedTasks", query="SELECT COUNT(a) FROM TaskEntity a WHERE a.erased = false")
@NamedQuery(name="Task.countNumberOfTasksByState", query="SELECT COUNT(a) FROM TaskEntity a WHERE a.stateId = :stateId")
@NamedQuery(name="Task.findTasksByStateId", query="SELECT a FROM TaskEntity a WHERE a.stateId = :stateId ORDER BY a.priority DESC, a.startDate ASC, a.limitDate ASC")
@NamedQuery(name="Task.totalTasksDoneByEachDay", query="SELECT a.conclusionDate, (SELECT COUNT(b) FROM TaskEntity b WHERE b.stateId = 300 AND b.conclusionDate <= a.conclusionDate) FROM TaskEntity a WHERE a.stateId = 300 AND a.erased = false GROUP BY a.conclusionDate ORDER BY a.conclusionDate DESC")

public class TaskEntity implements Serializable{

    private static final long serialVersionUID = 1L;

    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name="id", nullable = false, unique = true, updatable = false)
    private String id;

    @Column (name="title", nullable = false, unique = false, length = 100)
    private String title;

    @Column (name="description", nullable = false, unique = false, length = 20000, columnDefinition = "TEXT")
    private String description;

    @Column (name="stateId", nullable = false, unique = false, updatable = true)
    private int stateId;

    @Column (name="priority", nullable = false, unique = false, updatable = true)
    private int priority;

    @CreationTimestamp
    @Column (name="creation_date", nullable = false, unique = false, updatable = false)
    private Timestamp creationDate;

    @Column (name="startDate", nullable = false, unique = false, updatable = true)
    private LocalDate startDate;

    @Column (name="limitDate", nullable = false, unique = false, updatable = true)
    private LocalDate limitDate;
    @Column (name="conclusionDate", nullable = true, unique = false, updatable = true)
    private LocalDate conclusionDate;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private CategoryEntity category;

    @Column (name="erased", nullable = false, unique = false, updatable = true)
    private boolean erased;

    //Owning Side User - task
    @ManyToOne
    @JoinColumn(name = "owner", referencedColumnName = "username")
    private UserEntity owner;


    public TaskEntity() {

    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Timestamp getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate)
    {
        this.creationDate = creationDate;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getLimitDate() {
        return limitDate;
    }

    public void setLimitDate(LocalDate limitDate) {
        this.limitDate = limitDate;
    }

    public LocalDate getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(LocalDate conclusionDate) {
        this.conclusionDate = conclusionDate;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public boolean getErased() {
        return erased;
    }

    public void setErased(boolean erased) {
        this.erased = erased;
    }
}
