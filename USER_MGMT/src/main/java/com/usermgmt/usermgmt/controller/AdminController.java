package com.usermgmt.usermgmt.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.usermgmt.usermgmt.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;
    public AdminController(UserService userService){ this.userService = userService; }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String,Object> stats(){
        long totalUsers = userService.getAllUsers().size();
        // For demo we return mock last login times (real would be per user)
        return Map.of("totalUsers", totalUsers, "message", "mocked last login times available in user records");
    }
}
