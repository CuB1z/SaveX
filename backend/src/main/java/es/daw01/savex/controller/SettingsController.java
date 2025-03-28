package es.daw01.savex.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.daw01.savex.DTOs.UserDTO;
import es.daw01.savex.DTOs.users.ModifyPasswordRequest;
import es.daw01.savex.components.ControllerUtils;
import es.daw01.savex.model.User;
import es.daw01.savex.service.UserService;
import jakarta.validation.Valid;

@Controller
public class SettingsController {

    @Autowired
    private ControllerUtils controllerUtils;

    @Autowired
    private UserService userService;

    @GetMapping("/settings")
    public String getSettingsPage(Model model) {
        return renderSettingsPage(model);
    }

    @PostMapping("/update-account-data")
    public String postUpdateAccountInfo(
            @Valid @ModelAttribute("user") UserDTO userDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        // Retrieve the authenticated user
        User currentUser = controllerUtils.getAuthenticatedUser();
        String currentUsername = currentUser.getUsername();

        // Errors map
        Map<String, String> errors = new HashMap<>();

        // Check email errors
        if (!userDTO.getEmail().isBlank() && bindingResult.getFieldError("email") != null) {
            errors.put("email", bindingResult.getFieldError("email").getDefaultMessage());
        }

        // Check username errors
        if (!userDTO.getUsername().isBlank() && bindingResult.getFieldError("username") != null) {
            errors.put("username", bindingResult.getFieldError("username").getDefaultMessage());
        }

        // Check name errors
        if (!userDTO.getName().isBlank() && bindingResult.getFieldError("name") != null) {
            errors.put("name", bindingResult.getFieldError("name").getDefaultMessage());
        }

        // Check password errors
        if (!userDTO.getPassword().isBlank() && bindingResult.getFieldError("password") != null) {
            errors.put("password", bindingResult.getFieldError("password").getDefaultMessage());
        }

        // If there are errors, return to the form with the errors mapped
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return renderSettingsPage(model);
        }

        // Try to update the user
        try {
            userService.updateUserAccount(currentUser, userDTO, errors);

            // If there are errors, return to the form with the errors mapped
            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                return renderSettingsPage(model);
            }
        } catch (Exception e) {
            return renderSettingsPage(model);
        }

        // Redirect
        redirectAttributes.addFlashAttribute("popupTitle", "Cambios guardados");
        redirectAttributes.addFlashAttribute("popupContent", "Usuario actualizado correctamente");
        if (userDTO.getUsername().equals(currentUsername))
            return "redirect:/settings?success=true";
        else
            return "/logout";
    }

    @PostMapping("/change-password")
    public String postChangePassword(@ModelAttribute ModifyPasswordRequest request, Model model,
        RedirectAttributes redirectAttributes
    ) {
        try {
            User user = controllerUtils.getAuthenticatedUser();
            userService.modifyPassword(user.getId(), request);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return renderSettingsPage(model);
        }

        redirectAttributes.addFlashAttribute("popupTitle", "Cambios guardados");
        redirectAttributes.addFlashAttribute("popupContent", "Contraseña cambiada correctamente");
        return "redirect:/settings?success=true";
    }

    @PostMapping("/delete-account")
    public String postDeleteAccount(Model model) {
        User user = controllerUtils.getAuthenticatedUser();
        user.getComments().forEach(comment -> comment.setAuthor(null));
        userService.deleteById(user.getId());
        return "/logout";
    }

    // Private Methods ------------------------------------------------------------>>

    private String renderSettingsPage(Model model) {
        // Retrieve the authenticated user
        User user = controllerUtils.getAuthenticatedUser();

        // Add user data to the model
        controllerUtils.addUserDataToModel(model);
        model.addAttribute("email", user.getEmail());
        model.addAttribute("title", "SaveX - ".concat(model.getAttribute("name").toString()));

        return "settings";
    }
}