package com.example.zhukov.cloudbox.repository;

import com.example.zhukov.cloudbox.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
