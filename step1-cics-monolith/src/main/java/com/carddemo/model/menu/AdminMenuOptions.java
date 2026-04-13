package com.carddemo.model.menu;

import java.util.List;

/**
 * Mirrors COADM02Y.cpy - Admin menu options.
 */
public class AdminMenuOptions {

    public record MenuOption(int number, String name, String programName) {}

    public static final int ADMIN_OPT_COUNT = 4;

    public static final List<MenuOption> OPTIONS = List.of(
        new MenuOption(1, "User List (Security)",    "COUSR00C"),
        new MenuOption(2, "User Add (Security)",     "COUSR01C"),
        new MenuOption(3, "User Update (Security)",  "COUSR02C"),
        new MenuOption(4, "User Delete (Security)",  "COUSR03C")
    );

    public static MenuOption getOption(int number) {
        if (number < 1 || number > ADMIN_OPT_COUNT) {
            return null;
        }
        return OPTIONS.get(number - 1);
    }
}
