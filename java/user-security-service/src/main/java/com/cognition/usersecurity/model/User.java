package com.cognition.usersecurity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Convert;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id @Column(name = "id", length = 8)
    private String id;
    @Column(nullable = false, length = 20) private String firstName;
    @Column(nullable = false, length = 20) private String lastName;
    @Column(nullable = false, length = 9) private String password;
    @Convert(converter = UserTypeConverter.class) @Column(nullable = false, length = 1)
    private UserType userType;

    protected User() {}
    public User(String id, String firstName, String lastName, String password, UserType userType) {
        this.id = id; this.firstName = firstName; this.lastName = lastName; this.password = password; this.userType = userType;
    }
    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPassword() { return password; }
    public UserType getUserType() { return userType; }
    public void update(String firstName, String lastName, String password, UserType userType) {
        this.firstName = firstName; this.lastName = lastName; this.password = password; this.userType = userType;
    }
}
