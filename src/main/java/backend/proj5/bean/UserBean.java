package backend.proj5.bean;

import backend.proj5.dao.TaskDao;
import backend.proj5.dao.UserDao;
import backend.proj5.dto.*;
import backend.proj5.entity.TaskEntity;
import backend.proj5.entity.UserEntity;
import backend.proj5.websocket.NotifierWS;
import com.google.gson.Gson;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Stateless
public class UserBean implements Serializable {

    @EJB
    private UserDao userDao;
    @EJB
    private TaskDao taskDao;
    @EJB
    private TaskBean taskBean;
    @EJB
    private EmailBean emailBean;
    @EJB
    private CategoryBean categoryBean;
    @EJB
    NotifierWS notifierWS;

    private static final Logger logger = LogManager.getLogger(TaskBean.class);


    public UserBean(){}

    public UserBean(UserDao userDao) {
        this.userDao = userDao;
    }

    public void createDefaultUsersIfNotExistent() {
        logger.info("Creating default users if not existent");

        UserEntity userEntity = userDao.findUserByUsername("admin");
        if (userEntity == null) {
            logger.info("Creating admin user");

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin");
            admin.setEmail("admin@admin.com");
            admin.setFirstName("admin");
            admin.setLastName("admin");
            admin.setPhone("123456789");
            admin.setPhotoURL("https://www.pngitem.com/pimgs/m/146-1468479_my-profile-icon-blank-profile-picture-circle-hd.png");
            admin.setVisible(false);
            admin.setConfirmed(true);

            register(admin);
            logger.info("Admin user created");
        }

        UserEntity userEntity2 = userDao.findUserByUsername("NOTASSIGNED");
        if (userEntity2 == null) {
            logger.info("Creating NOTASSIGNED user");

            User deletedUser = new User();
            deletedUser.setUsername("NOTASSIGNED");
            deletedUser.setPassword("123");
            deletedUser.setEmail("deleted@user.com");
            deletedUser.setFirstName("Deleted");
            deletedUser.setLastName("User");
            deletedUser.setPhone("123456788");
            deletedUser.setPhotoURL("https://www.pngitem.com/pimgs/m/146-1468479_my-profile-icon-blank-profile-picture-circle-hd.png");
            deletedUser.setTypeOfUser(400);
            deletedUser.setVisible(false);
            deletedUser.setConfirmed(true);

            register(deletedUser);
            logger.info("NOTASSIGNED user created");

        }
    }

    //Permite ao utilizador entrar na app, gera token
    public User login(Login user) {
        logger.info("Logging in user: {}", user.getUsername());
        UserEntity userEntity = userDao.findUserByUsername(user.getUsername());

        if (userEntity != null && userEntity.isVisible() && userEntity.isConfirmed()) {
            logger.info("User found {} and is visible and confirmed", user.getUsername());

            //Verifica se a password coincide com a password encriptada
            if (BCrypt.checkpw(user.getPassword(), userEntity.getPassword())) {

                logger.info("Password matches for user: {}", user.getUsername());

                String token = generateNewToken();
                userEntity.setToken(token);
                userDao.updateLastAccess(userEntity.getUsername(), LocalDateTime.now());
                User userDto = convertUserEntitytoUserDto(userEntity);
                return createUserLogged(userDto);
            } else {
                logger.error("Password does not match for user: {}", user.getUsername());
            }

        } else if (userEntity != null && userEntity.isVisible() && !userEntity.isConfirmed()) {
            logger.error("User found {} but is not confirmed", user.getUsername());

            if (userEntity.getPassword().trim().isEmpty()) {
                logger.info("User {} has no password defined", user.getUsername());
                return convertUserEntitytoUserDto(userEntity);

            } else {
                logger.error("User {} has a password defined", user.getUsername());

                if (BCrypt.checkpw(user.getPassword(), userEntity.getPassword())) {
                    logger.info("Password matches for user: {}", user.getUsername());
                    return convertUserEntitytoUserDto(userEntity);
                }
            }
        } else {
            logger.error("User not found or is not visible or confirmed: {}", user.getUsername());
        }

        return null;
    }

