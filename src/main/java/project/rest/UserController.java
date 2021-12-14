package project.rest;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.dto.ChangeBalanceDTO;
import project.dto.ChangePasswordDTO;
import project.dto.OpenPositionDTO;
import project.dto.UserDTO;
import project.services.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserDTO getUsersById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping("/change_password")
    public void changePassword(HttpServletRequest httpServletRequest,
                               @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(httpServletRequest, changePasswordDTO);
    }

    @PostMapping("/change_balance")
    public void changeBalance(HttpServletRequest httpServletRequest,
                              @RequestBody ChangeBalanceDTO newBalance) {
        userService.changeBalance(httpServletRequest, newBalance);
    }
}