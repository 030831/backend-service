package com.hyunjun.backend.member.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email @NotBlank @Column(nullable = false, unique = true)
    private String email;

    @NotBlank @Column(nullable = false)
    private String passwordHash;

    @NotBlank @Column(nullable = false, unique = true)
    private String nickname;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
