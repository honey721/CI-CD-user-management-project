package com.usermgmt.usermgmt.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.usermgmt.usermgmt.entity.Role;
import com.usermgmt.usermgmt.entity.User;
import com.usermgmt.usermgmt.repository.RoleRepository;
import com.usermgmt.usermgmt.repository.UserRepository;

import io.micrometer.common.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisherService eventPublisher;

    public UserService(UserRepository ur, RoleRepository rr, PasswordEncoder pe, EventPublisherService ep){
        this.userRepo = ur; this.roleRepo = rr; this.passwordEncoder = pe; this.eventPublisher = ep;
    }

    @Transactional
    public User register(String username, String email, String rawPassword, String role) {
        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        
        String assignedRole = null;
        if(StringUtils.isBlank(role) || role.equals("USER")) {
        	assignedRole = "USER";
        }else if(role.equals("ADMIN")) {
        	assignedRole = "ADMIN";
        }
        Role userRole = null;
        if(StringUtils.isNotBlank(assignedRole)) {
        	userRole = roleRepo.findByName(assignedRole).orElse(null);
        }
        
        if (userRole != null) u.addRole(userRole);
        User saved = userRepo.save(u);

        // Save event to outbox table within the same DB transaction
        Map<String,Object> payload = Map.of(
            "id", saved.getId(),
            "email", saved.getEmail(),
            "username", saved.getUsername(),
            "time", LocalDateTime.now().toString()
        );
        eventPublisher.createRegistrationOutboxEvent(payload);

        return saved;
    }

    @Transactional
    public Optional<User> login(String email, String rawPassword) {
        return userRepo.findByEmail(email)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                .map(u -> {
                    u.setLastLogin(LocalDateTime.now());
                    userRepo.save(u);
                    
                    // Save event to outbox table within the same DB transaction
                    Map<String,Object> payload = Map.of(
                        "id", u.getId(),
                        "email", u.getEmail(),
                        "username", u.getUsername(),
                        "time", LocalDateTime.now().toString()
                    );
                    eventPublisher.createLoginOutboxEvent(payload);
                    return u;
                });
    }

    @Transactional
    public void assignRolesToUser(Long userId, Set<String> roleNames) {
        User u = userRepo.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        for (String rn : roleNames) {
            Role r = roleRepo.findByName(rn).orElseThrow(() -> new NoSuchElementException("Role not found: " + rn));
            u.addRole(r);
        }
        userRepo.save(u);
    }

    public List<User> getAllUsers(){ return userRepo.findAll(); }

}
