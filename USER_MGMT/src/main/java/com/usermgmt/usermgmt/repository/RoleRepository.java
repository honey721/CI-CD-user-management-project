package com.usermgmt.usermgmt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usermgmt.usermgmt.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
