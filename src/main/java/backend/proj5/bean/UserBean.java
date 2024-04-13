package backend.proj5.bean;

import backend.proj5.dao.TaskDao;
import backend.proj5.dao.UserDao;
import backend.proj5.dto.Login;
import backend.proj5.dto.Task;
import backend.proj5.dto.User;
import backend.proj5.entity.TaskEntity;
import backend.proj5.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.mindrot.jbcrypt.BCrypt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;

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

    public UserBean(){}

    public UserBean(UserDao userDao) {
        this.userDao = userDao;
    }

    public void createDefaultUsersIfNotExistent() {
        UserEntity userEntity = userDao.findUserByUsername("admin");
        if (userEntity == null) {
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
            System.out.println("Admin created" + admin);
        }

        UserEntity userEntity2 = userDao.findUserByUsername("NOTASSIGNED");
        if (userEntity2 == null) {
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

        }
    }

    //Permite ao utilizador entrar na app, gera token
    public User login(Login user) {

        UserEntity userEntity = userDao.findUserByUsername(user.getUsername());

        if (userEntity != null && userEntity.isVisible() && userEntity.isConfirmed()) {

            //Verifica se a password coincide com a password encriptada
            if (BCrypt.checkpw(user.getPassword(), userEntity.getPassword())) {

                String token = generateNewToken();
                userEntity.setToken(token);
                User userDto = convertUserEntitytoUserDto(userEntity);
                return createUserLogged(userDto);
            }
        } else if (userEntity != null && userEntity.isVisible() && !userEntity.isConfirmed()) {
            if (userEntity.getPassword().trim().isEmpty()) {
                return convertUserEntitytoUserDto(userEntity);
            } else {
                if (BCrypt.checkpw(user.getPassword(), userEntity.getPassword())) {
                    return convertUserEntitytoUserDto(userEntity);
                }
            }
        }
        return null;
    }

    //Faz o registo do utilizador, adiciona à base de dados
    public boolean register(User user) {

        if (user != null) {
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

                if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().isBlank()) {
                    user.setPassword("");

                } else {

                    //Encripta a password usando BCrypt
                    String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

                    //Define a password encriptada
                    user.setPassword(hashedPassword);
                }

                UserEntity userEntity = convertUserDtotoUserEntity(user);
                userEntity.setValidationToken(generateValidationToken(48*60));

                if (emailBean.sendConfirmationEmail(user, userEntity.getValidationToken())) {
                    userDao.persist(userEntity);
                    return true;
                }
            }
        } else {
            return false;
        }
        return false;
    }


    //Apaga todos os registos do utilizador da base de dados
    public boolean delete(String username) {

        UserEntity u = userDao.findUserByUsername(username);

        if (u != null) {
            ArrayList<TaskEntity> tasks = taskDao.findTasksByUser(u);
            UserEntity notAssigned = userDao.findUserByUsername("NOTASSIGNED");

            notAssigned.addNewTasks(tasks);

            for (TaskEntity t : tasks) {
                t.setOwner(notAssigned);
                taskDao.merge(t);
            }
            userDao.remove(u);

            return true;
        } else
            return false;
    }


    //Métodos de conversão

    public UserEntity convertUserDtotoUserEntity(User user) {
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
        SecureRandom secureRandom = new SecureRandom(); //threadsafe
        Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    private String generateValidationToken(int expirationMinutes) {
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
    public static boolean isValidationTokenValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        // Split token and expiry timestamp
        String[] parts = token.split("\\|");
        if (parts.length != 2) {
            return false;
        }

        try {
            // Parse expiry timestamp
            long expiryTimestamp = Long.parseLong(parts[1]);
            Instant expiryTime = Instant.ofEpochMilli(expiryTimestamp);

            // Check if current time is before expiry time
            return Instant.now().isBefore(expiryTime);
        } catch (NumberFormatException e) {
            return false;
        }
    }


    //Logout
    public boolean logout(String token) {
        UserEntity u = userDao.findUserByToken(token);

        if (u != null) {
            u.setToken(null);
            return true;
        }
        return false;
    }

    public ArrayList<User> getUsers() {

        ArrayList<UserEntity> userEntities = userDao.findAllUsers();
        if (userEntities != null) {
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
        //Retorna uma lista vazia se não forem encontradas tarefas
        return new ArrayList<>();
    }

    //Receber users pelo tipo de user
    public ArrayList<User> getUsersByType(int typeOfUser) {

        ArrayList<UserEntity> userEntities = userDao.findAllUsersByTypeOfUser(typeOfUser);
        if (userEntities != null) {
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

        ArrayList<UserEntity> userEntities = userDao.findAllUsersByVisibility(visible);
        if (userEntities != null) {
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

        ArrayList<UserEntity> userEntities = userDao.findAllUsersByTypeOfUserAndVisibility(typeOfUser, visible);
        if (userEntities != null) {
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

        UserEntity u = userDao.findUserByUsername(username);
        UserEntity userLogged = userDao.findUserByToken(token);

        if (u !=null) {
            if ((u.getTypeOfUser() == 400 || !u.isVisible() || !u.isConfirmed()) && userLogged.getTypeOfUser() != 300) {
                return convertUserEntitytoUserDto(userLogged);
            } else {
                return convertUserEntitytoUserDto(u);
            }
        } else {
            return null;
        }
    }

    public User createUserLogged(User user) {
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
        boolean status = false;

        UserEntity u = userDao.findUserByUsername(username);

        if (u != null && u.getUsername().equals(username)){

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
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        return status;
    }

    public boolean updateUserEntityVisibility(String username) {
        boolean status = false;

        UserEntity u = userDao.findUserByUsername(username);

        if (u != null){

            u.setVisible(!u.isVisible());

            status = true;
        }

        return status;
    }

    public int updateUserEntityConfirmation(String validationToken) {
        boolean isValidationTokenValid = isValidationTokenValid(validationToken);

        UserEntity u = userDao.findUserByValidationToken(validationToken);

        if (!isValidationTokenValid) {
            userDao.remove(u);
            return 0; // token expired
        }

        if (u == null) {
            return 0; // user not found
        } else if (u.isConfirmed()) {
            return 2; // user was already confirmed
        } else {
            u.setConfirmed(true);
            u.setValidationToken(null);
            return 1; // user successfully confirmed
        }
    }

    public boolean isAuthenticated(String token) {

        boolean validUser = false;
        UserEntity user = userDao.findUserByToken(token);
        if (user != null && user.isVisible()) {
            validUser = true;
        }

        return validUser;
    }

    public boolean isUsernameAvailable(User user) {

        UserEntity u = userDao.findUserByUsername(user.getUsername());
        boolean status = false;

        if (u == null) {
            status = true;
        }

        return status;
    }

    public boolean isEmailFormatValid(String email) {
        // Use a regular expression to perform email format validation
        // This regex is a basic example and may need to be adjusted
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    public boolean isEmailValid(User user) {

        UserEntity u = userDao.findUserByEmail(user.getEmail());
        // Check if the email format is valid
        if (isEmailFormatValid(user.getEmail()) && u == null) {
            return true;
        }

        return false;
    }

    public boolean isEmailUpdatedValid(User user) {

        //Se for null é porque não houve nenhuma atualização
        if (user.getEmail() == null || user.getEmail().isEmpty() || user.getEmail().isBlank()){
            return true;
        }

        UserEntity u = userDao.findUserByEmail(user.getEmail());
        // Check if the email format is valid
        if ((isEmailFormatValid(user.getEmail()) && u == null) || (u != null && u.getEmail().equals(user.getEmail()))) {
            return true;
        }

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
        boolean status = true;
        int i = 0;

        UserEntity u = userDao.findUserByPhone(user.getPhone());

        while (status && i < user.getPhone().length() - 1) {
            if (user.getPhone().length() == 9) {
                for (; i < user.getPhone().length(); i++) {
                    if (!Character.isDigit(user.getPhone().charAt(i))) {
                        status = false;
                    }
                }
            } else {
                status = false;
            }
        }

        //Se existir contacto na base de dados retorna false
        if (u != null) {
            status = false;
        }

        return status;
    }

    public boolean isPhoneNumberUpdatedValid(User user) {
        boolean status = true;

        //Se for null é porque não houve nenhuma atualização
        if (user.getPhone()==null){
            return true;
        }

        int i = 0;

        UserEntity u = userDao.findUserByPhone(user.getPhone());

        while (status && i < user.getPhone().length() - 1) {
            if (user.getPhone().length() == 9) {
                for (; i < user.getPhone().length(); i++) {
                    if (!Character.isDigit(user.getPhone().charAt(i))) {
                        status = false;
                    }
                }
            } else {
                status = false;
            }
        }

        //Se existir contacto na base de dados retorna false
        if (u != null) {
            status = false;
        }

        return status;
    }

    public void validatePhotoUrl(User user) {

        String defaultUrl = "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcTWfm4QX7YF7orMboLv4jjuwoYgd85bKBqeiBHLOfS6MgfHUW-d";
        String url = user.getPhotoURL();

        if (url == null || url.isEmpty() || url.isBlank()){
            user.setPhotoURL(defaultUrl);
        } else {
            try {
                BufferedImage img = ImageIO.read(new URL(url));
                if (img == null) {
                    user.setPhotoURL(defaultUrl);
                }
            } catch (IOException e) {
                user.setPhotoURL(defaultUrl);
            }
        }

    }

    public boolean isImageUrlUpdatedValid(String url) {
        boolean status = true;

        //Se for null é porque não houve nenhuma alteração
        if (url == null) {
            return true;
        }

        try {
            BufferedImage img = ImageIO.read(new URL(url));
            if (img == null) {
                status = false;
            }
        } catch (IOException e) {
            status = false;
        }

        return status;
    }


    public boolean userIsTaskOwner(String token, String id) {
        UserEntity userEntity = userDao.findUserByToken(token);
        TaskEntity taskEntity = taskDao.findTaskById(id);
        boolean authorized = false;
        if (userEntity != null) {
            if (taskEntity.getOwner().getUsername().equals(userEntity.getUsername())) {
                authorized = true;
            }
        }
        return authorized;
    }

    public boolean userIsDeveloper(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        boolean authorized = false;
        if (userEntity != null) {
            if (userEntity.getTypeOfUser() == User.DEVELOPER) {
                authorized = true;
            }
        }
        return authorized;
    }

    public boolean userIsScrumMaster(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        boolean authorized = false;
        if (userEntity != null) {
            if (userEntity.getTypeOfUser() == User.SCRUMMASTER) {
                authorized = true;
            }
        }
        return authorized;
    }

    public boolean userIsProductOwner(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        boolean authorized = false;
        if (userEntity != null) {
            if (userEntity.getTypeOfUser() == User.PRODUCTOWNER) {
                authorized = true;
            }
        }
        return authorized;
    }


    //Converte a Entidade com o email "email" para DTO
    public User convertEntityByEmail (String email){

        UserEntity userEntity = userDao.findUserByEmail(email);
        User user = convertUserEntitytoUserDto(userEntity);

        if (user != null){
            return user;
        }else return null;

    }

    public boolean thisTokenIsFromThisUsername(String token, String username){

        if(userDao.findUserByToken(token).getUsername().equals(username)){
            return true;
        }else return false;

    }

    public User getUserByToken(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        if (userEntity != null) {
            return convertUserEntitytoUserDto(userEntity);
        }
        return null;
    }

    public boolean verifyOldPassword(String username, String oldPassword){

        UserEntity user = userDao.findUserByUsername(username);
        if (user!=null){
            return BCrypt.checkpw(oldPassword, user.getPassword());
        }
        return false;
    }

    public boolean updatePassword(String username, String newPassword) {

        UserEntity user = userDao.findUserByUsername(username);
        if (user != null) {
            //Encripta a password usando BCrypt
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            //Define a password encriptada
            user.setPassword(hashedPassword);
            return true;
        }
        return false;
    }

    //Método para reset de password e para definição da primeira password quando user é registado pelo PO
    public boolean setPassword(String validationToken, String password) {

        boolean status = false;
        boolean isValidationTokenValid = isValidationTokenValid(validationToken);

        UserEntity user = userDao.findUserByValidationToken(validationToken);

        if (!isValidationTokenValid) {
            user.setValidationToken(null);
            userDao.merge(user);
            return status;
        }

        if (user != null && !password.trim().isEmpty()) {
            //Encripta a password usando BCrypt
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            //Define a password encriptada
            user.setPassword(hashedPassword);

            if (user.isConfirmed()) {
                user.setValidationToken(null);
            }
            userDao.merge(user);
            status = true;
        }

        return status;
    }

    public boolean doesUserHavePasswordDefined(String validationToken) {
        return userDao.doesUserHavePasswordDefined(validationToken);
    }

    public int sendPasswordResetEmail(String email) {

        int sent = 0;
        UserEntity user = userDao.findUserByEmail(email);

        if (user != null) {

            if (!user.isConfirmed()) {
                sent = 1;
            } else {

                String token = generateValidationToken(5);
                user.setValidationToken(token);
                userDao.merge(user);

                if (emailBean.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), token)) {
                    sent = 2;
                } else {
                    user.setValidationToken(null);
                    userDao.merge(user);
                }
            }
        }
        return sent;
    }

    public int countAllUsers() {
        return userDao.countAllUsers();
    }

    public int countAllUsersByConfirmed(boolean confirmed) {
        return userDao.countAllUsersByConfirmed(confirmed);
    }

    public int countAllUsersByVisibility(boolean visible) {
        return userDao.countAllUsersByVisibility(visible);
    }

    public Number [] userStats() {
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

        return stats;
    }
}