package Bean;

import backend.proj5.bean.NotificationBean;
import backend.proj5.bean.UserBean;
import backend.proj5.dto.Notification;
import backend.proj5.websocket.NotifierWS;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class WebSocketTest {

    @InjectMocks
    NotifierWS notifierWS;

    @Mock
    UserBean userBean;

    @Mock
    NotificationBean notificationBean;

    @Mock
    Session session;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testToDoOnClose() {
        CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test");

        notifierWS.toDoOnClose(session, closeReason);

    }

    @Test
    public void testSendWithoutSession() {
        notifierWS.send("testToken", "testMessage");

    }

    @Test
    public void testToDoOnOpenWithNoToken() {
        notifierWS.toDoOnOpen(session, null);

    }

    @Test
    public void testToDoOnOpenWithInvalidToken() {
        when(userBean.isAuthenticated(anyString())).thenReturn(false);

        notifierWS.toDoOnOpen(session, "invalidToken");

        verify(userBean, times(1)).isAuthenticated(anyString());
    }

    @Test
    public void testToDoOnCloseWithExistingToken() {
        CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test");

        notifierWS.toDoOnClose(session, closeReason);

    }

    @Test
    public void testToDoOnCloseWithNoToken() {
        CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test");

        notifierWS.toDoOnClose(session, closeReason);

    }

    @Test
    public void testToDoOnCloseWithInvalidToken() {
        CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test");

        notifierWS.toDoOnClose(session, closeReason);

    }

    @Test
    public void testSendWithNoMessage() {
        notifierWS.send("testToken", null);

    }

    @Test
    public void testSendWithNullToken() {
        notifierWS.send(null, "testMessage");

    }

    @Test
    public void testToDoOnOpenWithNullSession() {
        notifierWS.toDoOnOpen(null, "testToken");

    }


    @Test
    public void testToDoOnCloseWithNullSession() {
        CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test");

        notifierWS.toDoOnClose(null, closeReason);

    }

    @Test
    public void testToDoOnOpenWithNullTokenAndSession() {
        notifierWS.toDoOnOpen(null, null);

    }


    @Test
    public void testToDoOnCloseWithNullTokenAndSession() {
        CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test");

        notifierWS.toDoOnClose(null, closeReason);

    }

    @Test
    public void testSendWithNullMessageAndToken() {
        notifierWS.send(null, null);

    }

    @Test
    public void testToDoOnOpenWithEmptyToken() {
        notifierWS.toDoOnOpen(session, "");

    }

}