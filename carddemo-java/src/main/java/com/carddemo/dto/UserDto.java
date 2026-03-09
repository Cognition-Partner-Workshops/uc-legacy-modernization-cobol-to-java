package com.carddemo.dto;

/**
 * User DTO for REST API responses and admin user management.
 * Maps to user management screens (BMS COUSR00-COUSR03).
 */
public class UserDto {

    private String usrId;
    private String usrFirstName;
    private String usrLastName;
    private String usrType;

    public UserDto() {
    }

    public UserDto(String usrId, String usrFirstName, String usrLastName, String usrType) {
        this.usrId = usrId;
        this.usrFirstName = usrFirstName;
        this.usrLastName = usrLastName;
        this.usrType = usrType;
    }

    public String getUsrId() {
        return usrId;
    }

    public void setUsrId(String usrId) {
        this.usrId = usrId;
    }

    public String getUsrFirstName() {
        return usrFirstName;
    }

    public void setUsrFirstName(String usrFirstName) {
        this.usrFirstName = usrFirstName;
    }

    public String getUsrLastName() {
        return usrLastName;
    }

    public void setUsrLastName(String usrLastName) {
        this.usrLastName = usrLastName;
    }

    public String getUsrType() {
        return usrType;
    }

    public void setUsrType(String usrType) {
        this.usrType = usrType;
    }
}
