package com.cardemo.model;

/**
 * Menu Option - migrated from COBOL copybook COMEN02Y.cpy.
 * Represents a single menu option in the CardDemo main menu.
 */
public class MenuOption {

    private int optionNum;
    private String optionName;
    private String programName;
    private String userType; // 'U' = User, 'A' = Admin

    public MenuOption() {
    }

    public MenuOption(int optionNum, String optionName, String programName, String userType) {
        this.optionNum = optionNum;
        this.optionName = optionName;
        this.programName = programName;
        this.userType = userType;
    }

    public int getOptionNum() { return optionNum; }
    public void setOptionNum(int optionNum) { this.optionNum = optionNum; }
    public String getOptionName() { return optionName; }
    public void setOptionName(String optionName) { this.optionName = optionName; }
    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public boolean isAdminOnly() {
        return "A".equals(userType);
    }

    @Override
    public String toString() {
        return optionNum + ". " + optionName;
    }
}
