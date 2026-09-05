package com.usermgmt.usermgmt.security;

import com.usermgmt.usermgmt.entity.User;
import com.usermgmt.usermgmt.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    public CustomUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User u = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Convert roles into Spring Security authorities
        List<SimpleGrantedAuthority> authorities =
                u.getRoles()
                 .stream()
                 .map(role -> new SimpleGrantedAuthority(role.getName()))
                 .toList();

        // Return Spring Security's User object
        return new org.springframework.security.core.userdetails.User(
                u.getEmail(),
                u.getPassword(),
                authorities
        );
    }
}
