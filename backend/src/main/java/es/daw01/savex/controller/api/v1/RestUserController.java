package es.daw01.savex.controller.api.v1;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import jakarta.persistence.EntityExistsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.daw01.savex.DTOs.ApiResponseDTO;
import es.daw01.savex.DTOs.PaginatedDTO;
import es.daw01.savex.DTOs.users.CreateUserRequest;
import es.daw01.savex.DTOs.users.ModifyPasswordRequest;
import es.daw01.savex.DTOs.users.ModifyUserRequest;
import es.daw01.savex.DTOs.users.PrivateUserDTO;
import es.daw01.savex.DTOs.users.PublicUserDTO;
import es.daw01.savex.components.ControllerUtils;
import es.daw01.savex.model.User;
import es.daw01.savex.model.UserType;
import es.daw01.savex.service.CommentService;
import es.daw01.savex.service.UserService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;


@RestController
@RequestMapping("/api/v1/users")
public class RestUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ControllerUtils controllerUtils;

    @GetMapping({ "", "/" })
    public ResponseEntity<Object> getUsers(
        @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        // Get all users
        PaginatedDTO<PublicUserDTO> response = userService.findAllByRoleNoPasswd(
            UserType.USER,
            pageable
        );

        // Return the users list
        return ApiResponseDTO.ok(response);
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<Object> getProfilePic(@PathVariable long id) throws SQLException, IOException {
        Resource avatar = userService.getUserAvatar(id);
        return ApiResponseDTO.ok(avatar);
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<Object> uploadAvatar( @PathVariable long id, @RequestParam MultipartFile avatar) throws IOException {
        User user = controllerUtils.getAuthenticatedUser();
        if (user.getId() != id) {
            return ApiResponseDTO.error("You cannot upload an avatar for another user", 403);
        }

        URI location = fromCurrentRequest().build().toUri();
        
        try {
            PublicUserDTO updatedUser = userService.createUserAvatar(id, avatar);
            return ApiResponseDTO.ok(updatedUser, location, 201);
        } catch (EntityExistsException e) {
            return ApiResponseDTO.error("Avatar already exists", 409);
        }
    }

    @PutMapping("/{id}/avatar")
    public ResponseEntity<Object> modifyAvatar(
            @PathVariable long id, @RequestParam MultipartFile avatar) throws IOException {
        User user = controllerUtils.getAuthenticatedUser();
        if (user.getId() != id) {
            return ApiResponseDTO.error("You cannot modify an avatar for another user", 403);
        }
        URI location = fromCurrentRequest().build().toUri();
        try {
            PublicUserDTO updatedUser = userService.modifyUserAvatar(id, avatar);
            return ApiResponseDTO.ok(updatedUser, location, 200);
        } catch (NoSuchElementException e) {
            return ApiResponseDTO.error("User not found", 404);
        }
    }

    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<Object> deleteAvatar(@PathVariable long id) {
        User user = controllerUtils.getAuthenticatedUser();
        if (user.getId() != id) {
            return ApiResponseDTO.error("You cannot delete an avatar for another user", 403);
        }
        try {
            userService.deleteUserAvatar(id);
        } catch (NoSuchElementException e) {
            return ApiResponseDTO.error("User not found", 404);
        }
        return ApiResponseDTO.ok("Avatar deleted successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable long id) {
        try {
            // Get the authenticated user
            User authenticatedUser = controllerUtils.getAuthenticatedUser();

            // Prevent admin from deleting himself
            if (authenticatedUser.getId() == id) {
                return ApiResponseDTO.error("You cannot delete yourself", 403);
            }

            // Before deleting the user, we must delete all the comments and posts associated with him
            // Delete comments
            commentService.deleteByAuthorId(id);
            // Delete user
            userService.deleteById(id);

            // Return success message
            return ApiResponseDTO.ok("User deleted successfully");
        } catch (Exception e) {
            // Return error message
            return ApiResponseDTO.error("Error deleting user");
        }
    }

    @PostMapping({"", "/"})
    public ResponseEntity<Object> registerNewUser(@ModelAttribute CreateUserRequest createUserRequest) {
        try {
            PrivateUserDTO privateUser = userService.register(createUserRequest);
            return ApiResponseDTO.ok(privateUser, 201);
        } catch (EntityExistsException e) {
            return ApiResponseDTO.error("User already exists");
        } catch (IllegalArgumentException e) {
            return ApiResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponseDTO.error("Error creating user");
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<Object> getUser(@PathVariable long id) {
        try {
            // Get the user
            PublicUserDTO user = userService.findPublicUserById(id);

            // Return the user
            return ApiResponseDTO.ok(user);
        } catch (NoSuchElementException e) {
            // Return error message
            return ApiResponseDTO.error("User not found");
        }
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<Object> modifyUser(
        @PathVariable long id,
        @ModelAttribute ModifyUserRequest modifyUser
    ) {
        try {
            User authenticatedUser = controllerUtils.getAuthenticatedUser();

            if (authenticatedUser.getId() != id) {
                return ApiResponseDTO.error("You cannot modify another user", 403);
            }
            
            PrivateUserDTO privateUser = userService.modifyUser(id, modifyUser);

            return ApiResponseDTO.ok(privateUser);
        } catch (NoSuchElementException e) {
            return ApiResponseDTO.error("User not found");
        } catch (IllegalArgumentException e) {
            return ApiResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponseDTO.error("Error modifying user");
        }
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Object> modifyPassword(
        @PathVariable long id,
        @ModelAttribute ModifyPasswordRequest modifyUserPassword
    ) {
        try {
            // Get the authenticated user
            User authenticatedUser = controllerUtils.getAuthenticatedUser();

            // Prevent user from modifying another user
            if (authenticatedUser.getId() != id) {
                return ApiResponseDTO.error("You cannot modify another user", 403);
            }

            // Modify the password
            PrivateUserDTO privateUserDTO = userService.modifyPassword(id, modifyUserPassword);

            return ApiResponseDTO.ok(privateUserDTO);
        } catch (NoSuchElementException e) {
            return ApiResponseDTO.error("User not found");
        } catch (IllegalArgumentException e) {
            return ApiResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponseDTO.error("Error modifying password");
        }
    }

}