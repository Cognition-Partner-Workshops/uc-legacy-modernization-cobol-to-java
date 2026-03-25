package com.cardemo.common;

import com.cardemo.model.MenuOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Menu options configuration - migrated from COBOL copybook COMEN02Y.cpy.
 * Replaces the COBOL CARDDEMO-MAIN-MENU-OPTIONS data structure.
 */
public final class MenuOptionsConfig {

    private static final List<MenuOption> MENU_OPTIONS;

    static {
        List<MenuOption> options = new ArrayList<>();
        options.add(new MenuOption(1, "Account View", "COACTVWC", "U"));
        options.add(new MenuOption(2, "Account Update", "COACTUPC", "U"));
        options.add(new MenuOption(3, "Credit Card List", "COCRDLIC", "U"));
        options.add(new MenuOption(4, "Credit Card View", "COCRDSLC", "U"));
        options.add(new MenuOption(5, "Credit Card Update", "COCRDUPC", "U"));
        options.add(new MenuOption(6, "Transaction List", "COTRN00C", "U"));
        options.add(new MenuOption(7, "Transaction View", "COTRN01C", "U"));
        options.add(new MenuOption(8, "Transaction Add", "COTRN02C", "U"));
        options.add(new MenuOption(9, "Transaction Reports", "CORPT00C", "U"));
        options.add(new MenuOption(10, "Bill Payment", "COBIL00C", "U"));
        options.add(new MenuOption(11, "Pending Authorization View", "COPAUS0C", "U"));
        MENU_OPTIONS = Collections.unmodifiableList(options);
    }

    private MenuOptionsConfig() {
    }

    public static List<MenuOption> getMenuOptions() {
        return MENU_OPTIONS;
    }

    public static int getMenuOptionCount() {
        return MENU_OPTIONS.size();
    }

    public static MenuOption getMenuOption(int index) {
        if (index < 1 || index > MENU_OPTIONS.size()) {
            return null;
        }
        return MENU_OPTIONS.get(index - 1);
    }
}
