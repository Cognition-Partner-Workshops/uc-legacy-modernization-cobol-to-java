package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Objects;

@Entity
@Table(name = "users")
public class UserSecurity {

    @Id
    @Size(max = 8)
    @Column(name = "user_id", length = 8)
    private String userId;

    @NotNull
    @Size(max = 20)
    @Column(name = "first_name", length = 20, nullable = false)
    private String firstName;

    @NotNull
    @Size(max = 20)
    @Column(name = "last_name", length = 20, nullable = false)
    private String lastName;

    @Size(max = 60)
    @Column(name = "password", length = 60, nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull
    @Size(max = 1)
    @Column(name = "user_type", length = 1, nullable = false)
    private String userType;

    public UserSecurity() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public boolean isAdmin() {
        return "A".equalsIgnoreCase(userType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserSecurity that)) return false;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
