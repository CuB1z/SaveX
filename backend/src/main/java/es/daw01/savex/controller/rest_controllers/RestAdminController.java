package es.daw01.savex.controller.rest_controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.daw01.savex.DTOs.ApiResponseDTO;
import es.daw01.savex.DTOs.PostDTO;
import es.daw01.savex.DTOs.UserDTO;
import es.daw01.savex.components.ControllerUtils;
import es.daw01.savex.model.User;
import es.daw01.savex.model.UserType;
import es.daw01.savex.service.CommentService;
import es.daw01.savex.service.PostService;
import es.daw01.savex.service.UserService;
import es.daw01.savex.utils.DataUtils;

@RestController
@RequestMapping("/api/admin")
public class RestAdminController {

    @Autowired
    ControllerUtils controllerUtils;

    @Autowired
    CommentService commentService;

    @Autowired
    UserService userService;

    @Autowired
    PostService postService;

    @DeleteMapping("/user/{id}")
    public ApiResponseDTO<Object> deleteUser(@PathVariable long id) {
        try{
            // Get the authenticated user
            User authenticatedUser = controllerUtils.getAuthenticatedUser();

            // Prevent admin from deleting himself
            if (authenticatedUser.getId() == id) {
                return ApiResponseDTO.error("You cannot delete yourself");
            }

            // Before deleting the user, we must delete all the comments and posts associated with him
            // Delete comments
            commentService.deleteByAuthorId(id);
            // Delete user
            userService.deleteById(id);

            // Return success message
            return ApiResponseDTO.ok("User deleted successfully");
        }
        catch (Exception e){
            // Return error message
            return ApiResponseDTO.error("Error deleting user");
        }
    }


    @GetMapping("/users")  // Used to be "/template/users"
    public ApiResponseDTO<Object> getUsersTemplateString(@RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "5") int limit) {
        try{
            // Get all users
            List<UserDTO> users = userService.getUsersDTO(userService.findAllByRole(UserType.USER));

            // Slice the list to show only 5 users
            users = users.subList(offset, DataUtils.clamp(limit, 0, users.size()));

            // Return the users list
            return ApiResponseDTO.ok(users);
        }
        catch (Exception e){
            // Return error message
            return ApiResponseDTO.error("Error getting users");    
        }
    }


    @GetMapping("/posts")  // Used to be "/template/posts"
    public ApiResponseDTO<Object> getPostsTemplateString(@RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "5") int limit) {
        try{
            // Get all posts
            List<PostDTO> posts = postService.getPostsDTO(postService.findAll());

            // Slice the list to show only 5 posts
            posts = posts.subList(offset, DataUtils.clamp(limit, 0, posts.size()));

            // Return the posts list
            return ApiResponseDTO.ok(posts);
        }
        catch (Exception e){
            // Return error message
            return ApiResponseDTO.error("Error getting posts");
        }
    }
}