package com.cardemo.web.service;

import com.cardemo.common.repository.UserSecurityRepository;
import org.springframework.stereotype.Service;

/**
 * User management service replacing logic from COUSR00C-COUSR03C.cbl.
 *
 * TODO: Implement user list from COUSR00C.cbl
 * TODO: Implement user add from COUSR01C.cbl
 * TODO: Implement user update from COUSR02C.cbl
 * TODO: Implement user delete from COUSR03C.cbl
 * TODO: Enforce admin-only access (user type 'A')
 */
@Service
public class UserManagementService {

    private final UserSecurityRepository userSecurityRepository;

    public UserManagementService(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    // TODO: Implement user CRUD from COUSR00C-03C
}
