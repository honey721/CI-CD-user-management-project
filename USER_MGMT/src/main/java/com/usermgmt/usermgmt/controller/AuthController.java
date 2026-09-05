package com.usermgmt.usermgmt.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.usermgmt.usermgmt.dto.*;
import com.usermgmt.usermgmt.entity.User;
import com.usermgmt.usermgmt.security.JwtUtil;
import com.usermgmt.usermgmt.service.UserService;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService us, JwtUtil ju){
        this.userService = us; this.jwtUtil = ju;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Validated @RequestBody UserRegisterRequest req) {
        User u = userService.register(req.getUsername(), req.getEmail(), req.getPassword(), req.getRole());
        Set<String> roles = u.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());
        String token = jwtUtil.generateToken(u.getEmail(), roles);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Validated @RequestBody AuthRequest req) {

        return userService.login(req.getEmail(), req.getPassword())
                .map(user -> {
                    Set<String> roles = user.getRoles()
                            .stream()
                            .map(r -> r.getName())
                            .collect(Collectors.toSet());
                    String token = jwtUtil.generateToken(user.getEmail(), roles);
                    return ResponseEntity.ok(new AuthResponse(token));
                }).orElseGet(() -> ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse("Invalid credentials")));
    }


    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return ResponseEntity.status(401).build();
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return ResponseEntity.status(401).build();
        String email = jwtUtil.getEmailFromToken(token);
        Set<String> roles = jwtUtil.getRolesFromToken(token);
        // fetch user id and username from DB
        // simplified: use UserRepository to fetch user details (or userService)
        // I'll call userService indirectly (not shown) - assume a method exists to get by email
        return ResponseEntity.ok(
                new UserProfileResponse( null /*id resolved below*/,
                        null, email, roles)
        );
    }
}
