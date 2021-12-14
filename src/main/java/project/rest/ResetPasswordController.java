package project.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import project.dto.UserDTO;
import project.model.PasswordResetToken;
import project.model.User;
import project.repositories.PasswordResetTokenRepository;
import project.repositories.UserRepository;
import project.security.model.ResetPasswordRequire;
import project.services.Mail.EmailSender;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.util.UUID;

@RestController
@RequestMapping(value = "/reset_password", produces = MediaType.APPLICATION_JSON_VALUE)
public class ResetPasswordController {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailSender emailSender;

    public ResetPasswordController(UserRepository userRepository, PasswordResetTokenRepository passwordResetTokenRepository, EmailSender emailSender) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailSender = emailSender;
    }

    @PostMapping("/{email}") // kiedy probuje odzyskać hasło to leci post
    public void resetPassword(@PathVariable String email) {
        UserDTO userDTO = userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Unable to find user with email " + email)).mapToDTO();
        PasswordResetToken passwordResetToken;
        if (passwordResetTokenRepository.existsByUserId(userDTO.getId())) {
            passwordResetToken = passwordResetTokenRepository.findByUserId(userDTO.getId());
            if (passwordResetToken.isValid()) {
                    throw new EntityExistsException("Mail has already sent");
            }
            passwordResetToken.setExpiryDate(PasswordResetToken.calculateExpiryDate());
        } else {
            passwordResetToken = new PasswordResetToken(userDTO.getId());
        }
        String token = UUID.randomUUID().toString();
        passwordResetToken.setToken(token);
        String link = emailSender.generateLinkToReset(token);
        String text = String.format(emailSender.templateResetTokenMessage().getText(), link);
        emailSender.sendSimpleMessage(email, "Reset your password", text);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @PostMapping("validate/{token}")
    public void confirmResetPassword(@RequestBody ResetPasswordRequire resetPasswordRequire, @PathVariable("token") String token) {
        PasswordResetToken passToken = passwordResetTokenRepository.findByToken(token);
        if ((passToken == null) /*|| (passToken.getUser().getId() != id)*/) {
            throw new AccessDeniedException("Invalid Token");
        }
        if (!passToken.isValid()) {
            passwordResetTokenRepository.delete(passToken);
            throw new AccessDeniedException("Token expired");
        }
        User user = passToken.getUser();
        user.setPassword( new BCryptPasswordEncoder().encode(resetPasswordRequire.getPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(passToken);
    }
}
