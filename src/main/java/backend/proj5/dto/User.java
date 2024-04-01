package backend.proj5.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class User {
    @XmlElement
    private String username;
    @XmlElement
    private String password;
    @XmlElement
    private String email;
    @XmlElement
    private String firstName;
    @XmlElement
    private String lastName;
    @XmlElement
    private String phone;
    @XmlElement
    private String photoURL;
    @XmlElement
    private boolean visible;
    @XmlElement
    private String token;
    @XmlElement
    int typeOfUser;
    @XmlElement
    int numberOfTasks;
    @XmlElement
    boolean confirmed;
    @XmlElement
    public static final int DEVELOPER = 100;
    @XmlElement
    public static final int SCRUMMASTER = 200;
    @XmlElement
    public static final int PRODUCTOWNER = 300;
    @XmlElement
    public static final int NOTASSIGNED = 400;
    public User() {
    }


    public String getUsername() {
        return username;
    }


    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public boolean isVisible() { return visible; }

    public void setVisible(boolean visible) { this.visible = visible; }

    public int getTypeOfUser() { return typeOfUser; }

    public void setTypeOfUser(int typeOfUser) { this.typeOfUser = typeOfUser; }

    public void setInitialTypeOfUser() {
        this.typeOfUser = DEVELOPER;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getNumberOfTasks() {
        return numberOfTasks;
    }

    public void setNumberOfTasks(int numberOfTasks) {
        this.numberOfTasks = numberOfTasks;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", photoURL='" + photoURL +
                '}';
    }
}