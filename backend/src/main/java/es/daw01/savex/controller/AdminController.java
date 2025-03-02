package es.daw01.savex.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import es.daw01.savex.DTOs.PostDTO;
import es.daw01.savex.DTOs.UserDTO;
import es.daw01.savex.components.ControllerUtils;
import es.daw01.savex.service.CommentService;
import es.daw01.savex.service.PostService;
import es.daw01.savex.service.UserService;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
@RequestMapping("/admin")
public class AdminController {
    
    // --- Services ---
    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private ControllerUtils controllerUtils;


    // --- Methods ---
    @GetMapping("")
    public String getAdminPage(Model model) {
        // Add user data to the model
        controllerUtils.addUserDataToModel(model);

        List<PostDTO> postDetails = postService.getPostsDTO(postService.findAll());
        List<UserDTO> userDetails = userService.getUsersDTO(userService.findAll());

        // Add data to the model
        model.addAttribute("title", "SaveX - Panel de administración");
        model.addAttribute("posts", postDetails);
        model.addAttribute("users", userDetails);

        // model.addAttribute("comments", commentService.findAll());

        return "admin";
    }

    @DeleteMapping("/user/{id}")
    public String deleteUser(Model model, @PathVariable long id) {
        // Delete comments
        commentService.deleteByAuthorId(id);
        // Delete user
        userService.deleteById(id);
        
        return "redirect:/admin";
    }

    @DeleteMapping("/post/{id}")
    public String deletePost(Model model, @PathVariable long id) {
        // Delete comments
        commentService.deleteByPostId(id);
        // Delete post
        postService.deleteById(id);
        
        return "redirect:/admin";
    }
    
}
