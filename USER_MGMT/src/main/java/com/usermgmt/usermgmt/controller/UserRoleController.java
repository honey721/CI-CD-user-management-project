package com.usermgmt.usermgmt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.usermgmt.usermgmt.service.UserService;

import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserRoleController {
    private final UserService userService;
    public UserRoleController(UserService s){ this.userService = s; }

    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> assignRoles(@PathVariable Long userId, @RequestBody Set<String> roles){
        try {
            userService.assignRolesToUser(userId, roles);
            return ResponseEntity.ok("Roles assigned");
        } catch (Exception ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
