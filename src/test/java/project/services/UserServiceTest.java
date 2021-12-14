package project.services;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import project.dto.ChangeBalanceDTO;
import project.dto.ChangePasswordDTO;
import project.model.User;
import project.repositories.UserRepository;
import project.security.JwtProvider;

import javax.persistence.EntityExistsException;
import java.util.Optional;


@RunWith(MockitoJUnitRunner.Silent.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private UserService userService;

    static private User user;
    static private MockHttpServletRequest httpServletRequest;
    static private ChangePasswordDTO changePasswordDTO;

    @BeforeClass
    static public void init(){
        user = new User();
        user.setId(1L);
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setEmail("mateusz.dabek@exchange.pl");
        user.setBalance(10000d);
        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
        user.setPassword(bCrypt.encode("password"));

        httpServletRequest = new MockHttpServletRequest();
        changePasswordDTO = new ChangePasswordDTO();
    }

    @Test(expected = EntityExistsException.class)
    public void createUser_userAlreadyExists_EntityExistsExceptionThrown () {
        Mockito.when(userRepository.findByEmail("mateusz.dabek@exchange.pl")).thenReturn(Optional.of(user));

        try {
            userService.createUser(user);
        } catch (EntityExistsException e) {
            Assert.assertEquals("The email has already been taken", e.getMessage());
            Mockito.verify(userRepository, Mockito.times(1)).findByEmail(Mockito.anyString());
            Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.isA(User.class));
            Mockito.verifyNoMoreInteractions(userRepository);
            throw e;
        }
    }

    @Test(expected = AccessDeniedException.class)
    public void createUser_passwordIsTooWeak_AccessDeniedExceptionThrown() {
        Mockito.when(userRepository.findByEmail("another.email@exchange.pl")).thenReturn(Optional.of(user));
        user.setPassword("password");

        try {
            userService.createUser(user);
        } catch (AccessDeniedException e) {
            Assert.assertEquals("Password is too weak", e.getMessage());
            Mockito.verify(userRepository, Mockito.times(1)).findByEmail(Mockito.anyString());
            Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.isA(User.class));
            Mockito.verifyNoMoreInteractions(userRepository);
            throw e;
        }
    }

    @Test
    public void createUser_rightValues_createdUser() {
        Mockito.when(userRepository.findByEmail("mateusz.dabek@onet.pl")).thenReturn(Optional.of(user));
        user.setPassword("passWORD1");

        userService.createUser(user);
        Mockito.verify(userRepository, Mockito.times(1)).findByEmail(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(ArgumentMatchers.isA(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = AccessDeniedException.class)
    public void changePassword_oldPasswordNotMatch_AccessDeniedExceptionThrown() {
        Mockito.when(jwtProvider.getToken(httpServletRequest)).thenReturn("");
        Mockito.when(jwtProvider.getIdFromToken("")).thenReturn(1L);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        changePasswordDTO.setOldPassword("anotherPassword");
        changePasswordDTO.setNewPassword("pass");

        try {
            userService.changePassword(httpServletRequest, changePasswordDTO);
        } catch (Exception e) {
            Assert.assertEquals("The old password is incorrect", e.getMessage());
            Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.isA(User.class));
            throw e;
        }
    }

    @Test(expected = AccessDeniedException.class)
    public void changePassword_newPasswordIsTooWeak_AccessDeniedExceptionThrown() {
        Mockito.when(jwtProvider.getToken(httpServletRequest)).thenReturn("");
        Mockito.when(jwtProvider.getIdFromToken("")).thenReturn(1L);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        changePasswordDTO.setOldPassword("password");
        changePasswordDTO.setNewPassword("pass");

        try {
            userService.changePassword(httpServletRequest, changePasswordDTO);
        } catch (Exception e) {
            Assert.assertEquals("New password is too weak", e.getMessage());
            Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.isA(User.class));
            throw e;
        }
    }

    @Test
    public void changePassword_rightValues_changedPassword() {

    }

    @Test
    public void changeBalance_rightValues_changedBalance() {
        Mockito.when(jwtProvider.getToken(httpServletRequest)).thenReturn("");
        Mockito.when(jwtProvider.getIdFromToken("")).thenReturn(1L);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        ChangeBalanceDTO changeBalanceDTO = new ChangeBalanceDTO();
        changeBalanceDTO.setNewBalance(20000.0);

        userService.changeBalance(httpServletRequest, changeBalanceDTO);

        Assert.assertEquals(changeBalanceDTO.getNewBalance(), user.getBalance());
        Mockito.verify(userRepository, Mockito.times(1)).save(ArgumentMatchers.isA(User.class));
    }
}