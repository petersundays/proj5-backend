package backend.proj5.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

@Entity
@Table(name="user")
@NamedQuery(name = "User.findAllUsers", query = "SELECT u FROM UserEntity u")
@NamedQuery(name = "User.findAllUsersByTypeOfUser", query = "SELECT u FROM UserEntity u WHERE u.typeOfUser = :typeOfUser")
@NamedQuery(name = "User.findAllUsersByVisibility", query = "SELECT u FROM UserEntity u WHERE u.visible = :visible")
@NamedQuery(name = "User.findAllUsersByTypeOfUserByVisibility", query = "SELECT u FROM UserEntity u WHERE u.typeOfUser = :typeOfUser AND u.visible = :visible")
@NamedQuery(name = "User.findUserByUsername", query = "SELECT u FROM UserEntity u WHERE u.username = :username")
@NamedQuery(name = "User.findUserByEmail", query = "SELECT u FROM UserEntity u WHERE u.email = :email")
@NamedQuery(name = "User.findUserByPhone", query = "SELECT  u FROM UserEntity u WHERE u.phone = :phone")
@NamedQuery(name = "User.findUserByToken", query = "SELECT DISTINCT u FROM UserEntity u WHERE u.token = :token")
@NamedQuery(name = "User.findUserByUsernameAndPassword", query = "SELECT u FROM UserEntity u WHERE u.username = :username AND u.password = :password")
@NamedQuery(name = "User.doesUserHavePasswordDefined", query = "SELECT CASE WHEN (u.password IS NULL OR TRIM(u.password) = '') THEN false ELSE true END FROM UserEntity u WHERE u.validationToken = :validationToken")
@NamedQuery(name = "User.findUserByValidationToken", query = "SELECT u FROM UserEntity u WHERE u.validationToken = :validationToken")
@NamedQuery(name = "User.countAllUsers", query = "SELECT COUNT(u) FROM UserEntity u")
@NamedQuery(name = "User.countAllUsersByVisibility", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.visible = :visible")
@NamedQuery(name = "User.countAllUsersByConfirmed", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.confirmed = :confirmed")
@NamedQuery(name = "User.totalUsersRegisteredByEachDay", query = "SELECT u.registrationDate, (SELECT COUNT(v) FROM UserEntity v WHERE v.visible = true AND v.confirmed = true AND v.registrationDate <= u.registrationDate) FROM UserEntity u WHERE u.visible = true AND u.confirmed = true GROUP BY u.registrationDate ORDER BY u.registrationDate DESC ")
@NamedQuery(name = "User.updateLastAccess", query = "UPDATE UserEntity u SET u.lastAccess = :lastAccess WHERE u.username = :username")
@NamedQuery(name = "User.findLastAccess", query = "SELECT u.lastAccess FROM UserEntity u WHERE u.username = :username")

public class UserEntity implements Serializable{

    private static final long serialVersionUID = 1L;

    //user unique username has ID - not updatable, unique, not null
    @Id
    @Column(name="username", nullable=false, unique = true, updatable = false)
    private String username;

    @Column(name="password")
    private String password;

    @Column(name="type_of_user", nullable=false, unique = false, updatable = true)
    private int typeOfUser;

    @Column(name="email", nullable=false, unique = true, updatable = true)
    private String email;

    @Column(name="first_name", nullable=false, unique = false, updatable = true)
    private String firstName;

    @Column(name="last_name", nullable=false, unique = false, updatable = true)
    private String lastName;

    @Column(name="phone", nullable=false, unique = true, updatable = true)
    private String phone;

    @Column(name="photo_url", nullable=false, unique = false, updatable = true)
    private String photoURL;

    @Column(name="registrationDate", nullable = false, updatable = false)
    private LocalDate registrationDate;

    @Column(name="token", nullable=true, unique = true, updatable = true)
    private String token;

    @Column(name="validationToken", nullable=true, unique = true, updatable = true)
    private String validationToken;

    @Column(name="visible", nullable = false, unique = false, updatable = true)
    private boolean visible;

    @Column(name="confirmed", nullable = false, unique = false, updatable = true)
    private boolean confirmed;

    @Column(name="last_access", nullable = true, unique = false, updatable = true)
    private LocalDateTime lastAccess;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<TaskEntity> userTasks;

    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<MessageEntity> sentMessages;

    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<MessageEntity> receivedMessages;

    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<NotificationEntity> notifications;


    //default empty constructor
    public UserEntity() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTypeOfUser() {
        return typeOfUser;
    }

    public void setTypeOfUser(int typeOfUser) {
        this.typeOfUser = typeOfUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<TaskEntity> getUserTasks() {
        return userTasks;
    }

    public void setUserTasks(Set<TaskEntity> userTasks) {
        this.userTasks = userTasks;
    }

    public Set<MessageEntity> getSentMessages() {
        return sentMessages;
    }

    public void setSentMessages(Set<MessageEntity> sentMessages) {
        this.sentMessages = sentMessages;
    }

    public Set<MessageEntity> getReceivedMessages() {
        return receivedMessages;
    }

    public void setReceivedMessages(Set<MessageEntity> receivedMessages) {
        this.receivedMessages = receivedMessages;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    @PrePersist
    public void setRegistrationDate() {
        this.registrationDate = LocalDate.now();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getValidationToken() {
        return validationToken;
    }

    public void setValidationToken(String validationToken) {
        this.validationToken = validationToken;
    }

    public boolean isVisible() {return visible;}

    public void setVisible(boolean visivel) {this.visible = visivel;}

    public boolean isConfirmed() {return confirmed;}

    public void setConfirmed(boolean confirmed) {this.confirmed = confirmed;}

    public LocalDateTime getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(LocalDateTime lastAccess) {
        this.lastAccess = lastAccess;
    }

    public void addNewTasks (ArrayList<TaskEntity> tasks) {
        userTasks.addAll(tasks);
    }

    public void addNewSentMessages (ArrayList<MessageEntity> messages) {
        sentMessages.addAll(messages);
    }

    public void addNewReceivedMessages (ArrayList<MessageEntity> messages) {
        receivedMessages.addAll(messages);
    }

    public void addNewNotifications (ArrayList<NotificationEntity> notifications) {
        this.notifications.addAll(notifications);
    }
}
