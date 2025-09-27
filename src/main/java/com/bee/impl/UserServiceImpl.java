package com.bee.impl;

import com.bee.model.User;
import com.bee.repository.UserRepository;
import com.bee.service.UserService;
import com.bee.utils.AppConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public User updateUserProfile(User user, MultipartFile file) throws IOException {
        User oldUser = userRepository.findById(user.getId()).get();

        if(!file.isEmpty()) {
            oldUser.setImage(file.getOriginalFilename());
        }

        if(!ObjectUtils.isEmpty(oldUser)) {
            oldUser.setName(user.getName());
            oldUser.setMobileNumber(user.getMobileNumber());
            oldUser.setCity(user.getCity());
            oldUser.setPostCode(user.getPostCode());
            userRepository.save(oldUser);
        }
        try {
            if(!file.isEmpty()) {
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return oldUser;
    }

    @Autowired
    private UserRepository userRepository;

    @Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User getUserByToken(String token) {
        return userRepository.findByResetToken(token);
    }

    @Override
    public void updateUserResetToken(String email, String token) {
        User user = userRepository.findByEmail(email);
        user.setResetToken(token);
        userRepository.save(user);
    }
    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void increaseFailedAttempt(User user) {
        int attempt = user.getFailedAttempt() + 1;
        user.setFailedAttempt(attempt);
        userRepository.save(user);
    }

    @Override
    public void userAccountLock(User user) {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    @Override
    public boolean unlockAccountTimeExpired(User user) {
        long lockTime = user.getLockTime().getTime();
        long unlockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;
        long currentTime = System.currentTimeMillis();

        if(unlockTime < currentTime) {
            user.setAccountNonLocked(true);
            user.setFailedAttempt(0);
            user.setLockTime(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void resetAttempt(int userId) {
        // This method is not implemented, but its dependency is handled.
    }

    @Override
    public Boolean updateAccountStatus(int id, Boolean status) {
        Optional<User> user = userRepository.findById(id);
        if(user.isPresent()) {
            User user1 = user.get();
            user1.setIsEnable(status);
            userRepository.save(user1);
            return true;
        }
        return false;
    }

    @Override
    public Boolean deleteById(int id) {
        User user = userRepository.findById(id).orElse(null);
        if (!ObjectUtils.isEmpty(user)) {
            userRepository.delete(user);
            return true;
        }
        return false;
    }

    @Override
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return user;
    }

    @Override
    public List<User> getAllUsers(String role) {
        return userRepository.findByRole(role);
    }

    @Override
    public User saveUser(User user) {
        user.setRole("ROLE_USER");
        user.setIsEnable(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        user.setLockTime(null);
        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        User saveUser = userRepository.save(user);
        return saveUser;
    }

//    @Override
//    public User saveAdmin(User user) {
//        user.setRole("ROLE_ADMIN");
//        user.setIsEnable(true);
//        user.setAccountNonLocked(true);
//        user.setFailedAttempt(0);
//        user.setLockTime(null);
//        String encodePassword = passwordEncoder.encode(user.getPassword());
//        user.setPassword(encodePassword);
//        User saveAdmin = userRepository.save(user);
//        return saveAdmin;
//    }


}