package com.example.dialogflow.model.repository;

import com.example.dialogflow.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<Users, Long> {

    Users findByUsername(String username);
}
