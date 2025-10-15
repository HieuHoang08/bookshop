package com.hh.Job.repository;

import com.hh.Job.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String username);

    Page<User> findAll(Specification<User> spec, Pageable pageable);

    boolean existsByEmail(String email);

    User findByRefreshTokenAndEmail(String refreshToken, String email);

    Optional<User> findFirstByEmail(String email);

}
