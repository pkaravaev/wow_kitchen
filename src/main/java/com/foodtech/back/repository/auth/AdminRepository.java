package com.foodtech.back.repository.auth;

import com.foodtech.back.entity.auth.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    @Query("SELECT DISTINCT a FROM Admin a " +
            "LEFT JOIN FETCH a.roles " +
            "WHERE a.name=?1")
    Optional<Admin> findByNameEquals(String name);
}
