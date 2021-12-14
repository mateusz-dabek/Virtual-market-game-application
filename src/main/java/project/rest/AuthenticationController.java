package project.rest;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import project.dto.UserDTO;
import project.dto.VerifyCodeDTO;
import project.model.AccountActivationCode;
import project.model.User;
import project.repositories.AccountActivationCodeRepository;
import project.repositories.UserRepository;
import project.security.model.UserToken;
import project.security.JwtProvider;
import project.security.model.JwtAuthenticationRequire;
import project.services.Mail.EmailSender;
import project.services.UserDetailsImpl;
import project.services.UserService;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final EmailSender emailSender;
    private final AccountActivationCodeRepository accountActivationCodeRepository;
    private final UserRepository userRepository;

    @Lazy
    private final AuthenticationManager authenticationManager;

    public AuthenticationController(JwtProvider jwtProvider, AuthenticationManager authenticationManager, UserService userService, EmailSender emailSender, AccountActivationCodeRepository accountActivationCodeRepository, UserRepository userRepository) {
        this.jwtProvider = jwtProvider;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.emailSender = emailSender;
        this.accountActivationCodeRepository = accountActivationCodeRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public UserToken createAuthenticationToken(@RequestBody JwtAuthenticationRequire authenticationRequire) throws AuthenticationException {
        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(authenticationRequire.getEmail(), authenticationRequire.getPassword());
        Authentication authentication = authenticationManager.authenticate(authReq);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtProvider.generateToken(userDetails.getUsername());
        return new UserToken(token);
    }

    @GetMapping("/refresh")
    public UserToken refreshAuthenticationToken(HttpServletRequest request) {
        String authToken = jwtProvider.getToken(request);
        String refreshedToken = jwtProvider.refreshToken(authToken);
        return new UserToken(refreshedToken);
    }

    @PostMapping("/register")
    public void registerUser(@RequestBody UserDTO userDTO) { // add error if user exist // i tak nie doda usera z takim samym emailem bo email = unique
        userService.createUser(userDTO.parseUser());
        AccountActivationCode accountActivationCode = new AccountActivationCode(userDTO.getId());
        Optional<User> user = userRepository.findByEmail(userDTO.getEmail());
        accountActivationCode.setUserId(user.get().getId());
        Integer code = new Random().nextInt(999999) + 100000;
        accountActivationCode.setCode(code.toString());
        String text = String.format(emailSender.templateActivationCodeMessage().getText(), code);
        emailSender.sendSimpleMessage(userDTO.getEmail(), "Virtual Stock Exchange", text);
        accountActivationCodeRepository.save(accountActivationCode);
    }

    private boolean validPassword(String password) {
        return password.matches("^(?=.*[0-9])(?=.*[a-zA-Z])(?=\\S+$).{8,}$");
    }

    @PostMapping("/verify_code")
    public void verifyCode(@RequestBody VerifyCodeDTO code) {
        if (accountActivationCodeRepository.existsByCode(code.getVerifyCode())) {
            AccountActivationCode activationCode = accountActivationCodeRepository.findByCode(code.getVerifyCode());
            if (activationCode.isValid()) {
                Optional<User> user = userRepository.findById(activationCode.getUserId());
                user.get().setEnabled(true);
                userRepository.save(user.get());
                accountActivationCodeRepository.delete(activationCode);
            } else {
                Optional<User> user = userRepository.findById(activationCode.getUserId());
                Integer newCode = new Random().nextInt(999999) + 100000;
                activationCode.setCode(newCode.toString());
                activationCode.setExpiryDate(AccountActivationCode.calculateExpiryDate());
                accountActivationCodeRepository.save(activationCode);
                String text = String.format(emailSender.templateActivationCodeMessage().getText(), newCode);
                emailSender.sendSimpleMessage(user.get().getEmail(), "Virtual Stock Exchange", text);
                throw new AccessDeniedException("Code has expired! A new code has been sent");
            }
        } else {
            throw new AccessDeniedException("Code is invalid");
        }

    }
}
