package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", length = 8, nullable = false)
    @NotBlank
    @Size(max = 8)
    private String userId;

    @Column(name = "first_name", length = 20)
    @Size(max = 20)
    private String firstName;

    @Column(name = "last_name", length = 20)
    @Size(max = 20)
    private String lastName;

    @Column(name = "password", length = 72, nullable = false)
    @NotBlank
    @Size(max = 72)
    private String password;

    @Column(name = "user_type", length = 1, nullable = false)
    @NotBlank
    @Size(max = 1)
    private String userType;
}
