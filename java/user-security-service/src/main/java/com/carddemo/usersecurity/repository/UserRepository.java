package com.carddemo.usersecurity.repository;

import com.carddemo.usersecurity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
