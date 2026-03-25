package com.cardemo.service.online;

import com.cardemo.common.CardDemoConstants;
import com.cardemo.model.CardDemoCommarea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Admin Menu Service - migrated from COBOL program COADM01C.cbl.
 * Handles the admin menu display and navigation for admin users.
 * Original: CICS program for admin users, separate menu with user management options.
 */
@Service
public class AdminMenuService {

    private static final Logger log = LoggerFactory.getLogger(AdminMenuService.class);

    private static final String[][] ADMIN_OPTIONS = {
            {"1", "User List", "COUSR00C"},
            {"2", "User Add", "COUSR01C"},
            {"3", "User Update", "COUSR02C"},
            {"4", "User Delete", "COUSR03C"},
    };

    /**
     * Process admin menu selection.
     * Migrated from PROCESS-ENTER-KEY paragraph.
     */
    public String processAdminMenuSelection(int option, CardDemoCommarea commarea) {
        if (!commarea.isAdmin()) {
            throw new AdminMenuException("Access denied - Admin only...");
        }

        if (option < 1 || option > ADMIN_OPTIONS.length) {
            throw new AdminMenuException("Please enter a valid option number...");
        }

        String programName = ADMIN_OPTIONS[option - 1][2];

        commarea.setFromTranId(CardDemoConstants.TRAN_MAIN_MENU);
        commarea.setFromProgram(CardDemoConstants.PGM_ADMIN_MENU);
        commarea.setPgmContext(0);

        log.info("Admin menu option {} selected: {} -> {}", option,
                ADMIN_OPTIONS[option - 1][1], programName);
        return programName;
    }

    /**
     * Get admin menu options for display.
     */
    public String[][] getAdminMenuOptions() {
        return ADMIN_OPTIONS.clone();
    }

    public static class AdminMenuException extends RuntimeException {
        public AdminMenuException(String message) {
            super(message);
        }
    }
}
