package com.cognition.usersecurity.repository;

import com.cognition.usersecurity.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByIdIgnoreCase(String id);
}
