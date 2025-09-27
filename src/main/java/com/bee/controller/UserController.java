package com.bee.controller;

import com.bee.model.User;
import com.bee.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @ModelAttribute
    public void getUserDatails(Principal p, Model m) {
        if(p != null) {
            String email = p.getName();
            User user = userService.getUserByEmail(email);
            m.addAttribute("user", user);
        }
    }
    public String home() {
        return "user/home";
    }

    private User getLoggedInUser (Principal p) {
        return userService.getUserByEmail(p.getName());
    }

    @GetMapping("/profile")
    public String profile() {
        return "/user/profile";
    }

    @PostMapping("/updateProfile")
    public String updateProfile(@ModelAttribute User user, @RequestParam MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        User updatedUser = userService.updateUserProfile(user, file);
        if(ObjectUtils.isEmpty(updatedUser)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Profile not updated");
        } else redirectAttributes.addFlashAttribute("successMsg", "Profile Updated");
        return "redirect:/user/profile";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p, RedirectAttributes redirectAttributes) {
        User loggedInUser = getLoggedInUser(p);
        boolean matches = passwordEncoder.matches(currentPassword, loggedInUser.getPassword());
        if(matches) {
            String encodeNewPassword = passwordEncoder.encode(newPassword);
            loggedInUser.setPassword(encodeNewPassword);
            User updateUser = userService.updateUser(loggedInUser);
            if(ObjectUtils.isEmpty(updateUser)) {
                redirectAttributes.addFlashAttribute("errorMsg", "Password not changed");
            } else {
                redirectAttributes.addFlashAttribute("successMsg", "Password changed successfully");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Current password is incorrect");
        }

        return "redirect:/user/profile";
    }

}
