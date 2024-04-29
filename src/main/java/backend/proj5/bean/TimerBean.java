package backend.proj5.bean;

import backend.proj5.dto.User;
import backend.proj5.entity.UserEntity;
import backend.proj5.websocket.NotifierWS;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

@Singleton
public class TimerBean {

    @Inject
    private UserBean userBean;

    @Schedule(second="*/30", minute="*", hour="*") // this automatic timer is set to expire every 30 seconds
    public void automaticTimer(){

        ArrayList<UserEntity> users = userBean.findAllUsers();
        int sessionTimeout = userBean.getSessionTimeout();
        LocalDateTime now = LocalDateTime.now();

        if (users == null) {
            return;
        }

        for (UserEntity user : users) {
            String validationToken = user.getValidationToken();
            if (validationToken != null) {
                boolean isTokenValid = userBean.isValidationTokenValid(validationToken);
                if (!isTokenValid) {
                    userBean.removeValidationToken(user.getUsername());
                }
            }

            if (user.isVisible() && user.isConfirmed()) {
                LocalDateTime lastAccess = user.getLastAccess();

                if (lastAccess != null) {
                    long minutes = ChronoUnit.MINUTES.between(lastAccess, now);

                    if (minutes > sessionTimeout) {
                        userBean.timeoutLogout(user.getToken());
                    }
                }
            }
        }
    }
}
