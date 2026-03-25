/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.online;

import com.cardemo.common.CardDemoConstants;
import com.cardemo.model.CardDemoCommarea;
import com.cardemo.model.UserSecurityRecord;
import com.cardemo.repository.UserSecurityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Signon Service - migrated from COBOL program COSGN00C.cbl.
 * Handles user authentication for the CardDemo application.
 * Original: CICS program with TRANID CC00, reads USRSEC VSAM file.
 */
@Service
public class SignonService {

    private static final Logger log = LoggerFactory.getLogger(SignonService.class);

    private final UserSecurityRepository userSecurityRepository;

    public SignonService(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    /**
     * Authenticate a user - migrated from PROCESS-ENTER-KEY and READ-USER-SEC-FILE paragraphs.
     *
     * @param userId   the user ID (equivalent to USERIDI OF COSGN0AI)
     * @param password the password (equivalent to PASSWDI OF COSGN0AI)
     * @return CardDemoCommarea populated with user info on success
     * @throws SignonException on authentication failure
     */
    public CardDemoCommarea authenticate(String userId, String password) {
        if (userId == null || userId.isBlank()) {
            throw new SignonException("Please enter User ID ...");
        }
        if (password == null || password.isBlank()) {
            throw new SignonException("Please enter Password ...");
        }

        String upperUserId = userId.toUpperCase();
        String upperPassword = password.toUpperCase();

        Optional<UserSecurityRecord> userOpt = userSecurityRepository.findById(upperUserId);

        if (userOpt.isEmpty()) {
            throw new SignonException("User not found. Try again ...");
        }

        UserSecurityRecord user = userOpt.get();

        if (!upperPassword.equals(user.getUsrPassword())) {
            throw new SignonException("Wrong Password. Try again ...");
        }

        // Build commarea - equivalent to COBOL MOVE statements after successful auth
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setFromTranId(CardDemoConstants.TRAN_SIGNON);
        commarea.setFromProgram(CardDemoConstants.PGM_SIGNON);
        commarea.setUserId(upperUserId);
        commarea.setUserType(user.getUsrType());
        commarea.setPgmContext(0);

        log.info("User '{}' authenticated successfully. Type: {}", upperUserId, user.getUsrType());
        return commarea;
    }

    /**
     * Determine the next program based on user type.
     * Migrated from READ-USER-SEC-FILE: CDEMO-USRTYP-ADMIN check.
     */
    public String getNextProgram(CardDemoCommarea commarea) {
        if (commarea.isAdmin()) {
            return CardDemoConstants.PGM_ADMIN_MENU;
        }
        return CardDemoConstants.PGM_MAIN_MENU;
    }

    public static class SignonException extends RuntimeException {
        public SignonException(String message) {
            super(message);
        }
    }
}
