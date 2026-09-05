package com.usermgmt.usermgmt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.usermgmt.usermgmt.entity.Role;
import com.usermgmt.usermgmt.repository.RoleRepository;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleRepository roleRepo;
    public RoleController(RoleRepository r){ this.roleRepo = r; }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createRole(@RequestBody Role role){
        if (roleRepo.findByName(role.getName()).isPresent()) return ResponseEntity.badRequest().body("Role exists");
        Role saved = roleRepo.save(role);
        return ResponseEntity.ok(saved);
    }
}



/*
 * --- @PreAuthorize :- 
 * 1. is a Spring Security annotation used for method-level authorization.
   2. It tells Spring:“Only allow this method to run if the logged-in user has the required permission/role.”
   
   --- What is hasAuthority('ADMIN') : Spring Security stores user permissions internally as "authorities".

For role-based apps:

ROLE_ADMIN
ROLE_USER


But for simplicity many people store authorities as:

ADMIN
USER


hasAuthority('ADMIN') means:

👉 The JWT token of the logged-in user must contain "ADMIN" as an authority.

Example payload from JWT:

{
  "sub": "john@example.com",
  "roles": ["ADMIN"]
}
   
 * 
 * */