package com.bee.controller;
import com.bee.model.User;
import com.bee.service.UserService;
import com.bee.utils.CommonUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {
    @Autowired
    private UserService userService;
    @Autowired
    private CommonUtil commonUtil;
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

    @GetMapping("/signin")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    //User
    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute User user, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        String imageName = file.isEmpty() ? "defaultImage.jpg" : file.getOriginalFilename();
        user.setImage(imageName);
        User saveUser = userService.saveUser(user);
        if(!ObjectUtils.isEmpty(saveUser)) {
            if(!file.isEmpty()) {
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }
            redirectAttributes.addFlashAttribute("successMsg", "Congratulations! You have Successfully Registered");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Oops! Something is wrong");
        }
        return "redirect:/register";
    }

    //Forget Password section
    @GetMapping("/forgot_password")
    public String loadForgotPassword() {
        return "forgot_password";
    }

    @PostMapping("/forgot_password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        User user = userService.getUserByEmail(email);
        if(!ObjectUtils.isEmpty(user)) {
            String resetToken = UUID.randomUUID().toString();
            userService.updateUserResetToken(email, resetToken);
            //generate a link to send in email like-> http://localhost:8080/reset_password?token=something
            String url = CommonUtil.generateUrl(request)+"/reset_password?token="+resetToken;

            Boolean sendMail = commonUtil.sendMail(url, email);
            if(sendMail) {
                redirectAttributes.addFlashAttribute("successMsg", "A reset link has sent to your email. Please check your email to reset password.");
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "Internal Server Error!");
            }

        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Invalid Email!");
        }
        return "redirect:/forgot_password";
    }

    @GetMapping("/reset_password")
    public String loadResetPassword(@RequestParam String token, Model m) {
        User user = userService.getUserByToken(token);
        if(ObjectUtils.isEmpty((user))) {
            m.addAttribute("msg", "Your link is invalid or expired");
            return "message";
        }
        m.addAttribute("token", token);
        return "reset_password";
    }

    @PostMapping("/reset_password")
    public String resetPassword(@RequestParam String token, @RequestParam String password, Model m) {
        User user = userService.getUserByToken(token);
        if(ObjectUtils.isEmpty(user)) {
            m.addAttribute("msg", "Your link is invalid or expired");
            return "message";
        } else {
            user.setPassword(passwordEncoder.encode(password));
            user.setResetToken(null);
            userService.updateUser(user);
            m.addAttribute("msg", "Password Reset Successfully!");
            return "message";
        }
    }

}