    //Faz o registo do utilizador, adiciona à base de dados
    public boolean register(User user) {

        if (user != null) {
            logger.info("Registering user: {}", user.getUsername());

            if (user.getUsername().equalsIgnoreCase("notAssigned")) {
                user.setUsername(user.getUsername().toUpperCase());
                user.setTypeOfUser(User.NOTASSIGNED);
                user.setVisible(false);
                user.setConfirmed(true);

                //Encripta a password usando BCrypt
                String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

                //Define a password encriptada
                user.setPassword(hashedPassword);

                //Persist o user
                userDao.persist(convertUserDtotoUserEntity(user));

                logger.info("User registered: {}", user.getUsername());

                return true;
            } else {
                if (user.getUsername().equals("admin")){
                    user.setTypeOfUser(300);
                    user.setVisible(true);
                    user.setConfirmed(true);

                } else {
                    if (user.getTypeOfUser() != 100 && user.getTypeOfUser() != 200 && user.getTypeOfUser() != 300) {
                        user.setInitialTypeOfUser();

                    } else {
                        if (user.getTypeOfUser() == 100) {
                            user.setTypeOfUser(User.DEVELOPER);
                        } else if (user.getTypeOfUser() == 200) {
                            user.setTypeOfUser(User.SCRUMMASTER);
                        } else if (user.getTypeOfUser() == 300) {
                            user.setTypeOfUser(User.PRODUCTOWNER);
                        }
                    }

                    user.setVisible(true);
                    user.setConfirmed(false);
                }

                logger.info("User type set to: {}", user.getTypeOfUser());

                if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().isBlank()) {
                    user.setPassword("");
                    logger.info("User {} has no password defined", user.getUsername());

                } else {

                    //Encripta a password usando BCrypt
                    String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

                    //Define a password encriptada
                    user.setPassword(hashedPassword);
                    logger.info("Password defined for user: {}", user.getUsername());
                }

                UserEntity userEntity = convertUserDtotoUserEntity(user);
                if (userEntity.getUsername().equalsIgnoreCase("admin")) {
                    userDao.persist(userEntity);
                    logger.info("Admin user registered: {}", user.getUsername());

                } else {
                    userEntity.setValidationToken(generateValidationToken(48 * 60));

                    if (emailBean.sendConfirmationEmail(user, userEntity.getValidationToken())) {
                        userDao.persist(userEntity);
                        logger.info("Confirmation email sent and user registered: {}", user.getUsername());
                        return true;
                    } else {
                        logger.error("Confirmation email not sent and user not registered: {}", user.getUsername());
                    }
                }
            }
        } else {
            return false;
        }
        logger.error("User not registered: {}", user.getUsername());
        return false;
    }


    //Apaga todos os registos do utilizador da base de dados
    public boolean delete(String username) {
        logger.info("Deleting user: {}", username);

        UserEntity u = userDao.findUserByUsername(username);

        if (u != null) {
            logger.info("User found: {}", username);
            ArrayList<TaskEntity> tasks = taskDao.findTasksByUser(u);
            UserEntity notAssigned = userDao.findUserByUsername("NOTASSIGNED");

            notAssigned.addNewTasks(tasks);

            for (TaskEntity t : tasks) {
                t.setOwner(notAssigned);
                taskDao.merge(t);
                logger.info("Task {} assigned to NOTASSIGNED", t.getId());
            }
            userDao.remove(u);
            logger.info("User deleted: {}", username);

            return true;
        } else {
            logger.error("User not found: {}", username);
        }
        return false;
    }


    //Métodos de conversão

    public UserEntity convertUserDtotoUserEntity(User user) {
        logger.info("Converting user DTO to entity");

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(user.getPassword());
        userEntity.setTypeOfUser(user.getTypeOfUser());
        userEntity.setEmail(user.getEmail());
        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        userEntity.setPhone(user.getPhone());
        userEntity.setPhotoURL(user.getPhotoURL());
        userEntity.setVisible(user.isVisible());
        userEntity.setConfirmed(user.isConfirmed());

        return userEntity;
    }

    public User convertUserEntitytoUserDto(UserEntity userEntity) {
        logger.info("Converting user entity to DTO");

        User user = new User();
        user.setUsername(userEntity.getUsername());
        user.setPassword(userEntity.getPassword());
        user.setTypeOfUser(userEntity.getTypeOfUser());
        user.setEmail(userEntity.getEmail());
        user.setFirstName(userEntity.getFirstName());
        user.setLastName(userEntity.getLastName());
        user.setPhone(userEntity.getPhone());
        user.setPhotoURL(userEntity.getPhotoURL());
        user.setVisible(userEntity.isVisible());
        user.setNumberOfTasks(taskBean.getNumberOfTasksFromUser(userEntity.getUsername()));
        user.setConfirmed(userEntity.isConfirmed());
        if (userEntity.getToken() != null) {
            user.setToken(userEntity.getToken());
        } else {
            user.setToken(null);
        }

        return user;
    }


    //Gerar token
    private String generateNewToken() {
        logger.info("Generating new token");

        SecureRandom secureRandom = new SecureRandom(); //threadsafe
        Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);

        return base64Encoder.encodeToString(randomBytes);
    }

    private String generateValidationToken(int expirationMinutes) {
        logger.info("Generating validation token");

        SecureRandom secureRandom = new SecureRandom(); //threadsafe
        Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        String token = base64Encoder.encodeToString(randomBytes);

        Instant expirationTime = Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES);
        token += "|" + expirationTime.toEpochMilli();

        return token;
    }

    // Validate token and check if it's expired
    public boolean isValidationTokenValid(String token) {
        logger.info("Validating validation token");

        if (token == null || token.isEmpty()) {
            logger.error("Validation token is null or empty");
            return false;
        }

        // Split token and expiry timestamp
        String[] parts = token.split("\\|");
        if (parts.length != 2) {
            logger.error("Validation token is invalid");
            return false;
        }

        try {
            // Parse expiry timestamp
            long expiryTimestamp = Long.parseLong(parts[1]);
            Instant expiryTime = Instant.ofEpochMilli(expiryTimestamp);

            logger.info("Validation token expiry time: {}", expiryTime);

            // Check if current time is before expiry time
            return Instant.now().isBefore(expiryTime);
        } catch (NumberFormatException e) {
            logger.error("Validation token is invalid");
            return false;
        }
    }


    //Logout
    public boolean logout(String token) {
        UserEntity u = userDao.findUserByToken(token);

        if (u != null) {
            logger.info("Logging out user: {}", u.getUsername());

            u.setToken(null);
            u.setLastAccess(null);
            return true;
        } else {
            logger.error("User not found");
        }
        return false;
    }

    public void timeoutLogout (String token) {
        logger.info("Timeout logout");

        UserEntity u = userDao.findUserByToken(token);
        if (u != null) {
            logger.info("Logging out user {} due to session timeout", u.getUsername());

            u.setToken(null);
            u.setLastAccess(null);
            userDao.merge(u);
            Notification timeoutNotification = new Notification(Notification.EXPIRED);
            notifierWS.send(token, new Gson().toJson(timeoutNotification));
        }
    }

    public ArrayList<User> getUsers() {
        logger.info("Finding all users");

        ArrayList<UserEntity> userEntities = userDao.findAllUsers();
        if (userEntities != null) {
            logger.info("Users found");

            ArrayList<User> users = new ArrayList<>();
            for (UserEntity userE : userEntities) {
                if (userE.isConfirmed()) {
                    if (userE.getTypeOfUser() != 400 && !userE.getUsername().equalsIgnoreCase("admin")) {
                        users.add(convertUserEntitytoUserDto(userE));
                    }
                }
            }
            return users;
        }
        //Retorna uma lista vazia se não forem encontrados users
        return new ArrayList<>();
    }

    public ArrayList<UserEntity> findAllUsers() {
        logger.info("Finding all users");

        ArrayList<UserEntity> userEntities = userDao.findAllUsers();
        if (userEntities != null) {
            logger.info("Users found");
            return userEntities;
        }
        //Retorna uma lista vazia se não forem encontrados users
        return new ArrayList<>();
    }

    //Receber users pelo tipo de user
    public ArrayList<User> getUsersByType(int typeOfUser) {
        logger.info("Finding users by type: {}", typeOfUser);

        ArrayList<UserEntity> userEntities = userDao.findAllUsersByTypeOfUser(typeOfUser);
        if (userEntities != null) {
            logger.info("Users found by type: {}", typeOfUser);
            ArrayList<User> users = new ArrayList<>();
            for (UserEntity userE : userEntities) {

                users.add(convertUserEntitytoUserDto(userE));

            }
            return users;
        }
        //Retorna uma lista vazia se não forem encontradas tarefas
        return new ArrayList<>();
    }

    //Receber users pelo tipo de visibilidade
    public ArrayList<User> getUsersByVisibility(boolean visible) {
        logger.info("Finding users by visibility: {}", visible);

        ArrayList<UserEntity> userEntities = userDao.findAllUsersByVisibility(visible);
        if (userEntities != null) {
            logger.info("Users found by visibility: {}", visible);
            ArrayList<User> users = new ArrayList<>();
            for (UserEntity userE : userEntities) {

                users.add(convertUserEntitytoUserDto(userE));

            }
            return users;
        }
        //Retorna uma lista vazia se não forem encontradas tarefas
        return new ArrayList<>();
    }

    //Receber users pelo tipo de user e de visibilidade
    public ArrayList<User> getUsersByTypeAndVisibility(int typeOfUser, boolean visible) {
        logger.info("Finding users by type: {} and visibility: {}", typeOfUser, visible);

        ArrayList<UserEntity> userEntities = userDao.findAllUsersByTypeOfUserAndVisibility(typeOfUser, visible);
        if (userEntities != null) {
            logger.info("Users found by type: {} and visibility: {}", typeOfUser, visible);
            ArrayList<User> users = new ArrayList<>();
            for (UserEntity userE : userEntities) {

                users.add(convertUserEntitytoUserDto(userE));

            }
            return users;
        }
        //Retorna uma lista vazia se não forem encontradas tarefas
        return new ArrayList<>();
    }

    public User getUser(String username, String token) {
        logger.info("Finding user: {}", username);

        UserEntity u = userDao.findUserByUsername(username);
        UserEntity userLogged = userDao.findUserByToken(token);

        if (u !=null) {
            if ((u.getTypeOfUser() == 400 || !u.isVisible() || !u.isConfirmed()) && userLogged.getTypeOfUser() != 300) {
                logger.error("The user is not visible or confirmed or logged user is not a product owner");
                return convertUserEntitytoUserDto(userLogged);
            } else {
                logger.info("User found: {}", username);
                return convertUserEntitytoUserDto(u);
            }
        } else {
            logger.error("User not found: {}", username);
            return null;
        }
    }

    public User getUserByUsername(String username) {
        logger.info("Finding user by username: {}", username);
        UserEntity u = userDao.findUserByUsername(username);
        if (u != null) {
            logger.info("User found by username: {}", username);
            return convertUserEntitytoUserDto(u);
        }
        logger.error("User not found by username: {}", username);
        return null;
    }

    public User findUserByToken (String token) {
        logger.info("Finding user by token: {}", token);
        UserEntity userEntity = userDao.findUserByToken(token);
        if (userEntity != null && userEntity.isVisible() && userEntity.isConfirmed()) {
            logger.info("User found by token: {}", token);
            return convertUserEntitytoUserDto(userEntity);
        }
        logger.error("User not found or is not visible or confirmed");
        return null;
    }

    public User createUserLogged(User user) {
        logger.info("Creating user logged");

        User userLogged = new User();
        userLogged.setUsername(user.getUsername());
        userLogged.setEmail(user.getEmail());
        userLogged.setFirstName(user.getFirstName());
        userLogged.setLastName(user.getLastName());
        userLogged.setPhone(user.getPhone());
        userLogged.setPhotoURL(user.getPhotoURL());
        userLogged.setTypeOfUser(user.getTypeOfUser());
        userLogged.setToken(user.getToken());
        userLogged.setVisible(user.isVisible());
        userLogged.setConfirmed(user.isConfirmed());

        return userLogged;
    }

    public boolean updateUser(User user, String username) {
        logger.info("Updating user: {}", username);

        boolean status = false;

        UserEntity u = userDao.findUserByUsername(username);

        if (u != null && u.getUsername().equals(username)){

            logger.info("User found: {}", username);

            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                u.setEmail(user.getEmail());
            }

            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                u.setPhone(user.getPhone());
            }

            if (user.getFirstName() != null && !user.getFirstName().isEmpty()) {
                u.setFirstName(user.getFirstName());
            }

            if (user.getLastName() != null && !user.getLastName().isEmpty()) {
                u.setLastName(user.getLastName());
            }

            if (user.getPhotoURL() != null && !user.getPhotoURL().isEmpty()) {
                u.setPhotoURL(user.getPhotoURL());
            }

            if (user.getTypeOfUser() != 0) {
                u.setTypeOfUser(user.getTypeOfUser());
            }

            try{
                userDao.merge(u);
                status = true;
                logger.info("User updated: {}", username);
            } catch (Exception e){
                e.printStackTrace();
                logger.error("User not updated: {}", username);
            }
        }

        return status;
    }

    public boolean updateUserEntityVisibility(String username) {
        logger.info("Updating user visibility: {}", username);
        boolean status = false;

        UserEntity u = userDao.findUserByUsername(username);

        if (u != null){

            u.setVisible(!u.isVisible());

            status = true;
            logger.info("User visibility updated: {}", username);
        } else {
            logger.error("User not found: {}", username);
        }

        return status;
    }

    public int updateUserEntityConfirmation(String validationToken) {
        logger.info("Updating user confirmation");
        boolean isValidationTokenValid = isValidationTokenValid(validationToken);

        UserEntity u = userDao.findUserByValidationToken(validationToken);

        if (!isValidationTokenValid) {
            userDao.remove(u);
            logger.error("User not confirmed: token expired");
            return 0; // token expired
        }

        if (u == null) {
            logger.error("User not found: token invalid");
            return 0; // user not found
        } else if (u.isConfirmed()) {
            logger.error("User already confirmed: {}", u.getUsername());
            return 2; // user was already confirmed
        } else {
            u.setConfirmed(true);
            u.setValidationToken(null);
            logger.info("User confirmed: {}", u.getUsername());
            return 1; // user successfully confirmed
        }
    }

    public boolean isAuthenticated(String token) {
        logger.info("Checking if user is authenticated");

        boolean validUser = false;
        UserEntity user = userDao.findUserByToken(token);
        if (user != null && user.isVisible() && user.isConfirmed()) {
            logger.info("User is authenticated");
            int sessionTimeout = userDao.getSessionTimeout();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastAccess = user.getLastAccess();
            long minutes = ChronoUnit.MINUTES.between(lastAccess, now);

            if (minutes < sessionTimeout) {
                user.setLastAccess(now);
                userDao.merge(user);
                validUser = true;
                logger.info("User is still authenticated");
            } else {
                timeoutLogout(token);
                logger.error("User is not authenticated: session timeout");
            }
        }

        return validUser;
    }

    public boolean isUsernameAvailable(User user) {
        logger.info("Checking if username is available");

        UserEntity u = userDao.findUserByUsername(user.getUsername());
        boolean status = false;

        if (u == null) {
            status = true;
            logger.info("Username is available: {}", user.getUsername());
        } else {
            logger.error("Username is not available: {}", user.getUsername());
        }

        return status;
    }

    public boolean isEmailFormatValid(String email) {
        logger.info("Checking if email format is valid");
        // Use a regular expression to perform email format validation
        // This regex is a basic example and may need to be adjusted
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    public boolean isEmailValid(User user) {
        logger.info("Checking if email is valid");

        UserEntity u = userDao.findUserByEmail(user.getEmail());
        // Check if the email format is valid
        if (isEmailFormatValid(user.getEmail()) && u == null) {
            logger.info("Email is valid: {}", user.getEmail());
            return true;
        }

        logger.error("Email is not valid: {}", user.getEmail());
        return false;
    }

    public boolean isEmailUpdatedValid(User user) {
        logger.info("Checking if email update is valid");

        //Se for null é porque não houve nenhuma atualização
        if (user.getEmail() == null || user.getEmail().isEmpty() || user.getEmail().isBlank()){
            logger.info("Email update is valid: no update");
            return true;
        }

        UserEntity u = userDao.findUserByEmail(user.getEmail());
        // Check if the email format is valid
        if ((isEmailFormatValid(user.getEmail()) && u == null) || (u != null && u.getEmail().equals(user.getEmail()))) {
            logger.info("Email update is valid: {}", user.getEmail());
            return true;
        }

        logger.error("Email update is not valid: {}", user.getEmail());
        return false;
    }


    public boolean isAnyFieldEmpty(User user) {
        boolean status = false;

        if (user.getUsername().isEmpty() ||
                user.getEmail().isEmpty() ||
                user.getFirstName().isEmpty() ||
                user.getLastName().isEmpty() ||
                user.getPhone().isEmpty() ||
                user.getPhotoURL().isEmpty()) {
            status = true;
        }
        return status;
    }

    public boolean isPhoneNumberValid(User user) {
        logger.info("Checking if phone number is valid");

        boolean status = true;
        int i = 0;

        UserEntity u = userDao.findUserByPhone(user.getPhone());

         while (status && i < user.getPhone().length() - 1) {
            if (user.getPhone().length() == 9) {
                for (; i < user.getPhone().length(); i++) {
                    if (!Character.isDigit(user.getPhone().charAt(i))) {
                        status = false;
                        logger.error("Phone number is not valid due to character: {}", user.getPhone());
                    }
                }
            } else {
                status = false;
                logger.error("Phone number is not valid due to length: {}", user.getPhone());
            }
        }

        //Se existir contacto na base de dados retorna false
        if (u != null) {
            status = false;
            logger.error("User with phone number already exists: {}", user.getPhone());
        }

        logger.info("Phone number is valid: {}", user.getPhone());
        return status;
    }

    public boolean isPhoneNumberUpdatedValid(User user) {
        logger.info("Checking if phone number update is valid");
        boolean status = true;

        //Se for null é porque não houve nenhuma atualização
        if (user.getPhone()==null){
            logger.info("Phone number update is valid: no update");
            return true;
        }

        int i = 0;

        UserEntity u = userDao.findUserByPhone(user.getPhone());
        if (u != null && u.getPhone().equals(user.getPhone())) {
            return true;
        }

        while (status && i < user.getPhone().length() - 1) {
            if (user.getPhone().length() == 9) {
                for (; i < user.getPhone().length(); i++) {
                    if (!Character.isDigit(user.getPhone().charAt(i))) {
                        status = false;
                        logger.error("Phone number update is not valid due to character: {}", user.getPhone());
                    }
                }
            } else {
                status = false;
                logger.error("Phone number update is not valid due to length: {}", user.getPhone());
            }
        }

        //Se existir contacto na base de dados retorna false
        if (u != null) {
            status = false;
            logger.error("User with phone number already exists: {}", user.getPhone());
        }

        return status;
    }

    public void validatePhotoUrl(User user) {
        logger.info("Validating photo URL");

        String defaultUrl = "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcTWfm4QX7YF7orMboLv4jjuwoYgd85bKBqeiBHLOfS6MgfHUW-d";
        String url = user.getPhotoURL();

        if (url == null || url.isEmpty() || url.isBlank()){
            user.setPhotoURL(defaultUrl);
            logger.info("Photo URL is empty, setting default photo");
        } else {
            try {
                BufferedImage img = ImageIO.read(new URL(url));
                if (img == null) {
                    user.setPhotoURL(defaultUrl);
                    logger.error("Photo URL is invalid, setting default photo");
                }
            } catch (IOException e) {
                user.setPhotoURL(defaultUrl);
                logger.error("Photo URL is not a valid Image URL, setting default photo");
            }
        }

    }

    public boolean isImageUrlUpdatedValid(String url) {
        logger.info("Checking if image URL update is valid");
        boolean status = true;

        //Se for null é porque não houve nenhuma alteração
        if (url == null) {
            logger.info("Image URL update is valid: no update");
            return true;
        }

        try {
            BufferedImage img = ImageIO.read(new URL(url));
            if (img == null) {
                status = false;
                logger.error("Image URL update is not valid: {}", url);
            }
        } catch (IOException e) {
            status = false;
            logger.error("Image URL update is not valid: {}", url);
        }

        logger.info("Image URL update is valid: {}", url);

        return status;
    }


    public boolean userIsTaskOwner(String token, String id) {
        logger.info("Checking if user is task owner");

        UserEntity userEntity = userDao.findUserByToken(token);
        TaskEntity taskEntity = taskDao.findTaskById(id);
        boolean authorized = false;
        if (userEntity != null) {
            logger.info("User found: {}", userEntity.getUsername());
            if (taskEntity.getOwner().getUsername().equals(userEntity.getUsername())) {
                authorized = true;
                logger.info("User is task owner");
            } else {
                logger.error("User is not task owner");
            }
        } else {
            logger.error("User not found");
        }
        return authorized;
    }

    public boolean userIsDeveloper(String token) {
        logger.info("Checking if user is developer");
        UserEntity userEntity = userDao.findUserByToken(token);
        boolean authorized = false;

        if (userEntity != null) {
            logger.info("User found: {}", userEntity.getUsername());
            if (userEntity.getTypeOfUser() == User.DEVELOPER) {
                authorized = true;
                logger.info("User is developer");
            } else {
                logger.error("User is not developer");
            }
        } else {
            logger.error("User not found");
        }
        return authorized;
    }

    public boolean userIsScrumMaster(String token) {
        logger.info("Checking if user is scrum master");
        UserEntity userEntity = userDao.findUserByToken(token);
        boolean authorized = false;

        if (userEntity != null) {
        logger.info("User found: {}", userEntity.getUsername());
            if (userEntity.getTypeOfUser() == User.SCRUMMASTER) {
                authorized = true;
                logger.info("User is scrum master");
            } else {
                logger.error("User is not scrum master");
            }
        } else {
            logger.error("User not found");
        }

        return authorized;
    }

    public boolean userIsProductOwner(String token) {
        logger.info("Checking if user is product owner");
        UserEntity userEntity = userDao.findUserByToken(token);
        boolean authorized = false;

        if (userEntity != null) {
            logger.info("User found: {}", userEntity.getUsername());
            if (userEntity.getTypeOfUser() == User.PRODUCTOWNER) {
                authorized = true;
                logger.info("User is product owner");
            } else {
                logger.error("User is not product owner");
            }
        } else {
            logger.error("User not found");
        }

        return authorized;
    }


    //Converte a Entidade com o email "email" para DTO
    public User convertEntityByEmail (String email) {
        logger.info("Converting entity by email: {}", email);

        UserEntity userEntity = userDao.findUserByEmail(email);
        User user = convertUserEntitytoUserDto(userEntity);

        if (user != null){
            logger.info("Entity converted by email: {}", email);
            return user;
        }else return null;

    }

    public boolean thisTokenIsFromThisUsername(String token, String username){
        logger.info("Checking if token is from username");

        if(userDao.findUserByToken(token).getUsername().equals(username)){
            logger.info("Token is from username");
            return true;
        }else return false;

    }

    public User getUserByToken(String token) {
        logger.info("Finding user by token: {}", token);
        UserEntity userEntity = userDao.findUserByToken(token);
        if (userEntity != null) {
            logger.info("User found by token: {}", token);
            return convertUserEntitytoUserDto(userEntity);
        } else {
            logger.error("User not found by token: {}", token);
        }
        return null;
    }

    public boolean verifyOldPassword(String username, String oldPassword){
        logger.info("Verifying old password");

        UserEntity user = userDao.findUserByUsername(username);
        if (user!=null){
            logger.info("User found: {}", username);
            return BCrypt.checkpw(oldPassword, user.getPassword());
        } else {
            logger.error("User not found: {}", username);
        }
        return false;
    }

    public boolean updatePassword(String username, String newPassword) {
        logger.info("Updating password");

        UserEntity user = userDao.findUserByUsername(username);
        if (user != null) {
            logger.info("User found: {}", username);
            //Encripta a password usando BCrypt
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            //Define a password encriptada
            user.setPassword(hashedPassword);
            logger.info("Password updated for user: {}", username);

            return true;
        }
        logger.error("User not found: {}", username);
        return false;
    }

    //Método para reset de password e para definição da primeira password quando user é registado pelo PO
    public boolean setPassword(String validationToken, String password) {
        logger.info("Setting password");

        boolean status = false;
        boolean isValidationTokenValid = isValidationTokenValid(validationToken);

        UserEntity user = userDao.findUserByValidationToken(validationToken);

        if (!isValidationTokenValid) {
            logger.error("Validation token is not valid");

            user.setValidationToken(null);
            userDao.merge(user);
            return status;
        }

        if (user != null && !password.trim().isEmpty()) {
            logger.info("User found: {}", user.getUsername());

            //Encripta a password usando BCrypt
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            //Define a password encriptada
            user.setPassword(hashedPassword);

            if (user.isConfirmed()) {
                user.setValidationToken(null);
            }
            userDao.merge(user);

            logger.info("Password set for user: {}", user.getUsername());
            status = true;
        }

        logger.error("User not found: {}", user.getUsername());
        return status;
    }

    public boolean doesUserHavePasswordDefined(String validationToken) {
        logger.info("Checking if user has password defined");
        return userDao.doesUserHavePasswordDefined(validationToken);
    }

    public int sendPasswordResetEmail(String email) {
        logger.info("Sending password reset email");

        int sent = 0;
        UserEntity user = userDao.findUserByEmail(email);

        if (user != null) {
            logger.info("User found: {}", user.getUsername());

            if (!user.isConfirmed()) {
                sent = 1;
                logger.error("User is not confirmed: {}", user.getUsername());

            } else {

                String token = generateValidationToken(5);
                user.setValidationToken(token);
                userDao.merge(user);

                if (emailBean.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), token)) {
                    sent = 2;
                    logger.info("Password reset email sent: {}", user.getUsername());
                } else {
                    user.setValidationToken(null);
                    userDao.merge(user);
                    logger.error("Password reset email not sent: {}", user.getUsername());
                }
            }
        }
        return sent;
    }

    public void removeValidationToken(String username) {
        logger.info("Removing validation token");

        UserEntity user = userDao.findUserByUsername(username);
        if (user != null) {
            logger.info("User found: {}", user.getUsername());
            user.setValidationToken(null);
            userDao.merge(user);
            logger.info("Validation token removed for user: {}", user.getUsername());
        } else {
            logger.error("User not found: {}", username);
        }
    }

    public int countAllUsers() {
        logger.info("Counting all users");
        return userDao.countAllUsers();
    }

    public int countAllUsersByConfirmed(boolean confirmed) {
        logger.info("Counting all users by confirmed");
        return userDao.countAllUsersByConfirmed(confirmed);
    }

    public int countAllUsersByVisibility(boolean visible) {
        logger.info("Counting all users by visibility");
        return userDao.countAllUsersByVisibility(visible);
    }

    public Number [] userStats() {
        logger.info("Getting user statistics");

        Number [] stats = new Number[9];

        int totalUsers = countAllUsers();
        int totalVisibleUsers = countAllUsersByVisibility(true);
        int totalNotVisibleUsers = countAllUsersByVisibility(false);
        int totalConfirmedUsers = countAllUsersByConfirmed(true);
        int totalNotConfirmedUsers = countAllUsersByConfirmed(false);
        double averageNumberOfTasksPerUser = taskBean.averageNumberOfTasksPerUser(totalVisibleUsers);
        int numberOfToDoTasks = taskBean.numberOfTasksByState(Task.TODO);
        int numberOfDoingTasks = taskBean.numberOfTasksByState(Task.DOING);
        int numberOfDoneTasks = taskBean.numberOfTasksByState(Task.DONE);

        stats[0] = totalUsers;
        stats[1] = totalVisibleUsers;
        stats[2] = totalNotVisibleUsers;
        stats[3] = totalConfirmedUsers;
        stats[4] = totalNotConfirmedUsers;
        stats[5] = averageNumberOfTasksPerUser;
        stats[6] = numberOfToDoTasks;
        stats[7] = numberOfDoingTasks;
        stats[8] = numberOfDoneTasks;

        logger.info("User statistics retrieved");
        return stats;
    }

    public List<Object[]> getTotalUsersRegisteredByEachDay() {
        logger.info("Getting total users registered by each day");
        return userDao.totalUsersRegisteredByEachDay();
    }

    public Statistics getAllStatistics() {
        logger.info("Getting all statistics");

        Statistics statistics = new Statistics();

        try {
            Number[] userStats = userStats();
            statistics.setUserStats(userStats);

            double averageTaskTime = taskBean.averageTimeToFinishTask();
            statistics.setAverageTaskTime(averageTaskTime);

            ArrayList<String> categories = categoryBean.listCategoriesByNumberOfTasks();
            statistics.setCategories(categories);

            List<Object[]> totalTasksDoneByEachDay = taskBean.totalTasksDoneByEachDay();
            statistics.setTotalTasksDoneByEachDay(totalTasksDoneByEachDay);

            List<Object[]> usersRegistered = getTotalUsersRegisteredByEachDay();
            statistics.setUsersRegistered(usersRegistered);

            logger.info("All statistics retrieved");

        } catch (Exception e) {
            logger.error("Error getting all statistics");
        }

        return statistics;
    }

    public LocalDateTime getLastAccess(String username) {
        logger.info("Getting last access");
        UserEntity user = userDao.findUserByUsername(username);
        return user.getLastAccess();
    }

    public void updateLastAccess(String username, LocalDateTime lastAccess) {
        logger.info("Updating last access");
        userDao.updateLastAccess(username, lastAccess);
    }

    public boolean setSessionTimeout(int timeout) {
        logger.info("Setting session timeout");

        boolean updated = false;


        if (timeout >= 1 && timeout <= 60) {
            userDao.updateSessionTimeout(timeout);
            updated = true;
            logger.info("Session timeout set to: {}", timeout);
        } else {
            logger.error("Session timeout not set: invalid value");
        }

        return updated;
    }

    public int getSessionTimeout() {
        logger.info("Getting session timeout");
        return userDao.getSessionTimeout();
    }
}