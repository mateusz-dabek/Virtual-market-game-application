package project.services;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import project.dto.ChangeBalanceDTO;
import project.dto.ChangePasswordDTO;
import project.dto.UserDTO;
import project.model.User;
import project.repositories.UserRepository;
import project.security.JwtProvider;

import javax.persistence.EntityExistsException;
import javax.servlet.http.HttpServletRequest;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public UserService(UserRepository userRepository, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }

    public UserDTO getUserById(Long id) {
        return userRepository.findById(id).get().mapToDTO();
    }


    private String encodePassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    public void createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EntityExistsException("The email has already been taken");
        }
        if (!validPassword(user.getPassword())) {
            throw new AccessDeniedException("Password is too weak");
        }
        user.setPassword(encodePassword(user.getPassword()));
        userRepository.save(user);
    }

    public void changePassword(HttpServletRequest httpServletRequest, ChangePasswordDTO changePasswordDTO) {
        Long id = jwtProvider.getIdFromToken(jwtProvider.getToken(httpServletRequest));
        User user = userRepository.findById(id).get();
        if (!comparePasswords(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new AccessDeniedException("The old password is incorrect"); // sprawdza czy stare jest prawidlowe
        }
        if (!validPassword(changePasswordDTO.getNewPassword())) { // sprawdza czy haslo jest silne
            throw new AccessDeniedException("New password is too weak");
        }
        user.setPassword(encodePassword(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
    }

    private boolean comparePasswords(String rawPassword, String encodedPassword) {
        return new BCryptPasswordEncoder().matches(rawPassword, encodedPassword);
    }

    private boolean validPassword(String password) {
        return password.matches("^(?=.*[0-9])(?=.*[a-zA-Z])(?=\\S+$).{8,}$");
    }

    public void changeBalance(HttpServletRequest httpServletRequest, ChangeBalanceDTO balanceDTO) {
        Long id = jwtProvider.getIdFromToken(jwtProvider.getToken(httpServletRequest));
        User user = userRepository.findById(id).get();
        user.setBalance(balanceDTO.getNewBalance());
        userRepository.save(user);
    }
}
