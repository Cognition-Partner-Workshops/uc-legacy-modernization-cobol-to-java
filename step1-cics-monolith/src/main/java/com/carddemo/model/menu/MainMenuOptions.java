package com.carddemo.model.menu;

import java.util.List;

/**
 * Mirrors COMEN02Y.cpy - 11 menu items for the main user menu.
 */
public class MainMenuOptions {

    public record MenuOption(int number, String name, String programName, String userType) {}

    public static final int MENU_OPT_COUNT = 11;

    public static final List<MenuOption> OPTIONS = List.of(
        new MenuOption(1,  "Account View",                "COACTVWC", "U"),
        new MenuOption(2,  "Account Update",              "COACTUPC", "U"),
        new MenuOption(3,  "Credit Card List",            "COCRDLIC", "U"),
        new MenuOption(4,  "Credit Card View",            "COCRDSLC", "U"),
        new MenuOption(5,  "Credit Card Update",          "COCRDUPC", "U"),
        new MenuOption(6,  "Transaction List",            "COTRN00C", "U"),
        new MenuOption(7,  "Transaction View",            "COTRN01C", "U"),
        new MenuOption(8,  "Transaction Add",             "COTRN02C", "U"),
        new MenuOption(9,  "Transaction Reports",         "CORPT00C", "U"),
        new MenuOption(10, "Bill Payment",                "COBIL00C", "U"),
        new MenuOption(11, "Pending Authorization View",  "COPAUS0C", "U")
    );

    public static MenuOption getOption(int number) {
        if (number < 1 || number > MENU_OPT_COUNT) {
            return null;
        }
        return OPTIONS.get(number - 1);
    }
}
