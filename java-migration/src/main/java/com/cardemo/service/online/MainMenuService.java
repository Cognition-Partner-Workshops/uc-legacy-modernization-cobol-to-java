/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.online;

import com.cardemo.common.CardDemoConstants;
import com.cardemo.common.MenuOptionsConfig;
import com.cardemo.model.CardDemoCommarea;
import com.cardemo.model.MenuOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Main Menu Service - migrated from COBOL program COMEN01C.cbl.
 * Handles the main menu display and navigation for regular users.
 * Original: CICS program with TRANID CM00.
 */
@Service
public class MainMenuService {

    private static final Logger log = LoggerFactory.getLogger(MainMenuService.class);

    /**
     * Get available menu options for a user.
     * Migrated from BUILD-MENU-OPTIONS paragraph.
     */
    public List<MenuOption> getMenuOptions() {
        return MenuOptionsConfig.getMenuOptions();
    }

    /**
     * Process a menu selection.
     * Migrated from PROCESS-ENTER-KEY paragraph.
     *
     * @param option   the selected menu option number
     * @param commarea the current commarea
     * @return the program name to navigate to
     * @throws MenuException on invalid selection
     */
    public String processMenuSelection(int option, CardDemoCommarea commarea) {
        if (option < 1 || option > MenuOptionsConfig.getMenuOptionCount()) {
            throw new MenuException("Please enter a valid option number...");
        }

        MenuOption menuOption = MenuOptionsConfig.getMenuOption(option);

        if (menuOption == null) {
            throw new MenuException("Please enter a valid option number...");
        }

        // Check admin-only access
        if (commarea.isRegularUser() && menuOption.isAdminOnly()) {
            throw new MenuException("No access - Admin Only option...");
        }

        // Check if the option is a dummy/coming soon option
        if (menuOption.getProgramName().startsWith("DUMMY")) {
            throw new MenuException("This option " + menuOption.getOptionName().trim() + " is coming soon ...");
        }

        // Update commarea for navigation
        commarea.setFromTranId(CardDemoConstants.TRAN_MAIN_MENU);
        commarea.setFromProgram(CardDemoConstants.PGM_MAIN_MENU);
        commarea.setPgmContext(0);

        log.info("Menu option {} selected: {} -> {}", option, menuOption.getOptionName(), menuOption.getProgramName());
        return menuOption.getProgramName();
    }

    public static class MenuException extends RuntimeException {
        public MenuException(String message) {
            super(message);
        }
    }
}
