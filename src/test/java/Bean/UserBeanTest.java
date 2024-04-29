package Bean;

import backend.proj5.bean.UserBean;
import backend.proj5.dao.UserDao;

import backend.proj5.dto.User;
import backend.proj5.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserBeanTest {

    @InjectMocks
    UserBean userBean;

    @Mock
    UserDao userDao;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void testFindUserByUsername() {
        // Criação do user mock
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("junitTest");
        userEntity.setPassword("123");
        userEntity.setFirstName("junit");
        userEntity.setLastName("test");
        userEntity.setEmail("junit@test.pt");
        userEntity.setPhone("976458123");
        userEntity.setTypeOfUser(100);
        userEntity.setVisible(true);

        // Comportamento esperado para o método findUserByUsername do UserDao
        when(userDao.findUserByUsername("junitTest")).thenReturn(userEntity);

        // Executando o método
        UserEntity foundUser = userDao.findUserByUsername("junitTest");

        // Verificando se o método retorna o user esperado
        assertNotNull(foundUser);
        assertEquals("junitTest", foundUser.getUsername());
        assertEquals("junit", foundUser.getFirstName());
    }


    @Test
    void testFindUserByToken() {
        // Criação do user mock
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("junitTest");
        userEntity.setToken("testToken");

        // Configurando o comportamento esperado para o método findUserByToken do UserDao
        when(userDao.findUserByToken("testToken")).thenReturn(userEntity);

        // Executa o método do UserBean que depende do UserDao
        boolean foundUser = userBean.thisTokenIsFromThisUsername("testToken","junitTest");

        // Verifica se o método do UserBean retorna o user esperado
        assertNotNull(foundUser);
    }


    @Test
    void testIsDeveloper() {
        // Configuração do cenário
        int typeOfUser = 100;
        String username = "testUser";
        String token = "123";
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setTypeOfUser(typeOfUser);
        userEntity.setVisible(true);

        when(userDao.findUserByToken(token)).thenReturn(userEntity);

        // Execução do método sob teste
        boolean authenticated = userBean.userIsDeveloper(token);

        // Verificação do resultado
        assertTrue(authenticated);
    }

    @Test
    void testIsScrumMaster() {
        // Configuração do cenário
        int typeOfUser = 200;
        String username = "testUser";
        String token = "123";
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setTypeOfUser(typeOfUser);
        userEntity.setVisible(true);

        when(userDao.findUserByToken(token)).thenReturn(userEntity);

        // Execução do método sob teste
        boolean authenticated = userBean.userIsScrumMaster(token);

        // Verificação do resultado
        assertTrue(authenticated);
    }

    @Test
    void testIsProductOwner() {
        // Configuração do cenário
        int typeOfUser = 300;
        String username = "testUser";
        String token = "123";
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setTypeOfUser(typeOfUser);
        userEntity.setVisible(true);

        when(userDao.findUserByToken(token)).thenReturn(userEntity);

        // Execução do método sob teste
        boolean authenticated = userBean.userIsProductOwner(token);

        // Verificação do resultado
        assertTrue(authenticated);
    }


    @Test
    public void testUpdateUserEntityVisibility() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("testUser");
        userEntity.setVisible(true);

        when(userDao.findUserByUsername("testUser")).thenReturn(userEntity);

        boolean result = userBean.updateUserEntityVisibility("testUser");

        assertTrue(result);
    }

    @Test
    public void testUserIsDeveloper() {
        UserEntity userEntity = new UserEntity();
        userEntity.setTypeOfUser(100); // Developer type

        when(userDao.findUserByToken("testToken")).thenReturn(userEntity);

        boolean result = userBean.userIsDeveloper("testToken");

        assertTrue(result);
    }

    @Test
    public void testUserIsScrumMaster() {
        UserEntity userEntity = new UserEntity();
        userEntity.setTypeOfUser(200); // Scrum Master type

        when(userDao.findUserByToken("testToken")).thenReturn(userEntity);

        boolean result = userBean.userIsScrumMaster("testToken");

        assertTrue(result);
    }

    @Test
    public void testUserIsProductOwner() {
        UserEntity userEntity = new UserEntity();
        userEntity.setTypeOfUser(300); // Product Owner type

        when(userDao.findUserByToken("testToken")).thenReturn(userEntity);

        boolean result = userBean.userIsProductOwner("testToken");

        assertTrue(result);
    }

    @Test
    public void testIsUsernameAvailable() {
        User user = new User();
        user.setUsername("testUser");

        when(userDao.findUserByUsername(anyString())).thenReturn(null);

        boolean result = userBean.isUsernameAvailable(user);

        assertTrue(result);
        verify(userDao, times(1)).findUserByUsername(anyString());
    }

    @Test
    public void testIsEmailValid() {
        User user = new User();
        user.setEmail("test@test.com");

        when(userDao.findUserByEmail(anyString())).thenReturn(null);

        boolean result = userBean.isEmailValid(user);

        assertTrue(result);
        verify(userDao, times(1)).findUserByEmail(anyString());
    }

    @Test
    public void testIsPhoneNumberValid() {
        User user = new User();
        user.setPhone("123456789");

        when(userDao.findUserByPhone(anyString())).thenReturn(null);

        boolean result = userBean.isPhoneNumberValid(user);

        assertTrue(result);
        verify(userDao, times(1)).findUserByPhone(anyString());
    }


    @Test
    public void testLogout() {
        String token = "testToken";

        UserEntity userEntity = new UserEntity();

        when(userDao.findUserByToken(anyString())).thenReturn(userEntity);

        boolean result = userBean.logout(token);

        assertTrue(result);
        verify(userDao, times(1)).findUserByToken(anyString());
    }
}

